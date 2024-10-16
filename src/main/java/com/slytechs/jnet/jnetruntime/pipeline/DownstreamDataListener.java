package com.slytechs.jnet.jnetruntime.pipeline;

public interface DownstreamDataListener<T> {
	
	void linkDownstream(T newData);
}
