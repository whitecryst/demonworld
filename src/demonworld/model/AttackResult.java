package demonworld.model;

import java.io.Serializable;

public class AttackResult implements Serializable{
	private Position hexCoords;
	private boolean elementSurvivedAttack;
	
	public AttackResult(Position hexCoords, boolean elementSurvivedAttack) {
		this.hexCoords = hexCoords;
		this.elementSurvivedAttack = elementSurvivedAttack;
	}
	
	public Position getHexCoords() {
		return hexCoords;
	}
	public void setHexCoords(Position hexCoords) {
		this.hexCoords = hexCoords;
	}
	public boolean isElementSurvivedAttack() {
		return elementSurvivedAttack;
	}
	public void setElementSurvivedAttack(boolean elementSurvivedAttack) {
		this.elementSurvivedAttack = elementSurvivedAttack;
	}
	
	
}
