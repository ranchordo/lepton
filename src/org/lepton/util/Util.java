package org.lepton.util;

import static org.lwjgl.opengl.GL46.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lepton.cpshlib.main.ComputeShader;
import org.lepton.optim.objpoollib.DefaultVecmathPools;
import org.lepton.optim.objpoollib.PoolElement;
import org.lepton.util.advancedLogger.Logger;
import org.lwjgl.BufferUtils;

import com.bulletphysics.linearmath.Transform;

public class Util {
	/**
	 * Shove a float[] into a floatBuffer of the same size.
	 */
	public static FloatBuffer asFloatBuffer(float[] values, FloatBuffer buffer) {
		buffer.clear();
		buffer.put(values);
		buffer.flip();
		return buffer;
	}
	/**
	 * Shove an int[] into an intBuffer of the same size.
	 */
	public static IntBuffer asIntBuffer(int[] values, IntBuffer buffer) {
		buffer.clear();
		buffer.put(values);
		buffer.flip();
		return buffer;
	}
	/**
	 * asIntBuffer but with bad gc strategies.
	 * DON'T USE THIS EXCEPT FOR THINGS THAT WILL ONLY RUN ONCE OR TWICE.
	 */
	public static IntBuffer asIntBuffer_badgc(int[] values) {
		IntBuffer ret=BufferUtils.createIntBuffer(values.length);
		asIntBuffer(values,ret);
		return ret;
	}
	private static Random rand=new Random();
	public static int randint(int max) {
		return rand.nextInt(max);
	}
	/**
	 * Quat() function but just return the AxisAngle4f and hold the pool functionality. Slower, uses reflection, and creates stuff to garbage collect.
	 * DON'T USE THIS EXCEPT FOR THINGS THAT WILL ONLY RUN ONCE OR TWICE.
	 */
	public static AxisAngle4f Quat_np(Quat4f q) {
		return noPool(Quat(q));
	}
	/**
	 * Get an InputStream but if the filename has " integrated" on the end it will fetch it from inside the jar.
	 */
	public static InputStream getOptionallyIntegratedStream(String resource, String ends) throws FileNotFoundException {
		try {
			if(!resource.endsWith(" integrated")) {
				return new FileInputStream(Util.getExternalPath()+"/"+resource+ends);
			} else {
				resource=resource.substring(0,resource.length()-11);
				InputStream ret=Util.class.getResourceAsStream("/"+resource+ends);
				if(ret==null) {
					throw new FileNotFoundException("/"+resource+ends+" integrated does not appear to exist.");
				}
				return ret;
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
			return null;
		}
	}
	/**
	 * Convert a rotation Quat4f into an active AxisAngle4f PoolElement.
	 */
	public static PoolElement<AxisAngle4f> Quat(Quat4f q) {
		PoolElement<AxisAngle4f> a=DefaultVecmathPools.axisAngle4f.alloc();
		a.o().angle=2.0f*(float)Math.acos(q.w);
		float b=(float)Math.sqrt(1-q.w*q.w);
		if(b==0.0f) {
			a.o().x=1;
			a.o().y=0;
			a.o().z=0;
			return a;
		}
		a.o().x=q.x/b;
		a.o().y=q.y/b;
		a.o().z=q.z/b;
		return a;
	}
	/**
	 * AxisAngle() function but just return the AxisAngle4f and hold the pool functionality. Slower, uses reflection, and creates stuff to garbage collect.
	 * DON'T USE THIS EXCEPT FOR THINGS THAT WILL ONLY RUN ONCE OR TWICE.
	 */
	public static Quat4f AxisAngle_np(AxisAngle4f i) {
		return noPool(AxisAngle(i));
	}
	public static PoolElement<Quat4f> AxisAngle(AxisAngle4f i) {
		PoolElement<Quat4f> ret=DefaultVecmathPools.quat4f.alloc();
		AxisAngle(i,ret.o());
		return ret;
	}
	/**
	 * Convert AxisAngle to Quat4f but stick it in an existing Quat4f.
	 */
	public static Quat4f AxisAngle(AxisAngle4f i, Quat4f out) {
		PoolElement<Vector3f> v=DefaultVecmathPools.vector3f.alloc();
		v.o().set(i.x,i.y,i.z);
		v.o().normalize();
		float f=(float)Math.sin(i.angle/2.0);
		//PoolElement<Quat4f> ret=DefaultVecmathPools.quat4f.alloc();
		out.set(v.o().x*f,v.o().y*f,v.o().z*f,(float)Math.cos(i.angle/2.0));
		v.free();
		return out;
	}
	/**
	 * Float modulo. Slow. But handles negative numbers well.
	 */
	public static float mod(float m, float n) {
		while(m>=n) {m=m-n;}
		while(m<0) {m=m+n;}
		return m;
	}
	/**
	 * Convert a Matrix4f into an opengl-style float[] containing the values.
	 */
	public static void openGLMatrix(Matrix4f in, float[] put) {
		put[0]=in.m00;put[1]=in.m01;put[2]=in.m02;put[3]=in.m03;
		put[4]=in.m10;put[5]=in.m11;put[6]=in.m12;put[7]=in.m13;
		put[8]=in.m20;put[9]=in.m21;put[10]=in.m22;put[11]=in.m23;
		put[12]=in.m30;put[13]=in.m31;put[14]=in.m32;put[15]=in.m33;
	}
	private static Matrix4f mat=new Matrix4f();
	/**
	 * Get average scale of a transform.
	 */
	public static float getAvgScale_tr(Transform in) {
		in.getMatrix(mat);
		return getAvgScale_4f(mat);
	}
	private static Matrix3f rs=new Matrix3f();
	/**
	 * Get average scale of a Matrix4f.
	 */
	public static float getAvgScale_4f(Matrix4f in) {
		in.getRotationScale(rs);
		return getAvgScale(rs);
	}
	private static Vector3f col1=new Vector3f();
	private static Vector3f col2=new Vector3f();
	private static Vector3f col3=new Vector3f();
	/**
	 * Get average scale of a Matrix3f.
	 */
	public static float getAvgScale(Matrix3f in) {
		col1.set(in.m00,in.m10,in.m20);
		col2.set(in.m01,in.m11,in.m21);
		col3.set(in.m02,in.m12,in.m22);
		return (col1.length()+col2.length()+col3.length())/3.0f;
	}
	@SuppressWarnings("unchecked")
	/**
	 * Use reflection magic to clone an object.
	 */
	public static <T extends Cloneable> T cloneObject(T i) {
		try {
			return (T)i.getClass().getMethod("clone").invoke(i);
		} catch (Exception e) {
			Logger.log(4,e.toString(),e);
			return null;
		}
	}
	/**
	 * Use type magic to un-poolify a thing.
	 */
	public static <T extends Cloneable> T noPool(PoolElement<T> q) {
		T ret=cloneObject(q.o());
		q.free();
		return ret;
	}
	/**
	 * Get external path where the project is located.
	 */
	public static String getExternalPath() {
		File jarpath=new File(ComputeShader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String externalPath=jarpath.getParentFile().getAbsolutePath().replace("\\", "/").replace("%20", " ");
		return externalPath;
	}
	/**
	 * Milliseconds since... well... something.
	 */
	public static long millis() {
		return System.nanoTime()/1000000l;
	}
	/**
	 * Microseconds since... well... something.
	 */
	public static long micros() {
		return System.nanoTime()/1000l;
	}
}
