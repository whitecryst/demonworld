package demonworld.calculation;

import java.util.Random;

import demonworld.model.ArmyElement;

public class FightCalculator {
	
	
	
	public static int getMinimumDieValueForDestroy(int bonusSaldo) {
		return 20 - bonusSaldo;
	}
	
	
	public static boolean isElementDestroyed(
			int dieValue, // würfelwurf
			ArmyElement attacker, 
			ArmyElement defender, 
			int supporter, 
			AttackFromDirection attackFrom, 
			boolean fightingSkillBonus) {
		
		int offset = calculateMeleeOffset(attacker, defender, supporter, attackFrom, fightingSkillBonus);
		if( dieValue + offset >= 20 ) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int getRandomW20() {
	
		// return random Number between 1-20
		Random rand = new Random();	

		return (rand.nextInt(20) + 1); 
	}
	/**
	 * berechnet alle Boni für einen Nahkampf gegeneinander und gibt den Saldo aller boni zurück
	 * @return
	 */
	public static int calculateMeleeOffset( 
			ArmyElement attacker, 
			ArmyElement defender, 
			int supporter, 
			AttackFromDirection attackFrom, 
			boolean fightingSkillBonus) {
		int result=0;
		
		// + Angriffswert
		result += attacker.fightingSkillClosedCombat[ attacker.elementState.equippedWeaponOffset ];
		
		// ggf + Angriffsbonus (z.B. Reiterei)
		if( fightingSkillBonus ) {
			result += attacker.fightingSkillBonus;
		}
		
		// + Angreifer Größe
		result += attacker.size;
		
		// Attackdirection Bonus
		if( attackFrom.equals( AttackFromDirection.SIDE ) ) {
			result += 2; // +2 für Flanke
		} else if ( attackFrom.equals( AttackFromDirection.BACK ) ) {
			result += 4; // +4 für Rücken
		}
		
		// + große Angreifer-Gruppen Bonus
		if( attacker.figureAmount == 5 ) {
			result += 2;
		}  

		// + wenige Verteidiger Bonus
		if( defender.figureAmount < 4 ) {
			result += 1;
		}
		
		//TODO: Pikenbonus (-1 wenn piken von vorn attackiert werden)
		
		// + 3 pro Unterstützer
		result += (supporter * 3);
		
		// - Verteidiger Größe
		result -= defender.size;
		
						
		// - Verteidiger Rüstung
		result -= defender.armorPoints;
		
		return result;
	}
	
	public static int calculateShootingOffset( 
			ArmyElement attacker,
			ArmyElement defender,
			int distance,
			int supporter) {
		
		int result = 0;
		// find Battle Factor for used weapon
		
		if( attacker == null ) {
			return -1;
		} else {
			if( attacker.distancesForLongRangeSkill == null ) {
				return -1;
			}
		}
		int distanceIndex = -1;
		for( int i=0; i < attacker.distancesForLongRangeSkill.length; i++ ) {
			if( distance <= attacker.distancesForLongRangeSkill[ i ] ) {
				distanceIndex = i;
				break;
			}
		}
		if( distanceIndex == -1 ) { // out of range
			return -1;
		}
		// + battle index for used weapon
		result += attacker.fightingSkillLongRange[ distanceIndex ];
		
		//System.out.println( "distanceIndex:"+distanceIndex );
		//System.out.println( "weapon:"+attacker.fightingSkillLongRange[ distanceIndex ] );
		
		// + 2 per supporter
		result += (supporter * 2);
		
		// - wenige Angreifer Malus
		if( attacker.figureAmount < 4 ) {
			//TODO: auch malus, wenn Einheit mit weniger als 4 Figuren supported
			result -= 1;
		}
		
		// - Verteidiger Rüstung
		result -= defender.armorPoints;
				
		return result;
	}
	
	public static enum AttackFromDirection {FRONT, SIDE, BACK};
}
