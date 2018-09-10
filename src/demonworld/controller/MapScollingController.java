package demonworld.controller;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import demonworld.init.DWPlayer;

public class MapScollingController implements MouseMotionListener, MouseListener {
	private JScrollPane scrollPane;
	private int scrollFactor = 40;
	private int scrollBorderWidth =50;
	private int xOffset=0, yOffset=0;
	private JPanel map;
	public boolean scrollingActive = false;
	
	private JViewport viewPort;
	private Point vpp;
	private Timer timer;
	
	public MapScollingController( JScrollPane scrollPane, JPanel map ) {
		this.scrollPane = scrollPane;
		viewPort = scrollPane.getViewport();
		this.map = map;
		//scrollThread = new Thread( new MapScrolling(this) );
		
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
        map.scrollRectToVisible(r);

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		//System.out.println(e.getX()+"/"+e.getY());
		boolean update = false;
        vpp = viewPort.getViewPosition();
        double x = e.getX()-vpp.getX();
        
		if( x > (viewPort.getWidth() - scrollBorderWidth) ) {
			xOffset = (int)Math.round( ((50-(viewPort.getWidth()-(e.getX()-vpp.getX()))) / scrollBorderWidth) * scrollFactor);
			update = true;
		} else if( x < scrollBorderWidth ) {
			xOffset = (int)Math.round( (-1)* ((50-(e.getX()-vpp.getX())) / scrollBorderWidth) * scrollFactor);
			//xOffset = scrollFactor * (-1);
			update = true;
		} 
		
		double y = e.getY()-vpp.getY();
		if( y > (viewPort.getHeight() - scrollBorderWidth) ) {
			//yOffset = scrollFactor;
			yOffset = (int)Math.round( ((50-(viewPort.getHeight()-(e.getY()-vpp.getY()))) / scrollBorderWidth) * scrollFactor);
			update = true;
		} else if( y < scrollBorderWidth) {
			//yOffset = scrollFactor * (-1);
			yOffset = (int)Math.round( (-1)* ((50-(e.getY()-vpp.getY())) / scrollBorderWidth) * scrollFactor);
			update = true;
		}
		if( update ) {
			
			if( !scrollingActive ) {
				scrollingActive = true;
				timer = new Timer( );
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						scrollMap();
						
					}}, 50l, 50l);
			}
		} else {
			if( scrollingActive ) {
				scrollingActive = false;
				timer.cancel();
				//scrollThread.interrupt();
			}
		}
		
		
		// 
	}

	public void scrollMap() {
		//System.out.println("scroll"+xOffset);
		vpp = viewPort.getViewPosition();
		vpp.translate(xOffset, yOffset);
		map.scrollRectToVisible(new Rectangle(vpp, viewPort.getSize()));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
		if( scrollingActive ) {
			scrollingActive = false;
			timer.cancel();
		}
		
	}
}


