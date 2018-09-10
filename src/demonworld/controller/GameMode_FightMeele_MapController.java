package demonworld.controller;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.Point;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;
import demonworld.calculation.FightCalculator;
import demonworld.calculation.FightCalculator.AttackFromDirection;
import demonworld.controller.server.ControlSource;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.HexagonPanel;
import demonworld.map.SatelliteData;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyUnit;
import demonworld.model.AttackResult;
import demonworld.model.CursorModel;
import demonworld.model.ElementState;
import demonworld.model.GameState;
import demonworld.model.UnitOrder;
import demonworld.model.ViewDirection;
import demonworld.tools.WinkelApp;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;

public class GameMode_FightMeele_MapController
implements InputHandlerInterface
{
	private HexagonPanel hexPanel;
	private CursorModel<Object> currentCursorElement;
	private Vector<CursorModel<Object>> cursorElements;
	private final String ATTACK = "Attack";
	private final String SUPPORT = "Support";
	private final String MARK_AS_DEAD = "isDead";
	private boolean attackInProgress = false;
	private boolean mouseOverElement = false;
	private ControlSource playerSource;
	private Hexagon attackFromHexagon;
	

	public HexagonPanel getHexPanel()
	{
		return this.hexPanel;
	}

	public GameMode_FightMeele_MapController(HexagonPanel hexPanel, ControlSource playerSource)
	{
		this.playerSource = playerSource;
		this.hexPanel = hexPanel;
		init();
	}

	public void init()
	{
		setActionChooser(true);

		updateCursorElement(0);
		this.hexPanel.mainViewControl.defaultInfoElement = null;
		//this.hexPanel.mainViewControl.showArmyElementInfo(null);
	}

	private void setActionChooser(boolean enable)
	{
		this.cursorElements = new Vector();
		if (enable)
		{
			BufferedImage img = null;
			try
			{
				img = ImageIO.read(new File("resources/images/attackMeele.png"));
			}
			catch (IOException localIOException) {}
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			CursorModel<Object> newElement = new CursorModel<Object>("Attack", toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg);
			this.cursorElements.add(newElement);
			
			try
			{
				img = ImageIO.read(new File("resources/images/support.png"));
			}
			catch (IOException localIOException1) {}
			toolkit = Toolkit.getDefaultToolkit();
			scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			this.cursorElements.add(new CursorModel<Object>("Support", toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));
			try
			{
				img = ImageIO.read(new File("resources/images/skull.png"));
			}
			catch (IOException localIOException2) {}
			toolkit = Toolkit.getDefaultToolkit();
			scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			this.cursorElements.add(new CursorModel<Object>("isDead", toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));
			
			try
			{
				img = ImageIO.read(new File("resources/images/wuerfel.png"));
			}
			catch (IOException localIOException1) {}
			toolkit = Toolkit.getDefaultToolkit();
			scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			this.cursorElements.add(new CursorModel<Object>("RollIt", toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));

		}
	}

	public void mouseClicked(MouseEvent e)
	{
		if (playerSource == ControlSource.LOCAL_PLAYER) {
			try
			{
				Hexagon hex = this.hexPanel.getHexByPixelCoordinate(e.getY(), e.getX());
				if (hex.getSatelliteData() == null) {
					hex.setSatelliteData(new SatelliteData());
				}
				SatelliteData data = (SatelliteData)hex.getSatelliteData();
				if ((this.currentCursorElement.name.equals("Attack")) || (this.currentCursorElement.name.equals("Support")) || (this.attackInProgress))
				{
					if (this.attackInProgress)
					{
						if (data.element != null)
						{
							data.setSelected(this.playerSource, true);
							this.hexPanel.setSelectedHexagon(this.playerSource, hex);
							endAttack(data);
							
						}
						else
						{
							System.out.println("cannot Attack to empty field");
						}
					}
					else if (data.element != null)
					{
						if (!data.element.getOrder().equals(UnitOrder.MOVE))
						{
							data.setSelected(this.playerSource, true);
							this.hexPanel.setSelectedHexagon(this.playerSource, hex);
							this.attackFromHexagon = hex;
							beginAttack(data);
						}
						else
						{
							System.out.println("Units with moving order cannot Attack");
						}
					}
					else {
						System.out.println("empty fields cannot Attack");
					}
				}
				else if ((this.currentCursorElement.name.equals("isDead")) && 
						(data.element != null)) {
					if( data.element.armyUnit.army == hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(ControlSource.LOCAL_PLAYER)) {		
				          if (data.element.elementState.markedAsDead) {
				            data.element.elementState.markedAsDead = false;
				          } else {
				            data.element.elementState.markedAsDead = true;
				          }
		        	  } else {
			        	  hexPanel.mainViewControl.mainControl.toolBarControl.printMessage("Cannot mark other players elements as dead!");  
			          }
				}
				else if ((this.currentCursorElement.name.equals("RollIt"))  
					&& (data.element != null) ) {
					// element is of my army
					if (data.element.armyUnit.army == hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(ControlSource.LOCAL_PLAYER)) {
						rollIt( data.element );
					}
					
				}
				this.hexPanel.repaint();
			}
			catch (HexagonNotFoundException localHexagonNotFoundException) {}
		}
		sendArmy();
	}

	public void rollIt( ArmyElement attacker) {
		if(attacker.elementState.attackAt != null ) {
			// play dice sound
			hexPanel.mainViewControl.mainControl.toolBarControl.playWaveSound(hexPanel.mainViewControl.mainControl.toolBarControl.diceWave);
			// roll dice
			int diceRoll = hexPanel.mainViewControl.mainControl.toolBarControl.random.nextInt(20) + 1;
			String msg = ""+diceRoll;
			//print msg and send msg to other player
			hexPanel.mainViewControl.mainControl.toolBarControl.printDiceResult(msg);
			hexPanel.mainViewControl.mainControl.serverBridge.sendMessage("Dice roll: "+msg);

			if( diceRoll >= attacker.getNeededDiceRoll() ) {
				hexPanel.mainViewControl.mainControl.serverBridge.sendAttackResult( 
						new AttackResult(attacker.elementState.attackAt.elementState.position, false) );
				hexPanel.mainViewControl.mainControl.toolBarControl.markAsDead(attacker.elementState.attackAt);
			} else {
				hexPanel.mainViewControl.mainControl.serverBridge.sendAttackResult( 
						new AttackResult(attacker.elementState.attackAt.elementState.position, true) );
				hexPanel.mainViewControl.mainControl.toolBarControl.markAsSurvived(attacker.elementState.attackAt);
			}
			//search for supporters, delete support
			for( ArmyElement se : attacker.getSupporters()) {
				se.elementState.supportAttack = null;
			} // delete attack
			attacker.elementState.attackAt = null;
		} 
	}
	
	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseDragged(MouseEvent e) {}

	public void mouseMoved(MouseEvent e)
	{
		this.hexPanel.mouseCursorPos.put(this.playerSource, e.getPoint());
		ArmyElement el = null;
		try {
			el = ((SatelliteData)this.hexPanel.getHexByPixelCoordinate(e.getPoint().y, e.getPoint().x).getSatelliteData()).element;
		} catch( HexagonNotFoundException exc ) {}
		
		if ((el == null) && (this.mouseOverElement))
		{
			this.mouseOverElement = false;
			//this.hexPanel.repaint();
		}
		else if ((el != null) )
		{
			this.mouseOverElement = true;
			this.hexPanel.mainViewControl.showArmyElementInfo(el);
			//this.hexPanel.repaint();	
		}
		this.hexPanel.repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if( playerSource == ControlSource.LOCAL_PLAYER ) {
			if (!this.attackInProgress)
			{
				int offset = e.getWheelRotation();

				changeActElement(offset);
			}
		}
	}

	private void changeActElement(int offset)
	{
		if (!this.attackInProgress)
		{
			int currentElementPos = this.cursorElements.indexOf(this.currentCursorElement);
			if ((currentElementPos + offset < this.cursorElements.size()) && (currentElementPos + offset >= 0)) {
				currentElementPos += offset;
			} else if (currentElementPos + offset >= this.cursorElements.size()) {
				currentElementPos = this.cursorElements.size() % offset;
			} else if (currentElementPos + offset < 0) {
				currentElementPos = this.cursorElements.size() - 1 - this.cursorElements.size() % (offset * -1);
			}
			updateCursorElement(currentElementPos);
		}
	}

	private void updateCursorElement(int currentElementPos)
	{
		if ((currentElementPos >= 0) && (currentElementPos < this.cursorElements.size()))
		{
			this.currentCursorElement = ((CursorModel<Object>)this.cursorElements.get(currentElementPos));
			this.hexPanel.updateCursorImage(this.currentCursorElement.c);
		}
	}

	private void sendArmy() {
		// send Army to other Player
		if( playerSource == ControlSource.LOCAL_PLAYER ) {
			hexPanel.mainViewControl.mainControl.serverBridge.sendArmy(hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource));
		}
	}

	public void keyTyped(KeyEvent e) {}

	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		switch (keyCode)
		{
		case 38: 
			if (!this.attackInProgress) {
				changeActElement(1);
			}
			break;
		case 40: 
			if (!this.attackInProgress) {
				changeActElement(-1);
			}
			break;
		case 37: 
			if (!this.attackInProgress) {
				changeActElement(0);
			}
			break;
		case 39: 
			if (!this.attackInProgress) {
				updateCursorElement(this.cursorElements.size() - 1);
			}
			break;
		case 83: 
			if (!this.attackInProgress) {
				for (CursorModel<Object> el : this.cursorElements) {
					if (el.name.equals("Attack")) {
						updateCursorElement(this.cursorElements.indexOf(el));
					}
				}
			}
			break;
		case 27: 
			if (this.attackInProgress)
			{
				this.hexPanel.showShootingRanges = null;
				this.hexPanel.shootInProgress = null;
				this.attackInProgress = false;
				this.hexPanel.repaint();
			}
			break;
		}
	}

	private void beginAttack(SatelliteData sourceData)
	{
		if (sourceData.element.fightingSkillClosedCombat[ sourceData.element.elementState.equippedWeaponOffset ] > 0)
		{
			this.attackInProgress = true;
			if( this.currentCursorElement.name.equals("Support") ) {
				this.hexPanel.supportInProgress.put(this.playerSource, sourceData);
			} else {
				this.hexPanel.attackInProgress.put(this.playerSource, sourceData);
			}
		}
	}

	private void endAttack(SatelliteData targetData)
	{
		ArmyElement attacker = ((SatelliteData)this.attackFromHexagon.getSatelliteData()).element;
		if( attacker.fightingSkillClosedCombat.length > 1 ) {
			hexPanel.showWeaponChooserMenu(attacker);
		}
		if (targetData.element != null)
		{
			
			
			if (this.currentCursorElement.name.equals("Attack"))
			{
				if (attacker.attack(targetData.element, hexPanel) ) {
					this.attackInProgress = false;
					this.attackFromHexagon = null;
				}
				this.hexPanel.attackInProgress.remove(this.playerSource);
			}
			else if (this.currentCursorElement.name.equals("Support"))
			{
				for (ArmyElement supporter : attacker.getSupporters()) {
					supporter.elementState.supportAttack = null;
				}
				attacker.elementState.attackAt = null;

				ArmyElement attackerToSupport = null;
				for (ArmyElement e : attacker.armyUnit.elements) {
					if ((e.elementState != null) && 
							(targetData.element.equals(e.elementState.attackAt))) {
						attackerToSupport = e;
					}
				}
				if ((!targetData.element.equals(attacker)) && (attackerToSupport != null)) {
					attacker.elementState.supportAttack = attackerToSupport;
				} else if (targetData.element.equals(attacker)) {
					attacker.elementState.supportAttack = null;
				}
				this.hexPanel.supportInProgress.remove(this.playerSource);
			}
			
			this.attackInProgress = false;
			this.attackFromHexagon = null;
			hexPanel.mainViewControl.mainControl.updateGameStateHistory();
		}
	}

	public void keyReleased(KeyEvent e) {}

	public void endPhase()
	{
		this.attackFromHexagon = null;
		this.attackInProgress = false;
		this.hexPanel.attackInProgress.put(this.playerSource, null);

		List<ArmyElement> toDelete = new ArrayList();
		ControlSource[] arrayOfControlSource;
		int j = (arrayOfControlSource = ControlSource.values()).length;
		for (int i = 0; i < j; i++)
		{
			ControlSource source = arrayOfControlSource[i];
			for (ArmyElement el : ((Army)this.hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(source)).getElements()) {
				if (el.elementState.markedAsDead)
				{
					toDelete.add(el);
				}
				else
				{
					el.elementState.attackAt = null;
					el.elementState.supportAttack = null;
					el.elementState.markedAsSurvived = false;
				}
			}
		}
		for (ArmyElement el : toDelete)
		{
			el.armyUnit.army.removeElement(el);

			el.elementState.setMapPosition(null);
		}
		this.hexPanel.refreshArmyElementPositions();
	}

	public void actionPerformed(ActionEvent e) {}

	
}
