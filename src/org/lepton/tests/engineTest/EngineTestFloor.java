package org.lepton.tests.engineTest;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lepton.engine.physics.PhysicsObject;
import org.lepton.engine.physics.WorldObject;
import org.lepton.engine.rendering.GObject;
import org.lepton.engine.rendering.Texture;
import org.lepton.engine.rendering.Tri;
import org.lepton.util.Util;
import org.lepton.util.advancedLogger.Logger;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;

public class EngineTestFloor {
	public static final float side_len=0.62f;
	private Vector3f origin;
	private Quat4f quat;
	transient WorldObject geo;

	public Vector3f shape;
	public EngineTestFloor(Vector3f origin, Quat4f quat) {
		this.shape=new Vector3f(25,25,1);
		this.origin=origin;
		this.quat=quat;
	}
	public void initPhysics() {
		this.geo.p.mass=0;
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
	public void initGeo() {
		this.geo=new WorldObject();
		this.geo.g=new GObject();
		this.geo.p=new PhysicsObject();
		this.geo.g.useTex=true; //If useTex=false but you're loading a texture, you'll get a memory access exception. This is characterized by the JVM suddenly exiting with code -1073741819. Check your texture usage if this happens.
		this.geo.g.useBump=false;
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
		Vector2f texOffset=new Vector2f(0,0);
		Vector2f aspect=new Vector2f(1,1);
		this.geo.g.vmap.texcoords=new ArrayList<Vector2f>();
		this.geo.g.vmap.texcoords.add(new Vector2f(texOffset.x                 ,texOffset.y+shape.y/aspect.y));
		this.geo.g.vmap.texcoords.add(new Vector2f(texOffset.x+shape.x/aspect.x,texOffset.y+shape.y/aspect.y));
		this.geo.g.vmap.texcoords.add(new Vector2f(texOffset.x+shape.x/aspect.x,texOffset.y));
		this.geo.g.vmap.texcoords.add(new Vector2f(texOffset.x                 ,texOffset.y));
		this.geo.g.clearTris();
		this.geo.g.addTri(new Tri(2,1,0, 0,0,0).setTexCoords(1,2,3));
		this.geo.g.addTri(new Tri(0,3,2, 0,0,0).setTexCoords(3,0,1));
		
		this.geo.g.addTri(new Tri(4,5,6, 1,1,1).setTexCoords(1,0,3));
		this.geo.g.addTri(new Tri(6,7,4, 1,1,1).setTexCoords(3,2,1));
		
		this.geo.g.addTri(new Tri(2,3,6, 3,3,3).setTexCoords(3,0,1));
		this.geo.g.addTri(new Tri(7,6,3, 3,3,3).setTexCoords(1,2,3));

		this.geo.g.addTri(new Tri(0,1,5, 2,2,2).setTexCoords(3,2,1));
		this.geo.g.addTri(new Tri(5,4,0, 2,2,2).setTexCoords(1,0,3));

		this.geo.g.addTri(new Tri(5,1,6, 5,5,5).setTexCoords(3,2,1));
		this.geo.g.addTri(new Tri(2,6,1, 5,5,5).setTexCoords(1,0,3));

		this.geo.g.addTri(new Tri(0,4,7, 4,4,4).setTexCoords(3,2,1));
		this.geo.g.addTri(new Tri(7,3,0, 4,4,4).setTexCoords(1,0,3));
		
		this.geo.g.setColor(1,1,1);
		this.geo.g.setMaterial(0.02f,2.0f,0,0);

		this.geo.g.lock();
		
		this.geo.g.vmap.tex.colorLoaded=true; //Before loading a texture manually, make sure to enable this to tell GObject that texCoords are specified. Otherwise loading will fail.
		try {
			this.geo.g.loadTexture("cropped_border.jpg integrated");
			this.geo.g.vmap.tex=new Texture(this.geo.g.vmap.tex); //Separate from the texture cache in order to modify
			this.geo.g.vmap.tex.reset(Texture.BUMP);
			this.geo.g.vmap.tex.reset(Texture.NORMAL);
			this.geo.g.vmap.tex.create_norm("cropped_border_normal_detail.jpg integrated","");
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
		}
		if(this.geo.g.vmap.tex.normLoaded) {
			this.geo.g.useBump=true;
		}
		
		//Make sure you do this last (even after loading textures) to fix what buffers need to be initialized:
		//(Otherwise you will spend a lot of time finding tiny bugs [mostly access violations])
		this.geo.g.initVBO();
		this.geo.g.refresh();
	}
}
