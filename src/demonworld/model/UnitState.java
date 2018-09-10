package demonworld.model;

import java.io.Serializable;

public class UnitState implements Serializable{
	//private Orientation orientation // Ausrichtung (Blickrichtung)
	public UnitOrder order = null;//UnitOrder.NO_ORDER; // aktueller Befehl (bewegen, plänkeln, angreifen, halten)
	public boolean isOrganized; // gibt an ob die Einheit geordnet ist
	public boolean isEngagedInMeele = false;
	public boolean engagedInMeleeThisRound = false;
	
	public int usedManeuverPoints, usedMovePoints;
	
}

