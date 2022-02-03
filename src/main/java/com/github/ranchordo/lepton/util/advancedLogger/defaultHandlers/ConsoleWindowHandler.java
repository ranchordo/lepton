package com.github.ranchordo.lepton.util.advancedLogger.defaultHandlers;

import com.github.ranchordo.lepton.util.advancedLogger.LogEntry;
import com.github.ranchordo.lepton.util.advancedLogger.LogHandler;
import com.github.ranchordo.lepton.util.console.ConsoleWindow;

public class ConsoleWindowHandler extends LogHandler {
	ConsoleWindow consoleWindow=null;
	public ConsoleWindowHandler(ConsoleWindow c) {
		consoleWindow=c;
	}
	@Override
	public void handle(LogEntry entry) {
		if(entry.level.botherPrinting && consoleWindow!=null && isUnique()) {
			consoleWindow.println(LogHandler.getLogString(entry));
		}
	}
}
