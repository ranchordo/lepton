package com.github.ranchordo.lepton.engine.rendering.lighting;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.cpshlib.ShaderDataCompatible;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.GObject;
import com.github.ranchordo.lepton.engine.util.GenericCubeFactory;
import com.github.ranchordo.lepton.optim.objpoollib.DefaultVecmathPools;
import com.github.ranchordo.lepton.optim.objpoollib.PoolElement;
import com.github.ranchordo.lepton.util.LeptonUtil;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

/**
 * Manage lighting data
 */
public class Lighting {
	private static GObject genericCube=null;
	private static Transform positionTransform;
	private static Matrix4f ptmat;
	private static Quat4f norot=LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,0));
	public static void startDebugRendering() {
		positionTransform=new Transform();
		genericCube=GenericCubeFactory.createGenericCube();
		genericCube.wireframe=true;
		ptmat=new Matrix4f();
	}
	public static void renderDebug() {
		if(genericCube==null) {
			Logger.log(3,"Lighting.renderDebug: Start debug rendering before trying a debug render.");
			return;
		}
		GL43.glDisable(GL43.GL_DEPTH_TEST);
		for(Light l : lights) {
			if(l.type==Light.LIGHT_POSITION) {
				PoolElement<Vector3f> pe1=DefaultVecmathPools.vector3f.alloc();
				PoolElement<Vector3f> pe2=DefaultVecmathPools.vector3f.alloc();
				pe1.o().set(l.prop);
				pe2.o().set(l.intensity.x,l.intensity.y,l.intensity.z);
				ptmat.set(norot,pe1.o(),1.02f-(float)Math.exp(-0.1*pe2.o().length()));
				positionTransform.set(ptmat);
				pe1.free();
				pe2.free();
				genericCube.setColor(Math.min(l.intensity.x,1),
						Math.min(l.intensity.y,1),
						Math.min(l.intensity.z,1));
				genericCube.copyData(GObject.COLOR_DATA,GL43.GL_STATIC_DRAW);
				genericCube.highRender_customTransform(positionTransform);
			}
		}
		GL43.glEnable(GL43.GL_DEPTH_TEST);
	}
	public static final int MAX_LIGHTS=50;
	private static ArrayList<Light> lights=new ArrayList<Light>();

	public static void addLight(Light light) {
		if(light==null) {
			Logger.log(4,"Adding null light? What is wrong with you?");
		}
		lights.add(light);
		reinitBuffers();
	}
	public static void removeLight(Light light) {
		lights.remove(light);
		reinitBuffers();
	}
	public static void clear() {
		lights.clear();
		reinitBuffers();
	}
	/**
	 * Apply lighting data to a shader
	 */
	public static void apply() {
		apply(GLContextInitializer.activeShader);
	}
	//private static FloatBuffer fbuffer;
	private static boolean firstSSBOInitialized=false;
	private static int firstSSBO;
	public static int getGLBufferPointer() {
		if(!firstSSBOInitialized) {
			throw new IllegalStateException("Lighting buffer has not been initialized yet.");
		}
		return firstSSBO;
	}
	public static void delete() {
		if(firstSSBOInitialized) {
			GL43.glDeleteBuffers(firstSSBO);
			firstSSBO=0;
			firstSSBOInitialized=false;
		}
		lights.clear();
	}
	private static void createInitialBuffer() {
		firstSSBO=GL43.glGenBuffers();
		firstSSBOInitialized=true;
		refreshLightSize();
	}
	private static void refreshLightSize() {
		GL43.glDeleteBuffers(firstSSBO);
		firstSSBO=GL43.glGenBuffers();
		ShaderDataCompatible.initSSBODataImmutable(Light.LIGHT_SIZE_FLOATS*lights.size()*4,firstSSBO,GL44.GL_DYNAMIC_STORAGE_BIT|GL44.GL_MAP_READ_BIT|GL44.GL_MAP_WRITE_BIT);
	}
	/**
	 * Apply lighting data to a specific shader.
	 */
	public static void apply(ShaderDataCompatible shader) {
		if(shader==null) {
			Logger.log(3,"Lighting.apply(ShaderDataCompatible): Input shader is null. This commonly means that no shader was active when apply() was called.");
			return;
		}
		//Initialize the lighting if not initialized:
		if(!shader.lightingInitialized) {
			if(!firstSSBOInitialized) {
				createInitialBuffer();
			}
			shader.setLightingSSBO(shader.generateFromExistingSSBO("lights_buffer",firstSSBO));
			shader.lightingInitialized=true;
		}
		shader.setUniform1i("num_lights",lights.size());
		shader.applySSBO(shader.getLightingDataSSBO());

	}
	private static float[] templight=new float[Light.LIGHT_SIZE_FLOATS];
	/**
	 * Update the lighting data for a single light.
	 */
	public static void updateLight(Light light) {
		if(!firstSSBOInitialized) {return;}
		int index=light.lightID*Light.LIGHT_SIZE_FLOATS;
		//FloatBuffer templight=ShaderDataCompatible.mappify(firstSSBO,GL43.GL_WRITE_ONLY);
		//templight.position(index);
//		templight.put((float)light.type);
//		templight.put(0.0f);
//		templight.put(0.0f);
//		templight.put(0.0f);
//		templight.put(light.prop.x);
//		templight.put(light.prop.y);
//		templight.put(light.prop.z);
//		templight.put(0.0f);
//		templight.put(light.intensity.x);
//		templight.put(light.intensity.y);
//		templight.put(light.intensity.z);
//		templight.put(light.intensity.w);
		templight[0]=(float)light.type;
		templight[1]=0.0f;
		templight[2]=0.0f;
		templight[3]=0.0f;
		templight[4]=light.prop.x;
		templight[5]=light.prop.y;
		templight[6]=light.prop.z;
		templight[7]=0.0f;
		templight[8]=light.intensity.x;
		templight[9]=light.intensity.y;
		templight[10]=light.intensity.z;
		templight[11]=light.intensity.w;
		GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER,firstSSBO);
		GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER,index*4,templight);
		GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER,0);
		//ShaderDataCompatible.unMappify();
	}
	public static void updateAllLights() {
		for(Light light : lights) {
			updateLight(light);
		}
	}
	/**
	 * Whenever you add or remove a light. ***Automatically done in addLight and removeLight***.
	 */
	public static void reinitBuffers() {
		if(!firstSSBOInitialized) {
			createInitialBuffer();
		}
		FloatBuffer fbuffer=ShaderDataCompatible.mappify(firstSSBO,GL43.GL_WRITE_ONLY);
		if(fbuffer.capacity()!=Light.LIGHT_SIZE_FLOATS*lights.size()) {
			ShaderDataCompatible.unMappify(); fbuffer=null; //Make sure we don't have a residual pointer
			refreshLightSize();
			fbuffer=ShaderDataCompatible.mappify(firstSSBO,GL43.GL_WRITE_ONLY);
		}
		int id=0;
		fbuffer.position(0);
		for(Light light : lights) {
			fbuffer.put((float)light.type);

			fbuffer.put(0.0f);
			fbuffer.put(0.0f);
			fbuffer.put(0.0f);

			light.lightID=id;
			fbuffer.put(light.prop.x);
			fbuffer.put(light.prop.y);
			fbuffer.put(light.prop.z);
			fbuffer.put(0.0f);

			fbuffer.put(light.intensity.x);
			fbuffer.put(light.intensity.y);
			fbuffer.put(light.intensity.z);
			fbuffer.put(light.intensity.w);
		}
		ShaderDataCompatible.unMappify();
	}
}
