import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

import demonworld.calculation.FightCalculator;
import demonworld.calculation.FightCalculator.AttackFromDirection;
import demonworld.init.ArmyFactory;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyType;


public class TestFightCalculation {

	@Test
	public void test() {
		
		ArmyFactory armyFactory = new ArmyFactory(null);
		FightCalculator fight = new FightCalculator();
		
		//Army imperials 	= armyFactory.createArmy( ArmyType.IMPERIAL );
		//Army orcs 		= armyFactory.createArmy( ArmyType.ORCS );
		
		HashMap<ArmyType, Army> armys = armyFactory.initFromFile( new File("./resources/Armys.csv") );
		
		Army imperials = armys.get( ArmyType.IMPERIAL );
		Army orcs = armys.get( ArmyType.ORCS );
		
		ArmyElement e1 = imperials.getElements().get(0);
		ArmyElement e2 = orcs.getElements().get(0);
		
		// result 2 = weapon 5 - armor 2 + aSize 1 - dSize 2 + alGroup 0 + dsgroup 0 + charge 0 - pikesFront 0 + flank 0 + back 0 +support 0 
		int offset1 = fight.calculateMeleeOffset(e1, e2, 0, AttackFromDirection.FRONT, false);
		
		// result 5 = weapon 5 - armor 2 + aSize 1 - dSize 2 + alGroup 0 + dsgroup 0 + charge 0 - pikesFront 0 + flank 0 + back 0 +support 3
		int offset2 = fight.calculateMeleeOffset(e1, e2, 1, AttackFromDirection.FRONT, false);
		
		// result 4 = weapon 5 - armor 2 + aSize 1 - dSize 2 + alGroup 0 + dsgroup 0 + charge 0 - pikesFront 0 + flank 2 + back 0 +support 0 
		int offset3 = fight.calculateMeleeOffset(e1, e2, 0, AttackFromDirection.SIDE, false);
		
		// result 6 = weapon 5 - armor 2 + aSize 1 - dSize 2 + alGroup 0 + dsgroup 0 + charge 0 - pikesFront 0 + flank 0 + back 4 +support 0 
		int offset4 = fight.calculateMeleeOffset(e1, e2, 0, AttackFromDirection.BACK, false);
		
		// result 2 = weapon 5 - armor 2 + aSize 1 - dSize 2 + alGroup 0 + dsgroup 0 + charge 0 - pikesFront 0 + flank 0 + back 4 +support 0 
		int offset5 = fight.calculateMeleeOffset(e1, e2, 0, AttackFromDirection.BACK, true);
		
		// result 5 = weapon 7 - armor 2 + support 0 - asgroup 0
		int offset6 = fight.calculateShootingOffset(e1, e2, 4, 0);
		
		// result 7 = weapon 7 - armor 2 + support 2 - asgroup 0
		int offset7 = fight.calculateShootingOffset(e1, e2, 4, 1);
		
		// result 1 = weapon 3 - armor 2 + support 0 - asgroup 0
		int offset8 = fight.calculateShootingOffset(e1, e2, 7, 0);
		
		assertEquals("test1 melee", 2, offset1);
		assertEquals("test2 melee", 5, offset2);
		assertEquals("test3 melee", 4, offset3);
		assertEquals("test4 melee", 6, offset4);
		assertEquals("test5 melee", 6, offset5);
		
		assertEquals("test6 shooting", 5, offset6);
		assertEquals("test7 shooting", 7, offset7);
		assertEquals("test8 shooting", 1, offset8);
		
		
		
	}

}
