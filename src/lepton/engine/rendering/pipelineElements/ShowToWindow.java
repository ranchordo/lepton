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
	public ShowToWindow(String name, byte status, int ms, int format) {
		super(name, status);
		this.ms=ms;
		this.format=format;
	}

	@Override
	public String[] inputNames_back() {
		return null;
	}

	@Override
	public String[] outputNames_back() {
		return null;
	}

	@Override
	public int inputMS() {
		return ms;
	}

	@Override
	public int outputMS() {
		return 0;
	}

	@Override
	public int inputFormat() {
		return format;
	}

	@Override
	public int outputFormat() {
		return GL15.GL_RGBA8;
	}

	@Override
	public void run_back() {
		GL15.glClearColor(1,0,1,1);
		GL15.glClear(GL15.GL_COLOR_BUFFER_BIT|GL15.GL_DEPTH_BUFFER_BIT|GL15.GL_STENCIL_BUFFER_BIT);
		Shader screen_basic=GLContextInitializer.shaderLoader.load("screen_basic");
		screen_basic.bind();
		FrameBuffer.unbind_all();
		GL15.glDisable(GL15.GL_DEPTH_TEST);
		getInputs().bindTexture(0);
		GLContextInitializer.defaultScreen.render();
		GL15.glEnable(GL15.GL_DEPTH_TEST);
		GLFW.glfwSwapBuffers(GLContextInitializer.win);
	}

	@Override
	public void init_back() {
		//No init needed
	}
	@Override public boolean onebuffer() {
		return true;
	}
}
