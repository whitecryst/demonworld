package demonworld.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import demonworld.controller.server.ControlSource;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.HexagonPanel;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyUnit;
import demonworld.tools.TextManager.ContentID;

public class ToolBarController implements InputHandlerInterface {
	MainController mainControl;
	public Random random = new Random();
	public String diceWave = "resources/sound/dice.wav";
	public boolean showMiniMap = false;
	
	public static enum ActionCommand {
		ROLL_DICE, AI_ACTION, AI_ROLLIT, AI_REMOVEKILLED
	}
	
	public ToolBarController( MainController mainC ) {
		mainControl = mainC;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if( e.getActionCommand().equals( ActionCommand.ROLL_DICE.name() ) ) {
			//PlayWave diceRollPlayer = new PlayWave( diceWave );
			//diceRollPlayer.start();
			playWaveSound(diceWave);
			rollIt();
			/*
			int diceRoll = random.nextInt(20) + 1;
			String msg = ""+diceRoll;
			printDiceResult(msg);
			mainControl.serverBridge.sendMessage("Dice roll: "+msg);*/
			
		} else if( e.getActionCommand().equals(ActionCommand.AI_ACTION.name()) ) {
			mainControl.aiController.doAction();
		} else if( e.getActionCommand().equals( ActionCommand.AI_ROLLIT.name() ) ) {
			mainControl.aiController.rollit();
		}
	}

	private void rollIt() {
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		GameMode mode = mainControl.gameState.currentGameMode; 
		
		for(Army army : mainControl.gameState.playerArmys.values()) {
			for( ArmyElement attacker : army.getElements() ) {
				if( mode.equals(GameMode.FIGHT_MEELE)) {
					if( attacker.elementState.attackAt != null ) {
						mainControl.gm_FightMeele_controllerLocal.rollIt(attacker);
					}
				} else if( mode.equals(GameMode.FIGHT_RANGED_SKIRMISH) || mode.equals(GameMode.FIGHT_RANGED_HOLD)) {
					if( attacker.elementState.aimsAt != null ) {
						mainControl.gm_FightRanged_controllerLocal.rollIt(attacker);
					}
				}
			}
		} 
		mainControl.updateGameStateHistory();
		hexPanel.repaint();
	}
		
	
	
	public void playWaveSound(String file) {
	    try {
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(file).getAbsoluteFile());
	        Clip clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        clip.start();
	    } catch(Exception ex) {
	        System.out.println("Error with playing sound.");
	        ex.printStackTrace();
	    }
	}
	
	public void printDiceResult(String msg) {
		mainControl.mainFrame.diceResultLabel.setText( " "+msg );
	}
	
	public void printMessage(String msg) {
		if( msg.contains("Dice roll:") ) {
			playWaveSound(diceWave);
		}
		LocalDateTime dt = LocalDateTime.now();
		mainControl.mainFrame.msgHistory.add( dt+" "+msg);
		mainControl.mainFrame.msgLabel.setText( " "+msg );
		String historyString = "<html>";
		for( String historicMsg : mainControl.mainFrame.msgHistory ) {
			historyString += historicMsg+"<br>";
		}
		historyString += "</html>";
		mainControl.mainFrame.msgLabel.setToolTipText(historyString);
	}
	
	public void updateArmy( Army otherPlayersArmy ) {
		// replace Color (is set for local Player on other side)
		otherPlayersArmy.armyColor = mainControl.gameState.playerColor.get(ControlSource.EXTERNAL_PLAYER);
		mainControl.gameState.playerArmys.put(ControlSource.EXTERNAL_PLAYER, otherPlayersArmy);
		mainControl.mainViewControl.hexPanel.refreshArmyElementPositions();
		//mainControl.mainFrame.armypoints.updateCurrentPoints(ControlSource.EXTERNAL_PLAYER, otherPlayersArmy);
		mainControl.mainViewControl.hexPanel.repaint();
		//printMessage( "Other players army updated:"+otherPlayersArmy.getUnits().size()+" units"+otherPlayersArmy.getElements().size()+" elements"+" color:"+otherPlayersArmy.armyColor );
		//System.out.println(otherPlayersArmy.getElements().get(0).getHtmlFormattedInfo() );
	}
	
	public void markAsDead( ArmyElement target ) {
		target.elementState.markedAsSurvived = false;
		target.elementState.markedAsDead = true;
	}
	
	public void markAsSurvived( ArmyElement target ) {
		//target.elementState.markedAsDead = true;
		target.elementState.markedAsSurvived = true;
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
		if (arg0.getComponent().getName().equals("miniMap")) {
			showMiniMap = true;
			mainControl.mainFrame.repaint();
		} else if (arg0.getComponent().getName().equals("announcements")) {
			
			mainControl.mainFrame.announcementLabel.setToolTipText( mainControl.textManager.getText(ContentID.TEST) );
			
			mainControl.mainFrame.repaint();
		}
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		showMiniMap = false;
		mainControl.mainFrame.repaint();
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
	
	
		
	   
	

}
