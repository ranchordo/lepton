package lepton.engine.rendering;

import static org.lwjgl.opengl.GL43.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;

import com.bulletphysics.linearmath.Transform;

import lepton.engine.physics.PhysicsObject;
import lepton.engine.util.Deletable;
import lepton.optim.objpoollib.DefaultVecmathPools;
import lepton.optim.objpoollib.PoolElement;
import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

public class GObject extends Deletable {
	//Data types for VBO copying
	public static final int VERTEX_DATA=0;
	public static final int TEXTURE_DATA=1;
	public static final int COLOR_DATA=2;
	public static final int NORMAL_DATA=3;
	public static final int TANGENT_DATA=4;
	public static final int MATERIAL_DATA=5;

	public boolean useTex=false;
	private boolean zombie=false;
	public boolean isZombie() {return zombie;}
	private ArrayList<Tri> tris=new ArrayList<Tri>();
	public VertexMap vmap=new VertexMap();
	public boolean fromOBJ=false;

	private Shader renderingShader=null;

	public boolean useLighting=true;
	public boolean wireframe=false;

	private boolean trisLocked=false;

	public boolean useCulling=true;


	private int v_id;
	private int t_id;
	private int c_id;
	private int n_id;
	private int m_id;

	private int tan_id;
	private int bit_id;
	
	/**
	 * For instanced rendering. Use on things like particle systems (also use abstractObjectPool for those too, they're awesome).
	 */
	public int instances=-1;
	

	public void addTri(Tri toAdd) {
		if(!trisLocked) {tris.add(toAdd);}
		else {
			throw new SecurityException("addTri: Tris are locked!");
		}
	}
	public ArrayList<Tri> getTris() {
		return tris;
	}
	@Override public int hashCode() {
		return vmap.hashCode();
	}
	/**
	 * Clean up the data to avoid resource leaks.
	 */
	public void delete() {
		clearVBOs();
		zombie=true;
		rdrt();
	}
	private int zombieSize=-1;
	/**
	 * Convert to a zombie (an object that doesn't have any CPU-side data and is only cached GPU-side). Useful for reducing memory consumption for large models.
	 */
	public void zombify() {
		unlock();
		refresh();
		lock();
		zombie=true;
		zombieSize=tris.size();
		tris.clear();
		glcolors_last=null;
		glmatdata_last=null;
	}
	public void clearTris() {
		if(!trisLocked) {tris.clear();}
		else {
			throw new SecurityException("clearTris: Tris are locked!");
		}
	}
	public boolean getLocked() {return trisLocked;}
	/**
	 * Tri locking is so that we don't need to reinitialize buffers like an idiot on every frame. Having a definite number of triangles lets us optimize our buffer creation strategies.
	 */
	public void lock() {trisLocked=true;}
	public void unlock() {
		if(zombie) {
			Logger.log(4,"Cannot unlock data for zombies. No data to unlock.");
		}
		trisLocked=false;
	}

	private FloatBuffer vertex_data;
	private FloatBuffer texture_data;
	private FloatBuffer color_data;
	private FloatBuffer normal_data;
	private FloatBuffer material_data;
	private FloatBuffer tangent_data;
	private FloatBuffer bitangent_data;
	private FloatBuffer handleBufferCreation(FloatBuffer buffer, float[] data) {
		if(data==null) {return null;}
		if(buffer==null || buffer.capacity()<data.length) {
			buffer=BufferUtils.createFloatBuffer(data.length+12);
		}
		LeptonUtil.asFloatBuffer(data,buffer);
		return buffer;
	}
	private static void glBufferData_c(int a,FloatBuffer b,int c) {
		if(b==null) {return;}
		glBufferData(a,b,c);
	}
	public void copyData(int type, int mode) {
		if(zombie) {Logger.log(4,"Can't copy data from a zombified object.");}
		switch(type) {
		case VERTEX_DATA:
			glBindBuffer(GL_ARRAY_BUFFER,v_id);
			vertex_data=handleBufferCreation(vertex_data,glvertices());
			glBufferData_c(GL_ARRAY_BUFFER,vertex_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,0);
			break;
		case TEXTURE_DATA:
			glBindBuffer(GL_ARRAY_BUFFER,t_id);
			texture_data=handleBufferCreation(texture_data,gltexcoords());
			glBufferData_c(GL_ARRAY_BUFFER,texture_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,0);
			break;
		case COLOR_DATA:
			glBindBuffer(GL_ARRAY_BUFFER,c_id);
			color_data=handleBufferCreation(color_data,glcolors());
			glBufferData_c(GL_ARRAY_BUFFER,color_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,0);
			break;
		case NORMAL_DATA:
			glBindBuffer(GL_ARRAY_BUFFER,n_id);
			normal_data=handleBufferCreation(normal_data,glnormals());
			glBufferData_c(GL_ARRAY_BUFFER,normal_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,0);
			break;
		case MATERIAL_DATA:
			glBindBuffer(GL_ARRAY_BUFFER,m_id);
			material_data=handleBufferCreation(material_data,glmatdata());
			glBufferData_c(GL_ARRAY_BUFFER,material_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,0);
			break;
		case TANGENT_DATA:
			float[][] tbi=tangentBitangent();
			glBindBuffer(GL_ARRAY_BUFFER,tan_id);
			tangent_data=handleBufferCreation(tangent_data,tbi[0]);
			glBufferData_c(GL_ARRAY_BUFFER,tangent_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,bit_id);
			bitangent_data=handleBufferCreation(bitangent_data,tbi[1]);
			glBufferData_c(GL_ARRAY_BUFFER,bitangent_data,mode);
			glBindBuffer(GL_ARRAY_BUFFER,0);
			break;
		}
	}
	public void initVBO() {
		v_id=glGenBuffers();
		t_id=glGenBuffers();
		c_id=glGenBuffers();
		n_id=glGenBuffers();
		m_id=glGenBuffers();

		tan_id=glGenBuffers();
		bit_id=glGenBuffers();
		adrt();
	}
	/**
	 * If a segment from a to b hits this GObject transformed by transform tr.
	 */
	public boolean rayTest(Vector3f a, Vector3f b, Transform tr) {
		for(Tri t : tris) {
			if(t.rayTest(a,b,vmap,tr)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * If a segment from a to b hits this GObject transformed by transform tr, get a list of tris it hits.
	 */
	public ArrayList<Tri> rayTest_list(Vector3f a, Vector3f b, Transform tr) {
		ArrayList<Tri> result=new ArrayList<Tri>();
		for(Tri t : tris) {
			if(t.rayTest(a,b,vmap,tr)) {
				result.add(t);
			}
		}
		return result;
	}
	/**
	 * If a segment from a to b hits this GObject transformed by transform tr, get the closest tri that gets hit to a.
	 */
	public Tri rayTest_closest(Vector3f a, Vector3f b, Transform tr) {
		float minDistance=Tri.CLIP_DISTANCE+2;
		Tri winner=null;
		for(Tri t : tris) {
			float t_val=t.rayTest_back(a, b, vmap, tr);
			if(t_val>=-0.1) {
				if(t_val<minDistance) {
					minDistance=t_val;
					winner=t;
				}
			}
		}
		if(winner!=null) {
		}
		return winner;
	}
	/**
	 * If a ray from a to b hits this GObject transformed by transform tr, get the closest distance along that ray.
	 */
	public float rayTest_distance(Vector3f a, Vector3f b, Transform tr) {
		float minDistance=Tri.CLIP_DISTANCE+2;
		for(Tri t : tris) {
			float t_val=t.rayTest_back(a, b, vmap, tr);
			if(t_val>=-0.1) {
				if(t_val<minDistance) {
					minDistance=t_val;
				}
			}
		}
		if(minDistance>=Tri.CLIP_DISTANCE+1) {
			return -1;
		}
		return minDistance;
	}
	private void clearVBOs() {
		glDeleteBuffers(v_id);
		glDeleteBuffers(c_id);
		glDeleteBuffers(t_id);
		glDeleteBuffers(n_id);
		
		glDeleteBuffers(m_id);
		glDeleteBuffers(tan_id);
		glDeleteBuffers(bit_id);
	}
	/**
	 * Copy all the needed data GPU-side.
	 */
	public void refresh() {
		this.copyData(VERTEX_DATA,GL_STATIC_DRAW);
		this.copyData(NORMAL_DATA,GL_STATIC_DRAW);
		if(texAV()) {this.copyData(TEXTURE_DATA,GL_STATIC_DRAW);}
		this.copyData(COLOR_DATA,GL_STATIC_DRAW);
		this.copyData(MATERIAL_DATA,GL_STATIC_DRAW);
		if(tbreq()) {
			this.copyData(TANGENT_DATA,GL_STATIC_DRAW);
		}
	}
	private float[] toFloatArray(Matrix3f m,float[] put) {
		put[0]=m.m00; put[1]=m.m10; put[2]=m.m20;
		put[3]=m.m01; put[4]=m.m11; put[5]=m.m21;
		put[6]=m.m02; put[7]=m.m12; put[8]=m.m22;
		return put;
	}
	private Transform mmc=new Transform();
	private Matrix3f m=new Matrix3f();
	private float[] mma=new float[16];
	private FloatBuffer fm=BufferUtils.createFloatBuffer(16);
	private FloatBuffer f=BufferUtils.createFloatBuffer(9);

	private Matrix4f m4_=new Matrix4f();
	private float[] m3_=new float[9];
	public ViewMatrixModifier viewMatrixModifier=null;
//	private float sc;
	public void highRender_customTransform(Transform mm) {
		if(renderingShader==null) {
			GLContextInitializer.defaultMainShader.bind();
		} else {
			renderingShader.bind();
		}
		if(mm!=null) {
			mm.getMatrix(m4_).getRotationScale(m);
//			sc=LeptonUtil.getAvgScale(m);
			f=LeptonUtil.asFloatBuffer(toFloatArray(m,m3_),f);
			//mma=new float[16];
			mm.getOpenGLMatrix(mma);
			fm=LeptonUtil.asFloatBuffer(mma,fm);
			GLContextInitializer.activeShader.setUniformMatrix4fv("master_matrix", fm);
		}

		mmc.set(GLContextInitializer.cameraTransform);
		if(viewMatrixModifier!=null) {
			mmc=viewMatrixModifier.modifyViewMatrix(mmc);
		}
		mmc.getOpenGLMatrix(mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		GLContextInitializer.activeShader.setUniformMatrix4fv("world2view",fm);

		LeptonUtil.openGLMatrix(GLContextInitializer.proj_matrix,mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		GLContextInitializer.activeShader.setUniformMatrix4fv("proj_matrix",fm);

		GLContextInitializer.activeShader.setUniform1i("millis",(int)(LeptonUtil.micros()));

		GLContextInitializer.activeShader.setUniform1f("useLighting", (this.useLighting && GLContextInitializer.useGraphics) ? 2 : 0);
		GLContextInitializer.activeShader.setUniform1i("textureUse", this.vmap.tex.loadedBitflag());

		GLContextInitializer.activeShader.applyAllSSBOs();
		this.vmap.tex.bind();
		//glScalef(scale.x,scale.y,scale.z);
		//glScalef(sc,sc,sc);
		this.render_raw();
	}
	public void highRender(PhysicsObject obj) {
		highRender_customTransform(obj.getTransform());
	}

	public int getNumTris() {
		return (zombie)?zombieSize:tris.size();
	}
	/**
	 * tex Used and Loaded
	 */
	//public boolean texUAL() {return useTex && vmap.tex.anyLoaded();}
	public boolean tbreq() {return vmap.tex.TBReq();}
	/**
	 * texCoords AVailable.
	 */
	public boolean texAV() {return vmap.texcoords.size()>0;}
	/**
	 * Draw the main triangles (do not use; use highRender instead).
	 */
	private void optionallyInstancedDrawArrays() {
		if(instances<1) {
			glDrawArrays(GL_TRIANGLES,0,getNumTris()*3);
		} else {
			glDrawArraysInstanced(GL_TRIANGLES,0,getNumTris()*3,instances);
		}
	}
	/**
	 * Draw the triangles with no setup whatsoever. Don't use unless you're doing something awesome.
	 */
	public void render_raw() {
		if(!useCulling) {glDisable(GL_CULL_FACE);}
		if(wireframe) {glPolygonMode(GL_FRONT,GL_LINE);}
		glEnableVertexAttribArray(0);
		if(texAV()) {glEnableVertexAttribArray(8);}
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		if(tbreq()) {glEnableVertexAttribArray(14);}
		if(tbreq()) {glEnableVertexAttribArray(15);}
		glEnableVertexAttribArray(13);


		glBindBuffer(GL_ARRAY_BUFFER,v_id);
		glVertexAttribPointer(0,3,GL_FLOAT,false,0,0);

		if(texAV()) {
			glBindBuffer(GL_ARRAY_BUFFER,t_id);
			glVertexAttribPointer(8,2,GL_FLOAT,false,0,0);
		}

		glBindBuffer(GL_ARRAY_BUFFER,n_id);
		glVertexAttribPointer(2,3,GL_FLOAT,false,0,0);

		glBindBuffer(GL_ARRAY_BUFFER,c_id);
		glVertexAttribPointer(3,4,GL_FLOAT,false,0,0);

		if(tbreq()) {
			glBindBuffer(GL_ARRAY_BUFFER,tan_id);
			glVertexAttribPointer(14,3,GL_FLOAT,false,0,0);

			glBindBuffer(GL_ARRAY_BUFFER,bit_id);
			glVertexAttribPointer(15,3,GL_FLOAT,false,0,0);
		}
		glBindBuffer(GL_ARRAY_BUFFER,m_id);
		glVertexAttribPointer(13,4,GL_FLOAT,false,0,0);


		optionallyInstancedDrawArrays();
		glBindBuffer(GL_ARRAY_BUFFER,0);

		glDisableVertexAttribArray(0);
		if(texAV()) {glDisableVertexAttribArray(8);}
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		if(wireframe) {glPolygonMode(GL_FRONT,GL_FILL);}
		if(tbreq()) {glDisableVertexAttribArray(14);}
		if(tbreq()) {glDisableVertexAttribArray(15);}
		glDisableVertexAttribArray(13);
		if(!useCulling) {glEnable(GL_CULL_FACE);}
	}
	public void setColor(float r, float g, float b) {
		Tri t0=tris.get(0);
		if(t0.color[0]==r && t0.color[1]==g && t0.color[2]==b) {
			return;
		}
		for(int i=0;i<getNumTris();i++) {
			tris.get(i).setColor(r, g, b);
		}
	}
	public void setMaterial(float spec, float rough, float mat2, float mat3) {
		for(int i=0;i<getNumTris();i++) {
			tris.get(i).setMaterial(spec,rough,mat2,mat3);
		}
	}
	public void setColor(float r, float g, float b, float a) {
		Tri t0=tris.get(0);
		if(t0.color[0]==r && t0.color[1]==g && t0.color[2]==b && t0.color[3]==a) {
			return;
		}
		for(int i=0;i<getNumTris();i++) {
			tris.get(i).setColor(r, g, b);
			tris.get(i).setAlpha(a);
		}
	}
	public void scale(float s) {
		this.vmap.scale(s);
	}
	public void scale(float x, float y, float z) {
		this.vmap.scale(x,y,z);
	}
	//	public GObject scale(float s) {
	//		for(Tri t : tris) {
	//			for(float[] vert : t.vertices) {
	//				for(int i=0;i<vert.length;i++) {
	//					vert[i]*=s;
	//				}
	//			}
	//		}
	//		return this;
	//	}
	private boolean glverticesGenerated=false;
	private float[] glvertices() {
		if(zombie) {Logger.log(4,"Can't generate array data from a zombified object.");}
		if(glverticesGenerated && trisLocked) {
			return null;
		}
		float[] glvertices=new float[getNumTris()*3*3];
		for(int i=0;i<getNumTris();i++) {
			Vector3f vertex1=vmap.vertices.get(tris.get(i).vertices[0]);
			Vector3f vertex2=vmap.vertices.get(tris.get(i).vertices[1]);
			Vector3f vertex3=vmap.vertices.get(tris.get(i).vertices[2]);
			glvertices[i*3*3+0]=vertex1.x;
			glvertices[i*3*3+1]=vertex1.y;
			glvertices[i*3*3+2]=vertex1.z;

			glvertices[i*3*3+3]=vertex2.x;
			glvertices[i*3*3+4]=vertex2.y;
			glvertices[i*3*3+5]=vertex2.z;

			glvertices[i*3*3+6]=vertex3.x;
			glvertices[i*3*3+7]=vertex3.y;
			glvertices[i*3*3+8]=vertex3.z;
		}
		return glvertices;
	}
	public boolean glnormalsGenerated=false;
	private float[] glnormals() {
		if(zombie) {Logger.log(4,"Can't generate array data from a zombified object.");}
		if(glnormalsGenerated && trisLocked) {
			return null;
		}
		float[] glnormals=new float[getNumTris()*3*3];
		for(int i=0;i<getNumTris();i++) {
			Vector3f vertex1=vmap.normals.get(tris.get(i).normals[0]);
			Vector3f vertex2=vmap.normals.get(tris.get(i).normals[1]);
			Vector3f vertex3=vmap.normals.get(tris.get(i).normals[2]);
			glnormals[i*3*3+0]=vertex1.x;
			glnormals[i*3*3+1]=vertex1.y;
			glnormals[i*3*3+2]=vertex1.z;

			glnormals[i*3*3+3]=vertex2.x;
			glnormals[i*3*3+4]=vertex2.y;
			glnormals[i*3*3+5]=vertex2.z;

			glnormals[i*3*3+6]=vertex3.x;
			glnormals[i*3*3+7]=vertex3.y;
			glnormals[i*3*3+8]=vertex3.z;
		}
		return glnormals;
	}
	private float[] glcolors_last;
	private float[] glcolors() {
		if(zombie) {Logger.log(4,"Can't generate array data from a zombified object.");}
		if(glcolors_last==null || getNumTris()*3*4!=glcolors_last.length) {
			glcolors_last=new float[getNumTris()*3*4];
		}
		for(int i=0;i<getNumTris();i++) {
			PoolElement<Vector4f> vertex1_pe=DefaultVecmathPools.vector4f.alloc();
			vertex1_pe.o().set(tris.get(i).color[0],tris.get(i).color[1],tris.get(i).color[2],tris.get(i).color[3]);
			Vector4f vertex1=vertex1_pe.o();
			glcolors_last[i*3*4+0]=vertex1.x;
			glcolors_last[i*3*4+1]=vertex1.y;
			glcolors_last[i*3*4+2]=vertex1.z;
			glcolors_last[i*3*4+3]=vertex1.w;

			glcolors_last[i*3*4+4]=vertex1.x;
			glcolors_last[i*3*4+5]=vertex1.y;
			glcolors_last[i*3*4+6]=vertex1.z;
			glcolors_last[i*3*4+7]=vertex1.w;

			glcolors_last[i*3*4+8]=vertex1.x;
			glcolors_last[i*3*4+9]=vertex1.y;
			glcolors_last[i*3*4+10]=vertex1.z;
			glcolors_last[i*3*4+11]=vertex1.w;
			vertex1_pe.free();
		}
		return glcolors_last;
	}
	private float[] glmatdata_last;
	private float[] glmatdata() {
		if(zombie) {Logger.log(4,"Can't generate array data from a zombified object.");}
		if(glmatdata_last==null || getNumTris()*3*4!=glmatdata_last.length) {
			glmatdata_last=new float[getNumTris()*3*4];
		}
		for(int i=0;i<getNumTris();i++) {
			PoolElement<Vector4f> vertex1_pe=DefaultVecmathPools.vector4f.alloc();
			vertex1_pe.o().set(tris.get(i).material[0],tris.get(i).material[1],tris.get(i).material[2],tris.get(i).material[3]);
			Vector4f vertex1=vertex1_pe.o();
			glmatdata_last[i*3*4+0]=vertex1.x;
			glmatdata_last[i*3*4+1]=vertex1.y;
			glmatdata_last[i*3*4+2]=vertex1.z;
			glmatdata_last[i*3*4+3]=vertex1.w;

			glmatdata_last[i*3*4+4]=vertex1.x;
			glmatdata_last[i*3*4+5]=vertex1.y;
			glmatdata_last[i*3*4+6]=vertex1.z;
			glmatdata_last[i*3*4+7]=vertex1.w;

			glmatdata_last[i*3*4+8]=vertex1.x;
			glmatdata_last[i*3*4+9]=vertex1.y;
			glmatdata_last[i*3*4+10]=vertex1.z;
			glmatdata_last[i*3*4+11]=vertex1.w;
			vertex1_pe.free();
		}
		return glmatdata_last;
	}
	private boolean gltexcoordsGenerated=false;
	private float[] gltexcoords() {
		if(zombie) {Logger.log(4,"Can't generate array data from a zombified object.");}
		if(gltexcoordsGenerated && trisLocked) {
			return null;
		}
		float[] gltexcoords=new float[getNumTris()*3*2];
		for(int i=0;i<getNumTris();i++) {
			Vector2f vertex1=vmap.texcoords.get(tris.get(i).texcoords[0]);
			Vector2f vertex2=vmap.texcoords.get(tris.get(i).texcoords[1]);
			Vector2f vertex3=vmap.texcoords.get(tris.get(i).texcoords[2]);
			gltexcoords[i*3*2+0]=vertex1.x;
			gltexcoords[i*3*2+1]=1-vertex1.y;

			gltexcoords[i*3*2+2]=vertex2.x;
			gltexcoords[i*3*2+3]=1-vertex2.y;

			gltexcoords[i*3*2+4]=vertex3.x;
			gltexcoords[i*3*2+5]=1-vertex3.y;
		}
		return gltexcoords;
	}
	private boolean tangentBitangentGenerated=false;
	private float[][] tangentBitangent() {
		if(zombie) {Logger.log(4,"Can't generate array data from a zombified object.");}
		if(tangentBitangentGenerated && trisLocked) {
			return null;
		}
		Vector3f[] tangents=new Vector3f[getNumTris()*3];
		Vector3f[] bitangents=new Vector3f[getNumTris()*3];
		float[] tan_floats=new float[getNumTris()*3*3];
		float[] bit_floats=new float[getNumTris()*3*3];
		for(int i=0;i<getNumTris();i++) {
			Vector3f v0=vmap.vertices.get(tris.get(i).vertices[0]);
			Vector3f v1=vmap.vertices.get(tris.get(i).vertices[1]);
			Vector3f v2=vmap.vertices.get(tris.get(i).vertices[2]);

			Vector2f uv0=vmap.texcoords.get(tris.get(i).texcoords[0]);
			Vector2f uv1=vmap.texcoords.get(tris.get(i).texcoords[1]);
			Vector2f uv2=vmap.texcoords.get(tris.get(i).texcoords[2]);

			Vector3f dp1=new Vector3f(); dp1.sub(v1,v0);
			Vector3f dp2=new Vector3f(); dp2.sub(v2,v0);

			Vector2f duv1=new Vector2f(); duv1.sub(uv1,uv0);
			Vector2f duv2=new Vector2f(); duv2.sub(uv2,uv0);

			/**
			 *  float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
			 *  glm::vec3 tangent = (deltaPos1 * deltaUV2.y   - deltaPos2 * deltaUV1.y)*r;
			 *  glm::vec3 bitangent = (deltaPos2 * deltaUV1.x   - deltaPos1 * deltaUV2.x)*r;
			 */
			float r=1.0f / (duv1.x*duv2.y - duv1.y*duv2.x);
			Vector3f tangent=new Vector3f();//(Vector3f) Util.mul(Util.sub(Util.mul(dp1,duv2.y),Util.mul(dp2,duv1.y)),r);
			Vector3f bitangent=new Vector3f();//(Vector3f) Util.mul(Util.add(Util.mul(dp1,-duv2.x),Util.mul(dp2,duv1.x)),r);
			tangent.x = r * (duv2.y * dp1.x - duv1.y * dp2.x);
			tangent.y = r * (duv2.y * dp1.y - duv1.y * dp2.y);
			tangent.z = r * (duv2.y * dp1.z - duv1.y * dp2.z);

			bitangent.x = r * (-duv2.x * dp1.x + duv1.x * dp2.x);
			bitangent.y = r * (-duv2.x * dp1.y + duv1.x * dp2.y);
			bitangent.z = r * (-duv2.x * dp1.z + duv1.x * dp2.z);
			tangents[i*3+0]=tangent;
			tangents[i*3+1]=tangent;
			tangents[i*3+2]=tangent;
			bitangents[i*3+0]=bitangent;
			bitangents[i*3+1]=bitangent;
			bitangents[i*3+2]=bitangent;
		}
		for(int i=0;i<getNumTris()*3;i++) {
			tan_floats[i*3+0]=tangents[i].x;
			tan_floats[i*3+1]=tangents[i].y;
			tan_floats[i*3+2]=tangents[i].z;
		}
		for(int i=0;i<getNumTris()*3;i++) {
			bit_floats[i*3+0]=bitangents[i].x;
			bit_floats[i*3+1]=bitangents[i].y;
			bit_floats[i*3+2]=bitangents[i].z;
		}
		float[][] ret=new float[][] {tan_floats,bit_floats};
		return ret;
	}
	/**
	 * Load an obj file. It'll look for a texture with the same name and try to load that. (file extension is added automatically: do not put ".obj" at the end.)
	 */
	public GObject loadOBJ(String filename) {
		if(trisLocked) {
			throw new SecurityException("LoadOBJ(FN) -> Tris are locked. What the heck were you thinking?");
		}
		try {
			tris=loadOBJ_raw(new FileInputStream(LeptonUtil.getExternalPath()+"/"+filename+".obj"));
			if(texAV()) {
				boolean texError=true;
				for(String ext : new String[] {".png",".jpg"}) {
					String search="3d/"+filename+ext;
					try {
						Logger.log(0,"loadOBJ(f) multi-ext texture search: Loading new texture stem "+filename+"(extension "+ext+"). If you see this often, there's a problem.");
						for(int i=0;i<Texture.NUM_TEXTURES;i++) {
							try {
								this.vmap.tex.create(1,filename,"_"+(i==0?"":Texture.tex_names[i])+"."+ext);
							} catch (FileNotFoundException e) {
								Logger.log(0,"GObject "+filename+": No "+Texture.tex_names[i]+" texture.");
							}
						}
						this.vmap.tex.name=search;
						texError=false;
						break;
					} catch (NullPointerException | IllegalArgumentException e) {
						Logger.log(2,e.toString());
					}
				}
				if(texError) {
					Logger.log(1,"Texture mappings included, but no textures found for model "+filename);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
			Logger.log(4,"IOException GObject.loadObj",e);
		}
		return this;
	}
	/**
	 * Load texture manually. Must have file extension as "ext" and not in "filename".
	 */
	public void loadTexture(String filename, String ext) throws IOException {
		if(texAV()) {
			String search="3d/"+filename+ext;
			boolean found=false;
			if(found) {return;}
			try {
				Logger.log(0,"loadTexture(f,e) file search: Loading new texture stem "+filename+"(extension "+ext+"). If you see this often, there's a problem.");
				for(int i=0;i<Texture.NUM_TEXTURES;i++) {
					String flagged_ext=(i==0?"":("_"+Texture.tex_names[i]))+"."+ext;
					try {
						this.vmap.tex.create(i,filename,flagged_ext);
					} catch (FileNotFoundException e) {
						Logger.log(0,"Texture "+filename+": No "+Texture.tex_names[i]+" texture at expected location "+filename+flagged_ext+".");
					}
				}
				this.vmap.tex.name=search;
			} catch (NullPointerException | IllegalArgumentException e) {
				Logger.log(2,e.toString());
			}
		}
	}
	/**
	 * Load an obj file with a custom texture. Filename must not have a file extension, texname must not have one either, but the file extension for texname must be put in tex_ext.
	 */
	public GObject loadOBJ(String filename, String texname, String tex_ext) { //Wavefront file parser "frontend"
		if(trisLocked) {
			throw new SecurityException("LoadOBJ(FN,TN) -> Tris are locked.");
		}
		try {
			tris=loadOBJ_raw(LeptonUtil.getOptionallyIntegratedStream(filename,".obj"));
			loadTexture(texname,tex_ext);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			Logger.log(4,filename+" does not appear to exist.",e);
		} catch(IOException e) {
			e.printStackTrace();
			Logger.log(4,"IOException GObject.loadObj(fname,tname)",e);
		}
		return this;
	}
	/**
	 * Load OBJ file with no setup whatsoever. Do not use.
	 */
	public ArrayList<Tri> loadOBJ_raw(InputStream f) throws FileNotFoundException, IOException { //Wavefront file parser "backend"
		if(trisLocked) {
			throw new SecurityException("LoadOBJ_raw -> Tris are locked. What the heck were you thinking?");
		}
		fromOBJ=true;
		BufferedReader reader=new BufferedReader(new InputStreamReader(f));
		String line;
		ArrayList<Vector3f> vertices=new ArrayList<Vector3f>();
		ArrayList<Vector3f> normals=new ArrayList<Vector3f>();
		ArrayList<Vector3f> vertIndices=new ArrayList<Vector3f>();
		ArrayList<Vector3f> normIndices=new ArrayList<Vector3f>();
		ArrayList<Vector3f> texIndices=new ArrayList<Vector3f>();
		while((line=reader.readLine())!=null) {
			if(line.startsWith("v ")) {
				float x=Float.valueOf(line.split(" ")[1]);
				float y=Float.valueOf(line.split(" ")[2]);
				float z=Float.valueOf(line.split(" ")[3]);
				vertices.add(new Vector3f(x,y,z));
			} else if(line.startsWith("vn ")) {
				float x=Float.valueOf(line.split(" ")[1]);
				float y=Float.valueOf(line.split(" ")[2]);
				float z=Float.valueOf(line.split(" ")[3]);
				normals.add(new Vector3f(x,y,z));
			} else if(line.startsWith("f ")) {
				try {
					float x=Float.valueOf(line.split(" ")[1].split("/")[0]);
					float y=Float.valueOf(line.split(" ")[2].split("/")[0]);
					float z=Float.valueOf(line.split(" ")[3].split("/")[0]);
					vertIndices.add(new Vector3f(x,y,z));
					float xn=Float.valueOf(line.split(" ")[1].split("/")[2]);
					float yn=Float.valueOf(line.split(" ")[2].split("/")[2]);
					float zn=Float.valueOf(line.split(" ")[3].split("/")[2]);
					normIndices.add(new Vector3f(xn,yn,zn));
					if(texAV()) {
						float xt=Float.valueOf(line.split(" ")[1].split("/")[1]);
						float yt=Float.valueOf(line.split(" ")[2].split("/")[1]);
						float zt=Float.valueOf(line.split(" ")[3].split("/")[1]);
						texIndices.add(new Vector3f(xt,yt,zt));
					}
				} catch (NumberFormatException e) {
					Logger.log(4,e.toString()+"\nOccurred on file line "+line,e);
				}
			} else if(line.startsWith("vt ")) {
				String[] split=line.split(" ");
				Vector2f n=new Vector2f(Float.valueOf(split[1]),Float.valueOf(split[2]));
				this.vmap.texcoords.add(n);
			}
		}
		ArrayList<Tri> obj=new ArrayList<Tri>();
		for(int i=0;i<vertIndices.size();i++) {
			Tri t=new Tri(
					-1+(int)vertIndices.get(i).x,
					-1+(int)vertIndices.get(i).y,
					-1+(int)vertIndices.get(i).z,

					-1+(int)normIndices.get(i).x,
					-1+(int)normIndices.get(i).y,
					-1+(int)normIndices.get(i).z);
			if(texAV()) { 
				t.texcoords[0]=-1+(int)texIndices.get(i).x;
				t.texcoords[1]=-1+(int)texIndices.get(i).y;
				t.texcoords[2]=-1+(int)texIndices.get(i).z;
			}
			obj.add(t);
		}
		for(int i=0;i<vertices.size();i++) {
			this.vmap.vertices.add(vertices.get(i));
		}
		for(int i=0;i<normals.size();i++) {
			this.vmap.normals.add(normals.get(i));
		}
		reader.close();
		f.close();
		return obj;
	}
	public Shader getRenderingShader() {
		return renderingShader;
	}
	public void setRenderingShader(Shader renderingShader) {
		this.renderingShader = renderingShader;
	}
}
