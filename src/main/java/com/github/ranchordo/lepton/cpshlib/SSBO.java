package com.github.ranchordo.lepton.cpshlib;

import org.lwjgl.opengl.GL43;

import com.github.ranchordo.lepton.engine.util.Deletable;

public class SSBO extends Deletable {
	public int buffer=-1;
	public int id;
	public int location;
	public boolean size_desynced=false;
	@Override
	public int hashCode() {
		return buffer*5+location*3+id;
	}
	public SSBO() {
		adrt();
	}
	public void delete() {
		GL43.glDeleteBuffers(buffer);
		rdrt();
	}
}
