package lepton.engine.physics;

import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.dispatch.CollisionWorld.RayResultCallback;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;

import lepton.engine.rendering.GLContextInitializer;
import lepton.util.advancedLogger.Logger;

/**
 * Oh gosh. This is a complicated class.
 */
public class Physics {
	/**
	 * Collision filter mask/group for NOTHING. 0. Lookup "JBullet collision filtering" to figure out how to use these.
	 */
	public static final short NOTHING   =0b000000000000000;
	/**
	 * Collision filter mask/group for EVERYTHING. ~0 (~ is bitwise not, so 0b11111...). Lookup "JBullet collision filtering" to figure out how to use these.
	 */
	public static final short EVERYTHING=0b111111111111111;
	/**
	 * JBullet thing.
	 */
	public static DiscreteDynamicsWorld dynamicsWorld;
	/**
	 * Whether or not to keep track of collision data accessibly thru the collisions and collisionVels lists in RigidBodyEntry's.
	 */
	public static boolean EXPOSE_COLLISION_DATA=false;
	private static HashSet<RigidBodyEntry> bodies=new HashSet<RigidBodyEntry>();
	private static ArrayList<LocalRayResult> rayTest=new ArrayList<LocalRayResult>();
	private static Vector3f gravity=new Vector3f(0,-10,0);
	//public static final float gravity_magnitude=10;
	/**
	 * For raycasting with JBullet. results are stored in an internal arraylist that can be fetched with getRayTestResults. Remember to use clearRayTestResults first.
	 */
	public static final RayResultCallback rayResultCallback=new RayResultCallback() {
		@Override
		public float addSingleResult(LocalRayResult arg0, boolean arg1) {
			Physics.rayTest.add(arg0);
			return 0;
		}
		
	};
	public ArrayList<LocalRayResult> getRayTestResults() {
		return rayTest;
	}
	public void clearRayTestResults() {
		rayTest.clear();
	}
	public static HashSet<RigidBodyEntry> getBodies() {
		return bodies;
	}
	public static void initPhysics() {
		BroadphaseInterface broadphase=new DbvtBroadphase();
		CollisionConfiguration collisionConfiguration=new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher=new CollisionDispatcher(collisionConfiguration);
		ConstraintSolver solver=new SequentialImpulseConstraintSolver();
		dynamicsWorld=new DiscreteDynamicsWorld(dispatcher,broadphase,solver,collisionConfiguration);
		dynamicsWorld.setGravity(gravity);
	}
	private static Vector3f linforce=new Vector3f();
	public static PhysicsStepModifier activePhysicsStepModifier=null;
	private static Vector3f linvel1=new Vector3f();
	private static Vector3f linvel2=new Vector3f();
	private static Vector3f posDifference=new Vector3f();
	private static Transform temp=new Transform();
	public static void step() {
		if(EXPOSE_COLLISION_DATA) {
			for(RigidBodyEntry be : bodies) {be.initializeCollisionListsPerFrame();}
		}
		if(GLContextInitializer.fr<0) {return;}
		if(activePhysicsStepModifier!=null) {
			activePhysicsStepModifier.preStepProcess(bodies);
		}
		for(RigidBodyEntry be : bodies) { //Gravity compensation
			linforce.set(be.constAcceleration);
			linforce.scale(be.b.getInvMass());
			be.b.applyCentralForce(linforce);
		}
		dynamicsWorld.stepSimulation(1.0f/GLContextInitializer.fr);
		if(EXPOSE_COLLISION_DATA) {
			int manifolds=dynamicsWorld.getDispatcher().getNumManifolds();
			for(int i=0;i<manifolds;i++) {
				PersistentManifold m=dynamicsWorld.getDispatcher().getManifoldByIndexInternal(i);
				boolean hit=false;
				for(int j=0;j<m.getNumContacts();j++) {
					if(m.getContactPoint(j).getDistance()<0.0f) {
						hit=true;
						break;
					}
				}
				if(!hit) {continue;}
				RigidBody rb1=(RigidBody)m.getBody0();
				RigidBody rb2=(RigidBody)m.getBody1();
				rb1.getLinearVelocity(linvel1);
				rb2.getLinearVelocity(linvel2);
				linvel1.sub(linvel2);
				posDifference.set(rb1.getMotionState().getWorldTransform(temp).origin);
				posDifference.sub(rb2.getMotionState().getWorldTransform(temp).origin);
				posDifference.normalize();
				RigidBodyEntry rbe1=getEntryFromRigidBody(rb1);
				RigidBodyEntry rbe2=getEntryFromRigidBody(rb2);
				//System.out.println(rbe1.typeDBG);
				//System.out.println(rbe2.typeDBG);
				//System.out.println();
				float collisionVel=linvel1.dot(posDifference);
				rbe1.collisions.add(rbe2);
				rbe2.collisions.add(rbe1);
				rbe1.collisionVels.add(collisionVel);
				rbe2.collisionVels.add(collisionVel);
			}
		}
		if(activePhysicsStepModifier!=null) {
			activePhysicsStepModifier.postStepProcess(bodies);
		}
	}
	public static void add(RigidBody b, short group) {
		dynamicsWorld.addRigidBody(b,EVERYTHING,EVERYTHING);
		bodies.add(new RigidBodyEntry(b,EVERYTHING,EVERYTHING));
	}
	public static void add(RigidBody b, short group, short mask) {
		dynamicsWorld.addRigidBody(b,group,mask);
		bodies.add(new RigidBodyEntry(b,group,mask));
	}
	public static void remove(RigidBody b) {
		if(b==null) {
			Logger.log(2,"Removing null body.");
		}
		try {
			dynamicsWorld.removeRigidBody(b);
		} catch (NullPointerException e) {
			Logger.log(4, "Concave-static to concave-static collision, NullPointerException on internal call freeCollisionAlgorithm.", e);
		}
		for(RigidBodyEntry be : bodies) {
			if(be.b==b) {
				bodies.remove(be);
				break;
			}
		}
	}
	public static RigidBodyEntry getEntryFromRigidBody(RigidBody b) {
		return ((UserPointerStructure)b.getUserPointer()).ParentRBEntryPointer;
	}
	public static void reAdd(RigidBody b, short group, short mask) {
		dynamicsWorld.removeRigidBody(b);
		dynamicsWorld.addRigidBody(b,group,mask);
		RigidBodyEntry entry=getEntryFromRigidBody(b);
		if(entry!=null) {
			entry.group=group;
			entry.mask=mask;
		}
	}
	public static void reAdd(RigidBodyEntry b, short group, short mask) {
		dynamicsWorld.removeRigidBody(b.b);
		dynamicsWorld.addRigidBody(b.b,group,mask);
		b.group=group;
		b.mask=mask;
	}
	/**
	 * Disable rigidbody deactivation. Useful to jumpstart a "stuck" simulation.
	 */
	public static void wake() {
		for(RigidBodyEntry be : bodies) {
			be.b.setActivationState(CollisionObject.ACTIVE_TAG);
		}
	}
	private static Vector3f gravityClone=new Vector3f();
	public static Vector3f getGravity() {
		gravityClone.set(gravity);
		return gravityClone;
	}
	public static void setGravity(Vector3f gravity) {
		Physics.gravity=gravity;
		dynamicsWorld.setGravity(gravity);
		for(RigidBodyEntry be : bodies) {
			be.refreshGravity();
		}
	}
}
