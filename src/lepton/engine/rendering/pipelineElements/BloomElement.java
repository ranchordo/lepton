package lepton.engine.rendering.pipelineElements;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.opengl.GL43;

import lepton.cpshlib.ShaderDataCompatible;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.pipelines.RenderPipeline;
import lepton.engine.rendering.pipelines.RenderPipelineElement;

public class BloomElement extends RenderPipelineElement {
	private int amt;
	protected BloomElement(int amount, String name, byte status) {
		super(name, status);
		amt=amount;
	}
	public static Screen screen;
	public static Shader blurShader;
	@Override public void init_back() {
		screen=new Screen();
		blurShader=GLContextInitializer.shaderLoader.load("specific/blur");
	}
	@Override public void run_back() {
		boolean horizontal=true;
		ShaderDataCompatible prevShader=GLContextInitializer.activeShader;
		blurShader.bind();
		for(int i=0;i<amt*2-1;i++) {
			blurShader.setUniform1i("horizontal",horizontal?1:0);
			(horizontal?getOutputs():getInputs()).bind();
			(horizontal?getInputs():getOutputs()).bindTexture(0);
			glClearColor(1,0,1,1);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			screen.render();
			glEnable(GL_DEPTH_TEST);
			horizontal=!horizontal;
		}
		getOutputs().unbind();
		prevShader.bind();
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
		return 0;
	}
	@Override
	public int outputMS() {
		return 0;
	}
	@Override
	public int inputFormat() {
		return GL43.GL_RGBA16F;
	}
	@Override
	public int outputFormat() {
		return GL43.GL_RGBA16F;
	}
}
