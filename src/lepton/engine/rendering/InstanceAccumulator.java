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
	private boolean dataChanged=false;
	public static final byte NO_MERGE=0;
	public static final byte ID_MERGE=1;
	public static final byte FULL_MERGE=2;
	public static boolean runAggressiveChangeCheckDefault=false;
	/**
	 * 0: no merge. 1: id merge. 2: full object+buffer merge.
	 */
	public static byte mergeSSBOsOnDuplicate=FULL_MERGE;
	public boolean runAggressiveChangeCheck=false;
	public boolean hasDataChanged() {
		return dataChanged;
	}
	public InstanceAccumulator(ShaderDataCompatible shader, int objectSize, String name) {
		this.shader=shader;
		this.objectSize=objectSize;
		this.buffer=BufferUtils.createFloatBuffer(0);
		SSBO s=shader.getSSBOMappings().get(name);
		if(s==null || mergeSSBOsOnDuplicate!=FULL_MERGE) {
			ssbo=shader.generateNewSSBO(name,0);
			if(mergeSSBOsOnDuplicate==ID_MERGE && s!=null) {
				shader.decrementSSBOId();
				ssbo.id=s.id;
			}
		} else {
			ssbo=s;
		}
		runAggressiveChangeCheck=runAggressiveChangeCheckDefault;
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
		dataChanged=false;
	}
	public void add(float[] data) {
		if(data.length!=objectSize) {
			Logger.log(4,"Incorrect size of "+data.length+" in instace accumulator. Correct object size (floats) is "+objectSize+".");
		}
		if((buffer.position()+data.length>buffer.capacity()) || buffer==null) {
			//We need to reallocate
			if(buffer.capacity()>1048576) {
				Logger.log(0,"We may have an oversized buffer. Reallocating with new size "+(buffer.position()+data.length));
			}
			FloatBuffer newbuffer=BufferUtils.createFloatBuffer(buffer.position()+data.length);
			shader.initSSBOData(newbuffer.capacity()*4,ssbo);
			newbuffer.position(0);
			for(int i=0;i<buffer.position();i++) {
				newbuffer.put(buffer.get(i));
			}
			buffer=newbuffer;
		}
		for(int i=0;i<data.length;i++) {
			if(runAggressiveChangeCheck) { dataChanged=dataChanged || (buffer.get(buffer.position())!=data[i]); }
			buffer.put(data[i]);
		}
		dataChanged=dataChanged || !runAggressiveChangeCheck;
	}
	public void reserveObject() {
		if((buffer.position()+objectSize>buffer.capacity()) || buffer==null) {
			//We need to reallocate
			if(buffer.capacity()>1048576) {
				Logger.log(0,"We may have an oversized buffer. Reallocating with new size "+(buffer.position()+objectSize));
			}
			FloatBuffer newbuffer=BufferUtils.createFloatBuffer(buffer.position()+objectSize);
			shader.initSSBOData(newbuffer.capacity()*4,ssbo);
			newbuffer.position(0);
			for(int i=0;i<buffer.position();i++) {
				newbuffer.put(buffer.get(i));
			}
			buffer=newbuffer;
			dataChanged=true;
		}
		buffer.position(buffer.position()+objectSize);
	}
	public void submit() {
		if(hasDataChanged()) {
			ShaderDataCompatible.updateSSBOData(buffer,ssbo);
		}
	}
}
