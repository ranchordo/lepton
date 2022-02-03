package com.github.ranchordo.lepton.optim.tensorlib.main;

public class TensorElement<T> {
	public TensorElement(T in, int[] p) {internal=in; pos=p;}
	public T internal;
	public int[] pos;
}
