package lepton.util.console;
import java.awt.event.KeyListener;

import javax.swing.JTextArea;

public class ConsoleJTextArea extends JTextArea {
	private static final long serialVersionUID = -3279675347906587201L;
	public ConsoleJTextArea() {
		super();
	}
	private transient ConsoleKeyListener consoleKeyListener;
	@Override public void addKeyListener(KeyListener arg0) {
		super.addKeyListener(arg0);
		if(arg0 instanceof ConsoleKeyListener) {
			consoleKeyListener=(ConsoleKeyListener)arg0;
		}
	}
	public int getConsoleRowHeight() {
		return getRowHeight();
	}
	public ConsoleKeyListener getConsoleKeyListener() {
		if(consoleKeyListener==null) {
			throw new IllegalStateException("Main.JTextArea$x: Internal ConsoleKeyListener instance is null. Please add a ConsoleKeyListener to use this method.");
		}
		return consoleKeyListener;
	}
	public boolean isConsoleInputSupressed() {
		return getConsoleKeyListener().isOutputSupressed();
	}
}
