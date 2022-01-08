package lepton.optim.objpoollib;

/**
 * An interface responsible for creating an initial value that pools can use.
 */
public interface VariedPoolInitCreator<T> {
	public T allocateInitValueVaried(int desc); //Create a new object that we can use
}
