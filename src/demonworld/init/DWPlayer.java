package demonworld.init;

import java.io.IOException;

import demonworld.controller.MainController;

public class DWPlayer implements Runnable {
	private String username;
	private String host;
	
	public DWPlayer(String username, String host) {
		this.username = username;
		this.host = host;
	}
	
	@Override
	public void run() {
		MainController mc = new MainController(  );
		try {
			mc.initGame(username, host);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

