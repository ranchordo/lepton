package lepton.optim.objpoollib;

import java.util.HashSet;

import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

/**
 * Strain through the active pools looking for old elements.
 */
public class PoolStrainer {
	public static long CLEAN_PERIOD=10000000; //10 seconds
	public static HashSet<AbstractObjectPool<?>> activePools=new HashSet<AbstractObjectPool<?>>();
	private static long lastClean=0;
	private static int cleanAll_noCheck() {
		int r=0;
		for(AbstractObjectPool<?> p : activePools) {
			r+=p.cleanOld();
		}
		return r;
	}
	/**
	 * Call this however often you want. This will clean every CLEAN_PERIOD us if you run this very often.
	 */
	public static void clean() {
		if(CLEAN_PERIOD<=0) {
			return;
		}
		if((LeptonUtil.micros()-lastClean)>CLEAN_PERIOD) {
			int r=cleanAll_noCheck();
			if(r!=0) {Logger.log(0,"Pool cleaning: Cleaned "+r+" old elements.");}
			lastClean=LeptonUtil.micros();
		}
	}
}