package lepton.engine.physics;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import com.bulletphysics.dynamics.RigidBody;

public class RigidBodyEntry {
	public RigidBody b;
	public short group;
	public short mask;
	protected Vector3f constAcceleration=new Vector3f(0,0,0);
	public boolean useGravity=true;
	public RigidBodyEntry(RigidBody b, short group, short mask) {
		this.b=b;
		if(b.getUserPointer()==null) {
			UserPointerStructure ups=new UserPointerStructure();
			ups.ParentRBEntryPointer=this;
			this.b.setUserPointer(ups);
		}
		this.mask=mask;
		this.group=group;
	}
	public void refreshGravity(PhysicsWorld pw) {
		Vector3f g=pw.getGravity();
		if(!useGravity) {
			constAcceleration.set(-g.x,-g.y,-g.z);
		} else {
			constAcceleration.set(0,0,0);
		}
	}
	protected ArrayList<RigidBodyEntry> collisions=null;
	protected ArrayList<Float> collisionVels=null;
	protected void initializeCollisionListsPerFrame() {
		if(collisions==null) {
			collisions=new ArrayList<RigidBodyEntry>();
		}
		if(collisionVels==null) {
			collisionVels=new ArrayList<Float>();
		}
		collisions.clear();
		collisionVels.clear();
	}
	public ArrayList<RigidBodyEntry> getCollisions() {
		return collisions;
	}
	public ArrayList<Float> getCollisionVels() {
		return collisionVels;
	}
}
