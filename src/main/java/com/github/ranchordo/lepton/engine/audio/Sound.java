package com.github.ranchordo.lepton.engine.audio;

import static org.lwjgl.openal.AL10.alDeleteBuffers;

import com.github.ranchordo.lepton.engine.util.Deletable;

/**
 * Wrapper for sample buffer.
 */
public class Sound extends Deletable {
	private Integer bufferId;
	public Sound() {adrt();}
	public Sound(String fname) {getFile(fname); adrt();}
	public int buffer() {return bufferId;}
	public void getFile(String fname) {
		if(bufferId!=null) {
			delete();
		}
		bufferId=Audio.getOGG(fname);
	}
	public void delete() {
		alDeleteBuffers(bufferId);
		rdrt();
	}
}