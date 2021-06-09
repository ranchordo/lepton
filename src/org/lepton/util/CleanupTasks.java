package org.lepton.util;

import java.util.ArrayList;

public class CleanupTasks {
	public static ArrayList<CleanupTask> cleanup=new ArrayList<CleanupTask>();
	public static void add(CleanupTask ct) {
		cleanup.add(ct);
	}
	public static void cleanUp() {
		for(CleanupTask ct : cleanup) {
			ct.run();
		}
	}
}
