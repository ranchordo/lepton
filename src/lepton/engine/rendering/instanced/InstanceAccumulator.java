package lepton.engine.rendering.instanced;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.GL43;

import lepton.cpshlib.SSBO;
import lepton.cpshlib.ShaderDataCompatible;
import lepton.util.advancedLogger.Logger;

public class InstanceAccumulator {
	public static boolean suppressOversizeWarnings=false;
	public int objectSize;
//	private FloatBuffer buffer;
	private int position=0;
	private int capacity=0;
	private SSBO ssbo;
	private ShaderDataCompatible shader;
	public static final byte NO_MERGE=0;
	public static final byte ID_MERGE=1;
	public static final byte FULL_MERGE=2;
	/**
	 * 0: no merge. 1: id merge. 2: full object+buffer merge.
	 */
	public static byte mergeSSBOsOnDuplicate=NO_MERGE;
	public InstanceAccumulator(ShaderDataCompatible shader, int objectSize, String name) {
		this.shader=shader;
		this.objectSize=objectSize;
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
	}
	public SSBO getSSBO() {
		return ssbo;
	}
	public int getPosition() {
		return position;
	}
	public int getCapacity() {
		return capacity;
	}
	public ShaderDataCompatible getShader() {
		return shader;
	}
	public void reset() {
		position=0;
	}
	private void reallocateTo(int sizeFloats) {
		if(sizeFloats>capacity) {
			if(true) {//sizeFloats>1048576 && !suppressOversizeWarnings) {
				Logger.log(2,"Allocating a new buffer with size "+sizeFloats+" for instanceaccumulator with shader "+shader.getInitialFname()+". Possible oversized buffer. Set suppressOversizeWarnings to disable this check.");
			}
			FloatBuffer newbuffer=BufferUtils.createFloatBuffer(capacity);
			GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER,ssbo.buffer);
			GL43.glGetBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER,0,newbuffer);
			GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER,sizeFloats*4,GL43.GL_STREAM_DRAW);
			GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER,0,newbuffer);
			GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER,0);
			capacity=sizeFloats;
		}
	}
	public void add(float[] data) {
		if(data.length!=objectSize) {
			Logger.log(4,"Incorrect size of "+data.length+" in instace accumulator. Correct object size (floats) is "+objectSize+".");
		}
		reallocateTo(data.length+position);
		GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER,ssbo.buffer);
		GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER,position*4,data);
		GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER,0);
		position+=objectSize;
	}
	public void reserveObject() {
		reallocateTo(objectSize+position);
		position+=objectSize;
	}
}
