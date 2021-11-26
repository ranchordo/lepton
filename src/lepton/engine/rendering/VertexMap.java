package lepton.engine.rendering;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class VertexMap {
	public Texture tex=new Texture();
	public ArrayList<Vector2f> texcoords=new ArrayList<Vector2f>();
	public ArrayList<Vector3f> vertices=new ArrayList<Vector3f>();
	public ArrayList<Vector3f> normals=new ArrayList<Vector3f>();
	public void scale(float s) {
		for(Vector3f e : vertices) {
			e.scale(s);
		}
	}
	public void scale(float x, float y, float z) {
		for(Vector3f e : vertices) {
			e.x=e.x*x;
			e.y=e.y*y;
			e.z=e.z*z;
		}
	}
	public void transform(Matrix4f t) {
		for(Vector3f e : vertices) {
			t.transform(e);
		}
	}
	@Override public int hashCode() {
		int ret=0;
		int multiplier=1;
		for(Vector2f v : texcoords) {
			ret+=((Float)v.x).hashCode()*multiplier;
			multiplier*=0x38927483;
			ret+=((Float)v.y).hashCode()*multiplier;
			multiplier*=0x38927483;
		}
		for(Vector3f v : vertices) {
			ret+=((Float)v.x).hashCode()*multiplier;
			multiplier*=0x38927483;
			ret+=((Float)v.y).hashCode()*multiplier;
			multiplier*=0x38927483;
			ret+=((Float)v.z).hashCode()*multiplier;
			multiplier*=0x38927483;
		}
		for(Vector3f v : normals) {
			ret+=((Float)v.x).hashCode()*multiplier;
			multiplier*=0x38927483;
			ret+=((Float)v.y).hashCode()*multiplier;
			multiplier*=0x38927483;
			ret+=((Float)v.z).hashCode()*multiplier;
			multiplier*=0x38927483;
		}
		return ret;
	}
}
