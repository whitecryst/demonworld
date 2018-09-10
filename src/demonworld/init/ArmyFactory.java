package demonworld.init;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.imageio.ImageIO;

import biz.pavonis.hexameter.example.HexagonPanel;
import demonworld.controller.MainImageController;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyType;
import demonworld.model.ViewDirection;


public class ArmyFactory {
	private MainImageController imageControl;
	
	public ArmyFactory(MainImageController mainImageControl) {
		imageControl = mainImageControl;
	}
	
	public static Army createArmy( ArmyType army ) {
		if( army.equals( army.IMPERIAL ) ){
			//return initImperials();
		} else if ( army.equals( army.ORCS ) ){
			//return initOrcs();
		} 
		return null;
		
	}

	/*
	private static Army initImperials(){
		Army army = new Army( ArmyType.IMPERIAL );
		
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("resources/images/footsoldiers.gif"));
		} catch (IOException e) {
		}
		
		// create first element
		ArmyElement e = new ArmyElement(
				"Imperial Arquebusiers", 
				false, // unitLeader
				img,
				16, //movePointsGeneral, 
				8, //movePointsSkirmish, 
				12, //movePointsAttack, 
				0, //movePointsHold, 
				2, //maneuverPoints, 
				0, //armorPoints, 
				new int[] {7,3}, // fightingSkillLongRange, 
				new int[] {4,7}, //distancesForLongRangeSkill, 
				5, //fightingSkillClosedCombat, 
				2, // fightingSkillBonus, 
				1, //hitPoints, 
				2, //initiative, 
				1, //size, 
				4, //figureAmount, 
				ArmyType.IMPERIAL, //armyMembership, 
				90 //armyValuePoints)
				);
		
		// create unit with 2 elements
		army.addUnit(e, 2);
		
		return army;
	}
	
	private static Army initOrcs(){
		Army army = new Army( ArmyType.ORCS );
		
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("resources/images/defaultElement.gif"));
		} catch (IOException e) {
		}
		
		// create first element
		ArmyElement e = new ArmyElement(
				"Dwarf-Eaters", 
				false, // unitLeader
				img,
				16, //movePointsGeneral, 
				8, //movePointsSkirmish, 
				12, //movePointsAttack, 
				0, //movePointsHold, 
				1, //maneuverPoints, 
				2, //armorPoints, 
				null, // fightingSkillLongRange, 
				null, //distancesForLongRangeSkill, 
				7, //fightingSkillClosedCombat, 
				0, // fightingSkillBonus, 
				1, //hitPoints, 
				2, //initiative, 
				2, //size, 
				4, //figureAmount, 
				ArmyType.ORCS, //armyMembership, 
				180 //armyValuePoints)
				);
		
		// create unit with 1 element
		army.addUnit(e, 1);
		return army;
	}
	*/
	public HashMap<ArmyType,Army> initFromInputStream( InputStream inStream ) {
		
		
		Army armyOrcs = new Army( ArmyType.ORCS );
		Army armyImperials = new Army( ArmyType.IMPERIAL );
		HashMap<ArmyType,Army> armys = new HashMap<ArmyType,Army>();
		armys.put(ArmyType.ORCS, armyOrcs);
		armys.put(ArmyType.IMPERIAL, armyImperials);
		
		Army currentArmy = null;
		String csvSplitBy = ";";
		BufferedReader br = null;
		
		
		
		try {
			String line;
			br = new BufferedReader(new InputStreamReader(inStream));
			// first line has no values
			br.readLine();

			while ((line = br.readLine()) != null) {
				//System.out.println( line );
			    // use comma as separator
				String[] armyElementValues = line.split(csvSplitBy);
				System.out.println( "read ArmyElement:"+armyElementValues[0]+"-"+armyElementValues[1] );
				// define ArmyType
				ArmyType aType = null;
				if( armyElementValues[0].toLowerCase().equals("orcs") ) {
					aType = ArmyType.ORCS;
					currentArmy = armyOrcs;
				} else if( armyElementValues[0].toLowerCase().equals("imperial") ) {
					aType = ArmyType.IMPERIAL;
					currentArmy = armyImperials;
				} else {
					System.err.println( "unknown Army: "+armyElementValues[0] );
					return null;
				}
				
				//define ShootingValue Arrays
				int[] fightingSkillLongRange = null;
				int[] distancesForLongRangeSkill = null;
				int[] fightingSkillClosedCombat = null;
				String[] meleeWeaponType = null;
				if( !armyElementValues[9].equals("") ) {
					String[] s1 = armyElementValues[10].split(","); // fightingSkillLongRange, 
					String[] s2 = armyElementValues[11].split(","); //distancesForLongRangeSkill,
					String[] s3 = armyElementValues[8].split(","); //distancesForLongRangeSkill,
					String[] s4 = armyElementValues[9].split(","); //distancesForLongRangeSkill,

					if( s1.length == s2.length ) {
						//System.out.println(""+armyElementValues[1]+s1);
						if( !s1[0].equals("") ) {
							fightingSkillLongRange = new int[s1.length];
							distancesForLongRangeSkill = new int[s2.length];
							for( int i = 0; i<s1.length; i++ ) {
								//System.out.println("i:"+i+" s1:"+s1[i]);
								fightingSkillLongRange[ i ] = Integer.parseInt( s1[i] );
								distancesForLongRangeSkill[ i ] = Integer.parseInt( s2[i] );
							}
						}
					} else {
						System.err.println( "Wrong size in shootingRange Array" );
					}
					
					if( s3.length == s4.length ) {
						//System.out.println(""+armyElementValues[1]+s1);
						if( !s3[0].equals("") ) {
							fightingSkillClosedCombat = new int[s3.length];
							meleeWeaponType = new String[s4.length];
							for( int i = 0; i<s3.length; i++ ) {
								fightingSkillClosedCombat[ i ] = Integer.parseInt( s3[i] );
								meleeWeaponType[ i ] = s4[i];
							}
						}
					} else {
						System.err.println( "Wrong size in meleeWeaponSkill/Type Array" );
					}
				}
				
				// set icon 
				File icon = new File("resources/images/"+ armyElementValues[20]);
				File portrait = new File("resources/images/portraits/"+ armyElementValues[21]);
				
				// add Icon to ImageController
				String unitDesignation = armyElementValues[1];
				if( imageControl != null ) {
					
					imageControl.addArmyElementImage(unitDesignation, icon);
					imageControl.addArmyElementPortrait(unitDesignation, portrait);
				}
				
				
				// create Element
				ArmyElement e = new ArmyElement(
						unitDesignation,  
						false, //unitLeader
						icon,
						portrait,
						Integer.parseInt(armyElementValues[2]), //movePointsGeneral, 
						Integer.parseInt(armyElementValues[4]), //movePointsSkirmish, 
						Integer.parseInt(armyElementValues[3]), //movePointsAttack, 
						Integer.parseInt(armyElementValues[5]), //movePointsHold, 
						//Integer.parseInt(armyElementValues[6]), //maneuverPoints,
						2, // in basic game, all elements have 2 maneuvers!! (rules page 26 top_right)
						Integer.parseInt(armyElementValues[7]), //armorPoints, 
						fightingSkillLongRange, 
						distancesForLongRangeSkill,  
						fightingSkillClosedCombat,
						meleeWeaponType,
						Integer.parseInt(armyElementValues[12]), // fightingSkillBonus, 
						Integer.parseInt(armyElementValues[13]), //hitPoints, 
						Integer.parseInt(armyElementValues[14]), //initiative, 
						Integer.parseInt(armyElementValues[15]), //size, 
						Integer.parseInt(armyElementValues[16]), //figureAmount, 
						aType, //armyMembership, 
						Integer.parseInt(armyElementValues[17]) //armyValuePoints)
						);
				
				currentArmy.addElement( e );
				
				// set default number of elements
				int defaultNumberOfElements = Integer.parseInt(armyElementValues[18]);
				//System.out.println("elNumber: "+defaultNumberOfElements);
				e.armyUnit.defaultNumberOfElements = defaultNumberOfElements;
				
				// first element of a unit gets the unitLeader status
				// this causes trouble in Gamemode STEUP_ARMYS case all units have the unitLeader status
				e.unitLeader = false;
	 
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return armys;

	}
	
	
	public HashMap<ArmyType,Army> initFromFile( File csvFile ) {
		
		if( csvFile.exists() ) {
				try {
					return initFromInputStream(new FileInputStream(csvFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		} else {
			System.err.println("File not found:"+csvFile.getPath());
		
		}
		return null; 
	}
	
	
}
