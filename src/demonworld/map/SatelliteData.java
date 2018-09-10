package demonworld.map;

import java.util.HashMap;

import demonworld.controller.server.ControlSource;
import demonworld.model.ArmyElement;

public class SatelliteData {

	private HashMap<ControlSource, Boolean> isSelected = new HashMap<ControlSource, Boolean>();

	public boolean isSelected(ControlSource playerSource) {
		if( !isSelected.containsKey(playerSource) ) {
			isSelected.put(playerSource, new Boolean(false));
		}
		return isSelected.get(playerSource);
	}

	public void setSelected(ControlSource playerSource, boolean isSelected) {
		this.isSelected.put(playerSource, new Boolean(isSelected));
	}
	
	public ArmyElement element;

}
