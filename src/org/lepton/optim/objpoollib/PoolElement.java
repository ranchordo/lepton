package org.lepton.optim.objpoollib;

import org.lepton.util.Util;

public class PoolElement<T> {
	public long tmout=-1;
	private boolean used=false;
	private long lastToggle;
	protected T o;
	/**
	 * Miscellaneous Data Object
	 */
	public Object mdo=null;
	public void free() {setUsed(false);}
	public PoolElement(T newobject) {
		o=newobject;
		lastToggle=Util.micros();
	}
	public PoolElement<T> setInternalObject(T newobject) {
		o=newobject;
		return this;
	}
	protected PoolElement<T> setUsed(boolean nused) {
		used=nused;
		lastToggle=Util.micros();
		return this;
	}
	public boolean isUsed() {return used;}
	public long getLastToggle() {return lastToggle;}
	public T o() {return o;}
}
