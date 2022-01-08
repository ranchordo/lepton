package lepton.engine.rendering.pipelines;

import lepton.engine.rendering.FrameBuffer;
import lepton.optim.objpoollib.VariedAbstractObjectPool;

public class FrameBufferPool extends VariedAbstractObjectPool<FrameBuffer> {
	public FrameBufferPool(String type) {
		super(type, null);
		freeThshld=-1;
	}
}
