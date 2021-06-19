package lepton.tests.engineTest;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.DoubleBuffer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import com.bulletphysics.linearmath.Transform;

import lepton.engine.audio.Audio;
import lepton.engine.physics.Physics;
import lepton.engine.physics.PhysicsObject;
import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.lighting.BloomHandler;
import lepton.engine.rendering.lighting.Light;
import lepton.engine.rendering.lighting.Lighting;
import lepton.optim.objpoollib.PoolStrainer;
import lepton.util.CleanupTasks;
import lepton.util.InputHandler;
import lepton.util.Util;
import lepton.util.advancedLogger.Logger;

public class EngineTest {
	
	private void doCubeMovement(PhysicsObject cube, InputHandler h) {
		float s=5000;
		if(h.i(GLFW_KEY_SPACE)) {
			cube.body.applyCentralForce(new Vector3f(0,10000,0));
		}
		if(h.i(GLFW_KEY_UP)) {
			cube.body.applyCentralForce(new Vector3f(0,0,-s));
		}
		if(h.i(GLFW_KEY_DOWN)) {
			cube.body.applyCentralForce(new Vector3f(0,0,s));
		}
		if(h.i(GLFW_KEY_LEFT)) {
			cube.body.applyCentralForce(new Vector3f(-s,0,0));
		}
		if(h.i(GLFW_KEY_RIGHT)) {
			cube.body.applyCentralForce(new Vector3f(s,0,0));
		}
	}
	/**
	 * Execute the main engine test.
	 */
	public void Main() {
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		CleanupTasks.add(()->Audio.cleanUp()); //----------IMPORTANT----------//
		GLContextInitializer.initializeGLContext(true,500,500,false,"Main Engine test. Includes physics, graphics, and sound.");
		Shader screen=new Shader("screen");
		Shader screen_basic_bloom=new Shader("screen_basic_bloom");
		Screen blindfold=new Screen();
		FrameBuffer fbo=new FrameBuffer(16,3,GL_RGBA16F);
		FrameBuffer interfbo=new FrameBuffer(0,2,GL_RGBA16F);
		FrameBuffer interfbo2=new FrameBuffer(0,2,GL_RGBA8);
		FrameBuffer interfbo3=new FrameBuffer(0,1,GL_RGBA8);
		float exposure=9.0f;
		float gamma=1.0f;
		float bloom_thshld=0.8f;
		int bloom_iterations=10;
		
		InputHandler h=new InputHandler(GLContextInitializer.win);
		
		Physics.initPhysics();
		Physics.EXPOSE_COLLISION_DATA=true;
		
		Audio.init();
		
		EngineTestFloor floor=new EngineTestFloor(new Vector3f(0,-10,0),Util.AxisAngle_np(new AxisAngle4f(1,0,0,(float)Math.toRadians(-90))));
		EngineTestCube cube=new EngineTestCube(new Vector3f(0,0,0),Util.AxisAngle_np(new AxisAngle4f(1,0,0,0)));
		cube.initSoundtrack();
		cube.initGeo();
		cube.initPhysics();
		floor.initGeo();
		floor.initPhysics();
		
		float inte=10f;
		float r=255.0f/255.0f;
		float g=250.0f/255.0f;
		float b=244.0f/255.0f;
		float amb=0.1f;
		GLContextInitializer.cameraTransform=new Transform(new Matrix4f(Util.AxisAngle_np(new AxisAngle4f(1,0,0,-0.1f)),new Vector3f(0,-6,20),1));
		Matrix4f t=GLContextInitializer.cameraTransform.getMatrix(new Matrix4f());
		t.invert();
		GLContextInitializer.cameraTransform.set(t);
		Lighting.addLight(new Light(Light.LIGHT_POSITION, 0,-15,-7, inte*r, inte*g, inte*b, 1));
		Lighting.addLight(new Light(Light.LIGHT_AMBIENT,0,0,0, amb,amb,amb,1));
		cube.geo.p.addToSimulation(Physics.EVERYTHING,Physics.EVERYTHING);
		floor.geo.p.addToSimulation(Physics.EVERYTHING,Physics.EVERYTHING);
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			GLContextInitializer.timeCalcStart();
			Physics.step();
			PoolStrainer.clean();
			glfwPollEvents();
			if(glfwGetKey(GLContextInitializer.win,GLFW_KEY_ESCAPE)==1) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			doCubeMovement(cube.geo.p,h);
			//System.out.println(cube.geo.p.body.getMotionState().getWorldTransform(new Transform()).getMatrix(new Matrix4f()));
			fbo.bind();
			glClearColor(0,0,0,1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
			cube.logic();
			cube.render();
			floor.render();
			
			fbo.unbind();
			
			fbo.blitTo(interfbo,0,0);
			interfbo2.bind();
			glClearColor(1,0,1,1);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			screen_basic_bloom.setUniform1f("bloom_thshld",bloom_thshld);
			screen_basic_bloom.bind();
			interfbo.bindTexture(0);
			blindfold.render();
			glEnable(GL_DEPTH_TEST);
			
			interfbo2.blitTo(interfbo3,1);
			BloomHandler.blur(interfbo3,bloom_iterations);
			FrameBuffer.unbind_all();
			glClearColor(1,1,0,1);
			glClear(GL_COLOR_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			screen.bind();
			screen.setUniform1f("exposure",exposure);
			screen.setUniform1f("gamma",gamma);
			interfbo2.bindTexture(0,0);
			interfbo3.bindTexture(0,1);
			blindfold.render();
			glEnable(GL_DEPTH_TEST);
			
			glfwSwapBuffers(GLContextInitializer.win);
			GLContextInitializer.timeCalcEnd();
		}
		
		CleanupTasks.cleanUp();
	}
	public static void main(String[] args) {
		EngineTest m=new EngineTest();
		m.Main();
	}
}
