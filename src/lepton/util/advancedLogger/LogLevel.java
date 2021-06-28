package lepton.util.advancedLogger;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LogLevel {
	private static boolean isFatal_internal=false;
	public static boolean isFatal() {
		return isFatal_internal;
	}
	String prefix;
	boolean botherPrinting=true;
	boolean inErrStream=false;
	boolean exitOnRecv=false;
	int exitCode=1;
	public LogLevel(String p, boolean b, boolean e, boolean ex) {
		this.prefix=p;
		this.botherPrinting=b;
		this.inErrStream=e;
		this.exitOnRecv=ex;
	}
	public LogLevel setExitCode(int nCode) {
		this.exitCode=nCode;
		return this;
	}
	public void tryExit() {
		if(exitOnRecv) {
			isFatal_internal=true;
			StackTraceElement[] trace=Thread.currentThread().getStackTrace();
			String stackTrace=Arrays.stream(trace).map(o->o.toString()).collect(Collectors.joining("\n"));
			Logger.log(Logger.logger_internal,"FATAL ERROR STACK TRACE:\n"+stackTrace);
			if(Logger.lfc!=null) {Logger.lfc.run();}
			System.exit(exitCode);
		}
	}
}
