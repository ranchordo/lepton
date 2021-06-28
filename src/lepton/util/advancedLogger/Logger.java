package lepton.util.advancedLogger;

import java.util.ArrayList;

import lepton.util.ArrayListDefaults;

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
	public static final LogLevel[] levels=new LogLevel[] {
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
	public static ArrayListDefaults<LogHandler> handlers=new ArrayListDefaults<LogHandler>(new ConsoleHandler());
	/**
	 * Whether to log locally or not.
	 */
	public static boolean localLog=false;
	public static void setCleanupTask(LoggerFinalCleanup lfc) {
		Logger.lfc=lfc;
	}
	public static void log(LogEntry entry) {
		if(localLog) {local.add(entry);}
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
	public static void clearLocal() {
		log(new LogEntry(logger_internal,"Clearing local logs"));
	}
}
