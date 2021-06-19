package lepton.cpshlib.main;


public class SSBO {
	public int buffer;
	public int id;
	public int location;
	public boolean size_desynced=false;
	@Override
	public int hashCode() {
		return buffer*5+location*3+id;
	}
}
