package demonworld.map;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.HexagonOrientation;
import biz.pavonis.hexameter.api.HexagonalGrid;
import biz.pavonis.hexameter.api.HexagonalGridBuilder;
import biz.pavonis.hexameter.api.HexagonalGridCalculator;
import biz.pavonis.hexameter.api.HexagonalGridLayout;
import biz.pavonis.hexameter.api.Point;
import biz.pavonis.hexameter.api.exception.HexagonalGridCreationException;
import demonworld.controller.Listener;
import demonworld.controller.MainController;
import demonworld.controller.MainViewController;
import demonworld.controller.server.ControlSource;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ElementState;
import demonworld.model.GameState;
import demonworld.model.Position;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

public class HexagonPanel extends JPanel
  
{
  private int width;
  private int height;
  private HexagonalGrid hexagonalGrid;
  public HexagonalGridCalculator hexagonalGridCalculator;
  private static final int DEFAULT_GRID_WIDTH = 20;
  private static final int DEFAULT_GRID_HEIGHT = 30;
  public static int DEFAULT_RADIUS = 20;
  private static final HexagonOrientation DEFAULT_ORIENTATION = HexagonOrientation.FLAT_TOP;
  private static final HexagonalGridLayout DEFAULT_GRID_LAYOUT = HexagonalGridLayout.RECTANGULAR;
  private int gridWidth = 30;
  private int gridHeight = 40;
  public int radius = DEFAULT_RADIUS;
  private HexagonOrientation orientation = DEFAULT_ORIENTATION;
  private HexagonalGridLayout hexagonGridLayout = DEFAULT_GRID_LAYOUT;
  public HashMap<ControlSource, java.awt.Point> mouseCursorPos = new HashMap<ControlSource, java.awt.Point>();
  public Listener currentController;
  public MainViewController mainViewControl;
  public HashMap<ControlSource, Integer> showMovementRange = new HashMap<ControlSource, Integer>();
  public HashMap<ControlSource, int[]> showShootingRanges = new HashMap<ControlSource, int[]>();
  
  public HashMap<ControlSource, SatelliteData> moveInProgress = new HashMap<ControlSource, SatelliteData>();
  public HashMap<ControlSource, SatelliteData> shootInProgress = new HashMap<ControlSource, SatelliteData>();
  public HashMap<ControlSource, SatelliteData> attackInProgress = new HashMap<ControlSource, SatelliteData>();
  public HashMap<ControlSource, SatelliteData> supportInProgress = new HashMap<ControlSource, SatelliteData>();
  private HashMap<ControlSource, Hexagon> prevSelected = new HashMap<ControlSource, Hexagon>();
  private HashMap<ControlSource, Hexagon> currSelected = new HashMap<ControlSource, Hexagon>();
  public Hexagon unitTurnInProgress_showTurnBase;
  private Font font;
  private int fontSize;
  public int numberOfArmyElementsToPlace = 1;
  public boolean drawGridCoordinates = false;
  Color backGroundColor = null;
  Color borderColor = Color.black;
  Color selectedColor = Color.lightGray;
  Color neighbourColor = Color.gray;
  Color movementRangeColor = Color.LIGHT_GRAY;
  public MapDrawer mapDrawer;
  public JPopupMenu weaponChooserMenu;
  public JPopupMenu fightingSkillBonusChooserMenu;
  
  public HexagonPanel (int width, int height)
  {
    DEFAULT_RADIUS = height / 30;
    this.width = width;
    this.height = height;
    regenerateHexagonGrid();
    this.setSize(width, height);
    this.setPreferredSize(new Dimension(width, height));
    this.setOpaque(false);
    this.mapDrawer = new GeneralMapDrawer(this);
    this.weaponChooserMenu = new JPopupMenu();
    this.fightingSkillBonusChooserMenu = new JPopupMenu();
    
  }
  
  public void setSelectedHexagon(ControlSource playerSource, Hexagon hex)
  {
    SatelliteData newData = (SatelliteData)hex.getSatelliteData();
    if (getSelectedHexagon(playerSource) != null)
    {
      SatelliteData currData = (SatelliteData)((Hexagon)this.currSelected.get(playerSource)).getSatelliteData();
      currData.setSelected(playerSource, false);
      newData.setSelected(playerSource, true);
    }
    this.prevSelected.put(playerSource, (Hexagon)this.currSelected.get(playerSource));
    
    this.currSelected.put(playerSource, hex);
  }
  
  public Hexagon getSelectedHexagon(ControlSource playerSource)
  {
    return (Hexagon)this.currSelected.get(playerSource);
  }
  
  private void regenerateHexagonGrid()
  {
    try
    {
      HexagonalGridBuilder builder = new HexagonalGridBuilder().setGridWidth(this.gridWidth).setGridHeight(this.gridHeight).setRadius(this.radius).setOrientation(this.orientation)
        .setGridLayout(this.hexagonGridLayout).setCustomStorage(new HashMap());
      this.hexagonalGrid = builder.build();
      this.hexagonalGridCalculator = builder.buildCalculatorFor(this.hexagonalGrid);
      for (String key : this.hexagonalGrid.getHexagons().keySet())
      {
        Hexagon hexagon = (Hexagon)this.hexagonalGrid.getHexagons().get(key);
        SatelliteData data = (SatelliteData)hexagon.getSatelliteData();
        if (data == null) {
          hexagon.setSatelliteData(new SatelliteData());
        }
      }
    }
    catch (HexagonalGridCreationException e)
    {
      e.printStackTrace();
    }
    repaint();
  }
  
  
  
  public void paint(Graphics g)
  {
	
	((GeneralMapDrawer)mapDrawer).paintMiniMap();
    this.mapDrawer.paint(g);
    //mainViewControl.mainControl.toolBarControl.updateMiniMap();
    
    // show ArmyPoints
    for( ControlSource c : ControlSource.values() ) {
    	mainViewControl.mainControl.mainFrame.armyPointsView.updatePoints(c, mainViewControl.mainControl.gameState.playerArmys.get(c));
    }
    
    
    
  }
  
	protected int[] convertToPointsArr(Point[] points) {
		int[] pointsArr = new int[12];
		int i = 0;
		for (Point point : points) {
			pointsArr[i] = (int) Math.round(point.x);
			pointsArr[i + 1] = (int) Math.round(point.y);
			i += 2;
		}
		return pointsArr;
	}
	
	protected int[] convertToPointsArrX(Point[] points) {
		int[] pointsArr = new int[6];
		int i = 0;
		for (Point point : points) {
			pointsArr[i] = (int) Math.round(point.y);
			i += 1;
		}
		return pointsArr;
	}
	
	protected int[] convertToPointsArrY(Point[] points) {
		int[] pointsArr = new int[6];
		int i = 0;
		for (Point point : points) {
			pointsArr[i] = (int) Math.round(point.x);
			i += 1;
		}
		return pointsArr;
	}
  
  public void refreshArmyElementPositions()
  {
    SatelliteData data;
    // delete all elements from hexagon
    for (Hexagon hexagon : this.hexagonalGrid.getHexagons().values())
    {
      data = (SatelliteData)hexagon.getSatelliteData();
      data.element = null;
    }

    // go through all ArmyElements, find out position on map, add element to corresponding hexagonData 
    for (ControlSource playerSource : ControlSource.values()) 
    {
      for (ArmyElement el : ((Army)this.mainViewControl.mainControl.gameState.playerArmys.get(playerSource)).getElements())
      {
        Hexagon hex = el.elementState.getMapPosition(this);
        if (hex == null) {
          System.err.println(el.unitDesignation + " has no mapPosition");
        }
        ((SatelliteData)hex.getSatelliteData()).element = el;
      }
    }
  }
  
  public Hexagon getHexByGridCoordinate(int gridX, int gridZ)
  {
    return this.hexagonalGrid.getByGridCoordinate(gridX, gridZ);
  }
  
  public Hexagon getHexByPosition(Position pos)
  {
    return this.hexagonalGrid.getByGridCoordinate(pos.x, pos.z);
  }
  
  public Hexagon getHexByPixelCoordinate(int x, int y)
  {
    return this.hexagonalGrid.getByPixelCoordinate(x, y);
  }
  
  public Hexagon getPrevSelected(ControlSource playerSource)
  {
    return (Hexagon)this.prevSelected.get(playerSource);
  }
  
  public Hexagon getCurrentSelected(ControlSource playerSource)
  {
    return (Hexagon)this.currSelected.get(playerSource);
  }
  
  public HexagonalGrid getHexagonalGrid()
  {
    return this.hexagonalGrid;
  }
  
  public Set<Hexagon> getHexNeighbours(Hexagon hex) {
	  
	  return this.getHexagonalGrid().getNeighborsOf(hex) ;
	  
	  
  }
  
  public java.awt.Point getToolTipLocation(MouseEvent e)
  {
    return new java.awt.Point(
      ((java.awt.Point)this.mouseCursorPos.get(ControlSource.LOCAL_PLAYER)).x + 2 * this.radius, 
      ((java.awt.Point)this.mouseCursorPos.get(ControlSource.LOCAL_PLAYER)).y + 2 * this.radius);
  }
  
  public void updateCursorImage(Cursor c)
  {
    if (c == null) {
      setCursor(Cursor.getDefaultCursor());
    } else {
      setCursor(c);
    }
  }
  
  public BufferedImage getCursorOverlay(BufferedImage img)
  {
    
	BufferedImage overlay = null;
    BufferedImage combined = null;
    try
    {
      overlay = ImageIO.read(new File("resources/images/cursor_sword.png"));
      
      int w = Math.max(img.getWidth(), overlay.getWidth());
      int h = Math.max(img.getHeight(), overlay.getHeight());
      combined = new BufferedImage(w, h, 2);
      
      Graphics g = combined.getGraphics();
      g.drawImage(overlay, 0, 0, null);
      g.drawImage(img, 0, 0, null);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return combined;
  }
  
  public void showWeaponChooserMenu(ArmyElement e) {
	  weaponChooserMenu.removeAll();
	  ActionListener al = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ArmyElement el = ((SatelliteData)prevSelected.get(ControlSource.LOCAL_PLAYER).getSatelliteData()).element; 
			// set Weapon for all elements in this unit
			for( ArmyElement unitElement : el.armyUnit.elements ) {
				unitElement.elementState.equippedWeaponOffset = Integer.parseInt( e.getActionCommand() );
			}
			// show dialog to set Attack Bonus on/off
			if( el.fightingSkillBonus != 0 ) {
				showFightingSkillBonusChooserMenu(el);
			}
			mainViewControl.mainControl.mainFrame.repaint();
			mainViewControl.mainControl.serverBridge.sendArmy(el.armyUnit.army);
			//weaponChooserMenu.hide();
		}
	};
	  for( int w = 0; w < e.meleeWeaponType.length; w++ ) {
		  JMenuItem menuItem = new JMenuItem(e.meleeWeaponType[w]+":"+e.fightingSkillClosedCombat[w]);
		  menuItem.setActionCommand(""+w);
		  menuItem.addActionListener(al);
		  weaponChooserMenu.add(menuItem);
	  }
	  int x = (int)e.elementState.getMapPosition(this).getCenterX();
	  int y = (int)e.elementState.getMapPosition(this).getCenterY();
	  weaponChooserMenu.show(this, y, x);
  }
  
  public void showFightingSkillBonusChooserMenu(ArmyElement e) {
	  fightingSkillBonusChooserMenu.removeAll();
	  ActionListener al = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ArmyElement el = ((SatelliteData)prevSelected.get(ControlSource.LOCAL_PLAYER).getSatelliteData()).element;
			for( ArmyElement unitElement : el.armyUnit.elements ) {
				unitElement.elementState.useFightingSkillBonus = Boolean.parseBoolean( e.getActionCommand() );
			}
			mainViewControl.mainControl.mainFrame.repaint();
			mainViewControl.mainControl.serverBridge.sendArmy(el.armyUnit.army);
			//weaponChooserMenu.hide();
		}
	};
	  JMenuItem menuItem = new JMenuItem("AttackBonus ON");
	  menuItem.setToolTipText("When Order=Attack , only in first melee Phase");
	  JMenuItem menuItem2 = new JMenuItem("AttackBonus OFF");
	  menuItem.setActionCommand("true");
	  menuItem2.setActionCommand("false");
	  menuItem.addActionListener(al);
	  menuItem2.addActionListener(al);
	  fightingSkillBonusChooserMenu.add(menuItem);
	  fightingSkillBonusChooserMenu.add(menuItem2);
	  
	  int x = (int)e.elementState.getMapPosition(this).getCenterX();
	  int y = (int)e.elementState.getMapPosition(this).getCenterY()-100;
	  fightingSkillBonusChooserMenu.show(this, y, x);
  }
}
