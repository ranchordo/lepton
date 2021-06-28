package lepton.util.console;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ConsoleKeyListener implements KeyListener {
	private OnEnterPress onEnter;
	private boolean supressOutput;
	public boolean isOutputSupressed() {
		return supressOutput;
	}
	public ConsoleKeyListener(OnEnterPress oe) {
		onEnter=oe;
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_ENTER) {
			onEnter.run();
		} if(arg0.getKeyCode()==KeyEvent.VK_BACK_QUOTE) {
			supressOutput=true;
		}
	}
	@Override public void keyTyped(KeyEvent arg0) {
		char c=arg0.getKeyChar();
		if(c=='`') {
			arg0.consume();
		}
	}
	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_BACK_QUOTE) {
			supressOutput=false;
		}
	}

}
