package com.github.ranchordo.lepton.util.advancedLogger;

/**
 * Exactly what it sounds like.
 */
public class LogEntry {
	public LogLevel level;
	public String message;
	public Exception attachment;
	private String timestamp;
	public String getTimestampString() {
		return timestamp;
	}
	private void sts() {
		timestamp=LogHandler.getTimestampString();
	}
	public LogEntry(int l, String m) {
		this.level=Logger.levels[l];
		this.message=m;
		sts();
	}
	public LogEntry(int l, String m, Exception a) {
		this.level=Logger.levels[l];
		this.message=m;
		this.attachment=a;
		sts();
	}
	public LogEntry(LogLevel l, String m) {
		this.level=l;
		this.message=m;
		sts();
	}
	public LogEntry(LogLevel l, String m, Exception a) {
		this.level=l;
		this.message=m;
		this.attachment=a;
		sts();
	}
}
