package com.github.ranchordo.lepton.engine.util;

import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public abstract class Deletable {
	private static DeletableResourceTracker drt=new DeletableResourceTracker();
	public static DeletableResourceTracker getDRT() {
		return drt;
	}
	public static void setDRT(DeletableResourceTracker drt) {
		if(!Deletable.drt.isEmpty()) {
			Logger.log(4,"Cannot set a new DeletableResourceTracker when the old one is not empty. Old toString is "+Deletable.drt.toString());
		}
		Deletable.drt=drt;
	}
	public abstract void delete();
	public final void adrt() {
		drt.getResourceList(this.getClass()).add(this);
	}
	public final void rdrt() {
		drt.getResourceList(this.getClass()).remove(this);
	}
}
