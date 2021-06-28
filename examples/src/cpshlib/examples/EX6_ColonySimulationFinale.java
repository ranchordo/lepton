package cpshlib.examples;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL42.*;

import lepton.cpshlib.ComputeShader;
import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.cpshlib.SSBO;
import lepton.cpshlib.ShaderDataCompatible;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.util.CleanupTasks;
import lepton.util.InputHandler;
import lepton.util.advancedLogger.Logger;

//Sebastian Lague-style colony simulation
public class EX6_ColonySimulationFinale {
	private void clearFrameBuffer(FrameBuffer fbo) {
		fbo.bind();
		glClearColor(0,0,0,1);
		glClear(GL_COLOR_BUFFER_BIT);
		fbo.unbind();
	}
	public void Main() {
		
		//This is just for fun. This isn't going to be super well documented; I want to you to figure it out
		
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		GLContextInitializer.initializeGLContext(true,1920,1080,true,"Realtime compute shader modification example");
		
		ComputeShader agentInitializer=new ComputeShader("example6/agentInitializer"); //Initializer
		ComputeShader agentProcessorRenderer=new ComputeShader("example6/agentRenderer"); //Processor and renderer
		ComputeShader agentPost=new ComputeShader("example6/agentPostProcessor"); //For processing the trail image on each frame
		ComputeShader screenRender=new ComputeShader("example6/agentDuplicateRender"); //For *just drawing* the agents to the screen, using colors and stuff
		ComputeShader screenPost=new ComputeShader("example6/renderedPostProcessor"); //Post processor for the special screen image
		
		Shader screen_basic=new Shader("screen_basic");
		Screen screen=new Screen();
		FrameBuffer trails=new FrameBuffer(0); //For storing the trails (You don't see this image)
		FrameBuffer shaderOutput=new FrameBuffer(0); //For showing the trajectories
		
		int numAgents=10000;
		int sizeOfAgent=1*4*4;
		SSBO agents=agentInitializer.generateNewSSBO("balls_buffer",numAgents*sizeOfAgent);
		agentProcessorRenderer.generateFromExistingSSBO("balls_buffer",agents);
		agentPost.generateFromExistingSSBO("balls_buffer",agents);
		screenRender.generateFromExistingSSBO("balls_buffer",agents);
		screenPost.generateFromExistingSSBO("balls_buffer",agents);
		ShaderDataCompatible.clearSSBOData(agents);
		
		agentInitializer.bind();
		agentInitializer.setUniform2f("image_size",GLContextInitializer.winW,GLContextInitializer.winH);
		agentInitializer.applyAllSSBOs();
		agentInitializer.dispatch(numAgents,1,1);
		glMemoryBarrier(GL_ALL_BARRIER_BITS);
		
		//Some game engine stuff:
		InputHandler h=new InputHandler(GLContextInitializer.win);
		
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			glfwPollEvents();
			if(glfwGetKey(GLContextInitializer.win,GLFW_KEY_ESCAPE)==1) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			
			trails.bindImage(0);
			agentProcessorRenderer.bind();
			agentProcessorRenderer.setUniform2f("image_size",GLContextInitializer.winW,GLContextInitializer.winH);
			if(h.i(GLFW_KEY_SPACE)) {
				agentProcessorRenderer.setUniform1f("turnAngleMultiplier",0);
			} else {
				agentProcessorRenderer.setUniform1f("turnAngleMultiplier",1);
			}
			if(h.ir(GLFW_KEY_C)) {
				clearFrameBuffer(trails);
				//clearFrameBuffer(shaderOutput);
			}
			agentProcessorRenderer.applyAllSSBOs();
			agentProcessorRenderer.dispatch(numAgents,1,1);
			glMemoryBarrier(GL_ALL_BARRIER_BITS);
			
			
			//THIS IS IMPORTANT: It shows you how to have a compute shader access multiple images. (Multiple SSBOs are pretty easy; just bind multiple SSBOs to the shader).
			shaderOutput.bindImage(0); //Bind this normally (To attachment point 0, which is default [That's not the 0 in the function]. They are called *units* in opengl-speak)
			screenRender.bind();
			screenRender.setUniform1i("img_output",0); //We treat our normal image like an integer, and set it to 0, which is the unit we want to access thru this shader var
			trails.bindImage(0,GL_READ_WRITE,1); //Remember this syntax: This is how to bind an image on unit 1.
			screenRender.setUniform1i("trails",1); //Set separate glsl image sampler "trails" to a different unit
			screenRender.setUniform2f("image_size",GLContextInitializer.winW,GLContextInitializer.winH);
			screenRender.applyAllSSBOs();
			screenRender.dispatch(numAgents,1,1);
			glMemoryBarrier(GL_ALL_BARRIER_BITS);
			
			trails.bindImage(0); //Normal shader image unit 0 binding
			agentPost.bind();
			agentPost.applyAllSSBOs();
			agentPost.dispatch(GLContextInitializer.winW,GLContextInitializer.winH,1);
			glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
			
			shaderOutput.bindImage(0); //For post-processing the rendered image
			screenPost.bind();
			screenPost.applyAllSSBOs();
			screenPost.dispatch(GLContextInitializer.winW,GLContextInitializer.winH,1);
			glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
			
			glClearColor(1,0,1,1);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			shaderOutput.bindTexture(0);
			screen_basic.bind();
			FrameBuffer.unbind_all();
			screen.render();
			glEnable(GL_DEPTH_TEST);
			glfwSwapBuffers(GLContextInitializer.win);
		}
		
		CleanupTasks.cleanUp();
		
	}
	
	
	public static void main(String[] args) {
		EX6_ColonySimulationFinale m=new EX6_ColonySimulationFinale();
		m.Main();
	}
}
