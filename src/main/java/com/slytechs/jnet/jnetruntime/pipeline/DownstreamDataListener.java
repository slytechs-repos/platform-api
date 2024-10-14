package com.slytechs.jnet.jnetruntime.pipeline;

public interface DownstreamDataListener<T> {
	
	void linkAllUpstream(T downstreamData);

	void onDataDownstreamChange(T newData);
}
