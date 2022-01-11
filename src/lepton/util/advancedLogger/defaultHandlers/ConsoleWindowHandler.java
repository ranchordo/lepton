package lepton.util.advancedLogger.defaultHandlers;

import lepton.util.advancedLogger.LogEntry;
import lepton.util.advancedLogger.LogHandler;
import lepton.util.console.ConsoleWindow;

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
