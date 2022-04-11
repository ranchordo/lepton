package com.github.ranchordo.lepton.tests;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.engine.rendering.FrameBuffer;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.Shader;
import com.github.ranchordo.lepton.engine.rendering.lighting.Light;
import com.github.ranchordo.lepton.engine.rendering.lighting.Lighting;
import com.github.ranchordo.lepton.engine.util.DefaultParticleSystem;
import com.github.ranchordo.lepton.util.InputHandler;
import com.github.ranchordo.lepton.util.LeptonUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class SDFTest {
	public void Main() throws Exception {
		GLContextInitializer.initializeGLContext(true, 1280, 720, false, "SDF Test");
		
		Shader SDFCompositor=new Shader("SDFTest/SDFCompositor");
		Shader deferred=new Shader("fancyTest/deferred"); //(*Stealing from myself intensifies*)//
		
		DefaultParticleSystem psys=new DefaultParticleSystem("SDFTest/particle_motion", null, "particles_buffer", 30, 0, 8, false);
		SDFCompositor.generateFromExistingSSBO("particles_buffer", psys.getSSBO());
		
		FrameBuffer SDFGBuf=new FrameBuffer(16,5,GL_RGB16F);
		
		InputHandler inputHandler=new InputHandler(GLContextInitializer.win);
		Matrix4f t=new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,-0.0f)),new Vector3f(0,1.5f,9f),1);
		t.invert();
		Lighting.addLight(new Light(Light.LIGHT_POSITION, 0,-1,0, 20,20,20,1f));
		Lighting.addLight(new Light(Light.LIGHT_AMBIENT, 0,5,0, 0.3f,0.3f,0.3f,3f));
		GLContextInitializer.cameraTransform=new Transform(t);
		InputHandler ih=new InputHandler(GLContextInitializer.win);
		glfwSwapInterval(0);
		long cc=0;
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			GLContextInitializer.timeCalcStart();
			glfwPollEvents();
			if(inputHandler.i(GLFW_KEY_ESCAPE)) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			glClearColor(0,0,0,1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			SDFGBuf.bind();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			psys.render(); //Although not much of a rendering, since rendering for this system is disabled.
			SDFCompositor.bind();
			SDFCompositor.setUniform2f("dimensions", GLContextInitializer.winW, GLContextInitializer.winH);
			SDFCompositor.setUniform1i("numParticles", psys.getActualNumParticles());
			LeptonUtil.setShaderUniformTransform("world2view", GLContextInitializer.cameraTransform);
			SDFCompositor.setSamplersDefault(5);
			SDFGBuf.bind();
			GLContextInitializer.defaultScreen.render();
			FrameBuffer.unbind_all();
			//Deferred time. Ready?
			deferred.bind();
			deferred.setSamplersDefault(5);
			deferred.setUniform2f("windowpixels",GLContextInitializer.winW, GLContextInitializer.winH);
			SDFGBuf.bindTexture(0,0);
			SDFGBuf.bindTexture(1,1);
			SDFGBuf.bindTexture(2,2);
			SDFGBuf.bindTexture(3,3);
			SDFGBuf.bindTexture(4,4);
			deferred.applyAllSSBOs();
			//Okay. Ready?
			GLContextInitializer.defaultScreen.render(); //Oh yeah
			
			glFinish();
			glfwSwapBuffers(GLContextInitializer.win);
			GLContextInitializer.timeCalcEnd();
			if(cc%100==0) {
				System.out.println(GLContextInitializer.fr);
			}
			cc++;
		}
		GLContextInitializer.destroyGLContext();
	}
	public static void main(String[] args) throws Exception {
		(new SDFTest()).Main();
	}
}
