package lepton.util.advancedLogger;

/**
 * Part of the advanced logger. The advanced logger uses a system of handler classes. This one prints log entries to the console.
 * Using this system, it is relatively easy to create a handler that, for instance, puts the entries in a file instead.
 * You can have multiple handlers.
 */
public class ConsoleHandler extends LogHandler {
	@Override
	public void handle(LogEntry entry) {
		if(entry.level.botherPrinting) {
			boolean isPrefixEmpty=entry.level.prefix.equals("");
			if(!entry.level.inErrStream) {
				System.out.println((isPrefixEmpty?"":"[")+entry.level.prefix+(isPrefixEmpty?"":"]: ")+entry.message);
			} else {
				System.err.println((isPrefixEmpty?"":"[")+entry.level.prefix+(isPrefixEmpty?"":"]: ")+entry.message);
			}
		}
	}

	@Override
	public byte getHandlerTypeID() {
		return 0;
	}
}
