package lepton.util.console;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import lepton.util.advancedLogger.Logger;
import lepton.util.cloneabletypes.ClBoolean;

public class ConsoleWindow {
	private static int numOpenConsoles=0;
	public static int MAX_CONSOLES=1;
	private static ConsoleWindow lastInitializedConsole=null;
	private JFrame frame=new JFrame();
	private ConsoleJTextArea textArea;
	private OnCommand onCommand;
	private OnEnterPress onClose;
	/**
	 * Technically an onEnterPress, but really just a general purpose function executor. Just use a lambda expression with no params like always.
	 */
	public void setOnCloseBehavior(OnEnterPress oc) {
		onClose=oc;
		frame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {
		        if(onClose!=null) {
		        	onClose.run();
		        }
		    }
		});
	}
	private ClBoolean interceptCloseRequest=new ClBoolean(false);
	/**
	 * if interceptCloseRequest, windows with the close() method called will do nothing.
	 * Child console windows (initialized due to console limiting) will have this state synchronized across them.
	 */
	public void setInterceptCloseRequest(boolean i) {
		interceptCloseRequest.v=i;
	}
	/**
	 * Newline automatically added. Displayed on console startup. Default value is "Command console ready"
	 */
	public String initMessage="ERROR ON NAME INITIALIZATION";
	public void println(String str) {
		if(!textArea.isConsoleInputSupressed()) {
			textArea.append(str+"\n");
			String s=textArea.getText();
			int numNewlines=(int)s.chars().filter(c->c=='\n').count();
			while(numNewlines>Math.floor(textArea.getHeight()/(float)textArea.getConsoleRowHeight())-4) {
				int idx=s.indexOf("\n");
				s=s.substring(idx+1);
				textArea.replaceRange("<FRC>", 0, idx+1);
				numNewlines--;
			}
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}
	public void onCommand() {
		if(onCommand==null) {
			return;
		}
		String s=textArea.getText();
		int idx=s.lastIndexOf("\n");
		if(s.length()>0 && idx>=0) {
			onCommand.run(s.substring(idx+1));
		}
	}
	public void setVisible(boolean v) {
		frame.setVisible(v);
	}
	public void close() {
		if(!interceptCloseRequest.v) {
			if(onClose!=null) {
				onClose.run();
			}
			setVisible(false);
			frame.dispose();
			frame=null;
			numOpenConsoles--;
		}
	}
	public boolean isClosed() {
		return frame==null;
	}
	public void waitForClose() {
		while(!isClosed()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}
	public ConsoleWindow(boolean alwaysOnTop, int w, int h, String name, OnCommand oc) {
		this(alwaysOnTop,w,h,name,oc,"Command console ready");
	}
	public ConsoleWindow(boolean alwaysOnTop, int w, int h, String name, OnCommand oc, String im) {
		if(MAX_CONSOLES<=0) {
			throw new IllegalStateException("MAX_CONSOLES is "+MAX_CONSOLES+". Make it more than 0 or you will die.");
		}
		this.initMessage=im;
		if(numOpenConsoles>=MAX_CONSOLES) {
			Logger.log(2,"Too many open consoles, redirecting output to last initialized console.");
			this.frame=lastInitializedConsole.frame;
			this.textArea=lastInitializedConsole.textArea;
			this.onCommand=(s)->{if(oc!=null) {oc.run(s);} if(lastInitializedConsole.onCommand!=null) {lastInitializedConsole.onCommand.run(s);}};
			this.onClose=()->{if(lastInitializedConsole.onClose!=null) {lastInitializedConsole.onClose.run();}};
			this.interceptCloseRequest=lastInitializedConsole.interceptCloseRequest;
			return;
		}
		onCommand=oc;
		ConsoleWindow.lastInitializedConsole=this;
		Dimension size=new Dimension(w,h);
		frame.setPreferredSize(size);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		int colA=10;
		int colB=80;
		int colFG=200;
		Color ca=new Color(colA,colA,colA);
		Color cb=new Color(colB,colB,colB);
		Color fg=new Color(colFG,colFG,colFG);
		frame.setTitle(name);
		frame.setLocation(550,400);
		
		JPanel textPanel=new JPanel();
		textArea=new ConsoleJTextArea();
		((AbstractDocument)textArea.getDocument()).setDocumentFilter(new DocumentFilter() {
			private boolean allowChange(int offset) {
				try {
					int offsetLastLine=textArea.getLineCount()==0?0:textArea.getLineStartOffset(textArea.getLineCount()-1);
					return offset>=offsetLastLine;
				} catch (BadLocationException ex) {
					throw new RuntimeException(ex);
				}
			}
			@Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
				if(allowChange(offset)) {
					super.remove(fb,offset,length);
				}
			}
			@Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				if(allowChange(offset)) {
					super.replace(fb,offset,length,text,attrs);
				} else if(text.startsWith("<FRC>")) {
					super.replace(fb,offset,length,text.substring(5),attrs);
				}
			}
			@Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				if(allowChange(offset)) {
					super.insertString(fb,offset,string,attr);
				}
			}
		});
		textArea.addKeyListener(new ConsoleKeyListener(()->this.onCommand()));
		textArea.setBounds(0,0,size.width,size.height);
		textPanel.setLayout(null);
		textPanel.setBackground(cb);
		textPanel.setForeground(fg);
		textArea.setBackground(ca);
		textArea.setForeground(fg);
		textPanel.add(textArea);
		frame.getContentPane().add(textPanel,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(false);
		textArea.append(initMessage+"\n");
		textArea.setCaretPosition(textArea.getDocument().getLength());
		frame.setAlwaysOnTop(alwaysOnTop);
		numOpenConsoles++;
	}
}