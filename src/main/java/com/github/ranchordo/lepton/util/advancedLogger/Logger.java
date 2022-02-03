package com.github.ranchordo.lepton.util.advancedLogger;

import java.util.ArrayList;

import com.github.ranchordo.lepton.util.advancedLogger.defaultHandlers.ConsoleHandler;

/***
 * Contains an array of LogLevels that are used to give different integer levels their meaning.
 */
public class Logger {
	/**
	 * Cleanup task for unexpected exits.
	 */
	public static LoggerFinalCleanup lfc;
	/**
	 * For logger internal messages and errors.
	 */
	protected static final LogLevel logger_internal=new LogLevel("LOGGER_INTERNAL",true,false,false);
	/**
	 * A utility no prefix logLevel.
	 */
	public static final LogLevel no_prefix=new LogLevel("",true,false,false);
	/**
	 * 0: DEBUG
	 * 1: INFO
	 * 2: WARN
	 * 3: ERROR (no exit)
	 * 4: FATAL (system exit with code 1 on recv)
	 */
	public static LogLevel[] levels=new LogLevel[] {
			new LogLevel("DEBUG",true,false,false), //0
			new LogLevel("INFO",true,false,false), //1
			new LogLevel("WARN",true,false,false), //2
			new LogLevel("ERROR",true,true,false), //3
			new LogLevel("FATAL",true,true,true) //4
	};
	/**
	 * Local list of entries (local log).
	 */
	public static ArrayList<LogEntry> local=new ArrayList<LogEntry>();
	/**
	 * List of console handlers. public: feel free to modify.
	 */
	public static ArrayList<LogHandler> handlers;
	static {
		handlers=new ArrayList<LogHandler>();
		handlers.add(new ConsoleHandler());
	}
	public static void cleanuphandlers() {
		for(LogHandler handler : Logger.handlers) {
			handler.delete();
		}
	}
	/**
	 * Whether to log locally or not.
	 */
	public static boolean localLog=false;
	public static void setCleanupTask(LoggerFinalCleanup lfc) {
		Logger.lfc=lfc;
	}
	public static void simulateLocalLog(LogHandler handler) {
		for(LogEntry e : local) {
			handler.handle(e);
		}
	}
	public static void simulateLocalLog() {
		for(LogEntry e : local) {
			for(LogHandler handler : handlers) {
				handler.handle(e);
			}
		}
	}
	public static void log(LogEntry entry) {
		if(localLog && entry.level.keepLocalLog) {local.add(entry);}
		for(LogHandler handler : handlers) {
			handler.handle(entry);
		}
		entry.level.tryExit();
	}
	public static void log(int level, String message) {
		log(new LogEntry(level,message));
	}
	public static void log(LogLevel level, String message) {
		log(new LogEntry(level,message));
	}
	public static void log(int level, String message, Exception attachment) {
		log(new LogEntry(level,message,attachment));
	}
	public static void log(int level, Object message) {
		log(new LogEntry(level,message.toString()));
	}
	public static void log(LogLevel level, Object message) {
		log(new LogEntry(level,message.toString()));
	}
	public static void log(int level, Object message, Exception attachment) {
		log(new LogEntry(level,message.toString(),attachment));
	}
	public static void clearLocal() {
		log(new LogEntry(logger_internal,"Clearing local logs"));
	}
}
