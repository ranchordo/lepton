package lepton.util.advancedLogger;

import lepton.util.console.ConsoleWindow;

public class ConsoleWindowHandler extends LogHandler {
	ConsoleWindow consoleWindow=null;
	public ConsoleWindowHandler(ConsoleWindow c) {
		consoleWindow=c;
	}
	@Override
	public void handle(LogEntry entry) {
		if(entry.level.botherPrinting && consoleWindow!=null && isUnique()) {
			boolean isPrefixEmpty=entry.level.prefix.equals("");
			consoleWindow.println((isPrefixEmpty?"":"[")+entry.level.prefix+(isPrefixEmpty?"":"]: ")+entry.message);
		}
	}
	@Override
	public byte getHandlerTypeID() {
		return 1;
	}

}
