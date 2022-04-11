package com.github.ranchordo.lepton.cpshlib;

import static org.lwjgl.opengl.GL44.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.lwjgl.system.MemoryStack;

import com.github.ranchordo.lepton.engine.util.Deletable;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public abstract class ShaderDataCompatible extends Deletable {
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
	public abstract void delete();
	/**
	 * No usey.
	 */
	public void setInitialFname(String ifn) {
		initialFname=ifn;
	}
	public String getInitialFname() {
		return initialFname;
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
		SSBO out=generateFromExistingSSBO(name,glGenBuffers());
		initSSBOData(initialLengthBytes,out);
		return out;
	}
	/**
	 * Create linked SSBO sharing the same buffer.
	 */
	public SSBO generateFromExistingSSBO(String name, SSBO in) {
		return generateFromExistingSSBO(name,in.buffer);
	}
	/**
	 * Create linked SSBO sharing the same buffer.
	 */
	public SSBO generateFromExistingSSBO(String name, int inbuffer) {
		SSBO out=new SSBO();
		out.buffer=inbuffer;
		out.id=SSBOId;
		if(SSBOId>=Math.min(0xFF,getBindingLimits())) {
			throw new IllegalStateException("SSBO binding limit.");
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
		initSSBOData(sizeBytes,ssbo.buffer,GL_STATIC_DRAW);
	}
	public static void initSSBOData(long sizeBytes, int ssbo, int mode) {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,ssbo);
		glBufferData(GL_SHADER_STORAGE_BUFFER,sizeBytes,mode);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,0);
	}
	public static void initSSBODataImmutable(long sizeBytes, int ssbo, int mode) {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,ssbo);
		glBufferStorage(GL_SHADER_STORAGE_BUFFER,sizeBytes,mode);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER,0);
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
				Logger.log(3,"Input capacity was "+data.capacity()+", current data's capacity was "+buffer.capacity());
				throw new IllegalArgumentException("Input capacity was "+data.capacity()+", current data's capacity was "+buffer.capacity());
			}
			buffer.position(0);
			int pos=data.position();
			data.position(0);
			buffer.put(data);
			data.position(pos);
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
		return mappify(ssbo.buffer,mode);
	}
	/**
	 * Map a buffer in GPU-side storage to CPU-accessible storage object for modification from CPU-side code. *****MAKE SURE TO CALL unMappify() AFTERWARDS*****
	 */
	public static FloatBuffer mappify(int buffer, int mode) {
		return mappify(GL_SHADER_STORAGE_BUFFER, buffer, mode);
	}
	/**
	 * Map a buffer in GPU-side storage to CPU-accessible storage object for modification from CPU-side code. *****MAKE SURE TO CALL unMappify() AFTERWARDS*****
	 */
	public static FloatBuffer mappify(int type, int buffer, int mode) {
		return mappifyBytes(type, buffer, mode).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	public static ByteBuffer mappifyBytes(int type, int buffer, int mode) {
		if(bufferMapped) {
			Logger.log(4,"We got a HUUUGE resource leak here. Unmap the buffer when you're done. Is it really that difficult?");
		}
		ByteBuffer ret=null;
		glBindBuffer(type,buffer);
		try {
			ret=glMapBuffer(type, mode);
			bufferMapped=true;
		} catch (NullPointerException e) {
			Logger.log(4,"Buffer map failure");
		}
		return ret;
	}
	/**
	 * Map a buffer in GPU-side storage to CPU-accessible storage object for modification from CPU-side code. *****MAKE SURE TO CALL unMappify() AFTERWARDS*****
	 */
	public static FloatBuffer mappifyRange(int buffer, int mode, long offset, int length) {
		return mappifyRange(GL_SHADER_STORAGE_BUFFER, buffer, mode, offset, length);
	}
	/**
	 * Map a buffer in GPU-side storage to CPU-accessible storage object for modification from CPU-side code. *****MAKE SURE TO CALL unMappify() AFTERWARDS*****
	 */
	public static FloatBuffer mappifyRange(int type, int buffer, int mode, long offset, int length) {
		return mappifyRangeBytes(type, buffer, mode, offset, length).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
	/**
	 * Map a buffer in GPU-side storage to CPU-accessible storage object for modification from CPU-side code. *****MAKE SURE TO CALL unMappify() AFTERWARDS*****
	 */
	public static ByteBuffer mappifyRangeBytes(int type, int buffer, int mode, long offset, int length) {
		if(bufferMapped) {
			Logger.log(4,"We got a HUUUGE resource leak here. Unmap the buffer when you're done. Is it really that difficult?");
		}
		ByteBuffer ret=null;
		glBindBuffer(type,buffer);
		try {
			ret=glMapBufferRange(type, offset, length, mode);
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
		unMappify(GL_SHADER_STORAGE_BUFFER);
	}
	/**
	 * Unmap active mapped GPU-side buffer.
	 */
	public static void unMappify(int type) {
		if(!glUnmapBuffer(type)) {
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
		if(ret==-1) {
			if(!IGNORE_MISSING) {Logger.log(2,name+" is not a valid shader uniform ("+initialFname+")");}
		}
		return ret;
	}
	public void setUniform1f(String name, float value) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform1f(location,value);
		}
	}
	public void setUniform1i(String name, int value) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform1i(location,value);
		}
	}
	public void setUniform3f(String name, Vector3f in) {
		setUniform3f(name,in.x,in.y,in.z);
	}
	public void setUniform3f(String name, float x, float y, float z) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform3f(location,x,y,z);
		}
	}
	public void setUniform2f(String name, float x, float y) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform2f(location,x,y);
		}
	}
	public void setUniform4f(String name, float x, float y, float z, float w) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform4f(location,x,y,z,w);
		}
	}
	public void setUniform3fv(String name, FloatBuffer d) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform3fv(location, d);
		}
	}
	public void setUniformiv(String name, IntBuffer d) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform1iv(location, d);
		}
	}
	public void setUniform4fv(String name, FloatBuffer d) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniform4fv(location, d);
		}
	}
	public void setUniformMatrix4fv(String name, FloatBuffer b) {
		setUniformMatrix4fv(name,b,false);
	}
	public void setUniformMatrix4fv(String name, FloatBuffer b, boolean transpose) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniformMatrix4fv(location, transpose, b);
		}
	}

	public void setUniformMatrix3fv(String name, FloatBuffer b) {
		int location=getUniformLocation(name);
		if(location!=-1) {
			glUniformMatrix3fv(location, false, b);
		}
	}
}