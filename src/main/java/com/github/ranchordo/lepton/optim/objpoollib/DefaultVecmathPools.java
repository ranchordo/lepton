package com.github.ranchordo.lepton.optim.objpoollib;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.bulletphysics.linearmath.Transform;
/**
 * These are the pools that are used specifically here. This is mostly packaged along for an example on new pools.
 */
public class DefaultVecmathPools {
	public static ObjectPool<Vector3f> vector3f=new ObjectPool<Vector3f>("Vector3f",new PoolInitCreator_clone<Vector3f>(new Vector3f()));
	public static ObjectPool<Vector4f> vector4f=new ObjectPool<Vector4f>("Vector4f",new PoolInitCreator_clone<Vector4f>(new Vector4f()));
	public static ObjectPool<AxisAngle4f> axisAngle4f=new ObjectPool<AxisAngle4f>("AxisAngle4f",new PoolInitCreator_clone<AxisAngle4f>(new AxisAngle4f()));
	public static ObjectPool<Quat4f> quat4f=new ObjectPool<Quat4f>("Quat4f",new PoolInitCreator_clone<Quat4f>(new Quat4f()));
	public static ObjectPool<Matrix4f> matrix4f=new ObjectPool<Matrix4f>("Matrix4f",new PoolInitCreator_clone<Matrix4f>(new Matrix4f()));
	public static ObjectPool<Transform> transform=new ObjectPool<Transform>("Transform",new PoolInitCreator<Transform>() {
		@Override
		public Transform allocateInitValue() {
			return new Transform();
		}});
}
