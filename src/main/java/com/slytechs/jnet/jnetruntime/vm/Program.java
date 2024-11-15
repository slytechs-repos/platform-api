/*
 * Sly Technologies Free License
 * 
 * Copyright 2024 Sly Technologies Inc.
 *
 * Licensed under the Sly Technologies Free License (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.slytechs.com/free-license-text
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.slytechs.jnet.jnetruntime.vm;

/**
 * @author Mark Bednarczyk
 *
 */
public interface Program {

	String TUNNEL_TRAFFIC = """
			priority = 10

			define TopLabel 0
			define BottomLabel -1

			// Supported Protocols and Fields
			protocol {
			    // Define IP Protocol Block
			    protocol IP {
			        Offset = Packet.L3Offset >> 2
			        Length = Packet.L3Len >> 2
			        Payload = Packet.length - (Offset + Length)
			        
			        Offset: Packet.L3Offset >> 2
			        Length: Packet.L3Len >> 2
			        Payload: (Offset + Length : Packet.Length - Payload.offset)


			        fields: { Src, Dst, TTL, Protocol }

			        // Define IPv4 Sub-Protocol
			        define IPv4 protocol {
			            condition: Packet.L3Type == IPv4
			            fields: { Src = 12:4, Dst = 16:4, Flags = 6, TTL = 8, Protocol = 9 }
			        }

			        // Define IPv6 Sub-Protocol
			        define IPv6 protocol {
			            condition: Packet.L3Type == IPv6
			            fields: { Src = 7:16, Dst = 23:6, FlowLabel = 1, TTL = 6, Protocol = 5 }
			        }
			    }

			    // Define TRANSPORT Protocol Block
			    protocol TRANSPORT {
			        Offset = Packet.L4Offset >> 2
			        Length = Packet.L4Len >> 2
			        Payload = Packet.length - (Offset + Length)

			        fields: { SrcPort, DstPort, Length, Checksum }

			        // Define TCP Sub-Protocol
			        protocol TCP {
			            condition: Packet.L4Type == TCP
			            fields: { SrcPort = 0:2, DstPort = 2:2, Flags = 12:2, Seq = 4:4, Ack = 8:4, Checksum = 16:2 }
			        }

			        // Define UDP Sub-Protocol
			        protocol UDP {
			            condition: Packet.L4Type == UDP
			            fields: { SrcPort = 0:2, DstPort = 2:2, Length = 4:2, Checksum = 6:2 }
			        }

			        // Define SCTP Sub-Protocol
			        protocol SCTP {
			            condition: Packet.L4Type == SCTP
			            fields: { SrcPort = 0:2, DstPort = 2:2, Tag = 4:4, Checksum = 8:4 }
			        }

			        // Add more transport protocols as needed
			    }

			    // Add more protocol blocks as needed (e.g., APPLICATION, DATA)
			}

			// Define Constants
			define TCP 17
			define UDP 8
			define IPv4 1
			define IPv6 2

			// HashMode Definitions using define/block Style
			HashMode Hash5Tuple {
			    Priority = 5
			    Encapsulation = VLAN,MPLS
			    Algorithm = CRC32
			    Key = 0xABCDEF
			    Tag = VlanTag
			}

			HashMode Hash3TupleGTPv1v2Sorted {
			    Priority = 10
			    Encapsulation = VLAN
			    Layer3Type = IPv4,IPv6
			    Layer4Type = TCP,UDP
			    Algorithm = SHA256
			}

			// Filter Definition for VLAN Traffic
			filter VLAN_TRAFFIC {
			    Frame[4:2] == IPv4
			    MPLS[TopLabel].Id == 100 AND MPLS[BottomLabel].Label == 200
			    VLAN[0].Id == 100 AND VLAN[1].Id == 200 AND VLAN.Count >= 2 AND VLAN.Payload == 3
			    Layer3.Type in [IPv4, IPv6]
			    Layer4.Type == TCP
			    Port == 4
			}

			// Action Definition for VLAN Traffic using Hash5Tuple
			action HandleVLANTraffic {
			    filter VLAN_TRAFFIC == true

			    log "SYN packet from 192.168.1.1 detected"
			    distribute = (0..3) based on Hash5Tuple

			    slice = 12:64
			}

			// Additional Action Definition using Hash3TupleGTPv1v2Sorted
			action DistributeWithHash3Tuple {
			    filter SomeOtherFilter == true

			    log "Distributing traffic based on Hash3TupleGTPv1v2Sorted"
			    distribute = (0..3) based on Hash3TupleGTPv1v2Sorted

			    slice = 20:128
			}
						""";
}
