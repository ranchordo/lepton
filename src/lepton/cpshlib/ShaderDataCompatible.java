package lepton.cpshlib;

import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.lwjgl.system.MemoryStack;

import lepton.engine.rendering.instanced.InstanceAccumulator;
import lepton.util.advancedLogger.Logger;

public abstract class ShaderDataCompatible {
	/**
	 * Ignore when a uniform variable doesn't exist in the shader. If a uniform variable is unused in the shader this will also happen.
	 */
	public boolean IGNORE_MISSING=true;
	private HashMap<String, SSBO> ssbo=new HashMap<String, SSBO>();
	private HashMap<String,Integer> locationCache=new HashMap<String,Integer>();
	private byte SSBOId=1;
	public HashMap<String, SSBO> getSSBOMappings() {return ssbo;}
	private boolean gotBindingLimits=false;
	private int lastBindingLimits=0;
	private boolean programSynced=false;
	private int program_internal;
	
	private String initialFname="ERROR IN NAME";
	
	/**
	 * No usey.
	 */
	public void setInitialFname(String ifn) {
		initialFname=ifn;
	}
	/**
	 * No usey.
	 */
	public void setLightingSSBO(SSBO lssbo) {
		lightingSSBO=lssbo;
	}
	public SSBO getLightingDataSSBO() {
		return lightingSSBO;
	}
	
	/**
	 * Used internally for keeping track of light states. Do not change. Queries are probably useless unless you're doing something weird
	 */
	public boolean lightingInitialized=false;
	private SSBO lightingSSBO=null;
	/**
	 * DO NOT USE unless you're doing some weird stuff with SSBO merging. This method could completely mess up your ssbo setup. **Be careful.**
	 */
	public void decrementSSBOId() {
		SSBOId--;
	}
	public abstract void bind();
	/**
	 * For use on shader initialization. Don't use unless you're creating your own shader management classes (like ComputeShader, Shader, etc).
	 */
	public void syncRequiredShaderDataValues(int program, boolean bindErrorPolicy) {
		if(programSynced) {
			throw new IllegalStateException("Don't sync the progam value multiple times. Isn't program-id initialization supposed to be contructor-exclusive?");
		}
		program_internal=program;
		programSynced=true;
		IGNORE_MISSING=bindErrorPolicy;
	}
	/**
	 * Get OpenGL program id.
	 */
	private int program() {
		if(!programSynced) {
			throw new IllegalStateException("Maybe try syncing the program state first, okay?");
		}
		return program_internal;
	}
	/**
	 * Get max SSBO bindings.
	 */
	public int getBindingLimits() {
		if(!gotBindingLimits) {
			gotBindingLimits=true;
			try(MemoryStack stack=MemoryStack.stackPush()){
				IntBuffer x=stack.mallocInt(1);
				glGetIntegerv(GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS,x);
				lastBindingLimits=x.get(0);
				return lastBindingLimits;
			}
		}
		return lastBindingLimits;
	}
	/**
	 * Get pointer to an SSBO variable within the shader by name. Includes location caching.
	 */
	public int getResourceLocation(String name) {
		if(locationCache.containsKey(name)) {
			return locationCache.get(name);
		}
		int ret=glGetProgramResourceIndex(program(),GL_SHADER_STORAGE_BLOCK,name);
		locationCache.put(name,ret);
		return ret;
	}
	public SSBO generateNewSSBO(String name, long initialLengthBytes) {
		SSBO out=new SSBO();
		out.buffer=glGenBuffers();
		out.id=SSBOId;
		if(SSBOId>=Math.min(0xFF,getBindingLimits())) {
			throw new IllegalStateException("Ya hit the SSBO limit. Nice");
		}
		SSBOId++;
		out.location=getResourceLocation(name);
		if(out.location==-1) {
			Logger.log(2,name+" is not a valid shader SSBO binding point (generateNewSSBO). (From shader "+initialFname+")");
		}
		ssbo.put(name,out);
		glShaderStorageBlockBinding(program(), out.location, out.id);
		initSSBOData(initialLengthBytes,out);
		return out;
	}
	/**
	 * Create linked SSBO sharing the same buffer.
	 */
	public SSBO generateFromExistingSSBO(String name, SSBO in) {
		SSBO out=new SSBO();
		out.buffer=in.buffer;
		out.id=SSBOId;
		if(SSBOId>=Math.min(0xFF,getBindingLimits())) {
			throw new IllegalStateException("Ya hit the SSBO limit. Nice");
		}
		SSBOId++;
		out.location=getResourceLocation(name);
		if(out.location==-1) {
			Logger.log(2,name+" is not a valid shader SSBO binding point (generateFromExistingSSBO). From shader "+initialFname);
		}
		ssbo.put(name,out);
		glShaderStorageBlockBinding(program(), out.location, out.id);
		return out;
	}
	public void initSSBOData(long sizeBytes, SSBO ssbo) {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,ssbo.buffer);
		glBufferData(GL_SHADER_STORAGE_BUFFER,sizeBytes,GL_DYNAMIC_DRAW);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,0);
		glMemoryBarrier(GL_ALL_BARRIER_BITS);
	}
	public static void updateSSBOData(FloatBuffer data, SSBO ssbo) {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,ssbo.buffer);
		try {
			FloatBuffer buffer=null;
			try {
				buffer=glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_WRITE_ONLY).order(ByteOrder.nativeOrder()).asFloatBuffer();
			} catch (NullPointerException e) {
				Logger.log(4,"Ladies and gentlemen: We have issues.");
			}
			if(data.capacity()!=buffer.capacity()) {
				Logger.log(3,"Uh... you realize this is a method for *copying*, right? Input capacity was "+data.capacity()+", current data's capacity was "+buffer.capacity());
				throw new IllegalArgumentException("Uh... you realize this is a method for *copying*, right? Input capacity was "+data.capacity()+", current data's capacity was "+buffer.capacity());
			}
			buffer.position(0);
			for(int i=0;i<buffer.capacity();i++) {
				buffer.put(data.get(i));
			}
			//buffer.flip();
			if(!glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)) {
				Logger.log(4,"Buffer unmap failure");
			}
		} catch (Throwable e) {
			Logger.log(0,"We caught an exception during a mapped buffer period. No resource leak.");
			glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
			throw e;
		}
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,0);
		glMemoryBarrier(GL_ALL_BARRIER_BITS);
	}
	public void refreshBlockBinding(SSBO ssbo) {
		glShaderStorageBlockBinding(program(), ssbo.location, ssbo.id);
	}
	private static boolean bufferMapped=false;
	/**
	 * Map a buffer in GPU-side storage to CPU-accessible storage object for modification from CPU-side code. *****MAKE SURE TO CALL unMappify() AFTERWARDS*****
	 */
	public static FloatBuffer mappify(SSBO ssbo, int mode) {
		if(bufferMapped) {
			Logger.log(4,"We got a HUUUGE resource leak here. Unmap the buffer when you're done. Is it really that difficult?");
		}
		FloatBuffer ret=null;
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,ssbo.buffer);
		try {
			ret=glMapBuffer(GL_SHADER_STORAGE_BUFFER, mode).order(ByteOrder.nativeOrder()).asFloatBuffer();
			bufferMapped=true;
		} catch (NullPointerException e) {
			Logger.log(4,"Buffer map failure");
		}
		return ret;
	}
	/**
	 * Unmap active mapped GPU-side buffer.
	 */
	public static void unMappify() {
		if(!glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)) {
			Logger.log(4,"Buffer unmap failure");
		}
		bufferMapped=false;
	}
	public static void clearSSBOData(SSBO ssbo) {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,ssbo.buffer);
		try {
			FloatBuffer buffer=null;
			try {
				buffer=glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_WRITE_ONLY).order(ByteOrder.nativeOrder()).asFloatBuffer();
			} catch (NullPointerException e) {
				Logger.log(4,"Ladies and gentlemen: We have issues.");
			}
			buffer.position(0);
			for(int i=0;i<buffer.capacity();i++) {
				buffer.put(0);
			}
			//buffer.flip();
			if(!glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)) {
				Logger.log(4,"Buffer unmap failure");
			}
		} catch (Throwable e) {
			Logger.log(0,"We caught an exception during a mapped buffer period. No resource leak.");
			glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
			throw e;
		}
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,0);
		glMemoryBarrier(GL_ALL_BARRIER_BITS);
	}
	
	public void applyAllSSBOs() {
		for(Entry<String, SSBO> e : ssbo.entrySet()) {
			applySSBO(e.getValue());
		}
	}
	public void applyAllSSBOsExcept(SSBO s) {
		for(Entry<String, SSBO> e : ssbo.entrySet()) {
			if(e.getValue()==s) {return;}
			applySSBO(e.getValue());
		}
	}
	public void applySSBO(SSBO ssbo) {
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ssbo.id, ssbo.buffer);
		glMemoryBarrier(GL_ALL_BARRIER_BITS);
	}
	
	
	
	public int getUniformLocation(String name) {
		if(locationCache.containsKey(name)) {
			return locationCache.get(name);
		}
		int ret=glGetUniformLocation(program(),name);
		locationCache.put(name,ret);
		return ret;
	}
	public void setUniform1f(String name, float value) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform1f(location,value);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniform1i(String name, int value) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform1i(location,value);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniform3f(String name, Vector3f in) {
		setUniform3f(name,in.x,in.y,in.z);
	}
	public void setUniform3f(String name, float x, float y, float z) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform3f(location,x,y,z);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniform2f(String name, float x, float y) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform2f(location,x,y);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniform4f(String name, float x, float y, float z, float w) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform4f(location,x,y,z,w);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniform3fv(String name, FloatBuffer d) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform3fv(location, d);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniformiv(String name, IntBuffer d) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform1iv(location, d);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniform4fv(String name, FloatBuffer d) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform4fv(location, d);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
	public void setUniformMatrix4fv(String name, FloatBuffer b) {
		setUniformMatrix4fv(name,b,false);
	}
	public void setUniformMatrix4fv(String name, FloatBuffer b, boolean transpose) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniformMatrix4fv(location, transpose, b);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}

	public void setUniformMatrix3fv(String name, FloatBuffer b) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniformMatrix3fv(location, false, b);
		} else {
			if(!IGNORE_MISSING) {Logger.log(3,name+" is not a valid shader uniform");}
		}
	}
}