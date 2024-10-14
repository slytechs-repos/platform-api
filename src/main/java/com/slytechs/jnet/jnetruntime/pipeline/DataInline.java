package com.slytechs.jnet.jnetruntime.pipeline;

public interface DataInline<T, T_BASE extends DataInline<T, T_BASE>>
		extends PipelineNode<T_BASE> {

	
	T inputData();
	
	T outputData();
	
	void outputData(T newOutput);
}
