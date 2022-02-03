package com.github.ranchordo.lepton.engine.physics;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.engine.animation.Animator;
import com.github.ranchordo.lepton.engine.rendering.GObject;
import com.github.ranchordo.lepton.engine.rendering.VertexMap;
import com.github.ranchordo.lepton.engine.util.OBJReturnObject;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class PhysicsObject {
	public static final int PHYSICS=0;
	public static final int ANIMATION=1;
	public static final int NONE=2;
	public static final int OTHER=3;
	public static final int VARIABLE=4;
	
	public RigidBody body;
	public Animator animator=new Animator();
	public PhysicsObject transformObject;
	public Transform transformVariable;
	
	private Vector3f inertia=new Vector3f(0,0,0);
	/**
	 * Set mass to 0 for a static object. I don't think negative masses do anything other than break... everything.
	 */
	public float mass=1;
	
	private boolean rnp=false;
	public void restrictNonPhysics() {rnp=true;}
	private int transformSource=PHYSICS;
	/**
	 * The transform source is where the physics object gets its object to world transform matrix from. Options are PHYSICS, ANIMATION, NONE, OTHER, and VARIABLE.
	 * OTHER uses the transformObject and just clones its transform. Useful for graphically attaching objects to other objects.
	 * VARIABLE uses transformVariable directly. Make sure to sync its pointer so that it can be mutable! No repeated setting required!
	 */
	public void setMotionSource(int motionSource) {
		if(!rnp) {
			this.transformSource=motionSource;
		}
	}
	public int getTransformSource() {return transformSource;}
	private transient Transform ret;
	public Transform getTransform() {
		if(ret==null) {ret=new Transform();}
		switch(transformSource) {
		case PHYSICS:
			if(body==null) {
				Logger.log(3,"Body is null");
				return null;
			}
			body.getWorldTransform(ret);
			return ret;
		case ANIMATION:
			return animator.getFrame();
		case NONE:
			return null;
		case OTHER:
			return transformObject.getTransform();
		case VARIABLE:
			ret.set(transformVariable);
			return ret;
		default:
			return null;
		}
	}
	/**
	 * Create rigidbody from RigidBodyConstructionInfo. Create one through initPhysics_mesh or initPhysics_shape.
	 */
	public void doBody(RigidBodyConstructionInfo bodyConstructionInfo) {
		this.body=new RigidBody(bodyConstructionInfo);
	}
	public void addToSimulation(short group, PhysicsWorld pw) {
		pw.add(body,group);
	}
	public void addToSimulation(short group, short mask, PhysicsWorld pw) {
		pw.add(body,group,mask);
	}
	public void removePhysics(PhysicsWorld pw) {
		pw.remove(body);
	}
	
	/**
	 * Construct a RigidBodyConstructionInfo from a GObject's vertexData.
	 */
	public RigidBodyConstructionInfo initPhysics_mesh(GObject vertdata, Vector3f pos, Quat4f rot) {
		CollisionShape shape=null;
		if(vertdata.fromOBJ) {
			OBJReturnObject o=new OBJReturnObject();
			o.fromVMap(vertdata.vmap, vertdata.getTris());
			TriangleIndexVertexArray mesh=new TriangleIndexVertexArray(o.numTriangles,o.indices,3*4,vertdata.vmap.vertices.size(),o.vertices,3*4);
			shape=new BvhTriangleMeshShape(mesh,true);
			MotionState motionState=new DefaultMotionState(new Transform(new Matrix4f(
					rot,
					pos,1.0f)));
			if(mass!=0) {
				shape.calculateLocalInertia(mass,inertia);
			}
			RigidBodyConstructionInfo bodyConstructionInfo=new RigidBodyConstructionInfo(mass,motionState,shape,inertia);
			return bodyConstructionInfo;
		}
		return null;
	}
	/**
	 * Construct a RigidBodyConstructionInfo from a GObject's vertexData.
	 */
	public RigidBodyConstructionInfo initPhysics_mesh(GObject vertdata, Transform initialTransform) {
		CollisionShape shape=null;
		if(vertdata.fromOBJ) {
			OBJReturnObject o=new OBJReturnObject();
			o.fromVMap(vertdata.vmap, vertdata.getTris());
			TriangleIndexVertexArray mesh=new TriangleIndexVertexArray(o.numTriangles,o.indices,3*4,vertdata.vmap.vertices.size(),o.vertices,3*4);
			shape=new BvhTriangleMeshShape(mesh,true);
			MotionState motionState=new DefaultMotionState(initialTransform);
			if(mass!=0) {
				shape.calculateLocalInertia(mass,inertia);
			}
			RigidBodyConstructionInfo bodyConstructionInfo=new RigidBodyConstructionInfo(mass,motionState,shape,inertia);
			return bodyConstructionInfo;
		}
		return null;
	}
	/**
	 * Get a RigidBodyConstructionInfo from a JBullet CollisionShape.
	 */
	public RigidBodyConstructionInfo initPhysics_shape(CollisionShape shape, Vector3f pos, Quat4f rot) {
		MotionState motionState=new DefaultMotionState(new Transform(new Matrix4f(
				rot,
				pos,1.0f)));
		if(mass!=0) {
			shape.calculateLocalInertia(mass,inertia);
		}
		RigidBodyConstructionInfo bodyConstructionInfo=new RigidBodyConstructionInfo(mass,motionState,shape,inertia);
		bodyConstructionInfo.restitution=0.25f;
		return bodyConstructionInfo;
	}
	/**
	 * Get a RigidBodyConstructionInfo from a JBullet CollisionShape.
	 */
	public RigidBodyConstructionInfo initPhysics_shape(CollisionShape shape, Transform initialTransform) {
		MotionState motionState=new DefaultMotionState(initialTransform);
		if(mass!=0) {
			shape.calculateLocalInertia(mass,inertia);
		}
		RigidBodyConstructionInfo bodyConstructionInfo=new RigidBodyConstructionInfo(mass,motionState,shape,inertia);
		bodyConstructionInfo.restitution=0.25f;
		return bodyConstructionInfo;
	}
}
