package demonworld.server;

/*
 * Multiplayer Example - Client
 * by ROOT
 * http://blakenet.no-ip.org
 */

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;

import demonworld.model.Army;
import demonworld.model.ServerTransferPackage;

public class MplayClient extends Thread {
	private int sessid;
	private String username = "nubcake";
	private MplayServer server;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Object receiveObject;
	private ServerTransferPackage transferPacketToSend;

	// Constructor
	public MplayClient(MplayServer server, Socket socket, int sessid) {
		this.server = server;
		this.socket = socket;
		this.sessid = sessid;

		try {
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("New client created");
		} catch (IOException e) {
			System.out.println("ERROR!\nCould not create client");
			kill();
		}
	}

	public void run() {
		System.out.println("MplayClient " + sessid
				+ " threaded. Listening to incoming");
		handleIncoming();
	}

	// Handle communications from client
	private void handleIncoming() {
		while (true) {
			try {
				receiveObject = in.readObject(); 
				if (receiveObject != null) {
					interpretData(receiveObject);
				} else {
					System.err.println( "Could not send Data to Other clients, Object is null" );
				}
			} catch (Exception e) {
				System.out.println("ERROR!\nCould not read data from client -"
						+ e.getMessage());
				e.printStackTrace();
				kill();
			} 
		}
	}

	// Kill the client connection/object
	public void kill() {
		server.removeMplayClient(sessid);
	}

	// Return client ID
	public int getID() {
		return this.sessid;
	}

	// Change client ID
	public void setID(int sessid) {
		this.sessid = sessid;
	}

	// Return username
	public String getUsername() {
		return this.username;
	}

	// Change username
	public void setUsername(String username) {
		this.username = username;
	}

	// Decide what to do from input
	private void interpretData(Object obj) {
		//System.out.println("MplayClient "+ username + sessid +"> received object" );
		// sendData("Server> msg received");
		ServerTransferPackage transferPacket = new ServerTransferPackage(
				this.getID(), this.username, obj);
		server.sendToAllClients(transferPacket);
		/*
		transferPacketToSend.sourceUserId = this.getID();
		transferPacketToSend.username = this.username;
		transferPacketToSend.obj = obj;
		server.sendToAllClients(transferPacketToSend);*/
	}

	// Send data to the client
	public void sendData(ServerTransferPackage transferPacket) {
		try {
			//System.out.println( username+"> try to sendData from user:"+transferPacket.username+" - "+transferPacket.obj.getClass() );
			// use writeUnshared to force rewrite of known Objects (otherwise, Army Object will be cached with first sended version)
			out.writeUnshared(transferPacket);
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR!\nCould not deliver message to client");
			kill();
		}
	}
}