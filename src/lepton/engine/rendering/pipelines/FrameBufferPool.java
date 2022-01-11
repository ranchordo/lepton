package lepton.engine.rendering.pipelines;

import lepton.engine.rendering.FrameBuffer;
import lepton.optim.objpoollib.VariedAbstractObjectPool;
import lepton.optim.objpoollib.VariedPoolInitCreator;

public class FrameBufferPool extends VariedAbstractObjectPool<FrameBuffer> {
	public FrameBufferPool(String type) {
		super(type, new VariedPoolInitCreator<FrameBuffer>() {
			@Override
			public FrameBuffer allocateInitValueVaried(int desc) {
				return new FrameBuffer(desc&0xFF,-1,desc>>8);
			}
		});
		freeThshld=-1;
	}
}
