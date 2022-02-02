package lepton.engine.util;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.GObject;
import lepton.engine.rendering.Tri;

public class GenericCubeFactory {
	private static final Vector3f shape=new Vector3f(1f,1f,1f);
	public static GObject createGenericCube() {
		GObject geo=new GObject();
		geo.useTex=false;
//		geo.useCulling=false;
		geo.useLighting=false;
		geo.vmap.vertices=new ArrayList<Vector3f>();
		geo.vmap.vertices.add(new Vector3f(-shape.x,-shape.y,-shape.z));
		geo.vmap.vertices.add(new Vector3f(+shape.x,-shape.y,-shape.z));
		geo.vmap.vertices.add(new Vector3f(+shape.x,+shape.y,-shape.z));
		geo.vmap.vertices.add(new Vector3f(-shape.x,+shape.y,-shape.z));
		geo.vmap.vertices.add(new Vector3f(-shape.x,-shape.y,+shape.z));
		geo.vmap.vertices.add(new Vector3f(+shape.x,-shape.y,+shape.z));
		geo.vmap.vertices.add(new Vector3f(+shape.x,+shape.y,+shape.z));
		geo.vmap.vertices.add(new Vector3f(-shape.x,+shape.y,+shape.z));
		geo.vmap.normals=new ArrayList<Vector3f>();
		geo.vmap.normals.add(new Vector3f(0,0,-1));
		geo.vmap.normals.add(new Vector3f(0,0, 1));
		geo.vmap.normals.add(new Vector3f(0, 1,0));
		geo.vmap.normals.add(new Vector3f(0,-1,0));
		geo.vmap.normals.add(new Vector3f( 1,0,0));
		geo.vmap.normals.add(new Vector3f(-1,0,0));
		geo.clearTris();
		geo.addTri(new Tri(2,1,0, 0,0,0));
		geo.addTri(new Tri(0,3,2, 0,0,0));
		geo.addTri(new Tri(4,5,6, 1,1,1));
		geo.addTri(new Tri(6,7,4, 1,1,1));
		geo.addTri(new Tri(2,3,6, 3,3,3));
		geo.addTri(new Tri(7,6,3, 3,3,3));

		geo.addTri(new Tri(0,1,5, 2,2,2));
		geo.addTri(new Tri(5,4,0, 2,2,2));

		geo.addTri(new Tri(5,1,6, 5,5,5));
		geo.addTri(new Tri(2,6,1, 5,5,5));

		geo.addTri(new Tri(0,4,7, 4,4,4));
		geo.addTri(new Tri(7,3,0, 4,4,4));
		geo.setColor(1,0,1);
		geo.setMaterial(0.02f,2.0f,0,0);
		
		geo.setColor(1,0,1);
		
		geo.lock();
		
		geo.initVBO();
		geo.refresh();
		geo.setRenderingShader(GLContextInitializer.shaderLoader.load("plainShader"));
		return geo;
	}
}
