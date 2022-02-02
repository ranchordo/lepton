package lepton.tests;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

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
import lepton.engine.rendering.TextureImage;
import lepton.engine.rendering.instanced.InstanceAccumulator;
import lepton.engine.rendering.instanced.InstancedRenderer;
import lepton.engine.rendering.instanced.InstancedRenderer.InstancedRenderRoutine;
import lepton.engine.rendering.lighting.BloomHandler;
import lepton.engine.rendering.lighting.Light;
import lepton.engine.rendering.lighting.Lighting;
import lepton.engine.util.Deletable;
import lepton.engine.util.Generic3DObject;
import lepton.optim.objpoollib.DefaultVecmathPools;
import lepton.optim.objpoollib.PoolElement;
import lepton.optim.objpoollib.PoolStrainer;
import lepton.tests.engineTest.EngineTestCube;
import lepton.tests.engineTest.EngineTestFloor;
import lepton.util.CleanupTasks;
import lepton.util.ImageUtil;
import lepton.util.InputHandler;
import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.LogHandler;
import lepton.util.advancedLogger.LogLevel;
import lepton.util.advancedLogger.Logger;
import lepton.util.advancedLogger.defaultHandlers.ConsoleWindowHandler;
import lepton.util.advancedLogger.defaultHandlers.FileHandler;
import lepton.util.console.ConsoleWindow;

public class FancyGraphicsTest {
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
	public static Fonts fonts=new Fonts();
	private static FileHandler fh;
	private static void errorLog() {
		fh=new FileHandler(LeptonUtil.getExternalPath()+"/EngineTest");
		Logger.simulateLocalLog(fh);
	}
	private static Transform mmc=new Transform();
	private static float[] mma=new float[16];
	private static FloatBuffer fm=BufferUtils.createFloatBuffer(16);
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
		Shader screen_basic=new Shader("screen_basic");
		Shader finalComposite=new Shader("/fancyTest/finalComposite");
		Shader screen_basic_bloom=new Shader("screen_basic_bloom");
		Shader deferred=new Shader("/fancyTest/deferred");
		Shader SSR=new Shader("/fancyTest/SSR");
		FrameBuffer fbo=new FrameBuffer(16,5,GL_RGBA16F);
		FrameBuffer deferredout=new FrameBuffer(16,2,GL_RGBA16F);
		FrameBuffer noms=new FrameBuffer(0,4,GL_RGBA16F);
		FrameBuffer blur=new FrameBuffer(0,1,GL_RGBA16F);
		float exposure=9.0f;
		float gamma=1.0f;
		float bloom_thshld=1.6f;
		int bloom_iterations=10;
		
		InputHandler h=new InputHandler(GLContextInitializer.win);
		
		physics.EXPOSE_COLLISION_DATA=true;
		InstanceAccumulator.mergeSSBOsOnDuplicate=InstanceAccumulator.NO_MERGE;
		
		Audio.init();
		
		float amb=0.3f;
		float pos=60f;
		Matrix4f t=new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,-0.1f)),new Vector3f(0,-6,20),1);
		t.invert();
		GLContextInitializer.cameraTransform=new Transform(t);
		Lighting.addLight(new Light(Light.LIGHT_AMBIENT,0,0,0, amb,amb,amb,1));
		Lighting.addLight(new Light(Light.LIGHT_POSITION,0,2,7, pos,pos,pos,1));
		
		fonts.add("consolas","consolas integrated",".png",6,18,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};':,./<>?");
		
		GLContextInitializer.defaultMainShader=new Shader("main_engineTest");
		
		glfwSwapInterval(0);
		Logger.log(0,"Initialization done. Ready for main loop.");
		
		EngineTestFloor floor=new EngineTestFloor(new Vector3f(0,-10,0),LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,(float)Math.toRadians(-90))));
		floor.initGeo();
		floor.geo.g.setRenderingShader(GLContextInitializer.shaderLoader.load("/fancyTest/gbuf"));
		floor.geo.g.additionalUniformRoutine=(s)->{s.setUniform1f("useSSR",2);};
		floor.initPhysics();
		floor.geo.g.zombify();
		
		Generic3DObject cube=new Generic3DObject(new Vector3f(0,-8,14),LeptonUtil.AxisAngle_np(new AxisAngle4f(0,1,0,(float)Math.toRadians(55))),1,GLContextInitializer.shaderLoader.load("/fancyTest/gbuf"),"cube integrated");
		float cubelight=0.7f;
		cube.objSpaceLights.add(new Light(Light.LIGHT_POSITION,0,0, 0.9f, 0,cubelight,cubelight, 1));
		cube.objSpaceLights.add(new Light(Light.LIGHT_POSITION,0,0,-0.9f, 0,cubelight,cubelight, 1));
		cube.objSpaceLights.add(new Light(Light.LIGHT_POSITION,0, 0.9f,0, 0,cubelight,cubelight, 1));
		cube.objSpaceLights.add(new Light(Light.LIGHT_POSITION,0,-0.9f,0, 0,cubelight,cubelight, 1));
		cube.objSpaceLights.add(new Light(Light.LIGHT_POSITION, 0.9f,0,0, 0,cubelight,cubelight, 1));
		cube.objSpaceLights.add(new Light(Light.LIGHT_POSITION,-0.9f,0,0, 0,cubelight,cubelight, 1));
		cube.initGeo();
		cube.geo.g.additionalUniformRoutine=(s)->{s.setUniform1f("useSSR",0);};
		//cube.geo.g.useLighting=false;
		cube.geo.g.useNegativeLightingValue=true;
		cube.geo.g.useTex=true;
		cube.geo.g.zombify();
		
		TextureImage colLUT=new TextureImage(2);
		try {
			colLUT.create(ImageUtil.getImage_handleNotFound(LeptonUtil.getOptionallyIntegratedStream("lut integrated",".png")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Logger.log(4,e.toString(),e);
		}
		while(!glfwWindowShouldClose(GLContextInitializer.win)) {
			GLContextInitializer.timeCalcStart();
			physics.step();
			PoolStrainer.clean();
			glfwPollEvents();
			if(glfwGetKey(GLContextInitializer.win,GLFW_KEY_ESCAPE)==1) {
				glfwSetWindowShouldClose(GLContextInitializer.win,true);
			}
			fbo.bind();
			glClearColor(0,0,0,1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
			
			//Main render
			cube.render();
			floor.render();
			
			fbo.bindTexture(0,0);
			fbo.bindTexture(1,1);
			fbo.bindTexture(2,2);
			fbo.bindTexture(3,3);
			fbo.bindTexture(4,4);
			
			deferred.bind();
			deferred.setSamplersDefault(5);
			mmc.set(GLContextInitializer.cameraTransform);
			mmc.getOpenGLMatrix(mma);
			fm=LeptonUtil.asFloatBuffer(mma,fm);
			GLContextInitializer.activeShader.setUniformMatrix4fv("world2view",fm);
			deferredout.bind();
			deferred.setUniform1f("bloom_thshld",bloom_thshld);
			deferred.setUniform2f("windowpixels",GLContextInitializer.winW,GLContextInitializer.winH);
			deferred.setUniform1f("useFog",2);
			deferred.setUniform4f("fog_color",0,0.15f,0.15f,1);
			deferred.setUniform4f("altLightingValue",2,2,2,1);
			GLContextInitializer.defaultScreen.render();
			
			deferredout.blitTo(noms,0,0);
			fbo.blitTo(noms,3,1);
			fbo.blitTo(noms,4,2);
			fbo.blitTo(noms,1,3);
			deferredout.blitTo(blur,1,0);
			
			BloomHandler.blur(blur,bloom_iterations);
			
			noms.bind();
			SSR.bind();
			SSR.setSamplersDefault(4);
			SSR.setUniform2f("windowpixels",GLContextInitializer.winW,GLContextInitializer.winH);
			noms.bindTexture(0,0);
			noms.bindTexture(1,1);
			noms.bindTexture(2,2);
			noms.bindTexture(3,3);
			GLContextInitializer.activeShader.setUniformMatrix4fv("world2view",fm);
			GLContextInitializer.defaultScreen.render();
			
			noms.bindTexture(0,0);
			blur.bindTexture(0,1);
			finalComposite.bind();
			colLUT.bind();
			finalComposite.setSamplersDefault(3);
			noms.unbind();
			GLContextInitializer.defaultScreen.render();
			
			glfwSwapBuffers(GLContextInitializer.win);
			GLContextInitializer.timeCalcEnd();

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
