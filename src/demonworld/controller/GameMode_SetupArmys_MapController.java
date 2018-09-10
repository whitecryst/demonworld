package demonworld.controller;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.Point;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;
import demonworld.controller.server.ControlSource;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.HexagonPanel;
import demonworld.map.SatelliteData;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyType;
import demonworld.model.ElementState;
import demonworld.model.GameState;
import demonworld.model.ViewDirection;

public class GameMode_SetupArmys_MapController implements InputHandlerInterface {
	private HexagonPanel hexPanel;
	private ArmyElement currentElement; // current choosed element to add to map
	private Vector<ArmyElement> elementList; // all elements from both armys
	private HashMap<ArmyElement, Cursor> elementCursors = new HashMap<ArmyElement, Cursor>();
	private final String DELETE = "Delete";
	private boolean setElementDirection = false;
	private ViewDirection lastViewDirection = ViewDirection.TOP;
	private ControlSource playerSource;
	
	
	


	public GameMode_SetupArmys_MapController(HexagonPanel hexPanel, ControlSource playerSource) {
		this.playerSource = playerSource;
		this.hexPanel = hexPanel;
		currentElement = hexPanel.mainViewControl.mainControl.gameState.availableArmyElements.get(0);	
		init();
	}
	
	public void init() {
		setNewUnitChooser(true);
		setActionChooser(true);
		updateCursorElement(0);
		
	}

	public HexagonPanel getHexPanel() {
		return hexPanel;
	}
	
	public void updateUnitChooserElements() {
		setNewUnitChooser(false);
		setNewUnitChooser(true);
		updateCursorElement( 0 );
	}
	
	private void setNewUnitChooser( boolean enable ) {
		if( elementList == null ) {
			this.elementList = new Vector<ArmyElement>();
		}
		if( enable ) {
			this.elementList.addAll( hexPanel.mainViewControl.mainControl.gameState.availableArmyElements );
			for( ArmyElement el : hexPanel.mainViewControl.mainControl.gameState.availableArmyElements ) {
				if( !elementCursors.containsKey(el) ) {
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					
					//Image i = ((GeneralMapDrawer)hexPanel.mapDrawer).getScaledImageFitInHexagon(el.elementIcon, 0);
					//Image i = ((GeneralMapDrawer)hexPanel.mapDrawer).getScaledImageFitInHexagon(hexPanel.mainViewControl.mainControl.mainImageControl.getArmyElementIcon(el), 0);
					Image i = hexPanel.mainViewControl.mainControl.mainImageControl.getArmyElementIcon(el);
					//elementCursors.put(el, toolkit.createCustomCursor(el.elementIcon , new java.awt.Point(0,0), "img"));
					elementCursors.put(el, toolkit.createCustomCursor(i , new java.awt.Point(30,20), "img"));
				}
			}
		} else {
			for( int i = elementList.size()-1; i >=0; i-- ){
				if( !elementList.get(i).unitDesignation.equals(DELETE)) {
					elementList.remove(i);
				}
			}
		}
	}
	
	private void setActionChooser( boolean enable ) {
		if( elementList == null ) {
			this.elementList = new Vector<ArmyElement>();
		}
		if( enable ) {
			File imgFile = new File("resources/images/delete.png");
			BufferedImage img = null;
			try {
				img = ImageIO.read( imgFile );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArmyElement el = new ArmyElement(DELETE, false, imgFile, null, 0, 0, 0, 0, 0, 0, null, null, null, null, 0, 0, 0, 0, 0, ArmyType.IMPERIAL, 0);
			this.elementList.add( el );
			if( !elementCursors.containsKey(el) ) {
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Image i = ((GeneralMapDrawer)hexPanel.mapDrawer).getScaledImageFitInHexagon(img, 0);
				elementCursors.put(el, toolkit.createCustomCursor(i , new java.awt.Point(30,30), "img"));
			}
		} else {
			this.elementList.remove( hexPanel.mainViewControl.mainControl.gameState.availableArmyElements );
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//System.out.println("MapSetup mouseClicked");
		if( playerSource == ControlSource.LOCAL_PLAYER ) {
			if( setElementDirection == true) {
				setElementDirection = false;
				Army army = hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource);
				army.totalArmyPoints = army.getPoints(); // update total army points
				hexPanel.mainViewControl.mainControl.updateGameStateHistory();
				hexPanel.mainViewControl.mainControl.serverBridge.sendArmy(army);
			} else {
				//mouseClicked(e);
				try {
					Hexagon hex = hexPanel.getHexByPixelCoordinate(e.getY(), e.getX());
					
					/*if( hex.getSatelliteData() == null ) {
						hex.setSatelliteData( new SatelliteData() );
					}*/
					SatelliteData data = ((SatelliteData)hex.getSatelliteData());
					data.setSelected(playerSource, true);
					
					
					if( currentElement.unitDesignation.equals( DELETE ) ) {
						// map field have to contain a ArmyElement
						if( data.element != null ) {
							// delete from army
							hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource).removeElement(data.element);
							// delete from map
							//hexPanel.armyElementPos.remove(data.element);
							data.element = null;
							hexPanel.refreshArmyElementPositions();
							Army army = hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource);
							army.totalArmyPoints = army.getPoints(); // update total army points
							hexPanel.mainViewControl.mainControl.updateGameStateHistory();
							hexPanel.mainViewControl.mainControl.serverBridge.sendArmy(army);
						}
					} else {
						// mapField must be empty
						if( data.element == null ) {
							ArmyElement newElement = currentElement.copy();
							// place element on map
							//data.element = newElement;
							// add element to payers army
							addArmyElement( newElement, hex, lastViewDirection );

							setElementDirection = true;
						}
					}
					
					hexPanel.setSelectedHexagon( playerSource, hex ); 
					hexPanel.repaint();
					//hexPanel.paint( hexPanel.getGraphics() );
				} catch (HexagonNotFoundException err) {
					
				}
			}	
		}
		//System.out.println( "mouse clicked" );
		
	}
	
	public void addArmyElement( ArmyElement el, Hexagon hexPos, ViewDirection viewDirection ) {
		// add element to payers army
		System.out.println( "add element:"+el.unitDesignation );
		el.elementState.viewDirection = viewDirection;
		hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource).addElement(el);
		el.armyUnit.defaultNumberOfElements = currentElement.armyUnit.defaultNumberOfElements;
		el.elementState.setMapPosition(hexPos);
		// save element Position in hexMap
		hexPanel.refreshArmyElementPositions();
		hexPanel.mainViewControl.mainControl.updateGameStateHistory();
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
	}



	@Override
	public void mouseMoved(MouseEvent e) {
		// update cursorPos
		java.awt.Point lastCursorPos = hexPanel.mouseCursorPos.get(playerSource);
		hexPanel.mouseCursorPos.put(playerSource, e.getPoint());
		 if( setElementDirection ) {
			 ArmyElement selectedElement = ((SatelliteData)hexPanel.getSelectedHexagon(playerSource).getSatelliteData()).element;
			 ElementState state = selectedElement.elementState;
			 double angle = Math.atan2(e.getY() - hexPanel.getSelectedHexagon(playerSource).getCenterX(), 
					hexPanel.getSelectedHexagon(playerSource).getCenterY() - e.getX());
			
				//double det = ((hexPanel.getSelectedHexagon().getCenterX()*e.getY()) - ((hexPanel.getSelectedHexagon().getCenterY()*e.getX()));
				//double angle = Math.toDegrees(Math.atan2(det, dot));
		
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
				
				lastViewDirection = state.viewDirection;
				//System.out.println( movedirection+angle );
				//selectedElement.updateElementIcon();
				 //hexPanel.repaint();
	        }
		//hexPanel.repaint(e.getX()-50, e.getY()-50, 100, 100);;
		 hexPanel.repaint();
       
	}



	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//System.out.println( "wheel before: "+currentElement.unitDesignation+" "+currentElementPos );
		if( playerSource == ControlSource.LOCAL_PLAYER ) {
			int offset = e.getWheelRotation(); 
			//System.out.println( "offset"+offset );
			changeActElement(offset);
		}
	}

	private void changeActElement(int offset) {
		int currentElementPos = elementList.indexOf( currentElement );
		if( ( currentElementPos + offset ) < elementList.size() && ( currentElementPos + offset ) >= 0 ) {
			currentElementPos += offset;	
		} else if ( ( currentElementPos + offset ) >= elementList.size() ) {
			currentElementPos = elementList.size() % offset;
		} else if( ( currentElementPos + offset ) < 0 ) {
			currentElementPos = (elementList.size()-1) - (elementList.size() % (offset*-1));
		} 
		
		updateCursorElement( currentElementPos );
		
		
		
		//System.out.println( "wheel after: "+currentElement.unitDesignation+" "+currentElementPos );	
	}

	private void updateCursorElement(int currentElementPos) {
		if( currentElementPos >= 0 && currentElementPos < elementList.size() ) {

			currentElement = elementList.get( currentElementPos );
			/*((GeneralMapDrawer)hexPanel.mapDrawer).setCursorImage( 
					((GeneralMapDrawer)hexPanel.mapDrawer).getScaledImageFitInHexagon( currentElement.elementIcon ,1 ),
					playerSource);*/
			hexPanel.updateCursorImage( elementCursors.get(currentElement) );
					
		}
		
		// update infoPanel
		if( currentElement.unitDesignation != DELETE ) {
			hexPanel.mainViewControl.defaultInfoElement = currentElement;	
			hexPanel.mainViewControl.showArmyElementInfo(null);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	
	}


	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
	    switch( keyCode ) { 
	    	case KeyEvent.VK_PLUS:
    			hexPanel.numberOfArmyElementsToPlace++;
            break;
	    	case KeyEvent.VK_MINUS:
	    		hexPanel.numberOfArmyElementsToPlace--;
	        break;

	    	case KeyEvent.VK_UP:
        		changeActElement(1);	
	            break;
	        case KeyEvent.VK_DOWN:
        		changeActElement(-1);	
	            break;
	        case KeyEvent.VK_LEFT:
        		updateCursorElement(0);	
	            break;
	        case KeyEvent.VK_RIGHT :
	        	// Last element
        		updateCursorElement( elementList.size()-1 );
	        	break;
	        case KeyEvent.VK_X :
	        	// delete
        		for( ArmyElement el : elementList ) {
        			if( el.unitDesignation.equals( DELETE ) ) {
        				updateCursorElement( elementList.indexOf(el) );
        			}
        		}
	        	break;
	        case KeyEvent.VK_ESCAPE :
	        	
	        	break;
	            
	     }
	
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		this.setElementDirection = false;
	}


	public void endPhase() {
		setElementDirection = false;		
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
