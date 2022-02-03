package com.github.ranchordo.lepton.engine.animation;

import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.util.LeptonUtil;

public class AnimTrack {
	public static final int LOOP=1;
	public static final int STAY=2;
	private float playbackFPS=-1;
	private int endMode;
	private float counter=0;
	public ArrayList<Keyframe> keyframes=new ArrayList<Keyframe>();
	public Transform blend2space=new Transform(new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,(float)Math.toRadians(270))),new Vector3f(0,0,0),1.0f));
	public Transform addTransform=new Transform(new Matrix4f(LeptonUtil.AxisAngle_np(new AxisAngle4f(1,0,0,(float)Math.toRadians(0))),new Vector3f(0,0,0),1.0f));
	public boolean useAddTransform=true;
	private Transform ret=new Transform();
	public void setPlaybackFPS(float nf) {
		if(playbackFPS<0) {
			playbackFPS=nf;
		} else {
			throw new SecurityException("Cannot change the playbackFPS after parsing");
		}
		if(playbackFPS<0) {
			throw new IllegalStateException("Why is playbackFPS negative?");
		}
	}
	private int protectKeyframes(int idx) {
		if(idx<0) {return 0;}
		if(idx>keyframes.size()-1) {return keyframes.size()-1;}
		return idx;
	}
	public Transform getFrame() {
		if(animIsDone()) {handleEnd();}
		ret.set(keyframes.get(protectKeyframes((int)Math.floor(counter))).tr);
//		if(!animIsDone()) {
//			System.out.println(ret.origin);
//		}
		ret.mul(blend2space,ret);
		//ret.set(new Matrix4f(keyframes.get((int)Math.floor(counter)).tr.getRotation(new Quat4f()),ret.origin,1.0f));
		if(useAddTransform) {ret.mul(addTransform,ret);}
		return ret;
	}
	public AnimTrack setEndMode(int nend) {
		endMode=nend;
		return this;
	}
	public float getFrameCounter() {return counter;}
	public void setFrameCounter(float nc) {counter=nc;}
	private void handleEnd() {
		switch(endMode) {
		case LOOP:
			counter=reverse?(keyframes.size()-1):0;
			return;
		case STAY:
			return;
		}
	}
	public void advance(float n) {
		if(animIsDone()) {
			handleEnd();
		} else {
			counter+=n;
		}
	}
	private boolean reverse=false;
	public void advance_tc(int nfr) {
		if(GLContextInitializer.fr<0) {return;}
		advance((reverse?-1:1)*nfr*(playbackFPS/GLContextInitializer.fr));
	}
	public void resetFrameCounter() {
		counter=0;
	}
	public boolean animIsDone() {
		return reverse?((int)Math.floor(counter)<=0):((int)Math.floor(counter)>=keyframes.size()-1);
	}
	public boolean isReversed() {
		return reverse;
	}
	public void setReversed(boolean reverse) {
		this.reverse=reverse;
	}
}
