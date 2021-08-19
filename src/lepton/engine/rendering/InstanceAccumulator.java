package lepton.engine.rendering;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import lepton.cpshlib.SSBO;
import lepton.cpshlib.ShaderDataCompatible;
import lepton.util.advancedLogger.Logger;

public class InstanceAccumulator {
	public int objectSize;
	private FloatBuffer buffer;
	private SSBO ssbo;
	private ShaderDataCompatible shader;
	public InstanceAccumulator(ShaderDataCompatible shader, int objectSize, String name) {
		this.shader=shader;
		this.objectSize=objectSize;
		this.buffer=BufferUtils.createFloatBuffer(0);
		ssbo=shader.generateNewSSBO(name,0);
	}
	public FloatBuffer getBuffer() {
		return buffer;
	}
	public SSBO getSSBO() {
		return ssbo;
	}
	public ShaderDataCompatible getShader() {
		return shader;
	}
	public void reset() {
		buffer.position(0);
	}
	public void add(float[] data) {
		if(data.length!=objectSize) {
			Logger.log(4,"Incorrect size of "+data.length+" in instace accumulator. Correct object size (floats) is "+objectSize+".");
		}
		if((buffer.position()+data.length>buffer.capacity()) || buffer==null) {
			//We need to reallocate
			FloatBuffer newbuffer=BufferUtils.createFloatBuffer(buffer.position()+data.length);
			shader.initSSBOData(newbuffer.capacity()*4,ssbo);
			newbuffer.position(0);
			for(int i=0;i<buffer.position();i++) {
				newbuffer.put(buffer.get(i));
			}
			buffer=newbuffer;
		}
		for(int i=0;i<data.length;i++) {
			buffer.put(data[i]);
		}
	}
	public void submit() {
		ShaderDataCompatible.updateSSBOData(buffer,ssbo);
	}
}
