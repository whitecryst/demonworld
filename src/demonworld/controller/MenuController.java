package demonworld.controller;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import demonworld.controller.ToolBarController.ActionCommand;
import demonworld.controller.server.ControlSource;
import demonworld.init.DWPlayer;
import demonworld.model.Army;
import demonworld.model.ArmyType;
import demonworld.model.GameState;

public class MenuController implements InputHandlerInterface{
	private MainController mainControl;
	private JFileChooser fc;
	private Timer timer;
	public File autoSaveFile;

	public MenuController( MainController mainC ) {
		mainControl = mainC;
		fc = new JFileChooser();
		autoSaveFile = new File("autosave.sav");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		String ac = arg0.getActionCommand();

		// test if actionCommand is gameMode
		for( GameMode mode : GameMode.values() ) {
			if( ac.equals( mode.name() ) ) {
				// if it's a GameMode, change GameMode 
				mainControl.setGameMode( mode );
				// select ToolbarButton (if action came from other User)
				if( mainControl.mainFrame.gameModeButtons.containsKey(ac) ) {
					mainControl.mainFrame.gameModeButtons.get(ac).setSelected(true);	
				}

			}
		}

		//test if actionCommand is Armytype 
		for( ArmyType type : ArmyType.values() ) {
			if( ac.equals( type.name()) ) {
				int n = JOptionPane.showConfirmDialog(
						mainControl.mainFrame,
						"Do you really want to create new Army of type: '"+ac+"'?",
						"Change Army Type",
						JOptionPane.YES_NO_OPTION);
				if( n == 0 ) {
					mainControl.gameState.playerArmys.put( ControlSource.LOCAL_PLAYER , new Army( type ));
					mainControl.gameState.availableArmyElements = mainControl.fullArmys.get( type ).getElements();
					if( mainControl.gm_mapSetupControlLocal != null ) {
						mainControl.gm_mapSetupControlLocal.updateUnitChooserElements();
					}
				}
			}
		}

		if( ac.equals("showRules") ) {
			if (Desktop.isDesktopSupported()) {
				try {
					File myFile = new File("resources/dwrules.pdf");
					Desktop.getDesktop().open(myFile);
				} catch (Exception ex) {
					// no application registered for PDFs
				}
			}
		} else if( ac.equals("newGame")) {
			for (ControlSource playerSource : ControlSource.values()) 
			{
				mainControl.gameState.playerArmys.get(playerSource).removeAllElements();	
			}

			mainControl.setGameMode(GameMode.SETUP_ARMYS);
			mainControl.mainFrame.hexMap.refreshArmyElementPositions();
			mainControl.mainFrame.repaint();


		} else if( ac.equals("saveGame") ) {

			int returnVal = fc.showSaveDialog(mainControl.mainFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				saveGame(file);
			} else {
				//log.append("Save command cancelled by user." + newline);
			}
		} else if( ac.equals("loadGame") ) {

			int returnVal = fc.showOpenDialog(mainControl.mainFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				loadGame(file);
				mainControl.serverBridge.sendGameState( mainControl.gameState );
			} else {

			}
		} else if( ac.equals("undo") ) {
			if( mainControl.currentHistoryPointer != null ) {
				System.out.println( "try to load pointer:"+mainControl.currentHistoryPointer );
				
				
				if( mainControl.currentHistoryPointer > 0 ) {
					mainControl.currentHistoryPointer -= 1;
					loadGame(mainControl.gameStateHistory.get(mainControl.currentHistoryPointer));
					mainControl.serverBridge.sendGameState( mainControl.gameState );
				} else {
					System.out.println( "unable to undo, reached first element of history. pointer:"+mainControl.currentHistoryPointer );
				}
				System.out.println( "act pointer:"+mainControl.currentHistoryPointer );
			}
			
		}else if( ac.equals("opponent_new") ) {

			Thread player2 = new Thread(new DWPlayer("opponent", "localhost"));
			player2.start();
			
		} else if( ac.equals(ActionCommand.AI_ACTION.name()) ) {
			mainControl.aiController.doAction();
		} else if( ac.equals( ActionCommand.AI_ROLLIT.name() ) ) {
			mainControl.aiController.rollit();
		} else if( ac.equals( "tools_gridcoords" ) ) {
			if( mainControl.mainViewControl.hexPanel.drawGridCoordinates ) {
				mainControl.mainViewControl.hexPanel.drawGridCoordinates = false;
			} else {
				mainControl.mainViewControl.hexPanel.drawGridCoordinates = true;
			}
			mainControl.mainViewControl.hexPanel.repaint();
		}



	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void startAutoSave() {
		// start timer for autosave
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				saveGame(autoSaveFile);

			}}, 0l, (5l*60l*1000l)); // warte 0ms, speichere alle 5 min 

	}

	public void saveGame( File file ) {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(file)));
			out.writeObject(mainControl.gameState);
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			mainControl.toolBarControl.printMessage("Error while saving Game, see log for details");
			e.printStackTrace();
		} 

	}

	public void loadGame( File file ) {
		if( file.exists() ) {
			try {
				final ObjectInputStream in = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(file)));
				final GameState gameState = (GameState) in.readObject();
				//System.out.println("test:"+armyFromFile.getElements().size());
				loadGame(gameState);
				in.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mainControl.toolBarControl.printMessage("Error while loading Game, see log for details");
				e.printStackTrace();
			}
		}

	}

	public void loadGame( GameState gameState ) {
		mainControl.setGameMode(gameState.currentGameMode); 
		mainControl.gameState = gameState;
		mainControl.mainViewControl.hexPanel.refreshArmyElementPositions();
		// TODO: set GameMode selection in Gui
		mainControl.mainFrame.repaint();
	}

}
