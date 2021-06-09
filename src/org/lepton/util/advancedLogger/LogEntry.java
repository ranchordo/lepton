package org.lepton.util.advancedLogger;

/**
 * Exactly what it sounds like.
 */
public class LogEntry {
	LogLevel level;
	String message;
	Exception attachment;
	public LogEntry(int l, String m) {
		this.level=Logger.levels[l];
		this.message=m;
	}
	public LogEntry(int l, String m, Exception a) {
		this.level=Logger.levels[l];
		this.message=m;
		this.attachment=a;
	}
	public LogEntry(LogLevel l, String m) {
		this.level=l;
		this.message=m;
	}
	public LogEntry(LogLevel l, String m, Exception a) {
		this.level=l;
		this.message=m;
		this.attachment=a;
	}
}
