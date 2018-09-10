package demonworld.controller;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.HexagonalGridCalculator;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;
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
import demonworld.server.PhaseState;
import demonworld.tools.WinkelApp;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class GameMode_FightRanged_MapController
implements InputHandlerInterface
{
	private HexagonPanel hexPanel;
	private CursorModel<Object> currentCursorElement;
	private Vector<CursorModel<Object>> cursorElements;
	private final String SHOOT = "Shoot";
	private final String SUPPORT = "Support";
	private final String MARK_AS_DEAD = "isDead";
	private boolean shootInProgress = false;
	private boolean setElementDirection = false;
	private boolean mouseOverElement = false;
	private ControlSource playerSource;
	private Hexagon shootFromHexagon;
	private boolean targetIsInMelee = false;
	private PhaseState phaseState;
	private JDialog dialog;

	public HexagonPanel getHexPanel()
	{
		return this.hexPanel;
	}

	public GameMode_FightRanged_MapController(HexagonPanel hexPanel, ControlSource playerSource)
	{
		this.playerSource = playerSource;
		this.hexPanel = hexPanel;

		init();
	}

	public void init()
	{
		setActionChooser(true);
		phaseState = new PhaseState(-1, null);
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
				img = ImageIO.read(new File("resources/images/arrowstorm.png"));
			}
			catch (IOException localIOException) {}
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Image scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			CursorModel<Object> newElement = new CursorModel("Shoot", toolkit.createCustomCursor(scaledImg, new Point(0, 0), "img"), null, scaledImg);
			this.cursorElements.add(newElement);

			try
			{
				img = ImageIO.read(new File("resources/images/support.png"));
			}
			catch (IOException localIOException1) {}
			toolkit = Toolkit.getDefaultToolkit();
			scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			this.cursorElements.add(new CursorModel("Support", toolkit.createCustomCursor(scaledImg, new Point(0, 0), "img"), null, scaledImg));

			try
			{
				img = ImageIO.read(new File("resources/images/skull.png"));
			}
			catch (IOException localIOException2) {}
			toolkit = Toolkit.getDefaultToolkit();
			scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
			this.cursorElements.add(new CursorModel("isDead", toolkit.createCustomCursor(scaledImg, new Point(0, 0), "img"), null, scaledImg));

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
				if ((this.currentCursorElement.name.equals("Shoot")) || (this.currentCursorElement.name.equals("Support")) || (this.shootInProgress))
				{
					if (this.shootInProgress)
					{
						int maxShootingDistance = ((SatelliteData)shootFromHexagon.getSatelliteData()).element.getMaxShootingDistance();
						int distanceToTarget = hexPanel.hexagonalGridCalculator.calculateDistanceBetween(shootFromHexagon, hex);
						
						if (data.element != null && (maxShootingDistance >= distanceToTarget))
						{
							//test if target is in melee
							targetIsInMelee = data.element.elementIsInMeleeFight(hexPanel);
							ArmyElement shooter = ((SatelliteData)shootFromHexagon.getSatelliteData()).element;
							ArmyElement target = data.element;
							boolean targetIsInMeleeWithShooter = ArmyUnit.isUnitInMelee(shooter.armyUnit.getElementPositions(), target.armyUnit.getElementPositions());
							boolean shootToEngagingEnemy = ( 
									targetIsInMeleeWithShooter && 
									shooter.armyUnit.status.order.equals(UnitOrder.HOLD) && 
									target.armyUnit.status.engagedInMeleeThisRound );
							if( targetIsInMelee && this.currentCursorElement.name.equals("Support") && !shootToEngagingEnemy) {
								JOptionPane pane = new JOptionPane("Cannot support shoot into melee", JOptionPane.INFORMATION_MESSAGE);
						        dialog = pane.createDialog(null, "Shoot into Melee");
						        dialog.setModal(false);
						        dialog.setVisible(true);
								shootInProgress = false;
								shootFromHexagon = null;
								return;
							} 

							data.setSelected(this.playerSource, true);
							this.hexPanel.setSelectedHexagon(this.playerSource, hex);
							endShooting(data);
						}
						else
						{
							System.out.println("cannot shoot to empty field");
						}
					}
					// element is on choosed field  and belongs to our army?
					else if (data.element != null && data.element.armyUnit.army == hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(ControlSource.LOCAL_PLAYER))
					{
						
						
						
						Boolean allowedToShoot = null;
						boolean notInMelee = !ArmyUnit.isUnitInMelee(data.element.armyUnit.getElementPositions(), hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(MainController.getOpponentControlSource(playerSource)).getElementPositions());
						switch (this.hexPanel.mainViewControl.mainControl.gameState.currentGameMode)
						{
						case FIGHT_RANGED_SKIRMISH: 
							// TODO test if unit is ordered
							boolean skirmishOrder = data.element.getOrder().equals(UnitOrder.SKIRMISH);
							boolean unitOrdered = data.element.armyUnit.isUnitOrderly();
							allowedToShoot = skirmishOrder && unitOrdered && notInMelee;
							break;
						case FIGHT_RANGED_HOLD: 
							// units engaged in melee are only allowed to shoot, if they are engaged in melee this round and have hold order
							boolean holdOrder = data.element.getOrder().equals(UnitOrder.HOLD);
							boolean engagedInMeleeThisRound = data.element.armyUnit.status.engagedInMeleeThisRound;
							allowedToShoot = ( holdOrder && (notInMelee || engagedInMeleeThisRound));
						}
						
						if (allowedToShoot)
						{
							data.setSelected(this.playerSource, true);
							this.hexPanel.setSelectedHexagon(this.playerSource, hex);
							this.shootFromHexagon = hex;
							beginShooting(data);
						}
					}
					else
					{
						System.out.println("empty fields cannot shoot");
					}
				}
				else if ((this.currentCursorElement.name.equals("isDead")) 
						&& (data.element != null) ) {
					// can only mark my own army elements (otherwise marker is not seen by other player as i can only send my own army via socket)
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

	public void rollIt( ArmyElement shooterElement ) {
		Random random = hexPanel.mainViewControl.mainControl.toolBarControl.random;
		if( shooterElement.elementState.aimsAt != null ) {
			// if shoot into melee, test if target is hit
			if(shooterElement.elementState.aimsAtTargetInMelee) {
				if( random.nextBoolean() ) { // 50% chance to fail
					// failed to hit target rool, which neighbour is hit
					int neighbourHit = random.nextInt(6);
					// message dialog for result
					
					// hit neighbour
					Hexagon[] neighbours = new Hexagon[6];
					hexPanel.getHexNeighbours(shooterElement.elementState.aimsAt.elementState.getMapPosition(hexPanel)).toArray(neighbours);
					ArmyElement newShotTarget = ((SatelliteData)neighbours[neighbourHit].getSatelliteData()).element;
					shooterElement.elementState.aimsAt = newShotTarget;
					hexPanel.repaint();
					String msg = "";
					if( newShotTarget == null ) {
						msg = "Shot hit an empty field";
					} else {
						msg = "Shot hit target: "+newShotTarget.unitDesignation;
					}
					JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
			        dialog = pane.createDialog(null, "Shoot into Melee: failed to hit target");
			        dialog.setModal(false);
			        dialog.setVisible(true);

			        /*new Timer(50000, new ActionListener() {
			            @Override
			            public void actionPerformed(ActionEvent e) {
			                dialog.setVisible(false);
			            }
			        }).start();*/

					
				}
			}

			// play dice sound
			hexPanel.mainViewControl.mainControl.toolBarControl.playWaveSound(hexPanel.mainViewControl.mainControl.toolBarControl.diceWave);
			// roll dice
			int diceRoll = random.nextInt(20) + 1;
			String msg = ""+diceRoll;
			//print msg and send msg to other player
			hexPanel.mainViewControl.mainControl.toolBarControl.printDiceResult(msg);
			hexPanel.mainViewControl.mainControl.serverBridge.sendMessage("Dice roll: "+msg);

			if( diceRoll >= shooterElement.getNeededDiceRoll() ) {
				hexPanel.mainViewControl.mainControl.serverBridge.sendAttackResult( 
						new AttackResult(shooterElement.elementState.aimsAt.elementState.position, false) );
				hexPanel.mainViewControl.mainControl.toolBarControl.markAsDead(shooterElement.elementState.aimsAt);
			} else {
				hexPanel.mainViewControl.mainControl.serverBridge.sendAttackResult( 
						new AttackResult(shooterElement.elementState.aimsAt.elementState.position, true) );
				hexPanel.mainViewControl.mainControl.toolBarControl.markAsSurvived(shooterElement.elementState.aimsAt);
			}
			//search for supporters, delete support
			for( ArmyElement se : shooterElement.getSupporters()) {
				se.elementState.supportAttack = null;
			} // delete attack
			shooterElement.elementState.aimsAt = null;
			shooterElement.elementState.aimsAtTargetInMelee = false;
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
		else if ((el != null) && (!this.mouseOverElement))
		{
			this.mouseOverElement = false;
			this.hexPanel.mainViewControl.showArmyElementInfo(el);
			//this.hexPanel.repaint();
		}
		else if (this.shootInProgress)
		{
			//this.hexPanel.repaint();
		}
		this.hexPanel.repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if( playerSource == ControlSource.LOCAL_PLAYER ) {
			if (!this.shootInProgress)
			{
				int offset = e.getWheelRotation();

				changeActElement(offset);
			}
		} 
		// send to other player to paint cursorsymbol
		//if( currentCursorElement != null ) {
		//  phaseState.setCursorImage( currentCursorElement.image );
		// hexPanel.mainViewControl.mainControl.serverBridge.sendPhaseState(phaseState);
		//}


	}

	private void changeActElement(int offset)
	{
		if (!this.shootInProgress)
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
			this.currentCursorElement = ((CursorModel)this.cursorElements.get(currentElementPos));
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
			if (!this.shootInProgress) {
				changeActElement(1);
			}
			break;
		case 40: 
			if (!this.shootInProgress) {
				changeActElement(-1);
			}
			break;
		case 37: 
			if (!this.shootInProgress) {
				changeActElement(0);
			}
			break;
		case 39: 
			if (!this.shootInProgress) {
				updateCursorElement(this.cursorElements.size() - 1);
			}
			break;
		case 83: 
			if (!this.shootInProgress) {
				for (CursorModel<Object> el : this.cursorElements) {
					if (el.name.equals("Shoot")) {
						updateCursorElement(this.cursorElements.indexOf(el));
					}
				}
			}
			break;
		case 27: 
			if (this.shootInProgress)
			{
				this.hexPanel.showShootingRanges.put(this.playerSource, null);
				this.hexPanel.shootInProgress.remove(this.playerSource);
				this.shootInProgress = false;
				this.hexPanel.repaint();
			}
			break;
		}
	}

	private void beginShooting(SatelliteData sourceData)
	{
		if (sourceData.element.fightingSkillLongRange != null)
		{
			this.shootInProgress = true;
			this.hexPanel.showShootingRanges.put(this.playerSource, sourceData.element.distancesForLongRangeSkill);
			if( (this.currentCursorElement.name.equals("Support")) ) {
				this.hexPanel.supportInProgress.put(this.playerSource, sourceData);
			} else {
				this.hexPanel.shootInProgress.put(this.playerSource, sourceData);  
			}

		}
	}

	private void endShooting(SatelliteData targetData)
	{
		ArmyElement shooterElement = ((SatelliteData)this.shootFromHexagon.getSatelliteData()).element;
		if (targetData.element != null)
		{
			if (this.currentCursorElement.name.equals("Shoot"))
			{
				boolean cancelShooting = false;
				if (!targetData.element.equals(shooterElement) )
				{
					// ask, before shoot into melee
					if( targetIsInMelee ) {
						// test if shooter is in melee with target
						boolean targetIsInMeleeWithShooter = ArmyUnit.isUnitInMelee(shooterElement.armyUnit.getElementPositions(), targetData.element.armyUnit.getElementPositions());
						if( !targetIsInMeleeWithShooter || !shooterElement.armyUnit.status.order.equals(UnitOrder.HOLD) || !targetData.element.armyUnit.status.engagedInMeleeThisRound){
							int result = JOptionPane.showConfirmDialog (
									hexPanel, 
									"Shoot into Melee?","Shoot into melee",JOptionPane.YES_NO_OPTION);
							if(result == JOptionPane.NO_OPTION){ 
								cancelShooting = true;
								System.out.println("cancel");
							} else if( result == JOptionPane.YES_OPTION ) {
								shooterElement.elementState.aimsAtTargetInMelee = true;
							}
						}
					}
					
					// shoot
					shooterElement.elementState.supportAttack = null;
					shooterElement.elementState.aimsAt = targetData.element;
					shooterElement.elementState.aimingDistance = this.hexPanel.hexagonalGridCalculator.calculateDistanceBetween(
							shooterElement.elementState.getMapPosition(hexPanel), 
							targetData.element.elementState.getMapPosition(hexPanel));
				}
				else
				{
					cancelShooting = true;
				}
				// cancel shooting in  progress
				if( cancelShooting ) {
					if (shooterElement.elementState.aimsAt != null)
					{
						shooterElement.elementState.aimsAt = null;
						shooterElement.elementState.aimingDistance = 0;
						shooterElement.elementState.aimsAtTargetInMelee = false;
					}
					this.shootInProgress = false;
					this.hexPanel.showShootingRanges.put(this.playerSource, null);
					this.hexPanel.shootInProgress.remove(this.playerSource);
				}
			}
			else if (this.currentCursorElement.name.equals("Support"))
			{
				for (ArmyElement supporter : shooterElement.getSupporters()) {
					supporter.elementState.supportAttack = null;
				}
				shooterElement.elementState.aimsAt = null;
				shooterElement.elementState.aimingDistance = -1;

				ArmyElement attackerToSupport = null;
				for (ArmyElement e : shooterElement.armyUnit.elements) {
					if ((e.elementState != null) && 
							(targetData.element.equals(e.elementState.aimsAt))) {
						attackerToSupport = e;
					}
				}
				if ((!targetData.element.equals(shooterElement)) && (attackerToSupport != null)) {
					shooterElement.elementState.supportAttack = attackerToSupport;
				} else if (targetData.element.equals(shooterElement)) {
					shooterElement.elementState.supportAttack = null;
				}
				this.hexPanel.supportInProgress.remove(this.playerSource);
			}
			this.hexPanel.showShootingRanges.put(this.playerSource, null);
			this.hexPanel.shootInProgress.remove(this.playerSource);
			this.shootInProgress = false;
			this.shootFromHexagon = null;
			hexPanel.mainViewControl.mainControl.updateGameStateHistory();
		}
	}
	
	

	public void keyReleased(KeyEvent e) {}

	public void endPhase()
	{
		this.setElementDirection = false;
		this.shootFromHexagon = null;
		this.shootInProgress = false;
		this.hexPanel.shootInProgress.put(this.playerSource, null);
		this.hexPanel.showShootingRanges.put(this.playerSource, null);

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
					el.elementState.aimsAt = null;
					el.elementState.aimingDistance = 0;
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
	
	public List<ArmyElement> getValidTargets(ArmyElement attacker) {
		List<ArmyElement> validTargets = new ArrayList<ArmyElement>();
		
		// get all fields within shooting angle
		Set<Hexagon> validShotingAngleHexes = getFieldsInShootingSight(attacker);

		// now test, if shootingLine is touching other elements (no sight to target)
		for( Hexagon actTargetHex : validShotingAngleHexes ) {
			SatelliteData targetData = actTargetHex.getSatelliteData();
			if( targetData.element == null ) {
				continue;
			} else if( targetData.element.armyUnit.army == attacker.armyUnit.army ) {
				continue;
			}
			
			Point2D.Double p1 = new Point2D.Double(
					attacker.elementState.getMapPosition(hexPanel).getCenterY(), 
					attacker.elementState.getMapPosition(hexPanel).getCenterX());
			
			Point2D.Double p2 = new Point2D.Double(
					targetData.element.elementState.getMapPosition(hexPanel).getCenterY(), 
					targetData.element.elementState.getMapPosition(hexPanel).getCenterX());
			
			double w = p2.x - p1.x ;
		    double h = p2.y - p1.y ;
		    double m = h/(double)w ;
		    double j = p1.y ;
		    boolean pathIsBlocked = false;
		    Hexagon lastPathHex = null;
		    Hexagon actPathHex = null;
		    for (double i=p1.x;i<=p2.x;i++) {
		    	actPathHex = hexPanel.getHexByPixelCoordinate( (int)Math.round(i), (int)Math.round(j));
		        if( actPathHex != lastPathHex ) {
		        	if( ((SatelliteData)actPathHex.getSatelliteData()).element != null ) {
		        		pathIsBlocked = true;
		        	}
		        }
		    	lastPathHex = actPathHex;
		        j += m ;
		    }
			if( !pathIsBlocked ) {
				validTargets.add( ((SatelliteData)actTargetHex.getSatelliteData()).element );
			}
		}
		return validTargets;
	}
	
	public Set<Hexagon> getFieldsInShootingSight(ArmyElement shooter) {
		Hexagon shooterPos = shooter.elementState.getMapPosition( hexPanel );
		int maxShootingDistance = shooter.distancesForLongRangeSkill[shooter.distancesForLongRangeSkill.length-1];
		
		// get all hexagon Fields witin shootmngrange
		Set<Hexagon> shootinghRangeHexes = hexPanel.hexagonalGridCalculator.calculateMovementRangeFrom(shooterPos, maxShootingDistance);
		
		// calc angle between front-sight-line (from center of shooter directly forward) to shooter-target-line 
		// (from center of shooter to center of target) this should be less or equal |30| (degree)
		
		Set<Hexagon> validShotingAngleHexes = new HashSet<Hexagon>();
		for( Hexagon actTargetHex : shootinghRangeHexes ) {
			//System.out.println( "actHex:"+actTargetHex );
			
			

			Point2D.Double p1 = new Point2D.Double(
					shooterPos.getCenterY(), 
					shooterPos.getCenterX());

			Point2D.Double p2 = new Point2D.Double(
					actTargetHex.getCenterY(), 
					actTargetHex.getCenterX());
			
			ViewDirection direction = shooter.elementState.viewDirection;

			biz.pavonis.hexameter.api.Point[] edgePoints = shooter.elementState.getMapPosition(hexPanel).getPoints();
			biz.pavonis.hexameter.api.Point viewDirectionPoint = null;
			if (direction.equals(ViewDirection.TOP)) {
				viewDirectionPoint = edgePoints[3];
			} else if (direction.equals(ViewDirection.TOP_RIGHT)) {
				viewDirectionPoint = edgePoints[2];
			} else if (direction.equals(ViewDirection.BOTTOM_RIGHT)) {
				viewDirectionPoint = edgePoints[1];
			} else if (direction.equals(ViewDirection.BOTTOM)) {
				viewDirectionPoint = edgePoints[0];
			} else if (direction.equals(ViewDirection.BOTTOM_LEFT)) {
				viewDirectionPoint = edgePoints[5];
			} else if (direction.equals(ViewDirection.TOP_LEFT)) {
				viewDirectionPoint = edgePoints[4];
			}
			Point2D.Double p3 = new Point2D.Double(
					viewDirectionPoint.y, 
					viewDirectionPoint.x);
			double winkel = WinkelApp.getWinkel(p1, p2, p3);
			//System.out.println( "winkel:"+winkel );
			if ((Math.abs(winkel) - 30 ) <= 0.01) {
				validShotingAngleHexes.add(actTargetHex);
			} else {
				continue;
			}
		}
		return validShotingAngleHexes;
	}
	
	
}
