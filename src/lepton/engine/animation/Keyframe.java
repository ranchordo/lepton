package lepton.engine.animation;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

public class Keyframe {
	int frame;
	Transform tr=new Transform();
	Matrix4f trmat=new Matrix4f();
	protected Keyframe(Vector3f pos, Quat4f rot, float scale, int fr) {
		trmat.set(rot,pos,scale);
		tr.set(trmat);
		frame=fr;
	}
}
