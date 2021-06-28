package cpshlib.examples;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.util.CleanupTasks;
import lepton.util.advancedLogger.Logger;

public class EX2_BasicFrameBufferSetupExample {
	public void Main() {
		//Old stuff:
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		GLContextInitializer.initializeGLContext(true,500,500,false,"Basic framebuffer setup example");
		
		//New stuff:
		Shader screen_basic=new Shader("screen_basic"); //This will be the shader we use to draw stuff to the window (don't worry about how it works unless you want to).
		Screen screen=new Screen(); //This is an object that deals with the big rectangle we'll stick over the screen.
		FrameBuffer shaderOutput=new FrameBuffer(0); //Param *has* to be zero because cpsh's don't work with multisampling.
		
		//glClearColor is equivalent to background() in processing.
		glClearColor(1,0,1,1); //If you see magenta (rgb 1,0,1 [a.k.a the first 3 params in your glClearColor call]) your framebuffer isn't set up correctly, i.e. it didn't draw over the background.
		glClear(GL_COLOR_BUFFER_BIT); //Apply background
		
		//In this block we basically stick the framebuffer (which is pure black) on a big rectangle filling the screen (with no shading) and render it.
		glDisable(GL_DEPTH_TEST); //Make sure we don't block our blindfold
		shaderOutput.bindTexture(0); //Bind our framebuffer as a texture
		screen_basic.bind(); //Use the screen_basic shader (just samples the texture on a quad)
		FrameBuffer.unbind_all(); //Make sure we're rendering to the actual window
		screen.render(); //Render the blindfold (Comment out this line to make sure for yourself that it is working; you'll just see magenta because the framebuffer isn't drawing)
		glEnable(GL_DEPTH_TEST); //Fix depth testing
		
		glfwSwapBuffers(GLContextInitializer.win); //Not sure what glfwSwapBuffers does, but it makes the screen actually visible.
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Old stuff:
		CleanupTasks.cleanUp();
		
		//If the framebuffer on the big quad doesn't draw correctly, we'll see the background, pure magenta.
		//But if the framebuffer (which is just black to begin with) draws correctly, we'll see a black window.
	}
	
	
	public static void main(String[] args) {
		EX2_BasicFrameBufferSetupExample m=new EX2_BasicFrameBufferSetupExample();
		m.Main();
	}
}
