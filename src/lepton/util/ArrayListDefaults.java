package lepton.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * It's close to an arraylist, except it can be initialized with defaults.
 */
public class ArrayListDefaults<T> implements Iterable<T> {
	private ArrayList<T> list;
	public ArrayList<T> getInternalArrayList() {
		return list;
	}
	@SafeVarargs
	public ArrayListDefaults(T... defaults) {
		list=new ArrayList<T>(defaults.length);
		for(T t : defaults) {
			list.add(t);
		}
	}
	public void add(T t) {
		list.add(t);
	}
	public T get(int i) {
		return list.get(i);
	}
	public void remove(int i) {
		list.remove(i);
	}
	public void remove(T t) {
		list.remove(t);
	}
	public int size() {
		return list.size();
	}
	
	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

}
