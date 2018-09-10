


import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import javax.swing.JFrame;

import demonworld.controller.MainController;
import demonworld.init.DWPlayer;

public class Main {
	
	/**
	 * Simple sample usage of the hexameter framework.
	 * @throws IOException 
	 * @throws HeadlessException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws HeadlessException, IOException, InterruptedException {
		File configFile = new File( "resources/config.properties" );
		if( !configFile.exists() ) {
			System.err.println(" config File not found: "+configFile.getAbsolutePath());
			return;
		}
		
		Properties p = new Properties();
		p.load(new FileInputStream(configFile));
		
		String host = p.getProperty("host", "localhost");
		//String host = "localhost";
		if( args.length == 1 ) {
			host = args[0];
		}
		
		Random r = new Random();
		String username = p.getProperty("username", "user"+r.nextInt(10));
		if( args.length == 2 ) {
			username = args[1];
		} 
		
		
		/*MainController mc = new MainController(  );
		try {
			mc.initGame(username, host);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/*
		JFrame f = new JFrame();
		f.setVisible(true);
		f.setSize(100, 100);
		f.setTitle("test");
		*/

		
		// start player1
		Thread player1 = new Thread(new DWPlayer(username, host));
		player1.start();
		// start player2
		/*Thread player2 = new Thread(new DWPlayer(username, host));
		player2.start();
		*/
		
	}

}

