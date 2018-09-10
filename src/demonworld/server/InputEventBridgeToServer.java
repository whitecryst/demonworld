package demonworld.server;

/*
 * Multiplayer Example - Simple User Interface
 * by ROOT
 * http://blakenet.no-ip.org
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.*;

import demonworld.controller.GameMode;
import demonworld.controller.InputHandlerInterface;
import demonworld.controller.MainController;
import demonworld.controller.MainInputEventHandler;
import demonworld.controller.ToolBarController;
import demonworld.controller.server.ControlSource;
import demonworld.init.DWPlayer;
import demonworld.init.DWServer;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.SatelliteData;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.AttackResult;
import demonworld.model.GameState;
import demonworld.model.Position;
import demonworld.model.ServerTransferPackage;

public class InputEventBridgeToServer extends Thread implements InputHandlerInterface
{
 
	 Socket socket = null;
	 ObjectOutputStream outStream = null;
	 ObjectInputStream inStream = null;
	 private ArrayList<InputHandlerInterface> inputEventSubHandler = new ArrayList<InputHandlerInterface>(); // every incoming event will be published (forwarded) to this EventHandler
	 private ToolBarController toolBarController;
	 private MainController mainControl;
	 public boolean exited;
	 private String username;
	 private long eventCount=0;
	 JDialog dialog;
	 
	 public InputEventBridgeToServer(
			 String host, 
			 int port, 
			 String username,
			 ArrayList<InputHandlerInterface> subHandler, 
			 ToolBarController toolBarController, MainController mainControl) throws IOException{
		 this.toolBarController = toolBarController;
		 this.mainControl = mainControl;
		 this.username = username;
		 this.inputEventSubHandler = subHandler;
		 init(host, port, true);
	 }
	 
	 private void init(String host, int port, boolean autoStartLocalServer) throws IOException {
		boolean connectionFailed = false;
		try {
			socket = new Socket(host, port);
			outStream = new ObjectOutputStream( socket.getOutputStream() );
			inStream = new ObjectInputStream( socket.getInputStream() );
		} catch (UnknownHostException e) {
			System.out.println("InputEventBrideToServer: Unknown or unreachable host " + host + " on port " + port);
			connectionFailed = true;
		} catch (IOException e) {
			System.out.println("InputEventBrideToServer: I/O error");
			connectionFailed = true;
		}
		
		if( connectionFailed && autoStartLocalServer ) {
			System.out.println( "try to start local server..." );
			Thread server = new Thread( new DWServer( port ) );
			server.start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("connect to local server...");
			try {	
				socket = new Socket(host, port);
				outStream = new ObjectOutputStream( socket.getOutputStream() );
				inStream = new ObjectInputStream( socket.getInputStream() );
			} catch (UnknownHostException e) {
				System.out.println("InputEventBrideToServer: Unknown or unreachable host " + host 	+ " on port " + port);
				System.exit(1);
			} catch (IOException e) {
				System.out.println("InputEventBrideToServer: I/O error");
				System.exit(1);
			}
		}
		
		System.out.println( "init connection to "+host+":"+port );
		
	 }

	 @Override
	 public void run() {
		while (true) {
			
			try {
				ServerTransferPackage transferPacket = (ServerTransferPackage)inStream.readObject();
				if(transferPacket != null) {
					//System.out.println("received event from user: "+transferPacket.username+" send to:"+subHandler.getClass());
					//System.out.println( transferPacket );
					// receive InputEvent
					// receive Message	
					if( transferPacket.obj instanceof String ) {
						toolBarController.printMessage(transferPacket.username+"> "+transferPacket.obj);
					// receive other players army to ensure syncronized positions	
					} else if( transferPacket.obj instanceof Army ) {
						Army army = (Army)transferPacket.obj;
						toolBarController.updateArmy(army);
					} else if( transferPacket.obj instanceof PhaseState ) {
						receivePhaseState((PhaseState)transferPacket.obj);
					} else if( transferPacket.obj instanceof GameState ) {
						GameState state = (GameState)transferPacket.obj;
						this.receivedGameState(state);
					} else if( transferPacket.obj instanceof AttackResult ) {
						receiveAttackResult((AttackResult)transferPacket.obj);
					} else { 
						eventCount++;
						//System.out.println("eventCount:"+eventCount);
						for( InputHandlerInterface handler : inputEventSubHandler ) {
							if( transferPacket.obj instanceof InputEvent ) {
								//System.out.println( "handler:"+handler+inputEventSubHandler.size() );
								MainInputEventHandler.publishEvent(handler, (InputEvent)transferPacket.obj);
							} else if( transferPacket.obj instanceof ActionEvent ) {
								//System.out.println( "actionEvent" );
								handler.actionPerformed((ActionEvent)transferPacket.obj);
							}
						}
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println( e.getMessage() );
			}
		}
		/*outStream.close();
		inStream.close();
		socket.close();*/
	 }
	 
	 private void sendObject(Object e) {
		 try {
			 outStream.writeObject(e);
			 outStream.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	 }
	 
	public void sendMessage( String msg ) {
		try {
			outStream.writeObject(msg);
			outStream.flush();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public void sendPhaseState(PhaseState state) {
		sendObject(state);
	}
	
	public void receivePhaseState(PhaseState state) {
		((GeneralMapDrawer)mainControl.mainFrame.hexMap.mapDrawer).setCursorImage(state.getCursorImage(), ControlSource.EXTERNAL_PLAYER);
	}
	
	public void sendArmy( Army army ) {
		try {
			//System.out.println( "InputEventBrindgeToServer.sendArmy():"+army.getUnits().size()+"/"+army.getElements().size() );
			outStream.writeObject(army);
			// reset Stream to force rewrite of known Objects (otherwise, Army Object will be cached with first sended version)
			outStream.reset();
			outStream.flush();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	 
	public void sendGameState( GameState gameState ) {
		try {
			//System.out.println( "InputEventBrindgeToServer.sendArmy():"+army.getUnits().size()+"/"+army.getElements().size() );
			outStream.writeObject(gameState);
			// reset Stream to force rewrite of known Objects (otherwise, Army Object will be cached with first sended version)
			outStream.reset();
			outStream.flush();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public void receivedGameState( GameState state ) {
		
        int result = JOptionPane.showConfirmDialog (
        		mainControl.mainFrame, 
        		"Other player: Load game?","Load Game",JOptionPane.YES_NO_OPTION);

        if(result == JOptionPane.YES_OPTION){ 
    		// switch armys and Army Colors
    		Army aL = state.playerArmys.remove(ControlSource.LOCAL_PLAYER);
    		Army aE = state.playerArmys.remove(ControlSource.EXTERNAL_PLAYER);
    		state.playerArmys.put(ControlSource.LOCAL_PLAYER, aE);
    		state.playerArmys.put(ControlSource.EXTERNAL_PLAYER, aL);
    		// switch army colors
    		Color tempC = aL.armyColor;
    		aL.armyColor = aE.armyColor;
    		aE.armyColor = tempC;
    		// load Game
    		mainControl.menuControl.loadGame(state);
        }
            

	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// send Event to Server
		sendObject(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//System.out.println( "serverBride: mousClick" );
		// send Event to Server
		sendObject(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// send Event to Server
		//sendObject(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		// send Event to Server
		sendObject(e);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// send Event to Server
		sendObject(e);
	}

	public void setInputEventSubHandler( ArrayList<InputHandlerInterface> newSubHandler ){
		this.inputEventSubHandler = newSubHandler;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		sendObject(e);
		
	}

	public void sendAttackResult(AttackResult attackResult) {
		sendObject(attackResult);
	}

	
	
	private void receiveAttackResult(AttackResult attackResult ) {
		Position targetCoords = attackResult.getHexCoords();
		if( mainControl.gameState.currentGameMode == GameMode.FIGHT_MEELE || mainControl.gameState.currentGameMode == GameMode.FIGHT_RANGED_HOLD || mainControl.gameState.currentGameMode == GameMode.FIGHT_RANGED_SKIRMISH ) {
			ArmyElement el = ((SatelliteData)mainControl.mainFrame.hexMap.getHexByGridCoordinate(targetCoords.x, targetCoords.z).getSatelliteData()).element; 
			if( attackResult.isElementSurvivedAttack() == false ) {
				mainControl.toolBarControl.markAsDead(el);
			} else {
				mainControl.toolBarControl.markAsSurvived(el);
				JOptionPane pane = new JOptionPane("Attack failed", JOptionPane.INFORMATION_MESSAGE);
		        dialog = pane.createDialog(null, "Title");
		        dialog.setModal(false);
		        dialog.setVisible(true);

		        new Timer(1000, new ActionListener() {
		            @Override
		            public void actionPerformed(ActionEvent e) {
		                dialog.setVisible(false);
		            }
		        }).start();

			}
			sendArmy( mainControl.gameState.playerArmys.get(ControlSource.LOCAL_PLAYER) );
		}
	}
	
}