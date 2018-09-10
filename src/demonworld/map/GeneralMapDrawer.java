package demonworld.map;


import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import demonworld.controller.GameMode;
import demonworld.controller.GameMode_Move_MapController;
import demonworld.controller.server.ControlSource;
import demonworld.model.ArmyElement;
import demonworld.model.UnitOrder;
import demonworld.model.ViewDirection;
import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.HexagonOrientation;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;

public class GeneralMapDrawer implements MapDrawer {
	private HexagonPanel hexPanel;
	private boolean showNeighbors = false;
	public ViewDirection directionChooserState = ViewDirection.TOP;
	private HashMap<ControlSource, Image> cursorImg = new HashMap<ControlSource, Image>();
	private HashMap<ControlSource, Color> playerColor = new HashMap<ControlSource, Color>();
	private List<Color> shootingRangeColors = new ArrayList<Color>();
	private List<Color> moveRangeColorsMouseOver = new ArrayList<Color>();
	private List<Color> rangeFightColorsMouseOver = new ArrayList<Color>();
	private HashMap<Image, Image> scaledHexagonImages = new HashMap<Image, Image>();
	private HashMap<BufferedImage, BufferedImage> scaledImages = new HashMap<BufferedImage, BufferedImage>();
	private int[] movePoints = new int[3];
	private final String MARKED_AS_DEAD = "marked_as_dead";
	private final String MARKED_AS_SURVIVED = "marked_as_survived";
	private final String SHOOT_INTO_MELEE = "shoot_into_melee";
	public BufferedImage miniMap;

	
	
	
	public GeneralMapDrawer(HexagonPanel hexPanel) {
		
		this.hexPanel = hexPanel;
        playerColor.put(ControlSource.LOCAL_PLAYER, Color.BLUE);
        playerColor.put(ControlSource.EXTERNAL_PLAYER, Color.RED);
        
        shootingRangeColors.add(Color.LIGHT_GRAY);
        shootingRangeColors.add(Color.GRAY);
        shootingRangeColors.add(Color.DARK_GRAY);
        
        //moveRangeColorsMouseOver.add(new Color(0x00AA00));
        //moveRangeColorsMouseOver.add(new Color(0x00CC00));
        //moveRangeColorsMouseOver.add(new Color(0x00FF00));
        moveRangeColorsMouseOver.add(Color.decode("#239f37"));
        moveRangeColorsMouseOver.add(Color.decode("#239f37"));
        moveRangeColorsMouseOver.add(Color.decode("#239f37"));
        
        rangeFightColorsMouseOver.add(new Color(0xFF5500));
        rangeFightColorsMouseOver.add(new Color(0xFF0000));
        
        miniMap = new BufferedImage(hexPanel.getWidth(), hexPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
	}
	
	public void initCursorImages() {
        // marked as dead icon
		String imgFilename = "skull.png";
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("resources/images/"+imgFilename));
		} catch (IOException e) {
			System.err.println( "cannot load Imagefile for Mark as Dead:"+imgFilename );
			e.printStackTrace();
		}
		//gc.drawImage(getScaledImageFitInHexagon(img, 0), (int)Math.round((y)), (int)Math.round((x)) ,null);
        hexPanel.mainViewControl.mainControl.mainImageControl.addCursorIcon(MARKED_AS_DEAD, getScaledImageFitInHexagon(img, 0));

     // shoot into melee icon
 		imgFilename = "shootIntoMelee.png";
 		img = null;
 		try {
 			img = ImageIO.read(new File("resources/images/"+imgFilename));
 		} catch (IOException e) {
 			System.err.println( "cannot load Imagefile for Shoot into melee:"+imgFilename );
 			e.printStackTrace();
 		}
 		//gc.drawImage(getScaledImageFitInHexagon(img, 0), (int)Math.round((y)), (int)Math.round((x)) ,null);
         hexPanel.mainViewControl.mainControl.mainImageControl.addCursorIcon(SHOOT_INTO_MELEE, img);
        
        // marked as survived icon
		imgFilename = "shield.png";
		img = null;
		try {
			img = ImageIO.read(new File("resources/images/"+imgFilename));
		} catch (IOException e) {
			System.err.println( "cannot load Imagefile for Mark as Survived:"+imgFilename );
			e.printStackTrace();
		}
		//gc.drawImage(getScaledImageFitInHexagon(img, 0), (int)Math.round((y)), (int)Math.round((x)) ,null);
        hexPanel.mainViewControl.mainControl.mainImageControl.addCursorIcon(MARKED_AS_SURVIVED, getScaledImageFitInHexagon(img, 0));

        
        // UnitOrder icons
        for( UnitOrder orderName : UnitOrder.values() ) {
			
			try {
				img = ImageIO.read(new File("resources/images/Order_"+orderName.name()+".png"));
				hexPanel.mainViewControl.mainControl.mainImageControl.addCursorIcon("Order_"+orderName.name(), getScaledImageFitInHexagon(img, 0));
			} catch (IOException e) {
				System.err.println( "cannot load Imagefile for UnitOrder:"+orderName );
				e.printStackTrace();
			}
        }
        
	}
	
	
	// scales a hexagon to fit exactly in the grid (a hexagon has different hejght and width)
	
	public Image getScaledHexagonImage( Image img, int border ) {
		if( scaledHexagonImages.containsKey(img) ) {
			return scaledHexagonImages.get(img);
		}
		
		int hexagonHeight = 2* hexPanel.radius -( border*6 ) ;
		int hexagonWidth = 2* ((int)Math.round( hexPanel.getHexByGridCoordinate(0, 0).getPoints()[1].y ) - (int)Math.round( hexPanel.getHexByGridCoordinate(0, 0).getPoints()[0].y )) -(border*4);
		//int hexagonWidth = 2 * hexPanel.radius;
		scaledHexagonImages.put(img, img.getScaledInstance(hexagonWidth, hexagonHeight, 1));
		return scaledHexagonImages.get(img);
	}
	
	
	// scales a image to fit inside a hexagon (prepares image width and height proportion)
	public BufferedImage getScaledImageFitInHexagon( BufferedImage img, int border ) {
		if( scaledImages.containsKey(img) ) {
			
			return scaledImages.get(img);
		}
		
		//int hexagonHeight = 2* hexPanel.radius -( border*6 ) ;
		int hexagonWidth = 2 * ((int)Math.round( hexPanel.getHexByGridCoordinate(0, 0).getPoints()[1].y ) - (int)Math.round( hexPanel.getHexByGridCoordinate(0, 0).getPoints()[0].y )) -(border*4);
		//hexagonWidth = hexagonWidth -4;
		//int hexagonWidth = 2 * hexPanel.radius;
		scaledImages.put(img, toBufferedImage( img.getScaledInstance(hexagonWidth, hexagonWidth, 1) ));
		return scaledImages.get(img);
	}
	
	public BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	public void setCursorImage( Image img, ControlSource player ) {
		// set cursor to current Element
		//Toolkit kit = Toolkit.getDefaultToolkit();
		
		
		//Image img = ((SatelliteData)hexPanel.currSelected.getSatelliteData()).element.elementIcon;
		
		
		//Cursor cursor = kit.createCustomCursor(img, new Point(img.getHeight(null)/2, img.getWidth(null)), "myCursor");
		//Cursor cursor = kit.createCustomCursor(img, new Point(10, 10), "myCursor");
		
		cursorImg.put(player,  img); 
		hexPanel.repaint();
	}
	
	
	public void paint( Graphics gc ) {
		
	    
		//callCount++;
		//System.out.println( "GeneralMapDrwawer.Paint() Aufruf: "+ callCount);
		
		if( hexPanel.getHexagonalGrid() == null ) {
			System.out.println( "hexGrid is empty" );
			return;
		}
		
		
		for (String key : hexPanel.getHexagonalGrid().getHexagons().keySet()) {
			Hexagon hexagon = hexPanel.getHexagonalGrid().getHexagons().get(key);
			SatelliteData data = hexagon.<SatelliteData> getSatelliteData();
			// fill selected with playercolor
			/*for( ControlSource source : ControlSource.values() ) {
				if( data.isSelected(ControlSource.LOCAL_PLAYER) ) {
					gc.setColor( playerColor.get(source) );
					gc.fillPolygon(
							hexPanel.convertToPointsArrX( hexagon.getPoints() ),
							hexPanel.convertToPointsArrY( hexagon.getPoints() ),
							6);
				} 
			}*/
			
			// write coordinates
			
			if( hexPanel.drawGridCoordinates ) {
				String str = hexagon.getGridX()+"/"+hexagon.getGridY()+"/"+hexagon.getGridZ();
				Font f = new Font("serif", Font.PLAIN, 8);
				gc.setFont( f );
				gc.drawString( str, (int)hexagon.getCenterY()-10, (int)hexagon.getCenterX() );
			}
			
			if (data != null && data.isSelected(ControlSource.LOCAL_PLAYER) && hexagon == hexPanel.getCurrentSelected(ControlSource.LOCAL_PLAYER)) {
				GameMode currentGameMode = hexPanel.mainViewControl.currentGameMode;
				
				// paint movementRange for current movementUnit
				Integer showMovementRange = hexPanel.showMovementRange.get(ControlSource.LOCAL_PLAYER);
				if( showMovementRange != null && currentGameMode.equals(GameMode.MOVE)) {
					for (Hexagon hex : hexPanel.hexagonalGridCalculator.calculateMovementRangeFrom(hexagon, hexPanel.showMovementRange.get(ControlSource.LOCAL_PLAYER))) {
						hexPanel.mapDrawer.drawColoredHexagon(gc, hex, hexPanel.movementRangeColor);
					}
				}
				
				//
				
				// paint shootingRange
				int[] shootingRanges = hexPanel.showShootingRanges.get(ControlSource.LOCAL_PLAYER);
				if ( shootingRanges != null  
						&& (currentGameMode.equals(GameMode.FIGHT_RANGED_SKIRMISH) || currentGameMode.equals(GameMode.FIGHT_RANGED_HOLD))) {
					
					Arrays.sort( shootingRanges );
					for( int rangeNr = shootingRanges.length-1; rangeNr >= 0; rangeNr-- ) {
						int range = shootingRanges[rangeNr];
						/*for (Hexagon hex : hexPanel.hexagonalGridCalculator.calculateMovementRangeFrom(hexagon, range)) {
							hexPanel.mapDrawer.drawColoredHexagon(gc, hex, shootingRangeColors.get(rangeNr));
						}*/
						for (Hexagon hex : hexPanel.mainViewControl.mainControl.gm_FightRanged_controllerLocal.getFieldsInShootingSight(((SatelliteData)hexagon.getSatelliteData()).element)) {
							//System.out.println( hex+" - "+hexagon );
							
							if( hexPanel.hexagonalGridCalculator.calculateDistanceBetween(hexagon, hex)  <= range) {
								hexPanel.mapDrawer.drawColoredHexagon(gc, hex, shootingRangeColors.get(rangeNr));
							}
						}
					}
				}
				
				
				if (showNeighbors) {
					for (Hexagon hex : hexPanel.getHexagonalGrid().getNeighborsOf(hexagon)) {
						hexPanel.mapDrawer.drawNeighborHexagon(gc, hex);
					}
				}
			}
			hexPanel.mapDrawer.drawEmptyHexagon(gc, hexagon);
			
			// draw GridPos in every hexagon 
			//gc.setFont(new Font("Arial", Font.PLAIN, 10));
			//gc.drawString(  "("+hexagon.getGridX()+","+hexagon.getGridY()+","+hexagon.getGridZ()+")", (int)hexagon.getCenterY()-20, (int)hexagon.getCenterX());

		}
		for (String key : hexPanel.getHexagonalGrid().getHexagons().keySet()) {
			Hexagon hexagon = hexPanel.getHexagonalGrid().getHexagons().get(key);
			SatelliteData data = hexagon.<SatelliteData> getSatelliteData();
			if (data != null && (data.isSelected(ControlSource.LOCAL_PLAYER) || data.element != null )) {
				hexPanel.mapDrawer.drawFilledHexagon(gc, hexagon);
			} 
		}
		// paint movementLine / shootingline / Attackline from source to mousepointer
		for (Hexagon hex : hexPanel.getHexagonalGrid().getHexagons().values()) {
			if( hex.getSatelliteData() != null ) {
	
				if( hex.getSatelliteData().equals(hexPanel.moveInProgress.get(ControlSource.LOCAL_PLAYER))
					|| hex.getSatelliteData().equals(hexPanel.shootInProgress.get(ControlSource.LOCAL_PLAYER))	
					|| hex.getSatelliteData().equals(hexPanel.attackInProgress.get(ControlSource.LOCAL_PLAYER))
					|| hex.getSatelliteData().equals(hexPanel.supportInProgress.get(ControlSource.LOCAL_PLAYER))
					) {
				
				switch( hexPanel.mainViewControl.mainControl.gameState.currentGameMode ) {
				case MOVE:
					gc.setColor(Color.GREEN);
				break;
				default: // fight
					gc.setColor(Color.RED);
				break;
				}
					
				if( hex.getSatelliteData().equals(hexPanel.supportInProgress.get(ControlSource.LOCAL_PLAYER) )) {
					gc.setColor(Color.BLUE);
				}
				
				drawArrowLine(gc, 
						(int)hex.getCenterY(),
						(int)hex.getCenterX(),
						hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).x, 
						hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).y, 
						10, 5, true);
				}
				// on move, show new positions of army elements
				if( hex.getSatelliteData().equals(hexPanel.moveInProgress.get(ControlSource.LOCAL_PLAYER) )) {
					
					
					
					ArmyElement hexEl = ((SatelliteData)hex.getSatelliteData()).element;
					if( hexEl != null ) {
						double offsetToCursorX =  hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).x - hex.getCenterY();
						double offsetToCursorY =  hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).y - hex.getCenterX();
						biz.pavonis.hexameter.api.Point[] hexPoints = hex.getPoints();
						
						List<ArmyElement> showNewPos = new ArrayList<ArmyElement>();
						// on MOVE_ELEMENT, show only pos of the current selected
						GameMode_Move_MapController moveControl = hexPanel.mainViewControl.mainControl.gm_moveMapControllerLocal;
						if( moveControl.currentCursorElement.name.equals( moveControl.MOVE_ELEMENT ) ) {
							showNewPos.add( hexEl );
						} else {
							showNewPos.addAll(hexEl.armyUnit.elements);
						}
						for( ArmyElement unitEl : showNewPos ) {
							
							Hexagon pos = unitEl.elementState.getMapPosition(hexPanel);
							double offsetToHexElX = pos.getCenterX() - hex.getCenterX();
							double offsetToHexElY = pos.getCenterY() - hex.getCenterY();
							
							int[] hexPointsX = hexPanel.convertToPointsArrX(hexPoints);
							int[] hexPointsY = hexPanel.convertToPointsArrY(hexPoints);
							for( int i=0; i< hexPointsX.length; i++ ) {
								hexPointsX[i] += offsetToCursorX + offsetToHexElY;
								hexPointsY[i] += offsetToCursorY + offsetToHexElX;
							}
							
							gc.setColor( playerColor.get(ControlSource.LOCAL_PLAYER) );
							gc.drawPolygon(
									hexPointsX,
									hexPointsY,
									6);
						}
						
					}
				}
			}
		}


		//draw Attack/ Shoot / Support lines from source to target 
		for( ArmyElement element : hexPanel.mainViewControl.mainControl.gameState.getAllArmyElementsFromAllPlayers() ) {
			
			int srcX = (int)element.elementState.getMapPosition(hexPanel).getCenterY();
			int srcY = (int)element.elementState.getMapPosition(hexPanel).getCenterX();
			
			GameMode currentGameMode = hexPanel.mainViewControl.currentGameMode;
			if( element.elementState.attackAt != null && currentGameMode.equals(GameMode.FIGHT_MEELE)) {
				Hexagon targetHex = element.elementState.attackAt.elementState.getMapPosition(hexPanel);
				gc.setColor(Color.RED);
				//biz.pavonis.hexameter.api.Point nearestBorderPoint = getNearestPoint(srcX, srcY, targetHex.getPoints());							
				//drawArrowLine(gc, srcX, srcY, (int)nearestBorderPoint.y, (int)nearestBorderPoint.x, 10, 5, false);
				drawArrowLine(gc, srcX, srcY, (int)targetHex.getCenterY(), (int)targetHex.getCenterX(), 10, 5, false);
				
			} else if( element.elementState.supportAttack != null ) {
				Hexagon targetHex = null;
				if( element.elementState.supportAttack.elementState.attackAt != null 
						&& currentGameMode.equals(GameMode.FIGHT_MEELE)){ 
					targetHex = element.elementState.supportAttack.elementState.attackAt.elementState.getMapPosition(hexPanel);
				} else if ( element.elementState.supportAttack.elementState.aimsAt != null
						&& ( ( currentGameMode.equals(GameMode.FIGHT_RANGED_SKIRMISH) && element.armyUnit.status.order == UnitOrder.SKIRMISH)
								|| (currentGameMode.equals(GameMode.FIGHT_RANGED_HOLD) && element.armyUnit.status.order == UnitOrder.HOLD) )){
					targetHex = element.elementState.supportAttack.elementState.aimsAt.elementState.getMapPosition(hexPanel);
				}
				gc.setColor(Color.BLUE);
				//biz.pavonis.hexameter.api.Point nearestBorderPoint = getNearestPoint(srcX, srcY, targetHex.getPoints());							
				//drawArrowLine(gc, srcX, srcY, (int)nearestBorderPoint.y, (int)nearestBorderPoint.x, 10, 5, true);
				if( targetHex != null ) drawArrowLine(gc, srcX, srcY, (int)targetHex.getCenterY(), (int)targetHex.getCenterX(), 10, 5, false);
				
			} else if( element.elementState.aimsAt != null 
					&& ( (currentGameMode.equals(GameMode.FIGHT_RANGED_SKIRMISH) && element.armyUnit.status.order == UnitOrder.SKIRMISH) 
							|| (currentGameMode.equals(GameMode.FIGHT_RANGED_HOLD) && element.armyUnit.status.order == UnitOrder.HOLD))) {
				Hexagon targetHex = element.elementState.aimsAt.elementState.getMapPosition(hexPanel);
				gc.setColor(Color.RED);
				//biz.pavonis.hexameter.api.Point nearestBorderPoint = getNearestPoint(srcX, srcY, targetHex.getPoints());							
				//drawArrowLine(gc, srcX, srcY, (int)nearestBorderPoint.y, (int)nearestBorderPoint.x, 10, 5, false);
				drawArrowLine(gc, srcX, srcY, (int)targetHex.getCenterY(), (int)targetHex.getCenterX(), 10, 5, element.elementState.aimsAtTargetInMelee);
			}
		
		}
		
		// if mousecursor is over an element (lacalPlayer) show additional infos about that element
		
		ControlSource key = ControlSource.LOCAL_PLAYER;	
		if( hexPanel.mouseCursorPos.containsKey(key) ) {
			
			int mouseX, mouseY;
			mouseX = hexPanel.mouseCursorPos.get(key).y;
			mouseY = hexPanel.mouseCursorPos.get(key).x;
			try {
				Hexagon hex = hexPanel.getHexByPixelCoordinate(mouseX, mouseY);
				ArmyElement el = ((SatelliteData)hex.getSatelliteData()).element;
				if( el != null ) {
					
					movePoints[0] = el.movePointsGeneral;
					movePoints[1] = el.movePointsAttack;
					movePoints[2] = el.movePointsSkirmish;
					
					
					for( int m=0 ; m<movePoints.length; m++ ) {
						//int diameter = (4* hexPanel.radius * Math.round(movePoints[m] / 3));
						int diameter = ((2 * hexPanel.radius) *  Math.round(movePoints[m] / 3));
						gc.setColor( moveRangeColorsMouseOver.get(m) );
						int[] xPoints = new int[6] ;
						int[] yPoints = new int[6];
						for (int p = 0; p < 6; p++) {
				            double angle = 2
				                    * Math.PI
				                    / 6
				                    * (p + HexagonOrientation.FLAT_TOP.getCoordinateOffset());
				            xPoints[p] = (int)hex.getCenterY() + (int)Math.round(diameter * cos(angle));
				            yPoints[p] = (int)hex.getCenterX()  + (int)Math.round(diameter * sin(angle));
				        }
						gc.drawPolygon(xPoints, yPoints, 6);
						/*gc.drawOval(
								(int)hex.getCenterY() - Math.round(diameter/2), 
								(int)hex.getCenterX() - Math.round(diameter/2), 
								diameter, diameter );*/
					}
					if( el.distancesForLongRangeSkill != null ) {
						for( int m=0 ; m<el.distancesForLongRangeSkill.length; m++ ) {
							//int diameter = 2 * (hexPanel.radius + (hexPanel.radius * 2 * el.distancesForLongRangeSkill[m]));
							//int diameter =  (2 * hexPanel.radius * el.distancesForLongRangeSkill[m]);
							int diameter = ((2 * hexPanel.radius) * el.distancesForLongRangeSkill[m]);
							//System.out.println( el.distancesForLongRangeSkill[m] );
							gc.setColor( rangeFightColorsMouseOver.get(m) );
							int[] xPoints = new int[6] ;
							int[] yPoints = new int[6];
							for (int p = 0; p < 6; p++) {
					            double angle = 2
					                    * Math.PI
					                    / 6
					                    * (p + HexagonOrientation.FLAT_TOP.getCoordinateOffset());
					            xPoints[p] = (int)hex.getCenterY() + (int)Math.round(diameter * cos(angle));
					            yPoints[p] = (int)hex.getCenterX()  + (int)Math.round(diameter * sin(angle));
					        }
							gc.drawPolygon(xPoints, yPoints, 6);

							/*gc.drawOval(
									(int)hex.getCenterY() - Math.round(diameter/2), 
									(int)hex.getCenterX() - Math.round(diameter/2), 
									diameter, diameter );
							gc.drawString(
									""+el.distancesForLongRangeSkill[m], 
									(int)hex.getCenterY()- Math.round(diameter/2) , 
									(int)hex.getCenterX() );*/
							
						}
					}

				}
			} catch ( HexagonNotFoundException e ) {
			//	System.err.println( "genralMapDrawer cannot found hexagon"+e.getMessage() );
			}
		}
	
		
	    
		paintCursors(gc);
		
	}
	
	public void paintMiniMap() {
		// Draw Minimap
	    Graphics g = miniMap.getGraphics();
	    //g.setColor(Color.white);
	    //g.fillRect(0, 0, miniMap.getWidth(), miniMap.getHeight());
 		g.setColor(Color.decode("#EEEEEE"));
 		g.fillRect(0, 0, miniMap.getWidth(), miniMap.getHeight());

	    this.paint(g);
	}

	private biz.pavonis.hexameter.api.Point getNearestPoint( int srcX, int srcY, biz.pavonis.hexameter.api.Point[] targetPoints ) {
		//calc nearest point at border of target (otherwise, arrowhead is overpainted)
		biz.pavonis.hexameter.api.Point srcPoint = new biz.pavonis.hexameter.api.Point(srcX, srcY);
		biz.pavonis.hexameter.api.Point nearestBorderPoint = null;
		double actDistance = Double.MAX_VALUE;
		for( biz.pavonis.hexameter.api.Point p : targetPoints ) {
			if( p.distance(srcPoint, p) < actDistance) {
				actDistance = p.distance(srcPoint, p);
				nearestBorderPoint = p;
			}
		}
		return nearestBorderPoint;
	}
	
	private void paintCursors(Graphics gc) {
		for( ControlSource key : ControlSource.values() ) {
			
			if( hexPanel.mouseCursorPos.containsKey(key) ) {
				//define transparency for cursorimage
				int mouseX, mouseY;
				mouseX = hexPanel.mouseCursorPos.get(key).x;
				mouseY = hexPanel.mouseCursorPos.get(key).y;
				
				// mark cursor with playercolor for other players
				if( !key.equals( ControlSource.LOCAL_PLAYER ) ) {
					//System.out.println( "other player" );
					gc.setColor( playerColor.get(key) );
					if( cursorImg.get(key) != null ) {
						int radius = Math.round(cursorImg.get(key).getWidth(null)/4);
						int x = mouseX + Math.round(cursorImg.get(key).getWidth(null)/2) - Math.round(radius/2);
						int y = mouseY + Math.round(cursorImg.get(key).getHeight(null)/2) - Math.round(radius/2);
						gc.drawImage(cursorImg.get(key), mouseX, mouseY, 40, 40, null);
					}
					//gc.fillOval(x, y,radius, radius);
					gc.fillOval(mouseX-5, mouseY-5, 10, 10);
				}
			}
		}
	}
	
	
	public void drawEmptyHexagon(Graphics gc, Hexagon hexagon) {
		//gc.drawPolygon(convertToPointsArr(hexagon.getPoints()));
		/*gc.setColor(hexPanel.backGroundColor );
		gc.fillPolygon(
				hexPanel.convertToPointsArrX( hexagon.getPoints() ),
				hexPanel.convertToPointsArrY( hexagon.getPoints() ),
				6);
		*/
		/*BufferedImage img= null;
		try {
			img = ImageIO.read(new File("resources/images/map/green.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};*/
		
		// paint bagImg for hex
		/*int degree = 90; 
		
		Graphics2D g2D = (Graphics2D)img.createGraphics();
		double scalefactor = hexPanel.radius / ((double)img.getWidth() * 0.51);
		AffineTransform at = new AffineTransform();
		at.scale(scalefactor, scalefactor);
		//at.rotate(Math.toRadians(degree), img.getWidth()/2, img.getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		img = op.filter(img, null);

		
		int x = (int)Math.round(hexagon.getCenterX() - hexPanel.radius + 0);
		int y = (int)Math.round(hexagon.getCenterY() - (hexPanel.radius*0.85));

		gc.drawImage(img, (int)Math.round((y)), (int)Math.round((x)) ,null);
		 */
		
		gc.setColor( hexPanel.borderColor );
		gc.drawPolygon(
				hexPanel.convertToPointsArrX( hexagon.getPoints() ),
				hexPanel.convertToPointsArrY( hexagon.getPoints() ),
				6);
		
	}

	public void drawFilledHexagon(Graphics gc, Hexagon hexagon) {
		SatelliteData data = hexagon.getSatelliteData();
		//gc.setColor( data.element.armyUnit.army.armyColor );
		//gc.drawPolygon(
		//		hexPanel.convertToPointsArrX( hexagon.getPoints() ),
		//		hexPanel.convertToPointsArrY( hexagon.getPoints() ),
		//		6);

		if( data.isSelected(ControlSource.LOCAL_PLAYER) ) {
			//System.out.println( hexagon.getGridX()+"/"+hexagon.getGridY()+" is selected " );
			/*gc.setColor( hexPanel.selectedColor );
			gc.fillPolygon(
					hexPanel.convertToPointsArrX( hexagon.getPoints() ),
					hexPanel.convertToPointsArrY( hexagon.getPoints() ),
					6);
			*/
			
		} 
		if( data.element != null ) {

			BufferedImage img = hexPanel.mainViewControl.mainControl.mainImageControl.getArmyElementIcon(data.element);
			int x = (int)Math.round(hexagon.getCenterX() - hexPanel.radius + 0);
			int y = (int)Math.round(hexagon.getCenterY() - (hexPanel.radius*0.85));
			ViewDirection direction = data.element.elementState.viewDirection;
			
			// draw element image
			if( direction.equals( ViewDirection.BOTTOM_LEFT ) ||  direction.equals( ViewDirection.TOP_LEFT ) ) { // flip image horizontally (look to the left)
				gc.drawImage(img, y + (int)Math.round(img.getWidth()*0.9), x, -img.getWidth(), img.getHeight(), null);	
			} else {
				gc.drawImage(img, (int)Math.round((y)), (int)Math.round((x)) ,null);	
			}
			
			// draw view direction
			int viewX=0, viewY=0, offset;
			if( direction.equals( ViewDirection.TOP ) ) {
				offset = 3;
				viewX = (int) Math.round(hexagon.getPoints()[offset].y-3);
				viewY = (int) Math.round(hexagon.getPoints()[offset].x);
			} else if ( direction.equals( ViewDirection.TOP_RIGHT ) ) {
				offset = 2;
				viewX = (int) Math.round(hexagon.getPoints()[offset].y-4);
				viewY = (int) Math.round(hexagon.getPoints()[offset].x-2);
			} else if ( direction.equals( ViewDirection.BOTTOM_RIGHT ) ) {
				offset = 1;
				viewX = (int) Math.round(hexagon.getPoints()[offset].y-4);
				viewY = (int) Math.round(hexagon.getPoints()[offset].x-4);
			} else if ( direction.equals( ViewDirection.BOTTOM ) ) {
				offset = 0;
				viewX = (int) Math.round(hexagon.getPoints()[offset].y-3);
				viewY = (int) Math.round(hexagon.getPoints()[offset].x-6);
			} else if ( direction.equals( ViewDirection.BOTTOM_LEFT ) ) {
				offset = 5;
				viewX = (int) Math.round(hexagon.getPoints()[offset].y);
				viewY = (int) Math.round(hexagon.getPoints()[offset].x-5);
			} else if ( direction.equals( ViewDirection.TOP_LEFT ) ) {
				offset = 4;
				viewX = (int) Math.round(hexagon.getPoints()[offset].y);
				viewY = (int) Math.round(hexagon.getPoints()[offset].x-1);
			}
			
			gc.setColor( data.element.armyUnit.army.armyColor );
			gc.fillOval( viewX, viewY, 6, 6);
			
			
			((Graphics2D)gc).setStroke(new BasicStroke(2));
			gc.drawPolygon(
					hexPanel.convertToPointsArrX( hexagon.getPoints() ),
					hexPanel.convertToPointsArrY( hexagon.getPoints() ),
					6);
			
			
			
			// draw 'marked as dead' / "marked as survived" / "shoot into melee"marker
			if( data.element.elementState.markedAsDead ) {
				gc.drawImage(hexPanel.mainViewControl.mainControl.mainImageControl.getCursorIcon(MARKED_AS_DEAD), (int)Math.round((y)), (int)Math.round((x)) ,null);
			} else if( data.element.elementState.markedAsSurvived ) {
				gc.drawImage(hexPanel.mainViewControl.mainControl.mainImageControl.getCursorIcon(MARKED_AS_SURVIVED), (int)Math.round((y)), (int)Math.round((x)) ,null);
			} 
			if( data.element.elementState.aimsAtTargetInMelee ) {
				//gc.drawImage(hexPanel.mainViewControl.mainControl.mainImageControl.getCursorIcon(SHOOT_INTO_MELEE), (int)Math.round((y)), (int)Math.round((x)) ,null);
			}
			
			// draw Order of unit
			// if element is unitLeader and we are not in Setup_Army Mode
			if( data.element.unitLeader && !hexPanel.mainViewControl.currentGameMode.equals(GameMode.SETUP_ARMYS)) {
				UnitOrder order = data.element.armyUnit.status.order;
				if( order != null ) {
					if( hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(ControlSource.LOCAL_PLAYER).equals( data.element.armyUnit.army) 
							|| !hexPanel.mainViewControl.currentGameMode.equals(GameMode.PLACE_ORDERS) ){
						gc.drawImage(hexPanel.mainViewControl.mainControl.mainImageControl.getCursorIcon("Order_"+order.name()), (int)Math.round((y)), (int)Math.round((x)) ,null);
					} else {
						gc.drawImage(hexPanel.mainViewControl.mainControl.mainImageControl.getCursorIcon("Order_NO_ORDER"), (int)Math.round((y)), (int)Math.round((x)) ,null);
					}
				}
			}
			
			// draw minimum diceRoll for shooting / attack
			if( data.element.elementState.attackAt != null || data.element.elementState.aimsAt != null) {
				gc.setFont(new Font("Trattatello", Font.PLAIN, 24));
                int xStr = (int)Math.round(y)+(int)Math.round(hexPanel.radius/2);
                int yStr = (int)Math.round(x)+(int)Math.round(hexPanel.radius*1.8);

				if( hexPanel.mouseCursorPos.containsKey(ControlSource.LOCAL_PLAYER) ) {
					int mouseX, mouseY;
					mouseX = hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).y;
					mouseY = hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).x;
					try {
						Hexagon hex = hexPanel.getHexByPixelCoordinate(mouseX, mouseY);
						ArmyElement el = ((SatelliteData)hex.getSatelliteData()).element;
						if( el == data.element ) {
							FontMetrics fm = gc.getFontMetrics();
			                Rectangle2D rect = fm.getStringBounds(""+data.element.getNeededDiceRoll(), gc);
			                gc.setColor(Color.LIGHT_GRAY);
			                gc.fillRect(xStr,
			                           yStr - fm.getAscent()/2,
			                           (int) rect.getWidth(),
			                           (int) Math.round(rect.getHeight()/2.75));

						}
					} catch( Exception e) {
					}
				}

				gc.setColor(Color.RED);
				gc.drawString(""+data.element.getNeededDiceRoll(), 
						xStr, 
						yStr);
			}

			// draw initiative for attack
			if( data.element.elementState.attackAt != null ) {
				gc.setFont(new Font("Trattatello", Font.PLAIN, 24));
                int xStr = (int)Math.round(y)+(int)Math.round(hexPanel.radius/1.9);
                int yStr = (int)Math.round(x)+(int)Math.round(hexPanel.radius*0.75);
                int initiative = data.element.getInitiative( true );
                
				if( hexPanel.mouseCursorPos.containsKey(ControlSource.LOCAL_PLAYER) ) {
					int mouseX, mouseY;
					mouseX = hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).y;
					mouseY = hexPanel.mouseCursorPos.get(ControlSource.LOCAL_PLAYER).x;
					try {
						Hexagon hex = hexPanel.getHexByPixelCoordinate(mouseX, mouseY);
						ArmyElement el = ((SatelliteData)hex.getSatelliteData()).element;
						if( el == data.element ) {
							FontMetrics fm = gc.getFontMetrics();
			                Rectangle2D rect = fm.getStringBounds(""+initiative, gc);
			                gc.setColor(Color.LIGHT_GRAY);
			                gc.fillRect(xStr,
			                           yStr - fm.getAscent()/2,
			                           (int) rect.getWidth(),
			                           (int) Math.round(rect.getHeight()/2.75));
						}
					} catch( Exception e) {
					}
				}

				
                
                gc.setColor(Color.BLUE);
				gc.drawString(""+initiative,xStr, yStr );
			}

			// if UnitTurn is in progress, mark turnUnitBase
			if( hexPanel.unitTurnInProgress_showTurnBase == hexagon ) {
				gc.setColor(Color.BLACK);
				int radius = (int)Math.round((hexPanel.radius*1.4)/2);
				gc.fillOval(
						(int)Math.round(hexagon.getCenterY())-(radius), 
						(int)Math.round(hexagon.getCenterX())-(radius), radius*2, radius*2);
				//gc.drawString("B", y,x);
				//System.out.println("test"+x+","+y);
			}
			
			/*
			biz.pavonis.hexameter.api.Point[] p = hexagon.getPoints();
			
			for( int i=0; i<p.length; i++ ) {
				gc.drawString(""+i, (int)p[i].y, (int)p[i].x);
			}*/
		}
		
	}

	public void drawNeighborHexagon(Graphics gc, Hexagon hexagon) {
		/*gc.setForeground(white);
		gc.setBackground(darkGray);
		gc.fillPolygon(convertToPointsArr(hexagon.getPoints()));
		gc.setForeground(darkBlue);
		gc.drawPolygon(convertToPointsArr(hexagon.getPoints()));*/
		gc.setColor( hexPanel.neighbourColor );
		gc.fillPolygon(
				hexPanel.convertToPointsArrX( hexagon.getPoints() ),
				hexPanel.convertToPointsArrY( hexagon.getPoints() ),
				6);
		//gc.setForeground(darkBlue);
		gc.setColor( hexPanel.borderColor );

		gc.drawPolygon(
				hexPanel.convertToPointsArrX( hexagon.getPoints() ),
				hexPanel.convertToPointsArrY( hexagon.getPoints() ),
				6);
	}

	public void drawColoredHexagon(Graphics gc, Hexagon hexagon, Color c) {
		/*gc.setForeground(darkBlue);
		gc.setBackground(yellow);
		gc.fillPolygon(convertToPointsArr(hexagon.getPoints()));
		gc.setForeground(darkBlue);
		gc.drawPolygon(convertToPointsArr(hexagon.getPoints()));*/
		gc.setColor( c );
		gc.fillPolygon(
				hexPanel.convertToPointsArrX( hexagon.getPoints() ),
				hexPanel.convertToPointsArrY( hexagon.getPoints() ),
				6);
		//gc.setForeground(darkBlue);
		gc.setColor( hexPanel.borderColor );

		gc.drawPolygon(
				hexPanel.convertToPointsArrX( hexagon.getPoints() ),
				hexPanel.convertToPointsArrY( hexagon.getPoints() ),
				6);
	}

	/**
     * Draw an arrow line betwwen two point 
     * @param g the graphic component
     * @param x1 x-position of first point
     * @param y1 y-position of first point
     * @param x2 x-position of second point
     * @param y2 y-position of second point
     * @param d  the width of the arrow
     * @param h  the height of the arrow
     */
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h, boolean dashedLine){
       int dx = x2 - x1, dy = y2 - y1;
       double D = Math.sqrt(dx*dx + dy*dy);
       double xm = D - d, xn = xm, ym = h, yn = -h, x;
       double sin = dy/D, cos = dx/D;

       x = xm*cos - ym*sin + x1;
       ym = xm*sin + ym*cos + y1;
       xm = x;

       x = xn*cos - yn*sin + x1;
       yn = xn*sin + yn*cos + y1;
       xn = x;

       int[] xpoints = {x2, (int) xm, (int) xn};
       int[] ypoints = {y2, (int) ym, (int) yn};

       if( dashedLine ) {
    	   drawDashedLine(g, x1, y1, x2, y2);
       } else {
    	   g.drawLine(x1, y1, x2, y2);
       }
       g.fillPolygon(xpoints, ypoints, 3);
    }

    public void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2){

        //creates a copy of the Graphics instance
        Graphics2D g2d = (Graphics2D) g.create();

        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(x1, y1, x2, y2);

        //gets rid of the copy
        g2d.dispose();
}
	private static double distance(Point p1, Point p2) {
        return Math.sqrt(
                (p1.getX() - p2.getX()) *  (p1.getX() - p2.getX()) + 
                (p1.getY() - p2.getY()) *  (p1.getY() - p2.getY())
            );
        }
	
}
