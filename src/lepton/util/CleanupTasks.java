package lepton.util;

import java.util.ArrayList;

import lepton.util.advancedLogger.Logger;

public class CleanupTasks {
	private static int problematicCleanupTaskID=-1;
	public static ArrayList<CleanupTask> cleanup=new ArrayList<CleanupTask>();
	public static void add(CleanupTask ct) {
		cleanup.add(ct);
	}
	public static void cleanUp() {
		for(int i=0;i<cleanup.size();i++) {
			if(i==problematicCleanupTaskID) {
				Logger.log(3,"CleanupTasks: Identified a problematic cleanupTask with id "+i+". Skipping this task to avoid infinite loops.");
				continue;
			} else if(i<problematicCleanupTaskID) {
				Logger.log(1,"CleanupTasks: Skipping this cleanupTask with id "+i+" because it has already been performed due to problematic loopback cleanupTask.");
				continue;
			}
			problematicCleanupTaskID=i;
			CleanupTask ct=cleanup.get(i);
			ct.run();
		}
		problematicCleanupTaskID=-1;
	}
}
