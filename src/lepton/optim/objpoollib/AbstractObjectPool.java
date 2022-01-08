package lepton.optim.objpoollib;

/**
 * Object pool with custom behavior and no object variation.
 */
public abstract class AbstractObjectPool<T> extends VariedAbstractObjectPool<T> {
	public AbstractObjectPool(String type, PoolInitCreator<T> p) {
		super(type,p);
	}
	public PoolElement<T> alloc() {
		PoolElement<T> ret=getFreeElement(0).setUsed(true);
		pool.remove(ret);
		inUse.add(ret);
		return ret;
	}
}
