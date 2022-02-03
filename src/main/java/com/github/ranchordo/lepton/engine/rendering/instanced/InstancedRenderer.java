package com.github.ranchordo.lepton.engine.rendering.instanced;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.cpshlib.SSBO;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.GObject;
import com.github.ranchordo.lepton.engine.rendering.Shader;
import com.github.ranchordo.lepton.engine.rendering.Texture;
import com.github.ranchordo.lepton.util.LeptonUtil;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class InstancedRenderer {
	@FunctionalInterface
	public interface InstancedRenderRoutine {
		public void run();
	}
	private String name;
	public InstancedRenderer(String n) {
		name=n;
	}
	public String getName() {
		return name;
	}
	private HashMap<InstancedRenderConfig,InstancedRenderConfig> renderConfigs=new HashMap<InstancedRenderConfig,InstancedRenderConfig>();
	private HashSet<SSBO> configuredSSBOs=new HashSet<SSBO>();
	private InstancedRenderConfig hashObject;
	public InstancedRenderConfig loadConfiguration(Shader s, Texture t, GObject g, int objectSize, String ssbo_name) {
		if(hashObject==null) {
			hashObject=new InstancedRenderConfig(null,null,null,0,null);
		}
		hashObject.shader=s;
		hashObject.tex=t;
		hashObject.geo=g;
		if(!renderConfigs.containsKey(hashObject)) {
			InstancedRenderConfig nconfig=new InstancedRenderConfig(s,t,g,objectSize,ssbo_name);
			renderConfigs.put(nconfig,nconfig);
			Logger.log(0,"Generating new "+name+" instanced render configuration for shader with name "+s.getFname()+", objectSize "+objectSize+", and SSBO "+ssbo_name);
			Logger.log(0,"New hashes for instanced render configuration are - S: "+Integer.toHexString(s.hashCode())
					+", T: "+LeptonUtil.getHashCodeHex(t)
					+", G: "+LeptonUtil.getHashCodeHex(g));
			Logger.log(0,"New identity hashes for instanced render configuration are - S: "+Integer.toHexString(System.identityHashCode(s))
					+", T: "+LeptonUtil.getIHashCodeHex(t)
					+", G: "+LeptonUtil.getIHashCodeHex(g));
			configuredSSBOs.add(nconfig.instanceAccumulator.getSSBO());
			return nconfig;
		}
		InstancedRenderConfig ret=renderConfigs.get(hashObject);
		if(ret==null) {
			Logger.log(4,"Instanced render configuration is null. This should never happen. xkcd.com/2200.");
		}
		return ret;
	}
	public int numConfigs() {
		return renderConfigs.size();
	}
	private Transform mmc=new Transform();
	private float[] mma=new float[16];
	private FloatBuffer fm=BufferUtils.createFloatBuffer(16);
	private void renderConfig(InstancedRenderConfig s) {
		if(s.instanceAccumulator.getPosition()==0) {
			//Nothing rendering in this config
			return;
		}
		s.shader.bind();
		mmc.set(GLContextInitializer.cameraTransform);
		mmc.getOpenGLMatrix(mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		GLContextInitializer.activeShader.setUniformMatrix4fv("world2view",fm);

		LeptonUtil.openGLMatrix(GLContextInitializer.proj_matrix,mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		s.shader.setUniformMatrix4fv("proj_matrix",fm);
		s.shader.setUniform1i("millis",(int)(LeptonUtil.micros())); //This is really dumb and I hate it.
		s.shader.setUniform1f("useLighting", (s.geo.useLighting && GLContextInitializer.useGraphics) ? 2 : 0);
		s.shader.setUniform1i("textureUse", s.geo.useTex?(s.tex==null?0:s.tex.loadedBitflag()):0);
		for(Entry<String,SSBO> se : s.shader.getSSBOMappings().entrySet()) { //Remote reconstruction of applyAllSSBOs(). But better.
			if(configuredSSBOs.contains(se.getValue())) {continue;}
			s.shader.applySSBO(se.getValue());
		}
		s.shader.refreshBlockBinding(s.instanceAccumulator.getSSBO());
		s.shader.applySSBO(s.instanceAccumulator.getSSBO());
		int prevInstances=s.geo.instances;
		if(s.tex!=null) {
			s.tex.bind();
		}
		s.geo.instances=(s.instanceAccumulator.getPosition())/s.instanceAccumulator.objectSize;
		s.geo.render_raw();
		s.geo.instances=prevInstances;
	}
	public void renderInstanced(InstancedRenderRoutine renderRoutine) {
		for(Entry<InstancedRenderConfig,InstancedRenderConfig> e : renderConfigs.entrySet()) {
			e.getValue().instanceAccumulator.reset();
		}
		renderRoutine.run();
		for(Entry<InstancedRenderConfig,InstancedRenderConfig> e : renderConfigs.entrySet()) {
			renderConfig(e.getValue());
		}
	}
}
