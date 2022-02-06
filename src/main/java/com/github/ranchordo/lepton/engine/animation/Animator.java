package com.github.ranchordo.lepton.engine.animation;

import java.util.HashMap;
import java.util.Map.Entry;

import com.bulletphysics.linearmath.Transform;

public class Animator {
	private HashMap<String, AnimTrack> tracks=new HashMap<String,AnimTrack>();
	private AnimTrack track;
	public Transform synchronizedTransform;
	public Animator() {}
	/**
	 * Copy the synchronizedTransform pointer into the AnimTrack's transform to put on top of the raw motion. Useful for transforming an animation procedurally.
	 */
	public void copyTransformPointer() {
		for(Entry<String,AnimTrack> e : tracks.entrySet()) {
			e.getValue().addTransform=synchronizedTransform;
		}
	}
	/**
	 * Add an animTrack with name "key"
	 */
	public void add(String key, AnimTrack t) {
		tracks.put(key,t);
	}
	/**
	 * Remove an animTrack with a name.
	 */
	public void remove(String key) {
		tracks.remove(key);
	}
	public Transform getFrame() {
		if(track==null) {
			throw new IllegalStateException("Active track is null");
		}
		return track.getFrame();
	}
	public AnimTrack getTrack() {
		return track;
	}
	public void clearStoredTracks() {track=null; tracks.clear();}
	public void setActiveTrack(String key) {
		track=tracks.get(key);
		if(track==null) {
			throw new IllegalStateException("Track "+key+" is not contained.");
		}
	}
	public void resetAllTrackFrameCounters() {
		for(Entry<String,AnimTrack> e : tracks.entrySet()) {
			e.getValue().resetFrameCounter();
		}
	}
	public void resetTrack() {
		track.resetFrameCounter();
	}
	public void advanceAllTracks() {
		for(Entry<String,AnimTrack> e : tracks.entrySet()) {
			e.getValue().advanceFrames(1);
		}
	}
	public void advanceTrack() {
		track.advanceFrames(1);
	}
}
