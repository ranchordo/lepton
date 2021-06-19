package lepton.engine.physics;

import java.util.HashSet;

/**
 * A way for you to implement methods and stuff that get executed during physics steps. Useful if you're using modified physics, like portals or something. 
 */
public abstract class PhysicsStepModifier {
	public abstract void preStepProcess(HashSet<RigidBodyEntry> bodies);
	public abstract void postStepProcess(HashSet<RigidBodyEntry> bodies);
}
