package com.github.ranchordo.lepton.optim.objpoollib;

import com.github.ranchordo.lepton.util.LeptonUtil;

public class PoolElement<T> {
	private long lastToggle;
	public VariedAbstractObjectPool<T> parentPool;
	protected T o;
	public int desc;
	/**
	 * Miscellaneous Data Object
	 */
	public Object mdo=null;
	public void free() {
		parentPool.inUse.remove(this);
		parentPool.pool.add(this);
		setUsed(false);
	}
	public PoolElement(T newobject) {
		o=newobject;
		lastToggle=LeptonUtil.micros();
	}
	public PoolElement<T> setInternalObject(T newobject) {
		o=newobject;
		return this;
	}
	protected PoolElement<T> setUsed(boolean nused) {
		lastToggle=LeptonUtil.micros();
		return this;
	}
	public long getLastToggle() {return lastToggle;}
	public T o() {return o;}
}
