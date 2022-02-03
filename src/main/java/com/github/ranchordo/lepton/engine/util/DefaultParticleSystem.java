package com.github.ranchordo.lepton.engine.util;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.cpshlib.ComputeShader;
import com.github.ranchordo.lepton.cpshlib.SSBO;
import com.github.ranchordo.lepton.engine.graphics2d.tiles.Tile2d;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.GObject;
import com.github.ranchordo.lepton.engine.rendering.Shader;
import com.github.ranchordo.lepton.util.LeptonUtil;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class DefaultParticleSystem {
	private ComputeShader cpsh;
	private Shader renderingShader;
	private Vector3f origin=new Vector3f(0,0,0);
	private Vector3f vi=new Vector3f(0,0,0);
	private int n;
	private int r;
	private GObject geo;
	private int power=1;
	public Vector3f getOrigin() {
		return origin;
	}
	public Vector3f getVi() {
		return vi;
	}
	public void setOrigin(Vector3f o) {
		origin.set(o);
	}
	public void setOrigin(float x, float y, float z) {
		origin.set(x,y,z);
	}
	public void setVi(Vector3f o) {
		vi.set(o);
	}
	public void setVi(float x, float y, float z) {
		vi.set(x,y,z);
	}
	public DefaultParticleSystem(String computeShaderName, String renderShaderName, String bufferName, int maxNumParticles, int particlesPerSecond, int floatsPerParticle) {
		cpsh=GLContextInitializer.cpshLoader.load(computeShaderName);
		renderingShader=GLContextInitializer.shaderLoader.load(renderShaderName);
		n=maxNumParticles;
		if(n>=100000000) { //I hope we never hit this
			power=3;
			n=(int)Math.round(Math.pow(n,1.0/power));
		} else if(n>=10000) {
			power=2;
			n=(int)Math.round(Math.pow(n,1.0/power));
		}
		r=particlesPerSecond;
		renderingShader.generateFromExistingSSBO(bufferName,cpsh.generateNewSSBO(bufferName,4*floatsPerParticle*(int)Math.pow(n,power)));
		geo=Tile2d.createGenericSquare();
		geo.instances=(int)Math.pow(n,power);
	}
	public DefaultParticleSystem(int maxNumParticles, int particlesPerSecond) {
		this("particleDefault","specific/particleDefaultRender","particles_buffer",maxNumParticles,particlesPerSecond,8);
	}
	private long micros=0;
	private int pparticles=0;
	private Transform mmc=new Transform();
	private float[] mma=new float[16];
	private FloatBuffer fm=BufferUtils.createFloatBuffer(16);
	private long pmicros=0;
	public void render() {
		if(micros==0) {
			micros=LeptonUtil.micros();
			pmicros=micros;
			return;
		}
		float t=(float)((double)(LeptonUtil.micros()-micros)/1000000.0); //Seconds
		int particlesTotal=Math.round(t*r);
		cpsh.bind();
		cpsh.setUniform1i("toemit",particlesTotal-pparticles);
		cpsh.setUniform1i("stindex",pparticles%((int)Math.pow(n,power)));
		cpsh.setUniform1f("time",t);
		cpsh.setUniform3f("origin",origin);
		cpsh.setUniform3f("Vi",vi);
		float dt=(float)((double)(LeptonUtil.micros()-pmicros)/1000000.0);
		cpsh.setUniform1f("dt",dt);
		pmicros=LeptonUtil.micros();
		cpsh.applyAllSSBOs();
		switch(power) {
		case 1:
			cpsh.dispatch(n,1,1);
			break;
		case 2:
			cpsh.dispatch(n,n,1);
			break;
		case 3:
			cpsh.dispatch(n,n,n);
			break;
		default:
			Logger.log(4,"Power is "+power+". What the heck?");
		}
		pparticles=particlesTotal;
		
		renderingShader.bind();
		mmc.set(GLContextInitializer.cameraTransform);
		mmc.getOpenGLMatrix(mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		GLContextInitializer.activeShader.setUniformMatrix4fv("world2view",fm);
		LeptonUtil.openGLMatrix(GLContextInitializer.proj_matrix,mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		renderingShader.setUniformMatrix4fv("proj_matrix",fm);
		renderingShader.setUniform1f("time",t);
		renderingShader.applyAllSSBOs();
		GL15.glDepthMask(false);
		geo.render_raw();
		GL15.glDepthMask(true);
	}
}
