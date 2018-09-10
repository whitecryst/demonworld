package demonworld.model;

import java.io.Serializable;

import biz.pavonis.hexameter.api.Hexagon;
import demonworld.calculation.FightCalculator.AttackFromDirection;
import demonworld.map.HexagonPanel;

public class ElementState implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3277215207723819068L;
	//public Hexagon mapPosition;
	public Position position = new Position(-1,-1,-1);
	public ViewDirection viewDirection;
	public AttackFromDirection attackFromDirection;
	public ArmyElement aimsAt; // element aims at another element (for Ranged Fight preparation)
	public boolean aimsAtTargetInMelee = false; // indicate, that element is giong to shoot into a melee fight
	public int aimingDistance; // distance to target
	//public ArmyElement aimedFrom; // element is aimed from another element (for Ranged Fight preparation)
	public ArmyElement attackAt; // element prepare Meele Attack at another element (for Melee Fight preparation)
	public ArmyElement supportAttack; // element support another element in meele or ranged attack (link to supported attacker element)
	public boolean markedAsDead = false; // mark army as Dead but dont remove it yet ( e.g in rangedFight)
	public boolean markedAsSurvived = false; // mark army as survived after beeing attacked
	public int equippedWeaponOffset = 0; // set offset of weapon, currently used for melee. regarded to fightingSkillClosedCombat[] and meleeWeaponType[]
	public boolean useFightingSkillBonus = false;
	public int usedMovePoints = 0;
	public int usedManeuverPoints = 0;
	
	public ElementState() {
		viewDirection = ViewDirection.TOP;
	}
	
	public void setMapPosition( Hexagon hex ) {
	
		if( hex != null ) {
			position.x = hex.getGridX();
			position.y = hex.getGridY();
			position.z = hex.getGridZ();
		} else {
			position.x = -1;
			position.y = -1;
			position.z = -1;
		}
	}
	
	public Hexagon getMapPosition( HexagonPanel hexPanel ) {
		return hexPanel.getHexByGridCoordinate(position.x, position.z); // X and Z is used
	}
	
	public ElementState copy() {
		ElementState copy = new ElementState();
		copy.position = position.getCopy();
		copy.viewDirection = viewDirection;
		copy.attackFromDirection = attackFromDirection;
		copy.aimsAt = null; // cannot copy reference
		copy.attackAt = null; // cannot copy reference
		copy.supportAttack = null; // cannot copy reference
		copy.markedAsDead = markedAsDead;
		copy.equippedWeaponOffset = equippedWeaponOffset;
		copy.useFightingSkillBonus = useFightingSkillBonus;
		copy.usedMovePoints = usedMovePoints;
		copy.usedManeuverPoints = usedManeuverPoints;
		return copy;
		
	}
}
