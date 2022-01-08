package lepton.engine.rendering.lighting;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43;

import lepton.cpshlib.SSBO;
import lepton.cpshlib.ShaderDataCompatible;
import lepton.engine.rendering.GLContextInitializer;
import lepton.util.advancedLogger.Logger;

/**
 * Manage lighting data
 */
public class Lighting {
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
			return;
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
	public static void updateLight(Light light) {
		if(firstSSBO==null) {return;}
		int index=light.lightID*Light.LIGHT_SIZE_FLOATS;
		fbuffer.position(index);
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
