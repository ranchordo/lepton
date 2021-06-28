package lepton.tests;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.glfw.GLFW.*;

import lepton.cpshlib.ComputeShader;
import lepton.cpshlib.SSBO;
import lepton.cpshlib.ShaderDataCompatible;
import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.util.CleanupTasks;
import lepton.util.advancedLogger.Logger;

public class CPSHLibSSBOTest {
	public void Main() {
		
		//This is probably going to be the most confusing example. Just a warning.
		
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		GLContextInitializer.initializeGLContext(true,500,500,false,"CPSHLIB initialization and GPU driver allocation test");
		
		ComputeShader ballInitializer=new ComputeShader("cpshlibtest/ballInitializer");
		ComputeShader ballProcessorRenderer=new ComputeShader("cpshlibtest/ballRenderer");
		ComputeShader ballPost=new ComputeShader("cpshlibtest/ballPostProcessor");
		
		Shader screen_basic=new Shader("screen_basic");
		Screen screen=new Screen();
		FrameBuffer shaderOutput=new FrameBuffer(0);
		
		//New stuff:
		//An SSBO, which stands for Shader Storage Buffer Object is a reusable hunk of memory that you can allocate, and it is stored GPU-side. You can access this memory from CPU-side code by mapping it into CPU memory.
		//In order to create one of these SSBOs, you can use the generateNewSSBO method. This is specific to a compute shader or graphical shader instance, as the SSBOs are bound to the shaders. This method takes two parameters: the name of the
		//SSBO binding within the shader, and the initial length of the memory hunk, in bytes. Remember: Floats and ints are always 4 bytes, and doubles and longs are always 8 bytes. All you can use are floats here, though.
		//In this shader example, we'll have a bunch of bouncing balls that leave trails on the screen that dissipate and diffuse over time. The position of each ball will be stored in an SSBO, and a compute shader will go through and
		//initialize them once. Another compute shader will then go through, and simultaneously update and render them. From there, another compute shader, running once for every pixel, will handle the dissipation and diffusion of the trails.
		//Each ball contains a single 4d vector. The x and y components will store the position, and the z and w components will store the velocity.
		//The size of each ball in bytes is therefore (1 vec4 per ball)*(4 floats per vec4)*(4 bytes per float)=16 bytes.
		int numBalls=50;
		int sizeOfBall=1*4*4;
		//If a frameshift occurrs in your SSBO, weird things can happen. By a frameshift I mean if a structural bug does not write a specific byte when it should or when it writes an additional byte, something like this:
		//Position: (A,B,C), Rotation (quat): (D,E,F,G), Scale: (H).
		//Position: (A,B,D), Rotation (quat): (E,F,G,H), Scale: ([last frame's H]).
		//The byte represented by "C" did not get written, causing components of various things to spill other places.
		//Bytes do not end up in the required place anymore and are instead shifted. This can be persistent if you are using SSBOs persistently (which is how you *should* be using them):
		//For example of how weird this can be visually, take a look at this: https://youtu.be/63gigCNJdZs?t=116
		//Make sure that your sizing lines up so that frameshifts don't happen, barring complete memory or paging corruption.
		
		SSBO balls=ballInitializer.generateNewSSBO("balls_buffer",numBalls*sizeOfBall);
		ballProcessorRenderer.generateFromExistingSSBO("balls_buffer",balls); //Creates a linked SSBO that uses the same data, but on a different compute shader
		ballPost.generateFromExistingSSBO("balls_buffer",balls); //Creates a linked SSBO that uses the same data, but on a different compute shader
		//Here, we will create the SSBO and have it zeroed out, and not change the size or the data in it CPU-side afterwards. It is possible to change the data inside an SSBO whenever you want, and it is possible to change
		//the amount of data in the ssbo as well. However, changing the amount of data requires detaching the old chunk of data, which then has to be garbage collected, which will gunk up the garbage collector and cause problems.
		//Moral of the story: Please avoid changing the length of the buffer as much as possible.
		ShaderDataCompatible.clearSSBOData(balls); //Zero out the data
		
		//We can also use uniforms to quickly set small pieces of (non SSBO) data:
		ballInitializer.bind();
		ballInitializer.setUniform2f("image_size",GLContextInitializer.winW,GLContextInitializer.winH); //Quickly sets a vec2 in the shader as the image size that we initialize balls across.
		//The reason we don't use uniforms for ball storage is that the uniform data is erased once the compute shader switches, and a shader cannot write to a uniform variable.
		ballInitializer.applyAllSSBOs(); //You *NEED* to remember this. If you don't, SSBO's in the shader will appear as either all zeroes, or as a bunch of random data.
		ballInitializer.dispatch(numBalls,1,1); //Initialize the balls! This sets their position and velocity to random values. The shader will fetch the correct ball from the buffer based on its gl_InvocationID.x value.
		glMemoryBarrier(GL_ALL_BARRIER_BITS); //Make sure we're done with initializing the SSBO values before continuing
		
		
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			glfwPollEvents();
			if(glfwGetKey(GLContextInitializer.win,GLFW_KEY_ESCAPE)==1) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			
			shaderOutput.bindImage(0);
			ballProcessorRenderer.bind();
			ballProcessorRenderer.setUniform2f("image_size",GLContextInitializer.winW,GLContextInitializer.winH);
			ballProcessorRenderer.applyAllSSBOs();
			ballProcessorRenderer.dispatch(numBalls,1,1); //Process the balls' physics and draw 'em! Again, the shader will fetch the correct ball from the buffer based on its gl_InvocationID.x value.
			glMemoryBarrier(GL_ALL_BARRIER_BITS); //Make sure we're done with the SSBO before continuing, because the shader writes to the SSBO. It would be very bad if we progressed while it was half-written.
			
			ballPost.bind(); //This shader runs for every pixel on the image.
			ballPost.applyAllSSBOs();
			ballPost.dispatch(GLContextInitializer.winW,GLContextInitializer.winH,1); //Post-process the trail image with a terrible blur algorithm that runs once per frame.
			glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT); //You know what this does, but I'll leave a comment here anyway. DO NOT FORGET THIS. If you forget SHADER_IMAGE_ACCESS_BARRIER_BIT, just use GL_ALL_BARRIER_BITS. Doesn't matter.
			
			//You know what this does:
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
		//Now you might be thinking "Isn't that a lot of work for something I can create in processing in about 2 minutes?". Yes. It is. But here's the thing: Go up and increase numBalls to, like, 100,000 (NOT 1,000,000, be careful). It runs smoothly.
		//If you have 60 balls, processing is better for this sort of thing. But with >1,000, this is going to work a lot better.
		//Again, remember the warning from EX3: Don't go above 500,000 for numBalls. It is possible to simulate more than that many balls, but it requires optimization and other refinements that this example shader is not built for.
	}
	
	
	public static void main(String[] args) {
		CPSHLibSSBOTest m=new CPSHLibSSBOTest();
		m.Main();
	}
}
