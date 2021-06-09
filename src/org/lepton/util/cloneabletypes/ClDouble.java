package org.lepton.util.cloneabletypes;

public class ClDouble implements Cloneable {
	public ClDouble(double in) {this.v=in;}
	public double v;
	public Object clone() {
		return new ClDouble(v);
	}
}
