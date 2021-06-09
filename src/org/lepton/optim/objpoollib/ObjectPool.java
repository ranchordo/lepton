package org.lepton.optim.objpoollib;

/**
 * Object pool with default behavior.
 */
public class ObjectPool<T> extends AbstractObjectPool<T> {

	public ObjectPool(String type, PoolInitCreator<T> p) {
		super(type, p);
	}

}
