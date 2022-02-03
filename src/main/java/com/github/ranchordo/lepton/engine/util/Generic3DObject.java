package com.github.ranchordo.lepton.engine.util;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.engine.physics.PhysicsObject;
import com.github.ranchordo.lepton.engine.physics.WorldObject;
import com.github.ranchordo.lepton.engine.rendering.GObject;
import com.github.ranchordo.lepton.engine.rendering.Shader;
import com.github.ranchordo.lepton.engine.rendering.lighting.Light;
import com.github.ranchordo.lepton.engine.rendering.lighting.Lighting;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class Generic3DObject {
	public static final float side_len=0.62f;
	private Vector3f origin;
	private Quat4f quat;
	transient public WorldObject geo;

	private float scale;
	private Shader shader;
	private String obj;
	public RigidBodyConstructionInfo rbci() {
		RigidBodyConstructionInfo b=this.geo.p.initPhysics_mesh(this.geo.g,origin,quat);
		b.restitution=0f;
		b.friction=0.74f;
		b.angularDamping=0.0f;
		b.linearDamping=0.16f;
		return b;
	}
	public ArrayList<Light> objSpaceLights=new ArrayList<Light>();
	public void initPhysics(RigidBodyConstructionInfo body) {
		this.geo.p.doBody(body);
		this.geo.p.body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
	}
	public Generic3DObject(Vector3f origin, Quat4f quat, float scale, Shader shader, String obj) {
		this.origin=origin;
		this.quat=quat;
		this.scale=scale;
		this.shader=shader;
		this.obj=obj;
	}
	public void render() {
		this.geo.g.highRender(this.geo.p);
	}
	public void initGeo() {
		this.geo=new WorldObject();
		this.geo.g=new GObject();
		this.geo.p=new PhysicsObject();
		this.geo.g.useTex=false;
		this.geo.g.useLighting=true;
        this.geo.g.useCulling=false;

        this.geo.p.setMotionSource(PhysicsObject.VARIABLE);
        this.geo.p.transformVariable=new Transform(new Matrix4f(quat,origin,scale));
		
		this.geo.g.loadOBJ(obj);
		
		if(shader!=null) {this.geo.g.setRenderingShader(shader);}
		this.geo.g.setMaterial(0.02f,2.0f,0,0);

		this.geo.g.lock();
		
		this.geo.g.initVBO();
		this.geo.g.refresh();
		
		if(!objSpaceLights.isEmpty()) {
			for(Light l : objSpaceLights) {
				if(l.type!=Light.LIGHT_POSITION) {
					Logger.log(3,"All object space lights must be a positional source.");
					continue;
				}
				this.geo.p.transformVariable.transform(l.prop);
				Lighting.addLight(l);
			}
		}
	}
}
