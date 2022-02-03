package com.github.ranchordo.lepton.tests;

import com.github.ranchordo.lepton.optim.tensorlib.main.Tensor;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;
import com.github.ranchordo.lepton.util.cloneabletypes.ClFloat;

public class TensorLibTest {
	public static void main(String[] args) {
		//Create a new float tensor that's rank 2 (2-dimensional), inits with 0, and 10x10
		
		//CFloat is a float wrapper that is cloneable, which is a necessity for tensor initialization.
		Tensor<ClFloat> f=new Tensor<ClFloat>(2,new ClFloat(0),10,10);
		
		Logger.log(0,""+f.getElement(0,0)); //getElement returns the TensorElement itself
		Logger.log(0,""+f.get(0,0)); //get just returns the CFloat
		
		f.get(0,0).v=5.0f;
		f.getElement(1,2).internal.v=4.0f;
		Logger.log(0,""+f.getElement(0,0).internal.v);
		Logger.log(0,""+f.get(1,2).v);
		Logger.log(0,""+f.getElement(1,2).pos[0]+", "+f.getElement(1,2).pos[1]); //Access the position of a TensorElement
		Logger.log(0,""+f.rank);
		Logger.log(0,""+f.dim[0]+"x"+f.dim[1]);
	}
}
