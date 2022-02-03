package com.github.ranchordo.lepton.util.cloneabletypes;

public class ClLong implements Cloneable {
	public ClLong(long in) {this.v=in;}
	public long v;
	public Object clone() {
		return new ClLong(v);
	}
}
