package com.github.ranchordo.lepton.util.cloneabletypes;

public class ClFloat implements Cloneable {
	public ClFloat(float in) {this.v=in;}
	public float v;
	public Object clone() {
		return new ClFloat(v);
	}
}
