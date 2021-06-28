package cpshlib.examples;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;

import lepton.cpshlib.ComputeShader;
import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.util.CleanupTasks;
import lepton.util.advancedLogger.Logger;

public class EX3_ComputeShaderHelloWorld {
	public void Main() {
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		GLContextInitializer.initializeGLContext(true,500,500,false,"Compute shader \"Hello world\"");
		
		//A compute shader is a program that runs on your GPU. It uses memory that exists on your GPU, called VRAM, instead of normal RAM.
		//In order to run a compute shader, you dispatch it across a rectangular prism. If you dispatched a compute shader over (10,100,10), it would run 10,000 times.
		//All of these executions are entirely *parallel*. They run at the same time. Keep that in mind.
		//Inside the code in the compute shader, you have access to a vector called gl_InvocationID. This is a 3d vector that tells you the where the specific shader invocation is located
		//within the dispatch cube. Using the values in this vector, you can have different compute shader invocations do different things, so that they're actually useful.
		//For example, and we'll see this here, in the "solidcolor" compute shader, a compute shader can set the pixel corresponding with its invocation in an image to a specific color,
		//effectively setting the whole image to that color (but with every pixel operation running in parallel).
		
		
		//WARNING: Whenever experimenting with compute shaders, make sure you have all your documents and stuff saved. There is a high likely-hood that you will end up crashing your computer a couple of times.
		//When dispatching a compute shader, make sure that you don't have a single dimension that exceeds about 500,000. If you ask your compute shader(s) to do too much, your computer will slow down and become unresponsive,
		//and then your computer will officially freeze. Past this point, there is no hope that it will become responsive again without rebooting it. Once your cursor has stopped moving for like a minute, you will
		//have to force-restart. All of these examples have been tested and are well below this point, but be careful when thinking "Hey, I'm going to bump this up to 1,000,000 to see how it scales!".
		
		//The solidcolor compute shader will attempt to set every pixel it is dispatched on to (0,1,1), or cyan.
		ComputeShader testComputeShader=new ComputeShader("solidcolor"); //Get the shader source code from the file, compile it, and set it up for execution.
		
		//Old stuff:
		Shader screen_basic=new Shader("screen_basic");
		Screen screen=new Screen();
		FrameBuffer shaderOutput=new FrameBuffer(0);
		//New stuff:
		
		//Here, in this block, we want to populate the shaderOutput framebuffer with interesting stuff with the compute shader.
		shaderOutput.bindImage(0); //Make sure that our solidcolor cpsh can access the framebuffer. This is setting the active image being modified to shaderOutput. Notice that this is bind*Image*, not bind*Texture*.
		testComputeShader.bind(); //Switch to the compute shader
		testComputeShader.dispatch(GLContextInitializer.winW,GLContextInitializer.winH,1); //We dispatch solidcolor. In this case, the cube is winW pixels wide, winH pixels tall, and one pixel deep (because it's a flat image).
		glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT); //Make sure that the shader is done with the image before we access it. This method just waits until the GPU is done.
		
		//Old stuff to display the framebuffer:
		glClearColor(1,0,1,1);
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);
		shaderOutput.bindTexture(0);
		screen_basic.bind();
		FrameBuffer.unbind_all();
		screen.render();
		glEnable(GL_DEPTH_TEST);
		glfwSwapBuffers(GLContextInitializer.win);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		CleanupTasks.cleanUp();
		
		//If you see a cyan screen for a second, it worked!
	}
	
	
	public static void main(String[] args) {
		EX3_ComputeShaderHelloWorld m=new EX3_ComputeShaderHelloWorld();
		m.Main();
	}
}
