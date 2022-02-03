package com.github.ranchordo.lepton.engine.rendering.lighting;

import static org.lwjgl.opengl.GL11.*;

import com.github.ranchordo.lepton.cpshlib.ShaderDataCompatible;
import com.github.ranchordo.lepton.engine.rendering.FrameBuffer;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.Screen;
import com.github.ranchordo.lepton.engine.rendering.Shader;

/**
 * Blurs an image
 */
public class BloomHandler {
	public static Shader blurShader;
	public static FrameBuffer fbo1;
	public static FrameBuffer fbo2;
	/**
	 * When you change screen resolution make sure to call update();
	 */
	public static void update() {
		blurShader=GLContextInitializer.shaderLoader.load("specific/blur");
		fbo2=new FrameBuffer(0);
	}
	public static void init() {
		update();
	}
	/**
	 * Blurs an image
	 */
	public static FrameBuffer blur(FrameBuffer in, int amount) {
		boolean horizontal=true;
		fbo1=in;
		ShaderDataCompatible prevShader=GLContextInitializer.activeShader;
		blurShader.bind();
		for(int i=0;i<amount*2;i++) {
			blurShader.setUniform1i("horizontal",horizontal?1:0);
			(horizontal?fbo2:fbo1).bind();
			(horizontal?fbo1:fbo2).bindTexture(0);
			glClearColor(1,0,1,1);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			GLContextInitializer.defaultScreen.render();
			glEnable(GL_DEPTH_TEST);
			horizontal=!horizontal;
		}
		fbo1.unbind();
		prevShader.bind();
		return fbo1;
	}
}
