import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lepton.engine.rendering.GLContextInitializer;
import lepton.tests.CPSHLibSSBOTest;
import lepton.tests.ObjPoolLibTest;
import lepton.tests.TensorLibTest;
import lepton.tests.engineTest.EngineTest;
import lepton.util.CleanupTasks;
import lepton.util.advancedLogger.Logger;
import lepton.util.advancedLogger.defaultHandlers.ConsoleWindowHandler;
import lepton.util.console.ConsoleWindow;

/**
 * Generic run configuration for packaging as runnable jar: Includes the self-testing utility.
 */
public class GenericRunConfiguration {
	private static Class<?>[] configs=new Class[] {CPSHLibSSBOTest.class, ObjPoolLibTest.class, TensorLibTest.class, EngineTest.class};
	private static String[] configNames=new String[configs.length];
	private static volatile short commandIssued=-2;
	private static final String base_command="run-config";
	private static final String base_exit_command="exit";
	private static final String base_list_command="list-configs";
	private static final String base_help_command="help";
	private static ConsoleWindow cw;
	private static boolean ignoreCommands=false;
	public static void onCWClose() {
		commandIssued=-1;
	}
	public static void recvCommand(String cmd) {
		if(ignoreCommands) {return;}
		if(cmd.startsWith(base_command)) {
			String rc=cmd.substring(cmd.indexOf(" ")+1);
			boolean command=false; 
			for(short i=0;i<configNames.length;i++) {
				String configName=configNames[i];
				if(rc.equals(configName)) {
					command=true;
					Logger.log(1,"Launching run configuration "+configName+"...");
					commandIssued=i;
					break;
				}
			}
			if(!command) {
				Logger.log(3,"Unrecognized run configuration "+rc);
			}
		} else if(cmd.startsWith(base_exit_command)) {
			Logger.log(1,"Closing...");
			commandIssued=-1;
		} else if(cmd.startsWith(base_list_command)) {
			cw.println("Listing possible run configurations:");
			for(String configName : configNames) {
				cw.println("    "+configName);
			}
		} else if(cmd.startsWith(base_help_command)) {
			cw.println("Listing commands: ");
			cw.println("run-config [configuration] - Run a test configuration");
			cw.println("exit - Exit this utility");
			cw.println("list-configs - List possible run configurations");
			cw.println("help - Display this message");
		} else {
			int f=cmd.indexOf(" ");
			Logger.log(3,"Unrecognized command "+(f<0?cmd:cmd.substring(0,f)));
		}
	}
	public static void main(String[] args) {
		Logger.setCleanupTask(()->CleanupTasks.cleanUp());
		
		for(int i=0;i<configs.length;i++) {
			String fullname=configs[i].getName();
			configNames[i]=fullname.substring(fullname.lastIndexOf(".")+1);
		}
		
		cw=new ConsoleWindow(false, 800, 600, "Lepton generic test command console", GenericRunConfiguration::recvCommand, "Welcome to the lepton testing utility.");
		cw.println("Listing commands: ");
		cw.println("run-config [configuration] - Run a test configuration");
		cw.println("exit - Exit this utility");
		cw.println("list-configs - List possible run configurations");
		cw.println("help - Display this message");
		cw.setOnCloseBehavior(GenericRunConfiguration::onCWClose);
		cw.setVisible(true);
		cw.println("Listing possible run configurations:");
		for(String configName : configNames) {
			cw.println("    "+configName);
		}
		
		Logger.handlers.add(new ConsoleWindowHandler(cw));
		int originalHandlerSize=Logger.handlers.size();
		
		while(!cw.isClosed()) {
			while(commandIssued<-1) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(commandIssued==-1) {
				if(!cw.isClosed()) {
					cw.close();
				}
			}
			if(commandIssued>=0) {
				ignoreCommands=true;
				cw.setInterceptCloseRequest(true);
				Class<?> runConfig=configs[commandIssued];
				try {
					Method mainMethod=runConfig.getDeclaredMethod("main",String[].class);
					GLContextInitializer.resetGlobalState();
					mainMethod.invoke(null, (Object)(new String[] {}));
					Logger.log(Logger.no_prefix,"\n");
					Logger.log(1,"Test completed.");
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					Logger.log(3,e.toString(),e);
				}
				ignoreCommands=false;
				cw.setInterceptCloseRequest(false);
				commandIssued=-2;
			}
		}
		
		if(Logger.handlers.size()!=originalHandlerSize) {
			Logger.log(2,"Handlers list size was originally "+originalHandlerSize+" before executing test config, but now is "+Logger.handlers.size());
		}
		
		cw.waitForClose();
		System.exit(0);
	}
}
