package com.github.ranchordo.lepton;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.junit.jupiter.api.Test;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.engine.audio.Audio;
import com.github.ranchordo.lepton.engine.physics.PhysicsWorld;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.engine.rendering.Shader;
import com.github.ranchordo.lepton.engine.rendering.instanced.InstancedRenderer;
import com.github.ranchordo.lepton.engine.rendering.instanced.InstancedRenderer.InstancedRenderRoutine;
import com.github.ranchordo.lepton.optim.objpoollib.ObjectPool;
import com.github.ranchordo.lepton.optim.objpoollib.PoolElement;
import com.github.ranchordo.lepton.optim.objpoollib.PoolInitCreator_clone;
import com.github.ranchordo.lepton.tests.engineTest.EngineTestCube;
import com.github.ranchordo.lepton.util.LeptonUtil;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;
import com.github.ranchordo.lepton.util.cloneabletypes.ClFloat;

public class LeptonTests {
	@Test
	public void testBasicGLContextInitialization() {
		Logger.noSystemExit=true;
		GLContextInitializer.initializeGLContext(false,10,10,false,"Lepton GLContextInit test");
		if(GLContextInitializer.win==0 || GLContextInitializer.win==-1) {
			throw new RuntimeException("Failed to initialize a GL Context.");
		}
		GLContextInitializer.destroyGLContext();
		GLContextInitializer.cleanAllRemainingGLData();
		Logger.noSystemExit=false;
	}
	@Test
	public void testShaderInclusionAndCompilation() {
		Logger.noSystemExit=true;
		GLContextInitializer.initializeGLContext(false,10,10,false,"Lepton ShaderLoader test");
		Shader shader=GLContextInitializer.shaderLoader.load("screen_basic");
		shader.delete();
		GLContextInitializer.destroyGLContext();
		GLContextInitializer.cleanAllRemainingGLData();
		Logger.noSystemExit=false;
	}
	@Test
	public void checkJBulletExistence() {
		Logger.noSystemExit=true;
		PhysicsWorld testWorld=new PhysicsWorld();
		Vector3f vec3=new Vector3f(0x6865,0x6C6C,0x6F21);
		Logger.log(0,testWorld);
		Logger.log(0,vec3);
		Logger.noSystemExit=false;
	}
	@Test
	public void checkPoolLib() {
		Logger.noSystemExit=true;
		ObjectPool<ClFloat> testpool=new ObjectPool<ClFloat>("testpool",new PoolInitCreator_clone<ClFloat>(new ClFloat(0)));
		PoolElement<ClFloat> pe=testpool.alloc();
		pe.o().v=0xFF;
		if(testpool.inUse.size()!=1) {
			throw new RuntimeException("testpool inUse is somehow of length "+testpool.inUse.size()+" (expected: 1)");
		}
		pe.free();
		if(testpool.inUse.size()!=0) {
			throw new RuntimeException("testpool inUse is somehow of length "+testpool.inUse.size()+" (expected: 0)");
		}
		testpool.delete();
		Logger.noSystemExit=false;
	}
	@Test
	public void test3DObjectAndAudioInit() {
		Logger.noSystemExit=true;
		Audio.init();
		GLContextInitializer.initializeGLContext(false,10,10,false,"Lepton GObject test");
		PhysicsWorld physics=new PhysicsWorld();
		physics.EXPOSE_COLLISION_DATA=true;
		EngineTestCube cube=new EngineTestCube(new Vector3f(0,0,0),LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,0)));
		cube.initSoundtrack();
		cube.initGeo();
		cube.initPhysics();
		cube.geo.g.zombify();
		cube.geo.p.addToSimulation(PhysicsWorld.EVERYTHING,PhysicsWorld.EVERYTHING,physics);
		Matrix4f t=new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,0)),new Vector3f(0,0,0),1);
		t.invert();
		GLContextInitializer.cameraTransform=new Transform(t);
		InstancedRenderer instancedRenderer=new InstancedRenderer("main_3d");
		InstancedRenderRoutine renderRoutine=new InstancedRenderRoutine() {
			@Override public void run() {
				cube.render();
			}
		};
		physics.step();
		cube.logic();
		instancedRenderer.renderInstanced(renderRoutine);
		GLContextInitializer.destroyGLContext();
		GLContextInitializer.cleanAllRemainingGLData();
		Audio.cleanUp();
		Logger.noSystemExit=false;
	}
}
