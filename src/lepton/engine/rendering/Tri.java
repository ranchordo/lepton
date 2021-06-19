package lepton.engine.rendering;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import lepton.optim.objpoollib.DefaultVecmathPools;
import lepton.optim.objpoollib.PoolElement;

public class Tri {
	public float[] color= {0.0f,0.0f,0.0f,1.0f};
	public float[] material= {0.0f,32.0f,0.0f,0.0f};
	public int[] normals;
	public int[] vertices;
	public int[] texcoords=new int[] {0,0,0};
	public float raytest_t=-1;
	public Vector3f raytest_intersection=null;
	public Object userPointer=null;
	public Tri(int v1, int v2, int v3, int n1, int n2, int n3) {
		normals=new int[] {n1,n2,n3};
		vertices=new int[] {v1,v2,v3};
	}
	public Tri setColor(float r, float g, float b) {
		color[0]=r;
		color[1]=g;
		color[2]=b;
		return this;
	}
	public Tri setMaterial(float a, float b, float c, float d) {
		material[0]=a;
		material[1]=b;
		material[2]=c;
		material[3]=d;
		return this;
	}
	public Tri setAlpha(float a) {
		color[3]=a;
		return this;
	}
	public Tri setTexCoords(int c1, int c2, int c3) {
		this.texcoords=new int[] {c1,c2,c3};
		return this;
	}
	private static final double EPSILON = 1e-6;
	public boolean rayTest(Vector3f la, Vector3f lb, VertexMap vmap, Transform tr) {
		float t=rayTest_back(la,lb,vmap,tr);
		if (t>0-EPSILON) {
			if(t<=1+EPSILON) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	public boolean rayTest_infi(Vector3f la, Vector3f lb, VertexMap vmap, Transform tr) {
		float t=rayTest_back(la,lb,vmap,tr);
		return t>EPSILON;
	}
	private Vector3d lad=new Vector3d();
	private Vector3d lbd=new Vector3d();
	private Vector3d rayOrigin=new Vector3d();
	private Vector3d rayVector=new Vector3d();
	private Vector3f rayOriginf=new Vector3f();
	private Vector3f rayVectorf=new Vector3f();
	private Vector3f vertex0f=new Vector3f();
	private Vector3f vertex1f=new Vector3f();
	private Vector3f vertex2f=new Vector3f();
	private Vector3d vertex0=new Vector3d();
	private Vector3d vertex1=new Vector3d();
	private Vector3d vertex2=new Vector3d();
	private Vector3d edge1=new Vector3d();
	private Vector3d edge2=new Vector3d();
	private Vector3d h=new Vector3d();
	private Vector3d s=new Vector3d();
	private Vector3d q=new Vector3d();
	private Vector3f intersection=new Vector3f();
	public synchronized PoolElement<Vector3f> rayTest_back_back(Vector3f la, Vector3f lb, VertexMap vmap, Transform tr) { //Implementation of the Moller-Trumbore intersection algorithm
		rayOrigin.set(la);
		//rayVector.set(0,0,0);
		lbd.set(lb);
		lad.set(la);
		rayVector.sub(lbd,lad);
		Vector3f pt0=vmap.vertices.get(vertices[0]);
		Vector3f pt1=vmap.vertices.get(vertices[1]);
		Vector3f pt2=vmap.vertices.get(vertices[2]);
		vertex0f.set(pt0);
		vertex1f.set(pt1);
		vertex2f.set(pt2);
		tr.transform(vertex0f);
		tr.transform(vertex1f);
		tr.transform(vertex2f);
		vertex0.set(vertex0f);
		vertex1.set(vertex1f);
		vertex2.set(vertex2f);
		double a,f,u,v;
		edge1.sub(vertex1,vertex0);
		edge2.sub(vertex2,vertex0);
		h.cross(rayVector,edge2);
		a=edge1.dot(h);
		if(a>-EPSILON && a<EPSILON) {
			this.raytest_t=-1;
			raytest_intersection=null;
			return null;
		}
		f=1.0f/a;
		s.sub(rayOrigin,vertex0);
		u=f*(s.dot(h));
		if(u<0.0f || u>1.0f) {
			this.raytest_t=-1;
			raytest_intersection=null;
			return null;
		}
		q.cross(s,edge1);
		v=f*rayVector.dot(q);
		if(v<0.0 || u+v>1.0) {
			this.raytest_t=-1;
			raytest_intersection=null;
			return null;
		}
		float t=(float)(f*edge2.dot(q));
		this.raytest_t=t;
		if(t>EPSILON) {
			intersection.set(0,0,0);
			rayVectorf.set(rayVector);
			rayOriginf.set(rayOrigin);
			intersection.scaleAdd(t,rayVectorf,rayOriginf);
			raytest_intersection=intersection;
			PoolElement<Vector3f> ret=DefaultVecmathPools.vector3f.alloc();
			ret.o().set(intersection);
			return ret;
		}
		raytest_intersection=null;
		return null;
	}
	public static final float CLIP_DISTANCE=200;
	private Vector3f diff=new Vector3f();
	public synchronized float rayTest_back(Vector3f a, Vector3f ob, VertexMap vmap, Transform tr) { //Boolean intersection algorithm
		diff.sub(ob,a);
		float vl=diff.length();
		diff.normalize();
		PoolElement<Vector3f> intersection=rayTest_back_back(a,ob,vmap,tr);
		if(intersection!=null) {
			  intersection.o().sub(a);
			  float d=intersection.o().dot(diff);
			  float t=d/vl;
			  raytest_t=t;
			  intersection.free();
			  return t;//0.5f;
			  
		}
		raytest_t=-1;
		return -1;
	}
//	public Vector3f rayTest_back_back(Vector3f a, Vector3f ob, VertexMap vmap, Transform tr) {
//		Vector3f diff=new Vector3f();
//		diff.sub(ob,a);
//		diff.normalize();
//		diff.scale(CLIP_DISTANCE);
//		Vector3f b=new Vector3f();
//		b.add(a,diff);
//		Vector3f pt0=vmap.vertices.get(vertices[0]);
//		Vector3f pt1=vmap.vertices.get(vertices[1]);
//		Vector3f pt2=vmap.vertices.get(vertices[2]);
//		Vector3f p0=new Vector3f(pt0.x,pt0.y,pt0.z);
//		Vector3f p1=new Vector3f(pt1.x,pt1.y,pt1.z);
//		Vector3f p2=new Vector3f(pt2.x,pt2.y,pt2.z);
//		tr.transform(p0);
//		tr.transform(p1);
//		tr.transform(p2);
//		Matrix3f s1=new Matrix3f(
//				a.x-b.x, p1.x-p0.x, p2.x-p0.x,
//				a.y-b.y, p1.y-p0.y, p2.y-p0.y,
//				a.z-b.z, p1.z-p0.z, p2.z-p0.z);
//		try {
//			s1.invert();
//		} catch (SingularMatrixException e) {
//			raytest_intersection=null;
//			return null;
//		}
//		Vector3f res=new Vector3f(
//				a.x-p0.x,
//				a.y-p0.y,
//				a.z-p0.z);
//		s1.transform(res);
//		if(res.x>=0-EPSILON && res.x<=1+EPSILON) {
//			//Intersection with plane containing tri
//			if (res.y>=0-EPSILON && res.y<=1+EPSILON && res.z>=0-EPSILON && res.z<=1+EPSILON && (res.y+res.z)<=1+EPSILON) {
//				  Vector3f edge1=new Vector3f();
//				  Vector3f edge2=new Vector3f();
//				  Vector3f edge3=new Vector3f();
//				  edge1.sub(p1,p0);
//				  edge2.sub(p2,p0);
//				  edge3.sub(p2,p1);
//				  Vector3f intersection=(Vector3f)p0.clone();
//				  edge2.scale(res.y);
//				  //edge3.scale(res.z);
//				  //intersection.add(edge3);
//				  intersection.add(edge2);
//				  raytest_intersection=(Vector3f)intersection.clone();
//				  return intersection;
//				  
//			}
//		}
//		raytest_intersection=null;
//		return null;
//	}
}
