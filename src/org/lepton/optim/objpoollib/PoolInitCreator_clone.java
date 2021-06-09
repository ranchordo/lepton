package org.lepton.optim.objpoollib;

import org.lepton.util.Util;

/**
 * PoolInitCreator that works by cloning a prototype object.
 */
public class PoolInitCreator_clone<T extends Cloneable> implements PoolInitCreator<T> {  //PoolInitCreator that just clones stuff
	T prototype;
	public PoolInitCreator_clone(T p) {
		prototype=p;
	}
	@Override
	public T allocateInitValue() {
		return Util.cloneObject(prototype);
	}
	
}
