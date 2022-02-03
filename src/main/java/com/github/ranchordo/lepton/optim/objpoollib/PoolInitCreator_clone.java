package com.github.ranchordo.lepton.optim.objpoollib;

import com.github.ranchordo.lepton.util.LeptonUtil;

/**
 * PoolInitCreator that works by cloning a prototype object.
 */
public class PoolInitCreator_clone<T extends Cloneable> extends PoolInitCreator<T> {  //PoolInitCreator that just clones stuff
	T prototype;
	public PoolInitCreator_clone(T p) {
		prototype=p;
	}
	@Override
	public T allocateInitValue() {
		return LeptonUtil.cloneObject(prototype);
	}
	
}
