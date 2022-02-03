package com.github.ranchordo.lepton.engine.physics;

import java.util.HashMap;

/**
 * A way for you to organise your rigidbodies using pointers so that you don't have to execute time-intensive searches.
 */
public class UserPointerStructure {
	public RigidBodyEntry ParentRBEntryPointer=null;
	protected HashMap<String,Object> userPointers=null;
	public void addUserPointer(String a, Object b) {
		if(userPointers==null) {userPointers=new HashMap<String, Object>();}
		userPointers.put(a,b);
	}
	public HashMap<String,Object> getUserPointers() {
		return userPointers;
	}
}
