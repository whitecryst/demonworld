package demonworld.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import demonworld.controller.GameMode;
import demonworld.controller.server.ControlSource;
import biz.pavonis.hexameter.api.HexagonalGrid;

/**
 * bündelt die datenelemente für ein Spiel
 * @author kbailly
 *
 */

public class GameState implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -560688178001393462L;
	
	public List<ArmyElement> availableArmyElements; // for setup Armys on the map at beginning of game
	public HashMap<ControlSource, Army> playerArmys = new HashMap<ControlSource, Army>();
	public HashMap<ControlSource, Color> playerColor = new HashMap<ControlSource, Color>();
	public transient HexagonalGrid map;
	public GameMode currentGameMode;
	
	public GameState() {
		playerColor.put(ControlSource.LOCAL_PLAYER, Color.decode("#2B65EC"));
		playerColor.put(ControlSource.EXTERNAL_PLAYER, Color.decode("#F62217"));
	}
	
	public void init(List<ArmyElement> availableArmyElements, HexagonalGrid map) {
		this.availableArmyElements = availableArmyElements;
		this.map = map;
		
	}
	
	public Vector<ArmyElement> getAllArmyElementsFromAllPlayers() {
		Vector<ArmyElement> allElements = new Vector<ArmyElement>();
		for( ControlSource source : ControlSource.values() ) {
			if( playerArmys.get(source) != null ) {
				allElements.addAll( playerArmys.get(source).getElements() );
			}
		}
		return allElements;
	}
	
	public boolean equals( GameState otherState ) {
		boolean equal = true;
		if( this.currentGameMode != otherState.currentGameMode ) {
			equal = false;
		} else if( ! this.playerArmys.equals(otherState.playerArmys) ) {
			equal = false;
		}  
		System.out.println( "GameState changed:"+!equal );
		return equal;
	}
	
	
	
}
