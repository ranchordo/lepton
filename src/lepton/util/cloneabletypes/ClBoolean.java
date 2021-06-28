package lepton.util.cloneabletypes;

public class ClBoolean implements Cloneable {
	public ClBoolean(boolean in) {this.v=in;}
	public boolean v;
	public Object clone() {
		return new ClBoolean(v);
	}
}
