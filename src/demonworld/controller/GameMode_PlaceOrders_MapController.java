package demonworld.controller;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;
import demonworld.controller.server.ControlSource;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.HexagonPanel;
import demonworld.map.SatelliteData;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyUnit;
import demonworld.model.CursorModel;
import demonworld.model.GameState;
import demonworld.model.UnitOrder;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;

public class GameMode_PlaceOrders_MapController
  implements InputHandlerInterface
{
  private HexagonPanel hexPanel;
  private CursorModel<UnitOrder> currentElement;
  private Vector<CursorModel<UnitOrder>> elementList = new Vector();
  private final String DELETE = "Delete";
  private ControlSource playerSource;
  private boolean mouseOverElement = false;
  
  public HexagonPanel getHexPanel()
  {
    return this.hexPanel;
  }
  
  public GameMode_PlaceOrders_MapController(HexagonPanel hexPanel, ControlSource playerSource)
  {
    this.playerSource = playerSource;
    this.hexPanel = hexPanel;
    init();
  }
  
  public void init()
  {
    setOrderChooser(true);
    setActionChooser(true);
    
    updateCursorElement(0);
    this.hexPanel.mainViewControl.defaultInfoElement = null;
   // this.hexPanel.mainViewControl.showArmyElementInfo(null);
  }
  
  private void setOrderChooser(boolean enable)
  {
    UnitOrder[] arrayOfUnitOrder;
    int j;
    int i;
    Image scaledImg;
    if (enable)
    {
      j = (arrayOfUnitOrder = UnitOrder.values()).length;
      for (i = 0; i < j; i++)
      {
        UnitOrder order = arrayOfUnitOrder[i];
        BufferedImage img = null;
        try
        {
          if (this.playerSource.equals(ControlSource.LOCAL_PLAYER)) {
            img = this.hexPanel.getCursorOverlay(ImageIO.read(new File("resources/images/Order_" + order.name() + ".png")));
          } else {
            img = this.hexPanel.getCursorOverlay(ImageIO.read(new File("resources/images/Order_NO_ORDER.png")));
          }
        }
        catch (IOException localIOException) {}
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(img, 0);
        
        CursorModel<UnitOrder> newElement = new CursorModel(
          order.name(), 
          toolkit.createCustomCursor(scaledImg, new Point(0, 0), "img"), 
          order,
          scaledImg);
        this.elementList.add(newElement);
      }
    }
    else
    {
      j = (arrayOfUnitOrder = UnitOrder.values()).length;
      for (i = 0; i < j; i++)
      {
        UnitOrder el1 = arrayOfUnitOrder[i];
        int elPos = -1;
        for (CursorModel<UnitOrder> el2 : this.elementList) {
          if (el1.name().equals(el2.name)) {
            elPos = this.elementList.indexOf(el2);
          }
        }
        if (elPos != -1) {
          this.elementList.remove(elPos);
        }
      }
    }
  }
  
  private void setActionChooser(boolean enable)
  {
    if (enable)
    {
      BufferedImage img = null;
      try
      {
        img = ImageIO.read(new File("resources/images/delete.png"));
      }
      catch (IOException localIOException) {}
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Image scaledImg = ((GeneralMapDrawer)this.hexPanel.mapDrawer).getScaledImageFitInHexagon(img, 0);
      CursorModel<UnitOrder> newElement = new CursorModel("Delete", toolkit.createCustomCursor(scaledImg, new Point(0, 0), "img"), null, scaledImg);
      this.elementList.add(newElement);
    }
  }
  
  public void mouseClicked(MouseEvent e)
  {
    try
    {
      Hexagon hex = this.hexPanel.getHexByPixelCoordinate(e.getY(), e.getX());
      
      SatelliteData data = (SatelliteData)hex.getSatelliteData();
      data.setSelected(this.playerSource, true);
      if (this.currentElement.name.equals("Delete")) {
        data.element.armyUnit.status.order = null;
      } else if (data.element != null) {
        data.element.armyUnit.status.order = ((UnitOrder)this.currentElement.obj);
      }
      hexPanel.mainViewControl.mainControl.updateGameStateHistory();
      // send Army to other Player
      if( playerSource == ControlSource.LOCAL_PLAYER ) {
    	  hexPanel.mainViewControl.mainControl.serverBridge.sendArmy(hexPanel.mainViewControl.mainControl.gameState.playerArmys.get(playerSource));
      }
      hexPanel.setSelectedHexagon( playerSource, hex ); 
      this.hexPanel.repaint();
    }
    catch (HexagonNotFoundException localHexagonNotFoundException) {}
  }
  
  public void mousePressed(MouseEvent e) {}
  
  public void mouseReleased(MouseEvent e) {}
  
  public void mouseEntered(MouseEvent e) {}
  
  public void mouseExited(MouseEvent e) {}
  
  public void mouseDragged(MouseEvent e) {}
  
  public void mouseMoved(MouseEvent e)
  {
	  hexPanel.mouseCursorPos.put(playerSource, e.getPoint());
    if( playerSource == ControlSource.LOCAL_PLAYER ) { // infofeld nur updaten local, nicht beim andern spieler
		Point p = e.getPoint();
		
		ArmyElement el = null;
		try {
			el = ((SatelliteData)this.hexPanel.getHexByPixelCoordinate(e.getPoint().y, e.getPoint().x).getSatelliteData()).element;
		} catch( HexagonNotFoundException exc ) {}
		
	    if ((el == null) && (this.mouseOverElement)) // cursor auf leerem Feld, davor auf besetztem feld
	    {
	      this.mouseOverElement = false;
	     // this.hexPanel.repaint();
	    }
	 // cursor auf besetztem Feld, davor auf leerem feld oder anderem element als jetzt
	    else if ((el != null) && ( (!this.mouseOverElement) || (el != hexPanel.mainViewControl.currentInfoElement) )) 
	    {
	      this.mouseOverElement = true;
	      hexPanel.mainViewControl.currentInfoElement = el;
	      this.hexPanel.mainViewControl.showArmyElementInfo(el);
	      //this.hexPanel.repaint();
	    }
	    
	    hexPanel.requestFocus();
    }	
    this.hexPanel.repaint();
  }
  
  public void mouseWheelMoved(MouseWheelEvent e)
  {
	  if( playerSource == ControlSource.LOCAL_PLAYER ) {
		  int offset = e.getWheelRotation();
		  changeActElement(offset);
	  }  
  }
  
  private void changeActElement(int offset)
  {
    int currentElementPos = this.elementList.indexOf(this.currentElement);
    if ((currentElementPos + offset < this.elementList.size()) && (currentElementPos + offset >= 0)) {
      currentElementPos += offset;
    } else if (currentElementPos + offset >= this.elementList.size()) {
      currentElementPos = this.elementList.size() % offset;
    } else if (currentElementPos + offset < 0) {
      currentElementPos = this.elementList.size() - 1 - this.elementList.size() % (offset * -1);
    }
    updateCursorElement(currentElementPos);
  }
  
  private void updateCursorElement(int currentElementPos)
  {
    if ((currentElementPos >= 0) && (currentElementPos < this.elementList.size()))
    {
      this.currentElement = ((CursorModel)this.elementList.get(currentElementPos));
      
      this.hexPanel.updateCursorImage(this.currentElement.c);
    }
  }
  
  public void keyTyped(KeyEvent e) {}
  
  public void keyPressed(KeyEvent e)
  {
    int keyCode = e.getKeyCode();
    switch (keyCode)
    {
    case 38: 
      changeActElement(1);
      break;
    case 40: 
      changeActElement(-1);
      break;
    case 37: 
      changeActElement(0);
      break;
    case 39: 
      updateCursorElement(this.elementList.size() - 1);
      break;
    }
  }
  
  public void keyReleased(KeyEvent e) {}
  
  public void endPhase() {}
  
  public void actionPerformed(ActionEvent e) {}
}
