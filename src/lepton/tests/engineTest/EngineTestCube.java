package lepton.tests.engineTest;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;

import lepton.engine.audio.Soundtrack;
import lepton.engine.audio.SourcePool;
import lepton.engine.physics.Physics;
import lepton.engine.physics.PhysicsObject;
import lepton.engine.physics.RigidBodyEntry;
import lepton.engine.physics.WorldObject;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.GObject;
import lepton.engine.rendering.Tri;

public class EngineTestCube {
	public static final float side_len=2f;
	private Vector3f origin;
	private Quat4f quat;
	WorldObject geo;
	
	Soundtrack soundtrack=new Soundtrack();
	SourcePool sourcePool=new SourcePool("EngineTestCube");
	
	public Vector3f shape;
	public EngineTestCube(Vector3f origin, Quat4f quat) {
		this.shape=new Vector3f(side_len/2.0f,side_len/2.0f,side_len/2.0f);
		this.origin=origin;
		this.quat=quat;
	}
	public void initPhysics() {
		this.geo.p.mass=2.7f*0.642f*6.0f*(float)Math.pow(side_len*100.0f,2)*0.001f;
		CollisionShape s=new BoxShape(shape);
		RigidBodyConstructionInfo body=this.geo.p.initPhysics_shape(s, origin, quat);
		body.restitution=0f;
		body.friction=0.74f;
		body.angularDamping=0.0f;
		body.linearDamping=0.16f;
		this.geo.p.doBody(body);
		this.geo.p.body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
	}
	public void render() {
		this.geo.g.highRender(this.geo.p);
	}
	int counter=0;
	private ArrayList<RigidBodyEntry> pcollisions=new ArrayList<RigidBodyEntry>();
	private Point3f tempPoint=new Point3f();
	private Matrix4f tempMat4=new Matrix4f();
	public void logic() {
		tempPoint.set(this.geo.p.getTransform().origin);
		GLContextInitializer.cameraTransform.getMatrix(tempMat4).transform(tempPoint);
		sourcePool.logic(tempPoint);
		ArrayList<RigidBodyEntry> collisions=Physics.getEntryFromRigidBody(this.geo.p.body).getCollisions();
		ArrayList<Float> collisionVels=Physics.getEntryFromRigidBody(this.geo.p.body).getCollisionVels();
		for(int i=0;i<collisions.size();i++) {
			RigidBodyEntry thing=collisions.get(i);
			if(!pcollisions.contains(thing)) {
				float v=Math.abs(collisionVels.get(i));
				if(v>0) {
					sourcePool.play("Clank",soundtrack);
					sourcePool.getPlaying("Clank").o().setGain(v);
				}
			}
		}
		pcollisions.clear();
		for(int i=0;i<collisions.size();i++) {
			pcollisions.add(collisions.get(i));
		}
	}
	public void initSoundtrack() {
		soundtrack.put("Clank","engineTest-clank integrated");
	}
	public void initGeo() {
		this.geo=new WorldObject();
		this.geo.g=new GObject();
		this.geo.p=new PhysicsObject();
		this.geo.g.useTex=false;
		this.geo.g.useLighting=true;
		
		this.geo.g.vmap.vertices=new ArrayList<Vector3f>();
		this.geo.g.vmap.vertices.add(new Vector3f(-shape.x,-shape.y,-shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(+shape.x,-shape.y,-shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(+shape.x,+shape.y,-shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(-shape.x,+shape.y,-shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(-shape.x,-shape.y,+shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(+shape.x,-shape.y,+shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(+shape.x,+shape.y,+shape.z));
		this.geo.g.vmap.vertices.add(new Vector3f(-shape.x,+shape.y,+shape.z));
		this.geo.g.vmap.normals=new ArrayList<Vector3f>();
		this.geo.g.vmap.normals.add(new Vector3f(0,0,-1));
		this.geo.g.vmap.normals.add(new Vector3f(0,0, 1));
		this.geo.g.vmap.normals.add(new Vector3f(0, 1,0));
		this.geo.g.vmap.normals.add(new Vector3f(0,-1,0));
		this.geo.g.vmap.normals.add(new Vector3f( 1,0,0));
		this.geo.g.vmap.normals.add(new Vector3f(-1,0,0));
		this.geo.g.clearTris();
		this.geo.g.addTri(new Tri(2,1,0, 0,0,0));
		this.geo.g.addTri(new Tri(0,3,2, 0,0,0));
		this.geo.g.addTri(new Tri(4,5,6, 1,1,1));
		this.geo.g.addTri(new Tri(6,7,4, 1,1,1));
		this.geo.g.addTri(new Tri(2,3,6, 3,3,3));
		this.geo.g.addTri(new Tri(7,6,3, 3,3,3));

		this.geo.g.addTri(new Tri(0,1,5, 2,2,2));
		this.geo.g.addTri(new Tri(5,4,0, 2,2,2));

		this.geo.g.addTri(new Tri(5,1,6, 5,5,5));
		this.geo.g.addTri(new Tri(2,6,1, 5,5,5));

		this.geo.g.addTri(new Tri(0,4,7, 4,4,4));
		this.geo.g.addTri(new Tri(7,3,0, 4,4,4));
		this.geo.g.setColor(1,0,1);
		this.geo.g.setMaterial(0.02f,2.0f,0,0);

		this.geo.g.lock();
		
		this.geo.g.initVBO();
		this.geo.g.refresh();
		//		ggeo.animator.add("Main", AnimParser.parse("3d/testcoords").setEndMode(AnimTrack.LOOP));
	}
}
