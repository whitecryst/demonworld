package demonworld.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;

import biz.pavonis.hexameter.api.Hexagon;

public interface MapDrawer {

	
	
	public void paint(Graphics g);

	public void drawEmptyHexagon(Graphics gc, Hexagon hexagon);
	
	public void drawFilledHexagon(Graphics gc, Hexagon hexagon);
	
	public void drawNeighborHexagon(Graphics gc, Hexagon hexagon);

	public void drawColoredHexagon(Graphics gc, Hexagon hexagon, Color c);
}
