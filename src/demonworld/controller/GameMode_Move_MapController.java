package demonworld.controller;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;
import demonworld.controller.server.ControlSource;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.HexagonPanel;
import demonworld.map.SatelliteData;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyType;
import demonworld.model.ArmyUnit;
import demonworld.model.CursorModel;
import demonworld.model.ElementState;
import demonworld.model.GameState;
import demonworld.model.Position;
import demonworld.model.UnitOrder;
import demonworld.model.ViewDirection;
import demonworld.tools.WinkelApp;

public class GameMode_Move_MapController implements  InputHandlerInterface {
	private HexagonPanel hexPanel;
	//private ArmyElement currentElement; // current choosed element to add to map
	//private Vector<ArmyElement> elementList; // all elements from both armys
	private Vector<CursorModel<Object>> cursorElements;
	public CursorModel<Object> currentCursorElement;
	
	private final String MOVE_UNIT = "MoveUnit";
	public final String MOVE_ELEMENT = "MoveElement";
	private final String TURN_UNIT = "TurnUnit";
	private final String ROTATE_ELEMENT = "RotateElement";
	private final String ROTATE_ALL_ELEMENTS = "RotateAll";
	private final String DELETE = "Delete";
	private boolean moveInProgress = false;
	private boolean unitTurnInProgress = false;
	private boolean setElementDirection = false;
	ArrayList<ArmyElement> elementsToRotate = new ArrayList<ArmyElement>();
	private ControlSource playerSource;
	private Hexagon moveFromHexagon;
	private Hexagon turnFromHexagon;
	/*
	 * for current move unit, save all neighbours (all unit members) and the offset of their Grid(!) position regarding to the moving unit
	 */
	private HashMap<ArmyElement, int[]> elementGridPos = new HashMap<ArmyElement, int[]>();
	private boolean mouseOverElement;

	
	public HexagonPanel getHexPanel() {
		return hexPanel;
	}


	public GameMode_Move_MapController(HexagonPanel hexPanel, ControlSource playerSource) {
		this.playerSource = playerSource;
		this.hexPanel = hexPanel;
		//currentElement = gameModel.availableArmyElements.get(0);
		init();
		
	}
	
	public void init() {
		//currentElement = gameModel.availableArmyElements.get(0);
		setActionChooser(true);
		// invoke repaint of cursor (necessary after gameMode switch)
		updateCursorElement( 0 );
		//init available move and maneuver points of all units
		for( ArmyUnit unit : hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource).getUnits() ) {
			unit.status.usedMovePoints = 0;
			unit.status.usedManeuverPoints = 0;
		}
	}

	private void setActionChooser( boolean enable ) {
		
		this.cursorElements = new Vector<CursorModel<Object>>();
		if( enable ) {
		      BufferedImage img = null;
		      try
		      {
		        img = ImageIO.read(new File("resources/images/unitMove.png"));
		      }
		      catch (IOException localIOException) {}
		      Toolkit toolkit = Toolkit.getDefaultToolkit();
		      Image scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
		      CursorModel<Object> newElement = new CursorModel<Object>(MOVE_UNIT, toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg);
		      this.cursorElements.add(newElement);

			// Add Move_action element
		      try
		      {
		        img = ImageIO.read(new File("resources/images/elementMove.png"));
		      }
		      catch (IOException localIOException) {}
		      scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
		      this.cursorElements.add(new CursorModel<Object>(MOVE_ELEMENT, toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));

			// Add Turn_action All_Elements
		      try
		      {
		        img = ImageIO.read(new File("resources/images/turnAllElements.png"));
		      }
		      catch (IOException localIOException) {}
		      scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
		      this.cursorElements.add(new CursorModel<Object>(TURN_UNIT, toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));

			// Add Turn_action element 
		      try
		      {
		        img = ImageIO.read(new File("resources/images/turn.png"));
		      }
		      catch (IOException localIOException) {}
		      scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
		      this.cursorElements.add(new CursorModel<Object>(ROTATE_ELEMENT, toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));

			// Add Turn_action element
		      try
		      {
		        img = ImageIO.read(new File("resources/images/rotateAll.png"));
		      }
		      catch (IOException localIOException) {}
		      scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
		      this.cursorElements.add(new CursorModel<Object>(ROTATE_ALL_ELEMENTS, toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));

			// Add Delete_action element (e.g. in skirmish_ranged attack phase, we need to delete elements and move other elements one step)
		      try
		      {
		        img = ImageIO.read(new File("resources/images/delete.png"));
		      }
		      catch (IOException localIOException) {}
		      scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(this.hexPanel.getCursorOverlay(img), 0);
		      this.cursorElements.add(new CursorModel<Object>(DELETE, toolkit.createCustomCursor(scaledImg, new java.awt.Point(0, 0), "img"), null, scaledImg));

		} else {
			
		}
	}
	
	private void sendArmy() {
		  // send Army to other Player
		  if( playerSource == ControlSource.LOCAL_PLAYER ) {
			  hexPanel.mainViewControl.mainControl.serverBridge.sendArmy(hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource));
		  }
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//System.out.println("1moveInProgress: "+moveInProgress);
		if( setElementDirection == true) {
			setElementDirection = false;
			elementsToRotate.clear();
			hexPanel.mainViewControl.mainControl.updateGameStateHistory();
			sendArmy(); // send Army to Other Player to avoid asynchronity
		} else {
			 
			//mouseClicked(e);
			try {
				Hexagon hex = hexPanel.getHexByPixelCoordinate(e.getY(), e.getX());
				
				/*if( hex.getSatelliteData() == null ) {
					hex.setSatelliteData( new SatelliteData() );
				}*/
				SatelliteData data = ((SatelliteData)hex.getSatelliteData());
				data.setSelected(playerSource, true);
				hexPanel.setSelectedHexagon( playerSource, hex );
				
				if( currentCursorElement.name.equals( MOVE_UNIT ) ) {
					if( moveInProgress ) {
						//System.out.println( "prepare unitMoveEnd" );
						ArmyElement moveElement = ((SatelliteData)moveFromHexagon.getSatelliteData()).element;
						moveUnit(moveElement, hex);	
						moveInProgress = false;
						hexPanel.showMovementRange.put(playerSource, null);
						// is used to draw line to targetPos
						if( playerSource.equals(ControlSource.LOCAL_PLAYER) ) {
							hexPanel.moveInProgress.remove(playerSource);
						}
						hexPanel.mainViewControl.mainControl.updateGameStateHistory();
						sendArmy(); // send Army to Other Player to avoid asynchronity
					} else {
						if( data.element != null ) {
							//System.out.println( "prepare unitMove" );
							moveFromHexagon = hex;
							beginMovement(data);
						} else {
							System.out.println("cannot move empty field");
						}
					}
	            } else if( currentCursorElement.name.equals( TURN_UNIT ) ) {
					if( unitTurnInProgress ) {
						//System.out.println( "prepare unitMoveEnd" );
						endUnitTurn();
						hexPanel.mainViewControl.mainControl.updateGameStateHistory();
						sendArmy(); // send Army to Other Player to avoid asynchronity
					} else {
						if( data.element != null ) {
							turnFromHexagon = hex;
							hexPanel.unitTurnInProgress_showTurnBase = hex;
							unitTurnInProgress = true;
							//beginUnitTurn(hex);
						} else {
							System.out.println("cannot Turn empty field");
						}
					}
	            } else if( currentCursorElement.name.equals( MOVE_ELEMENT ) ) {			
					
	            		if( moveInProgress ) {
						endMovement(hex);
						sendArmy(); // send Army to Other Player to avoid asynchronity
					} else {
						if( data.element != null ) {
							moveFromHexagon = hex;
							beginMovement(data);
						} else {
							System.out.println("cannot move empty field");
						}
					}
				} else if( currentCursorElement.name.equals( ROTATE_ELEMENT ) ) {
					if( data.element != null ) {
						moveFromHexagon = hex;
						setElementDirection = true;
						elementsToRotate.add(data.element);
					}
				} else if( currentCursorElement.name.equals( ROTATE_ALL_ELEMENTS ) ) {
					if( data.element != null ) {
						moveFromHexagon = hex;
						setElementDirection = true;
						elementsToRotate.addAll(data.element.armyUnit.elements);
					}
				}else if( currentCursorElement.name.equals( DELETE ) ) {
					// map field have to contain a ArmyElement
					if( data.element != null ) {
						int n = JOptionPane.showConfirmDialog(hexPanel,
							    "Delete element '"+data.element.unitDesignation+"'? ",
							    "Confirm delete",
							    JOptionPane.YES_NO_OPTION,
							    JOptionPane.QUESTION_MESSAGE
							    );
						if( n != 0) { return;}

						// if anyone aims or attack this unit, reset these settings
						// first remove all supports for units, that attack or shooting at the unit to delete
						for( ArmyElement otherEl : hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource).getElements() ) {
							if( otherEl.elementState.supportAttack != null) {
								if( otherEl.elementState.supportAttack.elementState.aimsAt == data.element 
										|| otherEl.elementState.supportAttack.elementState.attackAt == data.element ) {
									otherEl.elementState.supportAttack = null;
								}
							}
						}
						// reset attacks or shootings at the unit to delete
						for( ControlSource source : ControlSource.values() ) {
							for( ArmyElement otherEl : hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(source).getElements() ) {
								if( otherEl.elementState.aimsAt == data.element ) {
									otherEl.elementState.aimsAt = null;
									otherEl.elementState.aimingDistance = 0;
								} else if(otherEl.elementState.attackAt == data.element ) {
									otherEl.elementState.attackAt = null;
								} 
							}
						}
						// delete from army
						hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource).removeElement(data.element);
						hexPanel.refreshArmyElementPositions();
						
						// delete from hexagon
						data.element = null;
						hexPanel.mainViewControl.mainControl.updateGameStateHistory();
						sendArmy(); // send Army to Other Player to avoid asynchronity
					}
				}
				
				 
				hexPanel.repaint();
				//hexPanel.paint( hexPanel.getGraphics() );
			} catch (HexagonNotFoundException err) {
				System.err.println( "Hexagon not found: "+err.getMessage() );
			}
		}	
		//System.out.println("2moveInProgress: "+moveInProgress);
		//System.out.println( "mouse clicked" );
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
//		System.out.println( "mousepressed" );
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		
		// update cursorPos
		hexPanel.mouseCursorPos.put(playerSource, e.getPoint());
		
        if( setElementDirection ) {
			double angle = Math.atan2(e.getY() - moveFromHexagon.getCenterX(), moveFromHexagon.getCenterY() - e.getX());
			
			//double det = ((hexPanel.getSelectedHexagon().getCenterX()*e.getY()) - ((hexPanel.getSelectedHexagon().getCenterY()*e.getX()));
			//double angle = Math.toDegrees(Math.atan2(det, dot));
			for( ArmyElement el : elementsToRotate ) {
				ElementState state = el.elementState;
				
				if(angle >= 0.1 && angle < 0.9){
					state.viewDirection = ViewDirection.BOTTOM_LEFT;
				} else if(angle >= 0.9 && angle < 2.05){
					state.viewDirection = ViewDirection.BOTTOM;
				} else if( (angle >= -3.25 && angle < -3.00) || (angle >= 2.05 && angle < 3.50) ){
					state.viewDirection = ViewDirection.BOTTOM_RIGHT;
				} else if( angle >= -3.00 && angle < -1.95 ){
					state.viewDirection = ViewDirection.TOP_RIGHT;
				} else if( angle >= -1.95 && angle < -1.0 ){
					state.viewDirection = ViewDirection.TOP;
				} else if( angle >= -1.0 && angle < 0.1 ){
					state.viewDirection = ViewDirection.TOP_LEFT;
				};
			}
			//System.out.println( movedirection+angle );
			
			//hexPanel.repaint();

        } else {
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
        }
        this.hexPanel.repaint();
	}



	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		
		//System.out.println( "wheel before: "+currentElement.unitDesignation+" "+currentElementPos );
		if( unitTurnInProgress ) {
			int offset = e.getWheelRotation(); 
			//System.out.println( "offset"+offset );
			ArmyElement turnBaseElement = ((SatelliteData)turnFromHexagon.getSatelliteData()).element;
			boolean turnIsValid = true;
			Set<Hexagon> marchThroughFields = getMarchThroughFieldsDuringUnitTurn(
					turnBaseElement.armyUnit.getElementPositions(), 
					turnBaseElement.elementState.position, 
					turnBaseElement.elementState.viewDirection, 
					offset);
			
			for( Hexagon actHex : marchThroughFields ) {
				ArmyElement actHexEl = null;
				try {
					actHexEl = ((SatelliteData)actHex.getSatelliteData()).element;
				}catch( HexagonNotFoundException ex ) {
					turnIsValid = false; // hexagon may be off grid
				}
				if( actHexEl != null ) {
					if( !actHexEl.armyUnit.equals(turnBaseElement.armyUnit) ) { 
						// if a targetfield contains element of other unit, dont use this move
						turnIsValid = false;
					}
				}
			}
			if( turnIsValid ) {
				turnUnit(turnBaseElement, offset);	
			} else {
				JOptionPane.showMessageDialog(hexPanel, "movement is invalid (non-empty fields or out of grid)");
			}
			
		} else if( !moveInProgress ) {
			if( playerSource == ControlSource.LOCAL_PLAYER ) {
				int offset = e.getWheelRotation(); 
				//System.out.println( "offset"+offset );
				changeActElement(offset);
			}
		} 

	}

	private void changeActElement(int offset) {
		int currentElementPos = this.cursorElements.indexOf(this.currentCursorElement);
	      if ((currentElementPos + offset < this.cursorElements.size()) && (currentElementPos + offset >= 0)) {
	        currentElementPos += offset;
	      } else if (currentElementPos + offset >= this.cursorElements.size()) {
	        currentElementPos = this.cursorElements.size() % offset;
	      } else if (currentElementPos + offset < 0) {
	        currentElementPos = this.cursorElements.size() - 1 - this.cursorElements.size() % (offset * -1);
	      }
	      updateCursorElement(currentElementPos);
		//System.out.println( "wheel after: "+currentElement.unitDesignation+" "+currentElementPos );	
	}

	private void updateCursorElement(int currentElementPos)
	  {
	    if ((currentElementPos >= 0) && (currentElementPos < this.cursorElements.size()))
	    {
	      this.currentCursorElement = ((CursorModel<Object>)this.cursorElements.get(currentElementPos));
	      this.hexPanel.updateCursorImage(this.currentCursorElement.c);
	    }
	  }

	@Override
	public void keyTyped(KeyEvent e) {

		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
	    switch( keyCode ) { 
	        case KeyEvent.VK_UP:
	        	if( !moveInProgress && !unitTurnInProgress ) {
	        		changeActElement(1);	
	        	}
	            break;
	        case KeyEvent.VK_DOWN:
	        	if( !moveInProgress && !unitTurnInProgress ) {
	        		changeActElement(-1);	
	        	}
	            break;
	        case KeyEvent.VK_LEFT:
	        	if( unitTurnInProgress ) {
	        		ArmyElement turnBaseElement = ((SatelliteData)turnFromHexagon.getSatelliteData()).element;
	        		turnUnit(turnBaseElement, -1);
	        		sendArmy(); // send Army to Other Player to avoid asynchronity
	        	} else if( !moveInProgress ) {
	        		changeActElement(0);	
	        	}
	            break;
	        case KeyEvent.VK_RIGHT :
	        	// Last element
	        	if( unitTurnInProgress ) {
	        		ArmyElement turnBaseElement = ((SatelliteData)turnFromHexagon.getSatelliteData()).element;
	        		turnUnit(turnBaseElement, 1);
	        		sendArmy(); // send Army to Other Player to avoid asynchronity
	        	} else if( !moveInProgress ) {
	        		updateCursorElement(this.cursorElements.size() - 1);
	        	}
	        	break;
	        case KeyEvent.VK_M :
	        	
	        	break;
	        case KeyEvent.VK_ESCAPE :
	        	if( moveInProgress ) {
	        		moveInProgress = false;
	        		hexPanel.showMovementRange.put(playerSource, null);
	        	}
	        	
	        	break;
	            
	     }
	
	}

	private void beginMovement(SatelliteData sourceData) {
		moveInProgress = true;
		int movePoints = sourceData.element.armyUnit.getBaseMovePoints();
		
		hexPanel.showMovementRange.put(playerSource, new Integer( Math.round(movePoints/3)) );
		// is used to draw line to targetPos
		if( playerSource.equals(ControlSource.LOCAL_PLAYER) ) {
			hexPanel.moveInProgress.put(playerSource, sourceData);
		}
	}
	
	
	private void endMovement(Hexagon pos){
		SatelliteData targetData = pos.getSatelliteData();
		ArmyElement moveElement = ((SatelliteData)moveFromHexagon.getSatelliteData()).element;
		if( targetData.element == null) { // zielfeld muss leer sein
			// add element to new Position
			moveElement.elementState.setMapPosition(pos);
			moveInProgress = false;
			hexPanel.showMovementRange.put(playerSource, null);
			// is used to draw line to targetPos
			if( playerSource.equals(ControlSource.LOCAL_PLAYER) ) {
				hexPanel.moveInProgress.remove(playerSource);
			}
			// refresh Map
			hexPanel.refreshArmyElementPositions();
		} else if( targetData.element.equals(moveElement) ){ // end movement (stay in position)
			moveInProgress = false;
			hexPanel.showMovementRange.put(playerSource, null);
			// is used to draw line to targetPos
			if( playerSource.equals(ControlSource.LOCAL_PLAYER) ) {
				hexPanel.moveInProgress.remove(playerSource);
			}
			  
		}
	}
	
	public boolean moveUnit(ArmyElement moveElement, Hexagon pos) {
		//System.out.println( "prepare unitMove" );
		SatelliteData targetData = pos.getSatelliteData();

		// save the grid pos of all unit elements
		elementGridPos.clear();
		for( ArmyElement el : moveElement.armyUnit.elements ) {
				int xPos = el.elementState.position.x; 
				int zPos = el.elementState.position.z;
				elementGridPos.put(el, new int[]{xPos, zPos});		
				//System.out.println("save offset of "+el.unitDesignation);
		}
		
		// zielfeld muss leer sein oder von eigener Einheit belegt (z.B ein Feld zurüch bei EInheiten mit mehreren reihen)
		if( targetData.element == null 
				|| (targetData.element.armyUnit == moveElement.armyUnit && !targetData.element.equals(moveElement))) { 
			// calc offset to new gridposition
			//System.out.println( "sourcePos"+moveFromHexagon.getGridX()+"/"+moveFromHexagon.getGridZ() );
			//System.out.println( "targetPos"+pos.getGridX()+"/"+pos.getGridZ() );
			int xOffset  = elementGridPos.get(moveElement)[0] - pos.getGridX(); 
			int zOffset  = elementGridPos.get(moveElement)[1] - pos.getGridZ(); 
			
			// for every element to move, save reference of this el and new offset (otherwise, a element in second row may move first and overwrite another element)
			for( ArmyElement el : elementGridPos.keySet() ) {
				//getTargetHexagon
				//System.out.println( "gridOffset:"+xOffset+"/"+zOffset );
				int newX = el.elementState.position.x - xOffset;
				int newZ = el.elementState.position.z - zOffset;
				//System.out.println( "newPos"+newX+"/"+newZ );
				Hexagon targetHex = hexPanel.getHexByGridCoordinate(newX, newZ);
				
				/*if( targetHex.getSatelliteData() == null ) targetHex.setSatelliteData(new SatelliteData());*/
				// add element to new Position
				el.elementState.setMapPosition(targetHex);
				// save new pos of armyElement in HexMap
				hexPanel.refreshArmyElementPositions();
				hexPanel.mainViewControl.mainControl.updateGameStateHistory();
			}
			
			
		} else if( targetData.element.equals(moveElement) ){ // end movement (stay in position)
			moveInProgress = false;
			hexPanel.showMovementRange.put(playerSource, null);
			// is used to draw line to targetPos
			if( playerSource.equals(ControlSource.LOCAL_PLAYER) ) {
				hexPanel.moveInProgress.remove(playerSource);
			}
		} else {
			return false; //invalid move
		}
		elementGridPos.clear();
		return true;
	}
	
	public boolean turnUnit(ArmyElement turnBaseElement, int turnOffset ) {
		
		for( int x = 0; x< Math.abs(turnOffset); x++ ) {
			HashMap<ArmyElement, Hexagon> newPositions = getNewElementPositionAfterUnitTurn(turnBaseElement, turnOffset);

			// if no error (e.g out of grid) move elements
			if( newPositions != null ) {
				if( turnOffset > 0 ) {
					turnBaseElement.rotateRight();
				} else {
					turnBaseElement.rotateLeft();
				}

				for( ArmyElement el : newPositions.keySet() ) {
					el.elementState.viewDirection = turnBaseElement.elementState.viewDirection;
					
					el.elementState.setMapPosition(newPositions.get(el));
					// save new pos of armyElement in HexMap
					hexPanel.refreshArmyElementPositions();
				}
			} else {
				return false;
			}

			hexPanel.repaint();
		}
		hexPanel.mainViewControl.mainControl.updateGameStateHistory();
		return true;
	}
	
	private HashMap<ArmyElement, Hexagon> getNewElementPositionAfterUnitTurn(ArmyElement turnBaseElement, int turnOffset) {
		//System.out.println( "prepare unitTurn" );
		
		// save the grid pos offset to the turnBaseHexgon for all unit elements
		for( ArmyElement el : turnBaseElement.armyUnit.elements ) {
			int xPos = el.elementState.position.x; 
			int yPos = el.elementState.position.y; 
			int zPos = el.elementState.position.z;
			// calc offset to turnBaseElement
			int xOffset  = turnBaseElement.elementState.position.x - xPos;
			int yOffset  = turnBaseElement.elementState.position.y - yPos;
			int zOffset  = turnBaseElement.elementState.position.z - zPos; 
			
			elementGridPos.put(el, new int[]{xOffset,yOffset, zOffset});		
		}
		
		// for every element to move, save reference of this el and new offset (otherwise, a element in second row may move first and overwrite another element)
		HashMap<ArmyElement, Hexagon> newPositions = new HashMap<ArmyElement, Hexagon>();
		boolean invalidMove = false;
		for( ArmyElement el : elementGridPos.keySet() ) {
			//if( el != turnBaseElement ) {
				//xx, yy, zz = -zz, -xx, -yy
			     //xx, yy, zz = -yy, -zz, -xx
				//System.out.println( "gridOffset:"+xOffset+"/"+zOffset );
				int elPosX = el.elementState.position.x;
				int elPosZ = el.elementState.position.z;
				// rotate by turning the vector (offset) coordinates
				int newX, newZ;
				if( turnOffset > 0 ) {
					newX = elPosX + (-1 * elementGridPos.get(el)[2]); // xOffset = -zOffset
					newZ = elPosZ + (-1 * elementGridPos.get(el)[1]); // zOffset = -yOffset
				} else {
					newX = elPosX + (-1 * elementGridPos.get(el)[1]); // xOffset = -yOffset
					newZ = elPosZ + (-1 * elementGridPos.get(el)[0]); // zOffset = -xOffset
				}
				//System.out.println( "newPos"+newX+"/"+newZ );
				try { // pos might be off the Grid
					Hexagon targetHex = hexPanel.getHexByGridCoordinate(newX, newZ);
					if( ((SatelliteData)targetHex.getSatelliteData()).element != null ) {
						if( ((SatelliteData)targetHex.getSatelliteData()).element.armyUnit != turnBaseElement.armyUnit ) {
							invalidMove = true;
						}
					} 
					newPositions.put(el, targetHex);
					// add element to new Position
				} catch( HexagonNotFoundException e ) {
					invalidMove = true;
				}
			//}
		}
		if( !invalidMove ) {
			return newPositions;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param elementPositions
	 * @param baseElementPosition
	 * @param turnOffset -1 or 1
	 * @return
	 */
	public Position[] getNewElementPositionAfterUnitTurn(Position[] elementPositions, Position baseElementPosition, int turnOffset, int turnAmount) {
		//System.out.println( "prepare unitTurn" );
		
		// save the grid pos offset to the turnBaseHexgon for all unit elements
		Position[] startPositions = elementPositions;
		
 		Position[] newPositions = new Position[startPositions.length];
		for( int n=0; n<turnAmount; n++ ) { // for 120 degree turn, we need to apply this twice
			for( int i=0; i<startPositions.length; i++ ) {
				// calc offset to turnBaseElement
				Position actPos = startPositions[i];
				Position actOffset = baseElementPosition.getOffsetTo( actPos );
				
				int actPosX = actPos.x;
				int actPosZ = actPos.z;
				// rotate by turning the vector (offset) coordinates
				int newX, newZ;
				if( turnOffset > 0 ) {
					newX = actPosX + (-1 * actOffset.z); // xOffset = -zOffset
					newZ = actPosZ + (-1 * actOffset.y); // zOffset = -yOffset
				} else {
					newX = actPosX + (-1 * actOffset.y); // xOffset = -yOffset
					newZ = actPosZ + (-1 * actOffset.x); // zOffset = -xOffset
				}
				int newY = -1 * (newX + newZ);
				newPositions[i] = new Position(newX, newY, newZ);
			}
			
			// prepare for next turn
			startPositions = newPositions;
		}
		
		return newPositions;
	}
	
	/**
	 * returns all fields that are involved in a unitturn (including the start- and Endposition of unit)
	 * @param shooter
	 * @return
	 */
	public Set<Hexagon> getMarchThroughFieldsDuringUnitTurn(
			Position[] elementPositions, 
			Position turnBasePosition, 
			ViewDirection viewDirection, 
			int turnOffset) throws HexagonNotFoundException{
		
		
		// find out, the width of the unit
		
		// get neighbour fields of turnbase in order: front_left, front_right, right, bottom_right, bottom_left, left
		Position[] neighboursInSightOrder = turnBasePosition.getNeighbourPositionsByDirection(viewDirection);
		// centerbase may be the outer left or outer right element, test which it is
		int offsetForWidthSearch = 5; // get element at left=5, right=2
		Position nextNeighbour = neighboursInSightOrder[offsetForWidthSearch];
		
		// test, if one of the give elementPosition is equal the position at left of baseEl (then its outher right, else outer left)
		//System.out.println("view:"+viewDirection );
		//System.out.println( "fieldAtLeft:"+nextNeighbour );
		boolean turnBaseIsOuterRight = false;
		for( Position p : elementPositions ) {
			if( nextNeighbour != null ) {
				//System.out.println( "Position:"+p );
				if( p.x == nextNeighbour.x && p.z == nextNeighbour.z ) {
					turnBaseIsOuterRight = true;
				}
			}
		}
		if( !turnBaseIsOuterRight ) offsetForWidthSearch = 2;
		
		//System.out.println( "turnBaseIsOuterRight:"+turnBaseIsOuterRight );
		
		int widthOfUnit = 0;
		// while left(or right) neighbour is in given list of elementPositions, get next neighbour and increase width of unit counter
		boolean hasNextNeighbour = true;
		while( hasNextNeighbour ) {
			neighboursInSightOrder = neighboursInSightOrder[ offsetForWidthSearch ].getNeighbourPositionsByDirection(viewDirection);
			
			hasNextNeighbour = false;
			Position neighbour = neighboursInSightOrder[ offsetForWidthSearch ];
			for( Position p : elementPositions ) {
				if(  neighbour != null ) {
					if( p.x == neighbour.x && p.z == neighbour.z ) {
						hasNextNeighbour = true;
					}
				}
			}
			if( hasNextNeighbour ) nextNeighbour = neighbour;
			widthOfUnit++;
		}
		
		// get all hexagon Fields witin range of unitTurn
		Hexagon turnBaseHex = hexPanel.getHexByPosition(turnBasePosition);
		Set<Hexagon> turnRangeHexes = hexPanel.hexagonalGridCalculator.calculateMovementRangeFrom(turnBaseHex, widthOfUnit);
		
		// calc angle between front-sight-line (from center of turnBase directly forward) to each hex center in turnBaseWidth 
		// this should be less or equal turnOffset * |30| (degree)
		
		Set<Hexagon> validturnAngleHexes = new HashSet<Hexagon>();
		for( Hexagon actTargetHex : turnRangeHexes ) {
			if( actTargetHex == null ) continue;
			//System.out.println( "actHex:"+actTargetHex );
			
			Point2D.Double p1 = new Point2D.Double(
					turnBaseHex.getCenterY(), 
					turnBaseHex.getCenterX());

			Point2D.Double p2 = new Point2D.Double(
					actTargetHex.getCenterY(), 
					actTargetHex.getCenterX());
			
			// center of left( or right) neighbour hex
			Hexagon outerHex = hexPanel.getHexByPosition(nextNeighbour); 
			Point2D.Double p3 = new Point2D.Double(outerHex.getCenterY(), outerHex.getCenterX());
			
			double winkel = Math.round(WinkelApp.get360GradWinkel(p1, p2, p3));
			//System.out.println( "winkel:"+winkel );
			((SatelliteData)actTargetHex.getSatelliteData()).setSelected(ControlSource.LOCAL_PLAYER, false);
			boolean isMarchThroughAngle = false;
			double angleSign = -1.;
			
			// when turnbase is outerright, we need all fields in angle  -60 to 0.
			// turnoffset is positive means turning forward
			if(turnBaseIsOuterRight) { 
				if( viewDirection.equals(ViewDirection.TOP) ) {
					if( turnOffset > 0 && (winkel == 0 || (winkel >= (360. - ( 60. * (double)turnOffset)))) ) {
						isMarchThroughAngle = true;
					}	
				} else {
					if( turnOffset > 0 && (winkel <= 0 && winkel >= (-60. * (double)turnOffset)) ) {
						isMarchThroughAngle = true;
					}
				}
			} else {
				// when turnbase is outerleft, we need all fields in angle 0 to 60.
				// turnoffset is negative means turning forward
				if( turnOffset < 0 && (winkel >= 0 && winkel <= ( -60. * (double)turnOffset)) ) {
					isMarchThroughAngle = true;
				} 
			}
			
			if ( isMarchThroughAngle ) {
				//((SatelliteData)actTargetHex.getSatelliteData()).setSelected(ControlSource.LOCAL_PLAYER, true);
				validturnAngleHexes.add(actTargetHex);
				//System.out.println( "marchThrough:"+actTargetHex );
			} else {
				continue;
			}
		}
		return validturnAngleHexes;
	}

	private void endUnitTurn(  ) {
		unitTurnInProgress = false;
		turnFromHexagon = null;
		hexPanel.unitTurnInProgress_showTurnBase = null;
		elementGridPos.clear();
	}
	
	
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void rotateUnitElements( ArmyUnit unit, ViewDirection newDirection ) {
		for( ArmyElement el : unit.elements ) {
			el.elementState.viewDirection = newDirection;
		}
		hexPanel.mainViewControl.mainControl.updateGameStateHistory();
	}


	public void endPhase() {
		this.moveFromHexagon = null;
		this.moveInProgress = false;
		this.setElementDirection = false;
		hexPanel.moveInProgress.put(playerSource, null);
		hexPanel.showMovementRange.put(playerSource, null);
		// test, if any element has got in contact to an enemy element (attack)
		
		for( ArmyUnit actUnit : hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource).getUnits() ) {
			
			if( ArmyUnit.isUnitInMelee( 
					actUnit.getElementPositions(), 
					hexPanel.mainViewControl.mainControl.gameState.playerArmys.get( MainController.getOpponentControlSource(playerSource) ).getElementPositions() ) ) {
				if( actUnit.status.engagedInMeleeThisRound == false ) {
					actUnit.status.engagedInMeleeThisRound = true;
					continue;
				} 
			}
			// if unit is not in melee or is in melee but from last round:
			actUnit.status.engagedInMeleeThisRound = false;
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
