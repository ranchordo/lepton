package com.github.ranchordo.lepton.util.advancedLogger.defaultHandlers;

import com.github.ranchordo.lepton.util.advancedLogger.LogEntry;
import com.github.ranchordo.lepton.util.advancedLogger.LogHandler;

/**
 * Part of the advanced logger. The advanced logger uses a system of handler classes. This one prints log entries to the console.
 * Using this system, it is relatively easy to create a handler that, for instance, puts the entries in a file instead.
 * You can have multiple handlers.
 */
public class ConsoleHandler extends LogHandler {
	@Override
	public void handle(LogEntry entry) {
		if(entry.level.botherPrinting) {
			(entry.level.inErrStream?System.err:System.out).println(LogHandler.getLogString(entry));
		}
	}
}
