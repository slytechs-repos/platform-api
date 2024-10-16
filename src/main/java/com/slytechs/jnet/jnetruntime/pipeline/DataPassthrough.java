package com.slytechs.jnet.jnetruntime.pipeline;

public interface DataPassthrough<T, T_BASE extends DataPassthrough<T, T_BASE>>
		extends PipelineNode<T_BASE> {

	default T inputData() {
		return outputData();
	}

	T outputData();

}
