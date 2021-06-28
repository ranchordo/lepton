package lepton.util.advancedLogger;

public abstract class LogHandler {
	private boolean unique=true;
	private boolean cunique=false;
	public boolean isUnique() {
		if(!cunique) {
			for(LogHandler handler : Logger.handlers) {
				if(handler.getHandlerTypeID()==this.getHandlerTypeID() && handler!=this) {
					unique=false;
					break;
				}
			}
			cunique=true;
		}
		return unique;
	}
	public abstract void handle(LogEntry entry);
	public abstract byte getHandlerTypeID();
}
