package com.github.ranchordo.lepton.util;

import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class TimeProfiler {
	public String[] time_names;
	public TimeProfiler(String... time_names) {
		this.time_names=time_names;
		times_pr=new long[time_names.length];
		times=new long[time_names.length];
	}
	private long[] times_pr;
	public long[] times;
	public int progress=0;
	public void clear() {
		for(int i=0;i<time_names.length;i++) {
			times_pr[i]=0;
		}
	}
	public void start(int time) {
		progress|=(1<<time);
		times_pr[time]=LeptonUtil.micros()-times_pr[time];
	}
	public void stop(int time) {
		if((progress&(1<<time))!=0) {
			progress&=~(1<<time);
			times_pr[time]=LeptonUtil.micros()-times_pr[time];
		}
	}
	public void checkValid() {
		if(progress!=0) {
			Logger.log(4,"In-progress variable for timeprofiler is not 0, it is "+Integer.toBinaryString(progress));
		}
	}
	public void submit() {
		checkValid();
		for(int i=0;i<times.length;i++) {
			times[i]=times_pr[i];
		}
	}
	@Override public String toString() {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<times.length;i++) {
			sb.append(time_names[i]);
			sb.append(": ");
			sb.append(times[i]);
			sb.append("us, ");
		}
		return sb.toString();
	}
}
