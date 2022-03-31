package com.github.ranchordo.lepton.tests;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.engine.rendering.FrameBuffer;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.Shader;
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
		
		
		Shader SDFDepthRenderer=new Shader("SDFTest/SDFDepthRenderer");
		Shader SDFRenderer=new Shader("SDFTest/SDFRenderer");
		Shader SDFPostProcessor=new Shader("SDFTest/SDFPostProcessor");
		
		FrameBuffer SDFDepth=new FrameBuffer(0,1,GL_RGB16F);
		FrameBuffer SDFGBuf=new FrameBuffer(0,5,GL_RGB16F);
		
		InputHandler inputHandler=new InputHandler(GLContextInitializer.win);
		Matrix4f t=new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,-0.1f)),new Vector3f(0,-6,20),1);
		t.invert();
		GLContextInitializer.cameraTransform=new Transform(t);
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			glfwPollEvents();
			if(inputHandler.i(GLFW_KEY_ESCAPE)) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			glClearColor(0,0,0,1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			SDFGBuf.bind();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			SDFDepth.bind();
			SDFDepthRenderer.bind();
			GLContextInitializer.defaultScreen.render();
			FrameBuffer.unbind_all();
			SDFRenderer.bind();
			SDFRenderer.setUniform2f("dimensions", GLContextInitializer.winW, GLContextInitializer.winH);
			SDFDepth.bindTexture(0);
			SDFGBuf.bind();
			GLContextInitializer.defaultScreen.render();
			FrameBuffer.unbind_all();
			SDFPostProcessor.bind();
			SDFPostProcessor.setUniform2f("dimensions", GLContextInitializer.winW, GLContextInitializer.winH);
			SDFPostProcessor.setSamplersDefault(5);
			SDFGBuf.bindTexture(0,0);
			SDFGBuf.bindTexture(1,1);
			SDFGBuf.bindTexture(2,2);
			SDFGBuf.bindTexture(3,3);
			SDFGBuf.bindTexture(4,4);
			GLContextInitializer.defaultScreen.render();
			glfwSwapBuffers(GLContextInitializer.win);
		}
		GLContextInitializer.destroyGLContext();
	}
	public static void main(String[] args) throws Exception {
		(new SDFTest()).Main();
	}
}
