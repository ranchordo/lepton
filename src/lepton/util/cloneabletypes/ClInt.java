package lepton.util.cloneabletypes;

public class ClInt implements Cloneable {
	public ClInt(int in) {this.v=in;}
	public int v;
	public Object clone() {
		return new ClInt(v);
	}
}
