package com.github.ranchordo.lepton.engine.rendering;

import static org.lwjgl.opengl.GL46.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.vecmath.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.cpshlib.CPSHLoader;
import com.github.ranchordo.lepton.cpshlib.SSBO;
import com.github.ranchordo.lepton.cpshlib.ShaderDataCompatible;
import com.github.ranchordo.lepton.engine.rendering.lighting.BloomHandler;
import com.github.ranchordo.lepton.engine.rendering.lighting.Lighting;
import com.github.ranchordo.lepton.engine.util.Deletable;
import com.github.ranchordo.lepton.util.LeptonUtil;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

/**
 * Serves as a rendering and OpenGL general utility.
 */
public class GLContextInitializer {
	public static int winW;
	public static int winH;
	public static float aspectRatio;
	public static long win=0;
	public static float fov=90;
	public static Matrix4f proj_matrix;
	public static ShaderDataCompatible activeShader;
	public static ShaderLoader shaderLoader=new ShaderLoader();
	public static CPSHLoader cpshLoader=new CPSHLoader();
	public static Screen defaultScreen;
	/**
	 * Default graphical rendering shader.
	 */
	public static Shader defaultMainShader=null;
	public static Transform cameraTransform;
	public static boolean useGraphics=true;
	/**
	 * FrameRate
	 */
	public static float fr=-1;
	/**
	 * Frame Period
	 */
	public static float fp=-1;
	
	private static long starttm=0;
	private static boolean calculatingTiming=false;
	/**
	 * Begin keeping track of frame time.
	 */
	public static void timeCalcStart() {
		if(calculatingTiming) {
			throw new IllegalStateException("TimeCalcStart: Use timeCalcStart and timeCalcEnd correctly or you will be eaten by dinosaurs. (redundant chronological call)");
		}
		starttm=LeptonUtil.micros();
		calculatingTiming=true;
	}
	/**
	 * End the frame time calculation.
	 */
	public static void timeCalcEnd() {
		if(!calculatingTiming) {
			throw new IllegalStateException("TimeCalcEnd: Use timeCalcStart and timeCalcEnd correctly or you will suffer from a gruesome fate. (redundant chronological call)");
		}
		fp=(float)((double)(LeptonUtil.micros()-starttm)/1000000.0);
		fr=1.0f/fp;
		calculatingTiming=false;
	}
	public static String getGPUName() {
		return glGetString(GL_RENDERER);
	}
	private static byte errorCount=0;
	/**
	 * Used to check whether timing has been calculated. Be careful: fp and fr will be -1 for the first frame.
	 */
	public static void checkTimeCalculation() {
		if(fp==-1 || fr==-1) {
			if(errorCount>0) {
				throw new IllegalStateException("Please just calculate timings before stuff happens.");
			}
			errorCount+=1;
		}
	}
	public static void cleanAllRemainingGLData() {
		Deletable.getDRT().deleteAll();
		//Delete misc data
		shaderLoader.shaders.clear();
		Lighting.delete();
	}
	public static void resetGlobalState() {
		cleanAllRemainingGLData();
		
	}
	private static void glPerspective(float fov, float aspect, float n, float f) {
		Matrix4f res=new Matrix4f();
		float tanHalfFovy = (float) Math.tan(Math.toRadians(fov) * 0.5);
        res.m00 = 1.0f / (aspect * tanHalfFovy);
        res.m01 = 0.0f;
        res.m02 = 0.0f;
        res.m03 = 0.0f;
        res.m10 = 0.0f;
        res.m11 = 1.0f / tanHalfFovy;
        res.m12 = 0.0f;
        res.m13 = 0.0f;
        res.m20 = 0.0f;
        res.m21 = 0.0f;
        res.m22 = -(f + n) / (f - n);
        res.m23 = -1.0f;
        res.m30 = 0.0f;
        res.m31 = 0.0f;
        res.m32 = -2.0f * f * n / (f - n);
        res.m33 = 0.0f;
		proj_matrix=res;
	}
	public static void setFOV(float f) {
		glMatrixMode(GL_PROJECTION_MATRIX); 
		glLoadIdentity();
		glPerspective(f,((float)winW)/((float)winH),0.05f,500f);
		fov=f;
		glMatrixMode(GL_MODELVIEW_MATRIX);
	}
	public static void initializeGLContext(boolean showWindow, int w, int h, boolean fullscreen, String windowTitle) {
		if(!glfwInit()) {
			Logger.log(3,"GLFW init error.");
		}
		GLFWVidMode d=glfwGetVideoMode(glfwGetPrimaryMonitor());
		fullscreen&=showWindow; //DO NOT DELETE THIS WHATEVER YOU DO
		if(fullscreen) {
			winW=d.width();
			winH=d.height();
			win=glfwCreateWindow(showWindow?winW:1,showWindow?winH:1,windowTitle,glfwGetPrimaryMonitor(),0);
		} else {
			win=glfwCreateWindow(showWindow?w:1,showWindow?h:1,windowTitle,0,0);
			winW=w;
			winH=h;
		}
		aspectRatio=(float)winW/(float)winH;
		if(win==0) {
			Logger.log(4,"win is 0, maybe there was an init error?");
		}
		if(showWindow) {
			glfwShowWindow(win);
		} else {
			glfwHideWindow(win);
		}
		glfwMakeContextCurrent(win);
		GL.createCapabilities();
		Logger.log(0,"Initializing GL Context on device "+getGPUName()+"...");

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT_AND_BACK, GL_POSITION);
		glEnable(GL_COLOR_MATERIAL);
		glEnable(GL_MULTISAMPLE);

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f); 
		glClearDepth(1.0); 
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL); 

		glEnable(GL_STENCIL_TEST);
		glEnable(GL_CULL_FACE);

		setFOV(90);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glViewport(0,0,winW,winH);
		defaultScreen=new Screen();
	}
	public static void doCursor(long win, boolean grabbed, boolean hidden) {
		if(!grabbed && !hidden) {glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_NORMAL);}
		if(!grabbed && hidden) {glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);}
		if(grabbed && hidden) {glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);}
	}
	public static void destroyGLContext() {
		if(win==0) {
			throw new IllegalStateException("Hey. Don't do that.");
		}
		glfwDestroyWindow(win);
		glfwTerminate();
	}
}
