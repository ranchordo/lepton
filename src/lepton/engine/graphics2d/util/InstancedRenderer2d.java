package lepton.engine.graphics2d.util;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;

import com.bulletphysics.linearmath.Transform;

import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.GObject;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.Texture;
import lepton.engine.rendering.TextureImage;
import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

public class InstancedRenderer2d {
	public static final String shader_priority="specific/textChar";
	public interface InstancedRenderRoutine2d {
		public void run();
	}
	private HashMap<InstancedRenderConfig2d,InstancedRenderConfig2d> renderConfigs=new HashMap<InstancedRenderConfig2d,InstancedRenderConfig2d>();
	private InstancedRenderConfig2d hashObject;
	public InstancedRenderConfig2d loadConfiguration(Shader s, TextureImage t, GObject g, int objectSize, String ssbo_name) {
		if(hashObject==null) {
			hashObject=new InstancedRenderConfig2d(s,t,g,0,null);
		} else {
			hashObject.shader=s;
			hashObject.image=t;
			hashObject.geo=g;
		}
		if(!renderConfigs.containsKey(hashObject)) {
			InstancedRenderConfig2d nconfig=new InstancedRenderConfig2d(s,t,g,objectSize,ssbo_name);
			renderConfigs.put(nconfig,nconfig);
			Logger.log(0,"Generating new 2d instanced render configuration for shader with name "+s.getFname());
			return nconfig;
		}
		return renderConfigs.get(hashObject);
	}
	private float[] mma=new float[16];
	private FloatBuffer fm=BufferUtils.createFloatBuffer(16);
	private InstancedRenderConfig2d priorityConfig=null;
	public HashMap<InstancedRenderConfig2d,InstancedRenderConfig2d> getRenderConfigMappings() {
		return renderConfigs;
	}
	private void renderConfig(InstancedRenderConfig2d s) {
		if(s.instanceAccumulator.getBuffer().position()==0) {
			//Nothing to render here
//			System.out.println("Nothing to render for config \""+s+"\"");
			return;
		}
//		System.out.println(s.toString());
		s.instanceAccumulator.submit();
		s.shader.bind();

		LeptonUtil.openGLMatrix(GLContextInitializer.proj_matrix,mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		s.shader.setUniformMatrix4fv("proj_matrix",fm);
		s.shader.setUniform1i("millis",(int)(LeptonUtil.micros())); //This is really dumb and I hate it.
		s.shader.applyAllSSBOs();
		int prevInstances=s.geo.instances;
		if(s.image!=null) {
			s.image.bind();
		}
		s.geo.instances=(s.instanceAccumulator.getBuffer().position())/s.instanceAccumulator.objectSize;
//		System.out.println(s.geo.instances);
		s.geo.render_raw();
		s.geo.instances=prevInstances;
	}
	public int numConfigs() {
		return renderConfigs.size();
	}
	public void renderInstanced(InstancedRenderRoutine2d renderRoutine) {
		priorityConfig=null;
		for(Entry<InstancedRenderConfig2d,InstancedRenderConfig2d> rc : renderConfigs.entrySet()) {
			rc.getValue().instanceAccumulator.reset();
		}
		renderRoutine.run();
		for(Entry<InstancedRenderConfig2d,InstancedRenderConfig2d> e : renderConfigs.entrySet()) {
			if(e.getValue().shader.getFname().equals(shader_priority)) {
				priorityConfig=e.getValue();
				continue;
			}
			InstancedRenderConfig2d s=e.getKey();
			renderConfig(s);
		}
		if(priorityConfig!=null) {
			renderConfig(priorityConfig); //Render text last
		}
	}
}
