package lepton.tests.engineTest;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import lepton.util.TimeProfiler;
import lepton.engine.audio.Audio;
import lepton.engine.graphics2d.util.Fonts;
import lepton.engine.physics.PhysicsObject;
import lepton.engine.physics.PhysicsWorld;
import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Screen;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.instanced.InstanceAccumulator;
import lepton.engine.rendering.instanced.InstancedRenderer;
import lepton.engine.rendering.instanced.InstancedRenderer.InstancedRenderRoutine;
import lepton.engine.rendering.lighting.BloomHandler;
import lepton.engine.rendering.lighting.Light;
import lepton.engine.rendering.lighting.Lighting;
import lepton.engine.util.Deletable;
import lepton.optim.objpoollib.DefaultVecmathPools;
import lepton.optim.objpoollib.PoolElement;
import lepton.optim.objpoollib.PoolStrainer;
import lepton.util.CleanupTasks;
import lepton.util.InputHandler;
import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.LogHandler;
import lepton.util.advancedLogger.LogLevel;
import lepton.util.advancedLogger.Logger;
import lepton.util.advancedLogger.defaultHandlers.ConsoleWindowHandler;
import lepton.util.advancedLogger.defaultHandlers.FileHandler;
import lepton.util.console.ConsoleWindow;

public class EngineTest {
	
	private static void doCubeMovement(PhysicsObject cube, InputHandler h) {
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
	 * Method given to ConsoleWindow's onCommand for execution on command reception.
	 */
	public static void recieveCommand(String command) {
		Logger.log(0,"Recieved console command: "+command);
		if(command.equals("exit")) {
			System.exit(0);
		}
	}
	public static PhysicsWorld physics=new PhysicsWorld();
	public static TimeProfiler timeProfiler=new TimeProfiler("Physics","Main render","Post-render","SwapBuffers","2D render","Misc");
	public static Fonts fonts=new Fonts();
	public static InstancedRenderer instancedRenderer=new InstancedRenderer("main_3d");
	private static InstancedRenderRoutine renderRoutine=new InstancedRenderRoutine() {
		@Override public void run() {
			cube.render();
		}
	};
	private static EngineTestCube cube;
	private static FileHandler fh;
	private static void errorLog() {
		fh=new FileHandler(LeptonUtil.getExternalPath()+"/EngineTest");
		Logger.simulateLocalLog(fh);
	}
	/**
	 * Execute the main engine test.
	 */
	public static void Main() {
//		Logger.levels[0].keepLocalLog=false; //We don't care about debug
		Logger.localLog=true;
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		CleanupTasks.add(()->GLContextInitializer.cleanAllRemainingGLData());
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext());
		CleanupTasks.add(()->Audio.cleanUp()); //----------IMPORTANT----------//
		
		
		ConsoleWindow mainConsoleWindow=new ConsoleWindow(false,640,480,"Engine test console",(s)->recieveCommand(s),"Engine test console ready.\nStarting engine test.");
		mainConsoleWindow.setVisible(true); //Console windows aren't required. Disable this one by deleting this whole group of lines.
		LogHandler consoleWindowHandler=new ConsoleWindowHandler(mainConsoleWindow);
		Logger.handlers.add(consoleWindowHandler);
		CleanupTasks.add(()->{if(LogLevel.isFatal()) {mainConsoleWindow.waitForClose();}}); //Wait for the console window to close if a fatal error lead to an exit
		CleanupTasks.add(()->mainConsoleWindow.close());
		CleanupTasks.add(()->Logger.handlers.remove(consoleWindowHandler));
		CleanupTasks.add(()->{if(LogLevel.isFatal()) {errorLog();}});
		
		GLContextInitializer.initializeGLContext(true,1280,720,false,"Physics, graphics, sound, computation, and instanced and non-instanced 2d and 3d rendering");
		
		Shader screen=new Shader("screen");
		Shader screen_basic_bloom=new Shader("screen_basic_bloom");
		Screen blindfold=new Screen();
		FrameBuffer fbo=new FrameBuffer(0,3,GL_RGBA16F);
		FrameBuffer interfbo=new FrameBuffer(0,2,GL_RGBA16F);
		FrameBuffer interfbo2=new FrameBuffer(0,2,GL_RGBA16F);
		FrameBuffer interfbo3=new FrameBuffer(0,1,GL_RGBA16F);
		float exposure=9.0f;
		float gamma=1.0f;
		float bloom_thshld=0.8f;
		int bloom_iterations=10;
		
		InputHandler h=new InputHandler(GLContextInitializer.win);
		
		physics.EXPOSE_COLLISION_DATA=true;
		InstanceAccumulator.mergeSSBOsOnDuplicate=InstanceAccumulator.NO_MERGE;
		
		Audio.init();
		
		EngineTestFloor floor=new EngineTestFloor(new Vector3f(0,-10,0),LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,(float)Math.toRadians(-90))));
		cube=new EngineTestCube(new Vector3f(0,0,0),LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,0)));
		cube.initSoundtrack();
		cube.initGeo();
		cube.initPhysics();
		floor.initGeo();
		floor.initPhysics();
		floor.geo.g.zombify();
		cube.geo.g.zombify();
		
		float amb=0.02f;
		Matrix4f t=new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,-0.1f)),new Vector3f(0,-6,20),1);
		t.invert();
		GLContextInitializer.cameraTransform=new Transform(t);
		Lighting.addLight(new Light(Light.LIGHT_AMBIENT,0,0,0, amb,amb,amb,1));
		cube.geo.p.addToSimulation(PhysicsWorld.EVERYTHING,PhysicsWorld.EVERYTHING,physics);
		floor.geo.p.addToSimulation(PhysicsWorld.EVERYTHING,PhysicsWorld.EVERYTHING,physics);
		
		fonts.add("consolas","consolas integrated",".png",6,18,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};':,./<>?");
		
		GLContextInitializer.defaultMainShader=new Shader("main_engineTest");
		
		EngineTestScreen debugScreen=new EngineTestScreen();
		debugScreen.init();
		glfwSwapInterval(0);
		Logger.log(0,"Initialization done. Ready for main loop.");
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			timeProfiler.clear();
			timeProfiler.start(5);
			PoolElement<Vector3f> testpe=DefaultVecmathPools.vector3f.alloc();
			testpe.free();
			GLContextInitializer.timeCalcStart();
			physics.step();
			PoolStrainer.clean();
			glfwPollEvents();
			if(glfwGetKey(GLContextInitializer.win,GLFW_KEY_ESCAPE)==1) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			timeProfiler.stop(5);
			timeProfiler.start(0);
			doCubeMovement(cube.geo.p,h);
			timeProfiler.stop(0);
			timeProfiler.start(1);
			fbo.bind();
			glClearColor(0,0,0,1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
			cube.logic();
 			floor.render();
			instancedRenderer.renderInstanced(renderRoutine);
			cube.postrender();
			glFinish();
			timeProfiler.stop(1);
			timeProfiler.start(4);
			debugScreen.logic();
			debugScreen.render();
			glFinish();
			//System.out.println(GLContextInitializer.fr);
			timeProfiler.stop(4);
			timeProfiler.start(2);
			fbo.unbind();
			
			fbo.blitTo(interfbo,0,0);
			interfbo2.bind();
			glClearColor(0,0,1,1);
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
			glFinish();
			timeProfiler.stop(2);
			timeProfiler.start(3);
			glfwSwapBuffers(GLContextInitializer.win);
			timeProfiler.stop(3);
			timeProfiler.submit();
			GLContextInitializer.timeCalcEnd();
//			System.out.println(EngineTest.timeProfiler.toString());
		}
		CleanupTasks.cleanUp();
	}
	public static void main(String[] args) {
		try {
			Main();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.log(4,e.toString()+", Logger-based stack trace is incorrect.",e);
		}
		Logger.log(1,"Successful usercode exit. Goodbye!");
	}
}
