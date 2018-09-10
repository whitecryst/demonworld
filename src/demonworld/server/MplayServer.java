package demonworld.server;

/*
 * Multiplayer Example - Server
 * by ROOT
 * http://blakenet.no-ip.org
 */

import java.util.*;
import java.io.*;
import java.net.*;

import demonworld.model.ServerTransferPackage;

public class MplayServer{
	private Map<Integer, MplayClient> clients;
	private int clientCount;
	private ServerSocket server;
	
	

	// Server constructor
	public MplayServer(int port) {
		clients = new HashMap<Integer, MplayClient>();
		clientCount = 0;

		try {

			

			System.out.print("Starting server on port " + port + "...");
			server = new ServerSocket(port);
			server.setSoTimeout(0);
			System.out.println("Done!");
			System.out.println("Now handling incoming communications");
			handleIncoming();
		} catch (IOException e) {
			System.out
					.println("ERROR!\nCould not start server. Shutting down...");
			kill();
		}
	}


	// Handle incoming communications
	private void handleIncoming() {
		try {
			while (true) {

				Socket socket = server.accept();
				socket.setSoTimeout(0);
				clientCount++;
				MplayClient client = new MplayClient(this, socket, clientCount);
				client.setUsername("User" + clientCount);
				clients.put(clientCount, client);
				System.out.println("MplayClient " + clientCount + " joined");
				client.start();
			}
		} catch (Exception e) {
			System.out
					.println("ERROR!\nFailed to handle incoming communication. Shutting down...");
			kill();
		}
	}

	// Entry point
	public static void main(String[] args) {
		System.out.println("Multiplayer Server\nhttp://blakenet.no-ip.org/\n");
		if (args.length == 1) {
			MplayServer mplayserver = new MplayServer(Integer.parseInt(args[0]));
		} else {
			System.out.println("Usage: java MplayServer port");
		}
	}

	// Shut down the server
	private void kill() {
		try {
			for (Object i : clients.values()) {
				((MplayClient) i).kill();
			}
		} catch (Exception e) {
			System.out.println("WARNING! Server could not be killed reliably");
		}
	}

	public void sendToAllClients(ServerTransferPackage transferPacket) {

		for (Integer clientNr : clients.keySet()) {
			//System.out.println( "sendToAllClients clientNr:"+clientNr+" packetUserId:"+transferPacket.sourceUserId );
			if (transferPacket.sourceUserId != clientNr) {
				//System.out.println("send");
				clients.get(clientNr).sendData(transferPacket);
			}
		}
	}

	// Remove a client
	public void removeMplayClient(int cid) {
		System.out.println("MplayClient "
				+ ((MplayClient) clients.get(cid)).getUsername() + "(" + cid
				+ ") removed");
		clients.remove(cid);
	}
	
	public List<String> getConnectedPlayers() {
		ArrayList<String> playerNames = new ArrayList<String>();
		for( MplayClient client : clients.values() ) {
			playerNames.add( client.getUsername() );
		}
		return playerNames;
	}
	
	
}