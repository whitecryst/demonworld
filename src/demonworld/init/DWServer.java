package demonworld.init;


import demonworld.server.MplayServer;

public class DWServer implements Runnable {
	private int port;
	
	public DWServer(int port) {
		
		this.port = port;
	}
	
	@Override
	public void run() {
		new MplayServer( port );
	}
	
}
