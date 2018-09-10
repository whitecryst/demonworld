package demonworld.tools;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import demonworld.controller.GameMode;

public class TextManager {
	public enum ContentID { TEST, Mode_PLACE_ORDERS, Mode_FIGHT_RANGED_SKIRMISH, Mode_MOVE, Mode_FIGHT_RANGED_HOLD, Mode_FIGHT_MEELE, Mode_PREPARE_NEXTROUND  };
	public HashMap<ContentID, String> textContent ;
	
	public TextManager() {

	    textContent = new HashMap<ContentID, String>();
		initContent();
		
	}
	

	
	public String getText( ContentID contentID ) {
		if( !textContent.containsKey(contentID) ) {
			System.err.println( "AnnouncementPanel: unable to load textContent for contentID:"+contentID );
			return "";
		} 
		String text = "<html><head></head><body>";
		text += textContent.get(contentID).replaceAll("\n","<br>");
		text += "</body></html>";
		return text;
		
	}
	
	private void initContent() {
		textContent.put(ContentID.TEST, "Test");
		
		textContent.put(ContentID.Mode_PLACE_ORDERS, "<b>ORDER PHASE</b><br>"
				+ "Lay out one hidden order counter for each unit not engaged in melee.\n"
				+ "Units with order counters forgotten have a Hold order.<br><br>"
				+ "– All units with <i>Move orders(M)</i> may move (but not attack). The player with lesser Move orders decides whether all of his qualified units move/ maneuver first or second.<br>"
				+ "– All units with <i>Skirmish orders(S)</i> may move/ maneuver (sequence as above).<br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;If an enemy is attacked that has not yet moved, he may, at the time of contact, perform turns and/or re-group moves. <br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;Thereafter, the attacker’s remaining movement is finalized.<br>"
				+ "– All units with <i>Attack orders(A)</i> may move/maneuver (sequence and enemy’s reaction as above).<br>"
				+ "– All units with <i>Hold orders(H)</i> may perform turns and/ or re-group moves, but may not contact an enemy (sequence as above).<br><br>"
				+ "In first phase of melee only: units with Attack or Hold order Initiative + 2, Skirmish order Initiative + 1");
		
		textContent.put(ContentID.Mode_FIGHT_RANGED_SKIRMISH, "<b>FIRST SHOOTING PHASE</b><br>"
				+ "– Announce all shooting attacks of units with Skirmish orders in orderly formation that are not engaged in melee; then resolve them simultaneously.<br>"
				+ "– Remove casualties.<br>"
				+ "– Elements that are not in melee and belong to units that suffered shooting casualties may move one field onto vacated fields.<br>");
		
		textContent.put(ContentID.Mode_FIGHT_RANGED_HOLD, "<b>SECOND SHOOTING PHASE</b><br>"
				+ "– Units with Hold orders may shoot as long as they are not engaged in melee from previous turns.<br>"
				+ "– Elements that are not in melee and belong to units that suffered shooting casualties may move one field onto vacated fields.<br>");
		
		textContent.put(ContentID.Mode_MOVE, "<b>MOVEMENT PHASE</b><br>"
				+ "– All units with <i>Move orders(M)</i> may move (but not attack). The player with lesser Move orders decides whether all of his qualified units move/ maneuver first or second.<br>"
				+ "– All units with <i>Skirmish orders(S)</i> may move/ maneuver (sequence as above).<br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;If an enemy is attacked that has not yet moved, he may, at the time of contact, perform turns and/or re-group moves. <br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;Thereafter, the attacker’s remaining movement is finalized.<br>"
				+ "– All units with <i>Attack orders(A)</i> may move/maneuver (sequence and enemy’s reaction as above).<br>"
				+ "– All units with <i>Hold orders(H)</i> may perform turns and/ or re-group moves, but may not contact an enemy (sequence as above).<br><br>"
				+ "<b>MOVEMENT:</b><br>"
				+ "orderly unit in direction of the front fields 3 MPs per field. Disorganized unit or movement in other direction 1 maneuver per field. <br>"
				+ "Columns only pay MPs (preceding element must be in front field; turns performed to accomplish this do not cost maneuvers). <br>"
				+ "Units with ≤3 elements execute turns for free and may not wheel.<br>"
				+ "<b>RE-GROUP MOVES:</b>1 maneuver/field for furthest movement.<br>"
				+ "<b>TURN:</b> 1 maneuver regardless of number of elements that turned or angle.<br>"
				+ "If organized before turning and disorganized afterwards, free re-group move by 1 field into orderly formation allowed.<br>"
				+ "<b>WHEEL:</b> 1 maneuver per 60°- or 120°-turn; no left and right wheel in the same movement phase.<br><br>"
				+ "First maneuver costs first half of MPs, 2nd and 3rd maneuver second half.");
		
		textContent.put(ContentID.Mode_FIGHT_MEELE, "<b>MELEE PHASE</b><br>"
				+"– All elements in melee attack in the sequence of their momentary initiative.<br>"
				+"– Remove casualties.<br>"
				+"– Elements of units in melee (not) in contact with an enemy may move one (two) field(s) and turn.<br><br>"
				+ " The player who has inflicted the highest losses in a melee decides whether all of his elements in this melee move and/or turn first or second.<br>"
				+"Units not yet in melee may only attack if they are in organized formation, have Attack or Skirmish orders and move into melee in the direction of their front fields.<br>"
				+"In first phase of melee only: units with Attack or Hold order +2, Skirmish order +1, spear or lance +1, pike +2 on initiative.<br>");
	}	

	

}
