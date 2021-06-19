package lepton.engine.physics;

import lepton.engine.rendering.GObject;

public class WorldObject {
	public PhysicsObject p;
	public GObject g;
	public WorldObject(PhysicsObject p_, GObject g_) {
		p=p_;
		g=g_;
	}
	public WorldObject(boolean ig, boolean ip) {
		if(ig) {ig();}
		if(ip) {ip();}
	}
	public WorldObject(boolean ia) {
		if(ia) {ia();}
	}
	public WorldObject() {}
	public void highRender() {
		g.highRender(p);
	}
	/**
	 * Is the WorldObject null or w's GObject is null? (useful for quick error checking)
	 */
	public static boolean gnull(WorldObject w) {
		if(w!=null) {
			return w.g==null;
		}
		return true;
	}
	/**
	 * Is the WorldObject null or w's GObject or Physics is null? (useful for quick error checking)
	 */
	public static boolean anull(WorldObject w) {
		if(w!=null) {
			return w.g==null || w.p==null;
		}
		return true;
	}
	/**
	 * Is the WorldObject null or w's PhysicsObject is null? (useful for quick error checking)
	 */
	public static boolean pnull(WorldObject w) {
		if(w!=null) {
			return w.p==null;
		}
		return true;
	}
	/**
	 * Initialize PhysicsObject
	 */
	public void ip() {
		p=new PhysicsObject();
	}
	/**
	 * Initialize GObject
	 */
	public void ig() {
		g=new GObject();
	}
	/**
	 * Initialize all internal structures.
	 */
	public void ia() {ip(); ig();}
}
