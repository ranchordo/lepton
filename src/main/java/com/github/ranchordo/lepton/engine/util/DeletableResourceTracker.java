package com.github.ranchordo.lepton.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class DeletableResourceTracker {
	private HashMap<Class<? extends Deletable>,ArrayList<Deletable>> map=new HashMap<Class<? extends Deletable>,ArrayList<Deletable>>();
	public ArrayList<Deletable> getResourceList(Class<? extends Deletable> cl) {
		ArrayList<Deletable> list=map.get(cl);
		if(list==null) {
			list=new ArrayList<Deletable>();
			map.put(cl,list);
		}
		return list;
	}
	public void deleteAll(Class<? extends Deletable> cl) {
		HashSet<Deletable> dls=new HashSet<Deletable>();
		ArrayList<Deletable> l=getResourceList(cl);
		Logger.log(0,"Deleting and cleaning up "+l.size()+" object(s) of type "+cl.getSimpleName()+".");
		for(Deletable d : l) {
			dls.add(d);
		}
		for(Deletable d : dls) {
			d.delete();
		}
		if(l.size()>0) {
			Logger.log(4,"Deleting was unsuccessful for class "+cl.getSimpleName());
		}
	}
	public boolean isEmpty() {
		if(map.size()==0) {
			return true;
		}
		for(Entry<Class<? extends Deletable>,ArrayList<Deletable>> e : map.entrySet()) {
			if(e.getValue().size()!=0) {
				return false;
			}
		}
		return true;
	}
	public void deleteAll() {
		for(Entry<Class<? extends Deletable>,ArrayList<Deletable>> e : map.entrySet()) {
			deleteAll(e.getKey());
		}
	}
	@Override public String toString() {
		StringBuilder sb=new StringBuilder();
		for(Entry<Class<? extends Deletable>,ArrayList<Deletable>> e : map.entrySet()) {
			sb.append(e.getKey().getSimpleName());
			sb.append(": ");
			sb.append(e.getValue().size());
			sb.append(", ");
		}
		return sb.toString();
	}
}