package lepton.optim.objpoollib;

import java.util.ArrayList;
import java.util.List;

import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

/**
 * Object pool with custom behavior.
 */
public abstract class AbstractObjectPool<T> {
	/**
	 * Amount of microseconds to keep unused elements
	 */
	public long freeThshld=2000000l;
	public List<PoolElement<T>> pool;
	public List<PoolElement<T>> inUse;
	private String poolType="";
	public boolean printExpansion=true;
	/**
	 * When we need to allocate more spaces, this object will control how new objects are created.
	 */
	private PoolInitCreator<T> prototype;
	
	public void setInitCreator(PoolInitCreator<T> p) {
		prototype=p;
	}
	
	public AbstractObjectPool(String type, PoolInitCreator<T> p) {
		pool=new ArrayList<PoolElement<T>>();
		inUse=new ArrayList<PoolElement<T>>();
		poolType=type;
		PoolStrainer.activePools.add(this);
		prototype=p;
	}
	/**
	 * Override this as a "destructor"
	 */
	public void handleDeletion(PoolElement<T> i) {
		
	}
	/**
	 * PoolStrainer keeps track of this. Just call its clean method occasionally.
	 */
	public int cleanOld() {
		List<PoolElement<T>> old=new ArrayList<PoolElement<T>>();
		for(int i=0;i<pool.size();i++) {
			PoolElement<T> e=pool.get(i);
			if(freeThshld>0 && ((LeptonUtil.micros()-e.getLastToggle())>freeThshld)) { //Timing out the not used ones
				old.add(e);
			}
		}
		for(int i=0;i<old.size();i++) {
			handleDeletion(old.get(i)); //Clean it up
			if(!pool.remove(old.get(i))) {
				Logger.log(4,poolType+" pool: Old element somehow disappeared! What!?");
			}
		}
		return old.size();
	}
	protected PoolElement<T> getFreeElement() {
		if(pool.size()>0) {
			PoolElement<T> e=pool.get(0);
			if(e.o()==null) {
				Logger.log(2,"PoolElement from "+poolType+" pool is null on getFreeElement retrieval");
				e.setInternalObject(prototype.allocateInitValue()); //Fix this null thing
			}
			return e;
		}
		//If we weren't able to get something from the pool
		if(printExpansion) {Logger.log(0,"Expanding "+poolType+" pool to "+(inUse.size()+1)+" elements.");}
		PoolElement<T> ret=new PoolElement<T>(prototype.allocateInitValue());
		ret.parentPool=this;
		pool.add(ret);
		return ret;
	}
	public PoolElement<T> alloc() {
		PoolElement<T> ret=getFreeElement().setUsed(true);
		pool.remove(ret);
		inUse.add(ret);
		return ret;
	}
	public String getPoolType() {return poolType;}
	/**
	 * Remove ourselves from PoolStrainer's active pools.
	 */
	public void die() {
		PoolStrainer.activePools.remove(this);
	}
	/**
	 * Drop all elements of the pool.
	 */
	public void free() {
		//Drop all elements and garbage collect
		for(PoolElement<T> pe : pool) {
			handleDeletion(pe);
		}
		for(PoolElement<T> pe : inUse) {
			handleDeletion(pe);
		}
		pool.clear();
		inUse.clear();
	}
}
