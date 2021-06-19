package lepton.tests;

import lepton.optim.objpoollib.*;
import lepton.util.cloneabletypes.ClFloat;

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
		System.out.println("Pool test 1: "+testElement.o().v);
		testElement.free(); //Send it back to the pool
		//Now we can't use testElement anymore, because we won't be able to access the thing. So let's set it null to remember:
		testElement=null;
		PoolElement<ClFloat> testElement1=examplePool.alloc();
		PoolElement<ClFloat> testElement2=examplePool.alloc();
		//Allocating two elements should expand the pool dynamically
		System.out.println(testElement1.o().v); //The values of old elements will spill over though, so it's not great at security.
		System.out.println(testElement2.o().v);
		testElement1.free();
		testElement2.free();
		//After a timeout, the pool will shrink back to 0 (no elements are used). This is polled through the method:
		PoolStrainer.clean();
		System.out.println("Clean 1");
		System.out.println("Pausing for 20 seconds to timeout the pool...");
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		PoolStrainer.clean();
		System.out.println("Clean 2");
	}
}
