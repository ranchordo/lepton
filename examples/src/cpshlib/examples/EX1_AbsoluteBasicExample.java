package cpshlib.examples;

import lepton.engine.rendering.GLContextInitializer;
import lepton.util.CleanupTasks;
import lepton.util.advancedLogger.Logger;

public class EX1_AbsoluteBasicExample {
	public void Main() {
		Logger.setCleanupTask(()->CleanupTasks.cleanUp()); //Make sure that when we panic exit we clean things up properly (Not the end of the world if you forget this)
		CleanupTasks.add(()->GLContextInitializer.destroyGLContext()); //Add the main cleanup function to our cleanup routine (Not the end of the world if you forget this)
		GLContextInitializer.initializeGLContext(true,500,500,false,"Absolute basic example"); //Set up our window and context. NOTHING WILL WORK WITHOUT THIS LINE.
		//Parameters here: (Show a window, Window width, Window height, Make it fullscreen, Window title).
		
		//More interesting stuff can go here later
		
		try {
			Thread.sleep(1000); //Waits for a second
		} catch (InterruptedException e) {
			e.printStackTrace(); //In case thread is interrupted (never happens)
		}
		
		CleanupTasks.cleanUp(); //Clean up our context, we're done!
		
		//If you saw a window appear for a second, it worked!
	}
	
	
	public static void main(String[] args) { //You don't really need to think about this method.
		EX1_AbsoluteBasicExample m=new EX1_AbsoluteBasicExample(); //Get a non-static context so we can work more easily (self-instantiation)
		m.Main(); //Run the main method
	}
}
