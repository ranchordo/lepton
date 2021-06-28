package cpshlib.examples;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
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

public class EX4_RealtimeComputeShaderModificationExample {
	public void Main() {
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		GLContextInitializer.initializeGLContext(true,500,500,false,"Realtime compute shader modification example");
		
		//dynamicaddition will sample the image stored in shaderOutput to get a vector describing the color. It will then set the pixel in question to (r+(0.01*radius),g+(0.007*radius),b+(0.013*radius), 1.0) so that the color will evolve over time,
		//Where radius=sqrt(x^2+y^2) and x and y are the current pixel's coordinates from 0 to 1 across the image, and r,g,b are the pixel values it fetched.
		//Remember, the framebuffer starts with all pixels at (0,0,0,0). Hence why we set the alpha to 1: otherwise it would just be transparent.
		ComputeShader testComputeShader=new ComputeShader("dynamicaddition");
		
		Shader screen_basic=new Shader("screen_basic");
		Screen screen=new Screen();
		FrameBuffer shaderOutput=new FrameBuffer(0);

		//We just stick stuff in a loop:
		while(!glfwWindowShouldClose(GLContextInitializer.win)) { //glfwWindowShouldClose is exactly what it sounds like.
			glfwPollEvents(); //This makes the host OS realize that the window is still "responding".
			if(glfwGetKey(GLContextInitializer.win,GLFW_KEY_ESCAPE)==1) { //Check if we're pressing Esc
				glfwSetWindowShouldClose(GLContextInitializer.win,true); //Send the flag to destroy the program
			}
			
			shaderOutput.bindImage(0); //Make our fbo (FrameBuffer Object) accessible to the compute shader
			testComputeShader.bind(); //Use our compute shader
			testComputeShader.dispatch(GLContextInitializer.winW,GLContextInitializer.winH,1); //Dispatch it!
			glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT); //Wait until done
			
			//Now we just display the framebuffer {
			glClearColor(1,0,1,1);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			shaderOutput.bindTexture(0);
			screen_basic.bind();
			FrameBuffer.unbind_all();
			screen.render();
			glEnable(GL_DEPTH_TEST);
			//}
			glfwSwapBuffers(GLContextInitializer.win); //This has to be inside the loop to update the window.
		}
		
		CleanupTasks.cleanUp();
		
		//You should see an evolving circular pattern centering on 0,0, the lower left corner of the window.
	}
	
	
	public static void main(String[] args) {
		EX4_RealtimeComputeShaderModificationExample m=new EX4_RealtimeComputeShaderModificationExample();
		m.Main();
	}
}
