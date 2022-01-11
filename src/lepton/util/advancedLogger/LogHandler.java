package lepton.util.advancedLogger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class LogHandler {
	private static DateTimeFormatter formatter=DateTimeFormatter.ofPattern("HH:mm:ss");
	public static String getTimestampString() {
		return formatter.format(LocalDateTime.now());
	}
	public static String getLogString(LogEntry entry) {
		return (entry.level.prefix.isEmpty()?entry.getTimestampString()+" ":("["+entry.getTimestampString()+" "))+entry.level.prefix+(entry.level.prefix.isEmpty()?"":"]: ")+entry.message;
	}
	public boolean isUnique() {
		for(LogHandler handler : Logger.handlers) {
			if(handler.getHandlerTypeID()==this.getHandlerTypeID() && handler!=this) {
				return false;
			}
		}
		return true;
	}
	public abstract void handle(LogEntry entry);
	public final int getHandlerTypeID() {
		return this.getClass().getName().hashCode();
	}
	public void delete() {}
}
