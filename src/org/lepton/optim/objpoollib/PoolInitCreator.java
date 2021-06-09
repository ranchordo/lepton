package org.lepton.optim.objpoollib;

/**
 * An interface responsible for creating an initial value that pools can use.
 */
public interface PoolInitCreator<T> {
	public T allocateInitValue(); //Create a new object that we can use
}
