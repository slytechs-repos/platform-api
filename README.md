![Maven Central Version](https://img.shields.io/maven-central/v/com.slytechs.jnet.jnetruntime/jnetruntime-api)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.slytechs.jnet.jnetruntime/jnetruntime-api?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)

# jnetruntime-api

`jnetruntime-api` is a powerful and versatile Java library designed for advanced network programming and interaction. It leverages modern Java features to provide efficient and flexible APIs for dealing with network operations, timestamps, hashing algorithms, and more.

## Features

- **Support for JDK 22 FFM Foreign Function API**
  - Interact seamlessly with native code using the Foreign Function & Memory API.

- **Bit-Structures with MemoryLayout and Var Handles**
  - Define and manipulate bit-structures using `MemoryLayout` and variable handles.

- **Network-Focused Timestamp Support**
  - Enhanced timestamp functionalities tailored for network operations, ensuring accurate time tracking and synchronization.

- **Advanced Network Hashing Algorithms**
  - Implement various network hashing algorithms to ensure data integrity and security.

- **Semantic Versioning**
  - Manage and enforce semantic versioning within your projects using the `Version` class.

- **Hex String Utilities**
  - Efficiently handle and manipulate hexadecimal strings.
    
- **Processor Pipeline Support**
  - Data is processed through a sequence of processors in a _Pipeline_ configuration:
  ```mermaid
  graph LR
    I1[Input 1] --> H
    I2[Input 2] --> H
    I3[Input 3] --> H
    H[Head Node] --> P1
    P1[Processor 1] --> P2
    P2[Processor N] --> T
    T[Tail Node] --> O1[Output 1]
    T --> O2[Output 2]
    T --> O3[Output 3]

    style H fill:#f9f,stroke:#333,stroke-width:2px,color:#000
    style T fill:#ff9,stroke:#333,stroke-width:2px,color:#000
    style P1 fill:#9cf,stroke:#333,stroke-width:2px,color:#000
    style P2 fill:#9cf,stroke:#333,stroke-width:2px,color:#000

  ```

## Installation

Add the following dependency to your `pom.xml` if you are using Maven:

![Maven Central Version](https://img.shields.io/maven-central/v/com.slytechs.jnet.jnetruntime/jnetruntime-api)
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>jnetruntime-api</artifactId>
    <version>X.Y.Z</version>
</dependency>
```
### Using Latest SNAPSHOT Releases

Snapshot releases are frequent development iterations made between official production releases. While we recommend using stable releases for production code, snapshots allow you to access the latest updates and improvements.

To use the latest SNAPSHOT release, add the following repository to your Maven `pom.xml` configuration:

![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.slytechs.jnet.jnetruntime/jnetruntime-api?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)

```xml
<project>
  ...
  <repositories>
    <repository>
      <id>sonatype-snapshots</id>
      <name>Sonatype Snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
  ...
  <dependency>
    <groupId>com.slytechs.jnet.jnetpcap</groupId>
    <artifactId>jnetpcap-wrapper</artifactId>
    <version>X.Y.Z-SNAPSHOT</version>
  </dependency>
</project>
```
