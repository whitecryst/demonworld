package demonworld.controller;

import demonworld.map.HexagonPanel;
import demonworld.model.ArmyElement;
import demonworld.view.InfoBox;
import demonworld.view.MainFrame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;

public class MainViewController
  implements MouseInputListener, ChangeListener
{
  public InfoBox infoBox;
  public HexagonPanel hexPanel;
  public GameMode currentGameMode = GameMode.SETUP_ARMYS;
  public MainController mainControl;
  public ArmyElement currentInfoElement;
  public ArmyElement defaultInfoElement;
  public Image defaultImg = null ;
  
  public MainViewController(MainController mc)
  {
    this.mainControl = mc;
  }
  
  public void mouseClicked(MouseEvent e) {}
  
  public void mousePressed(MouseEvent e) {}
  
  public void mouseReleased(MouseEvent e) {}
  
  public void mouseEntered(MouseEvent e) {}
  
  public void mouseExited(MouseEvent e) {}
  
  public void mouseDragged(MouseEvent e) {}
  
  public void mouseMoved(MouseEvent e) {}
  
  public void showArmyElementInfo(ArmyElement element)
  {
    if (element != null)
    {
     // if (!element.equals(this.currentInfoElementCopy))
     // {
    	mainControl.mainFrame.infoIcon.setImage( mainControl.mainImageControl.getArmyElementPortrait(element) );
        this.mainControl.mainFrame.infoLabel.setText(element.getHtmlFormattedInfo());
        this.currentInfoElement = element;
      //}
    }
    else if (this.defaultInfoElement != null)
    {
      this.mainControl.mainFrame.infoLabel.setText(this.defaultInfoElement.getHtmlFormattedInfo());
      mainControl.mainFrame.infoIcon.setImage( mainControl.mainImageControl.getArmyElementPortrait(defaultInfoElement) );
      //this.mainControl.mainFrame.infoLabel.repaint();
    }
    else if (this.currentInfoElement != null)
    {
    	mainControl.mainFrame.infoIcon.setImage( mainControl.mainImageControl.emptyImage );
    	this.mainControl.mainFrame.infoLabel.setText("<html><body><table width=270><tr><td>&nbsp;</td></tr></table></body></html>");
      this.currentInfoElement = null;
    }
  }
  
  public void stateChanged(ChangeEvent e)
  {
    JSlider source = (JSlider)e.getSource();
    source.getValueIsAdjusting();
  }
}
