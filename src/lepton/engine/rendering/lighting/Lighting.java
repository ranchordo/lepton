package lepton.engine.rendering.lighting;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;

import org.lwjgl.BufferUtils;

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
	private static int[] types_arr;
	private static float[] prop_arr;
	private static float[] intensities_arr;
	
	public static void addLight(Light light) {
		if(light==null) {
			Logger.log(4,"Adding null light? What is wrong with you?");
		}
		lights.add(light);
		doUniforms();
	}
	public static void removeLight(Light light) {
		lights.remove(light);
		doUniforms();
	}
	public static void clear() {
		lights.clear();
		doUniforms();
	}
	/**
	 * Apply lighting data to a shader
	 */
	public static void apply() {
		apply(GLContextInitializer.activeShader);
	}
	private static IntBuffer types_buffer;
	private static FloatBuffer prop_buffer;
	private static FloatBuffer int_buffer;
	/**
	 * Apply lighting data to a specific shader.
	 */
	public static void apply(ShaderDataCompatible shader) {
		if(shader==null) {
			Logger.log(3,"Lighting.apply: No shader is active.");
			return;
		}
		shader.setUniformiv("types",types_buffer);
		shader.setUniform3fv("prop",prop_buffer);
		shader.setUniform4fv("intensities",int_buffer);
	}
	/**
	 * Update the lighting data for a single light.
	 */
	public static void updateUniforms(Light light) {
		prop_arr[light.propPointer]=light.prop.x;
		prop_arr[light.propPointer+1]=light.prop.y;
		prop_arr[light.propPointer+2]=light.prop.z;
		
		intensities_arr[light.intensityPointer]=light.intensity.x;
		intensities_arr[light.intensityPointer+1]=light.intensity.y;
		intensities_arr[light.intensityPointer+2]=light.intensity.z;
		intensities_arr[light.intensityPointer+3]=light.intensity.w;
		
		Util.asFloatBuffer(prop_arr,prop_buffer);
		Util.asFloatBuffer(intensities_arr,int_buffer);
	}
	/**
	 * Whenever you add or remove a light. Automatically done in addLight and removeLight.
	 */
	public static void doUniforms() {
		ArrayList<Integer> types=new ArrayList<Integer>();
		ArrayList<Float> prop_exp=new ArrayList<Float>();
		ArrayList<Float> intensities_exp=new ArrayList<Float>();
		for(Light light : lights) {
			types.add(light.type);
			
			light.propPointer=prop_exp.size();
			prop_exp.add(light.prop.x);
			prop_exp.add(light.prop.y);
			prop_exp.add(light.prop.z);
			
			light.intensityPointer=intensities_exp.size();
			intensities_exp.add(light.intensity.x);
			intensities_exp.add(light.intensity.y);
			intensities_exp.add(light.intensity.z);
			intensities_exp.add(light.intensity.w);
		}
		types_arr=new int[MAX_LIGHTS];
		prop_arr=new float[MAX_LIGHTS*3];
		intensities_arr=new float[MAX_LIGHTS*4];
		
		for(int i=0;i<types.size();i++) {types_arr[i]=types.get(i);}
		for(int i=0;i<prop_exp.size();i++) {prop_arr[i]=prop_exp.get(i);}
		for(int i=0;i<intensities_exp.size();i++) {intensities_arr[i]=intensities_exp.get(i);}
		
		types_buffer=BufferUtils.createIntBuffer(types_arr.length);
		prop_buffer=BufferUtils.createFloatBuffer(prop_arr.length);
		int_buffer=BufferUtils.createFloatBuffer(intensities_arr.length);
		
		Util.asIntBuffer(types_arr,types_buffer);
		Util.asFloatBuffer(prop_arr,prop_buffer);
		Util.asFloatBuffer(intensities_arr,int_buffer);
	}
}
