package com.github.ranchordo.lepton.tests;

import com.github.ranchordo.lepton.optim.objpoollib.ObjectPool;
import com.github.ranchordo.lepton.optim.objpoollib.PoolElement;
import com.github.ranchordo.lepton.optim.objpoollib.PoolInitCreator_clone;
import com.github.ranchordo.lepton.optim.objpoollib.PoolStrainer;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;
import com.github.ranchordo.lepton.util.cloneabletypes.ClFloat;

public class ObjPoolLibTest {
	//CFloat is a wrapper for float that is cloneable
	public static ObjectPool<ClFloat> examplePool=
			new ObjectPool<ClFloat>( //Object pool instantiation takes
					"Float", //A string (the name of the pool)
					new PoolInitCreator_clone<ClFloat>( //And the poolInitCreator that creates new elements, which takes
							new ClFloat(0))); //The initial element to be cloned.
	public static void main(String[] args) {
		PoolElement<ClFloat> testElement=examplePool.alloc();
		testElement.o().v=4.549688f;
		//o() gets the inside value, v is the field in the float wrapper.
		Logger.log(0,"Pool test 1: "+testElement.o().v);
		testElement.free(); //Send it back to the pool
		//Now we can't use testElement anymore, because we won't be able to access the thing. So let's set it null to remember:
		testElement=null;
		PoolElement<ClFloat> testElement1=examplePool.alloc();
		PoolElement<ClFloat> testElement2=examplePool.alloc();
		//Allocating two elements should expand the pool dynamically
		Logger.log(0,((Float)testElement1.o().v).toString()); //The values of old elements will spill over though, so it's not great at security.
		Logger.log(0,((Float)testElement2.o().v).toString());
		testElement1.free();
		testElement2.free();
		//After a timeout, the pool will shrink back to 0 (no elements are used). This is polled through the method:
		PoolStrainer.clean();
		Logger.log(0,"Clean 1");
		Logger.log(0,"Pausing for 20 seconds to timeout the pool...");
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		PoolStrainer.clean();
		Logger.log(0,"Clean 2");
	}
}
