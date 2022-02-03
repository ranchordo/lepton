package com.github.ranchordo.lepton.optim.objpoollib;

/**
 * An interface responsible for creating an initial value that pools can use.
 */
public abstract class PoolInitCreator<T> implements VariedPoolInitCreator<T> {
	public abstract T allocateInitValue(); //Create a new object that we can use
	@Override public final T allocateInitValueVaried(int desc) {
		return allocateInitValue();
	}
}
