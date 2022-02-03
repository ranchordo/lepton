package com.github.ranchordo.lepton.engine.audio;

import static org.lwjgl.openal.AL10.*;

import javax.vecmath.Vector3f;

import com.github.ranchordo.lepton.engine.util.Deletable;

/**
 * Pretty self-explanatory.
 */
public class Source extends Deletable {
	public int sourceId;
	public boolean playing=false;
	private Vector3f pos=new Vector3f(0,0,0);
	private Vector3f vel=new Vector3f(0,0,0);
	public void setPosVel(Vector3f pos, Vector3f vel) {
		this.pos=pos;
		this.vel=vel;
	}
	public Vector3f getPos() {return pos;}
	public Vector3f getVel() {return vel;}
	private Sound activeSound=null;
	public Source() {
		sourceId=alGenSources();
		alSourcef(sourceId,AL_GAIN,Audio.masterVolume);
		alSourcef(sourceId,AL_PITCH,1);
		alSource3f(sourceId,AL_POSITION,pos.x,pos.y,pos.z);
		alSource3f(sourceId,AL_VELOCITY,vel.x,vel.y,vel.z);
		alSourceStop(sourceId);
		adrt();
	}
	public void updatePosVel() {
		alSource3f(sourceId,AL_POSITION,pos.x,pos.y,pos.z);
		alSource3f(sourceId,AL_VELOCITY,vel.x,vel.y,vel.z);
	}
	private void prePlay() {
		//Audio.activeListener.apply();
	}
//	public void setPosition(Float x, Float y, Float z) {
//		if(x!=null) {this.pos.x=x;}
//		if(y!=null) {this.pos.y=y;}
//		if(z!=null) {this.pos.z=z;}
//	}
	public void setGain(float gain) {
		alSourcef(sourceId,AL_GAIN,gain*Audio.masterVolume);
	}
	public void setPitch(float pitch) {
		alSourcef(sourceId,AL_PITCH,pitch);
	}
	public void play(Sound buffer) {
		if(activeSound!=null) {
			activeSound.delete();
			activeSound=null;
		}
		activeSound=buffer;
		prePlay();
		alSourcei(sourceId,AL_BUFFER,buffer.buffer());
		alSourcePlay(sourceId);
	}
	public void stop() {
		if(activeSound!=null) {
			activeSound.delete();
			activeSound=null;
		}
		alSourceStop(sourceId);
	}

	public void delete() {
		stop();
		alDeleteSources(sourceId);
		rdrt();
	}
	public boolean isPlaying() {
		boolean ret=alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
		if(!ret && activeSound!=null) {
			activeSound.delete();
			activeSound=null;
		}
		return ret;
	}
}
