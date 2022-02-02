package lepton.engine.rendering.pipelineElements;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL15;

import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.pipelines.RenderPipelineElement;

public class ShowToWindow extends RenderPipelineElement {
	private int ms;
	private int format;
	public ShowToWindow(String name, int ms, int format) {
		super(name);
		this.ms=ms;
		this.format=format;
	}

	@Override
	public void run_back() {
		GL15.glClearColor(1,0,1,1);
		GL15.glClear(GL15.GL_COLOR_BUFFER_BIT|GL15.GL_DEPTH_BUFFER_BIT|GL15.GL_STENCIL_BUFFER_BIT);
		Shader screen_basic=GLContextInitializer.shaderLoader.load("screen_basic");
		screen_basic.bind();
		FrameBuffer.unbind_all();
		GL15.glDisable(GL15.GL_DEPTH_TEST);
		getBuffer().bindTexture(0);
		GLContextInitializer.defaultScreen.render();
		GL15.glEnable(GL15.GL_DEPTH_TEST);
		GLFW.glfwSwapBuffers(GLContextInitializer.win);
	}

	@Override
	public void init_back() {

	}

	@Override
	public String[] compNames_back() {
		return null;
	}

	@Override
	public int MS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int format() {
		// TODO Auto-generated method stub
		return GL15.GL_RGBA8;
	}

	@Override
	public boolean safeElement() {
		// TODO Auto-generated method stub
		return true;
	}
}
