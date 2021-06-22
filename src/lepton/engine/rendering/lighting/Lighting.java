package lepton.engine.rendering.lighting;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL46.GL_WRITE_ONLY;

import lepton.cpshlib.main.SSBO;
import lepton.cpshlib.main.ShaderDataCompatible;
import lepton.engine.rendering.GLContextInitializer;
import lepton.util.Util;
import lepton.util.advancedLogger.Logger;

/**
 * Manage lighting data
 */
public class Lighting {
	public static final int MAX_LIGHTS=50;
	private static HashSet<Light> lights=new HashSet<Light>();
	
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
	private static FloatBuffer fbuffer;
	private static boolean firstSSBOInitialized=false;
	private static SSBO firstSSBO;
	/**
	 * Apply lighting data to a specific shader.
	 */
	public static void apply(ShaderDataCompatible shader) {
		if(shader==null) {
			Logger.log(3,"Lighting.apply(ShaderDataCompatible): Input shader is null. This commonly means that no shader was active when apply() was called.");
			return;
		}
		if(fbuffer==null) {
			Logger.log(4,"Lighting buffer has not yet been populated.");
		}
		//Initialize the lighting if not initialized:
		if(!shader.lightingInitialized) {
			if(!firstSSBOInitialized) {
				firstSSBO=shader.generateNewSSBO("lights_buffer",fbuffer.capacity()*4);
				shader.setLightingSSBO(firstSSBO);
				firstSSBOInitialized=true;
			} else {
				shader.setLightingSSBO(shader.generateFromExistingSSBO("lights_buffer",firstSSBO));
			}
			shader.lightingInitialized=true;
		}
		if(firstSSBO==null) {
			Logger.log(4,"xkcd.com/2200");
		}
		ShaderDataCompatible.updateSSBOData(fbuffer,firstSSBO);
		if(shader.getLightingDataSSBO()==null) {
			Logger.log(4,"LightingDataSSBO is null. Uggghh");
		}
		shader.setUniform1i("num_lights",fbuffer.capacity()/Light.LIGHT_SIZE_FLOATS);
		shader.applySSBO(shader.getLightingDataSSBO());
	}
	/**
	 * Update the lighting data for a single light.
	 */
	public static void updateUniforms(Light light) {
		FloatBuffer fb=ShaderDataCompatible.mappify(firstSSBO,GL_WRITE_ONLY); {
			int index=light.lightID*Light.LIGHT_SIZE_FLOATS;
			fb.position(index);
			fbuffer.put((float)light.type);
			
			fbuffer.put(0.0f);
			fbuffer.put(0.0f);
			fbuffer.put(0.0f);
			
			fbuffer.put(light.prop.x);
			fbuffer.put(light.prop.y);
			fbuffer.put(light.prop.z);
			fbuffer.put(0.0f);
			
			fbuffer.put(light.intensity.x);
			fbuffer.put(light.intensity.y);
			fbuffer.put(light.intensity.z);
			fbuffer.put(light.intensity.w);
		} ShaderDataCompatible.unMappify();
	}
	public static void updateUniforms() {
		reinitBuffers();
	}
	/**
	 * Whenever you add or remove a light. ***Automatically done in addLight and removeLight***.
	 */
	public static void reinitBuffers() {
		if(fbuffer==null) {
			fbuffer=BufferUtils.createFloatBuffer(Light.LIGHT_SIZE_FLOATS*lights.size());
		}
		if(fbuffer.capacity()!=Light.LIGHT_SIZE_FLOATS*lights.size()) {
			fbuffer=BufferUtils.createFloatBuffer(Light.LIGHT_SIZE_FLOATS*lights.size());
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
	}
}
