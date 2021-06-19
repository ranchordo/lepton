package lepton.optim.tensorlib.main;

import java.util.ArrayList;

import lepton.util.Util;


public class Tensor<T extends Cloneable> {
	ArrayList<TensorElement<T>> elements;
	public int rank;
	public int[] dim;
	private static int[] cloneIntArray(int[] in) {
		int[] ret=new int[in.length];
		for(int i=0;i<ret.length;i++) {
			ret[i]=in[i]+1-1;
		}
		return ret;
	}
	private static void carry(int[] arr, int[] dim, int currpos) {
		arr[currpos]+=1;
		if(arr[currpos]>=dim[currpos]) {
			arr[currpos]=0;
			carry(arr,dim,currpos+1);
		}
	}
	public Tensor(int rank, T i, int... dim) {
		this.rank=rank;
		this.dim=dim;
		if(rank!=dim.length) {
			throw new IllegalArgumentException("Length of dimensions is not equal to rank.");
		}
		elements=new ArrayList<TensorElement<T>>();
		int[] currpos=new int[rank];
		int t=1;
		for(int dim_e : dim) {
			t*=dim_e;
		}
		for(int j=0;j<rank;j++) {
			currpos[j]=0;
		}
		for(int j=0;j<t;j++) {
			elements.add(new TensorElement<T>(Util.cloneObject(i),cloneIntArray(currpos)));
			if(j!=t-1) {carry(currpos,dim,0);}
		}
	}
	public TensorElement<T> getElement(int... loc) {
		if(rank!=loc.length) {
			throw new IllegalArgumentException("Length of location is not equal to rank.");
		}
		int index=loc[0];
		for(int i=1;i<rank;i++) {
			int p=1;
			for(int j=0;j<i;j++) {
				p*=dim[j];
			}
			index+=(loc[i]*p);
		}
		TensorElement<T> ret=elements.get(index);
		boolean correct=true;
		for(int i=0;i<rank;i++) {
			if(ret.pos[i]!=loc[i]) {
				correct=false;
			}
		}
		if(!correct) {
			System.out.print("Requested location: ");
			for(int i=0;i<rank;i++) {
				System.out.print(loc[i]+", ");
			}
			System.out.println();
			System.out.print("Check location result: ");
			for(int i=0;i<rank;i++) {
				System.out.print(ret.pos[i]+", ");
			}
			System.out.println();
			System.out.println("Generated index: "+index);
			throw new IllegalStateException("Generated element index is not correct.");
		}
		return ret;
	}
	public T get(int... loc) {
		return getElement(loc).internal;
	}
}