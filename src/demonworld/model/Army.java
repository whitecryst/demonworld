package demonworld.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import demonworld.calculation.FightCalculator;

public class Army implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 12605050661184808L;
	private ArmyType armyType;
	private ArrayList<ArmyUnit> units;
	private ArrayList<ArmyElement> elements;
	public Color armyColor;
	public int totalArmyPoints = 0; // total ArmyPoints at Beginning of Game, use getPoints() to get current points
	
	public Army( ArmyType armyType ) {
		this.armyType = armyType;
		this.units = new ArrayList<ArmyUnit>();
		this.elements = new ArrayList<ArmyElement>();
		this.armyColor = Color.white;
	}
	
	public void addUnit( List<ArmyElement> elements ) {
		// create new Unit Object
		ArmyUnit unit = new ArmyUnit(this);
		
		// create amount of elements for unit
		for( int x = 0; x < elements.size(); x++ ) {
			ArmyElement newElement = elements.get(x);
			// add element to unit 
			unit.addElement( newElement );
			//add element to elements vector
			this.elements.add( newElement );
		}
		// add unit to unit vector
		this.units.add( unit );
		//System.out.println("army add unit");
		
		// update armypoints
		totalArmyPoints = totalArmyPoints+unit.getRandomElement().armyValuePoints;
		
	}
	
	/**
	 * adds element to existing unit. create new unit if needed
	 * @param e
	 */
	public void addElement( ArmyElement e ) {
		ArmyUnit existingUnit = null;
		// find out if unit already exist
		for( ArmyUnit unit : units ) {
			if( unit.elements.size() < unit.defaultNumberOfElements ) {// there is space for another element
				if( unit.getRandomElement() != null) {
					if( unit.getRandomElement().unitDesignation.equalsIgnoreCase( e.unitDesignation ) ) {
						existingUnit = unit;
					}
				}
			}
		}
		
		if( existingUnit != null ) {
			existingUnit.addElement( e );
			elements.add(e);
		} else {
			ArrayList<ArmyElement> eList = new ArrayList<ArmyElement>();
			eList.add(e);
			addUnit( eList );
			
		}
		
		//System.out.println("army add element");
		//System.out.println("army:"+this.toString());
		
	}
	
	public void removeElement( ArmyElement e ) {
		// if unit is empty after remove, completely destroy unit
		if( e.armyUnit == null ) {
			System.err.println( "cannot remove element, no unit registered" );
		}
		if( e.armyUnit.elements.size() == 1) {
			units.remove(e.armyUnit);
			e.armyUnit = null;
			elements.remove(e);
			e.elementState.setMapPosition(null);
			//System.out.println("army removed unit because empty");
		} else {
			e.armyUnit.removeElement(e);
			e.armyUnit = null;
			this.elements.remove(e);
			e.elementState.setMapPosition(null);
		}
		//System.out.println("army removed element");
		//System.out.println("army:"+this.toString());
	}
	
	public void removeAllElements() {
		units.clear();
		elements.clear();
		totalArmyPoints = 0;
	}
	
	public ArmyElement getRandomUnitElementByName( String elementDesignation) {
		for( int x = 0; x< elements.size(); x++ ) {
			if( elements.get(x).unitDesignation.equals( elementDesignation ) ) {
				return elements.get(x);
			}
		}
		return null;
	}
	
	public String[] getUnitDesignations() {
		HashMap<String,String> names=new HashMap<String,String>();
		//jeder name soll nur einmal vorkommen, wird über hashmap erreicht
		for( ArmyElement e : elements ) {
			names.put(e.unitDesignation, e.unitDesignation);
		}
		
		String[] result = new String[ names.size() ];
		int i = 0;
		for( String s : names.keySet() ) {
			result[i] = s;
			i++;
		}
		return result;
	}
	
	public String[] getShootingUnitDesignations() {
		HashMap<String,String> names=new HashMap<String,String>();
		//jeder name soll nur einmal vorkommen, wird über hashmap erreicht
		for( ArmyElement e : elements ) {
			if( e.fightingSkillLongRange != null ) {
				names.put(e.unitDesignation, e.unitDesignation);
			}
		}
		
		String[] result = new String[ names.size() ];
		int i = 0;
		for( String s : names.keySet() ) {
			result[i] = s;
			i++;
		}
		return result;
	}
	
	
	
	
	
	public ArrayList<ArmyUnit> getUnits() {
		return units;
	}

	public ArrayList<ArmyElement> getElements() {
		return elements;
	}
	
	public Position[] getElementPositions() {
		Position[] result = new Position[elements.size()];
		for(int i=0; i<elements.size(); i++ ) {
			result[i] = elements.get(i).elementState.position.getCopy();
		}
		return result;
	}

	@Override
	public String toString() {
		
		String s = "---Army---\n";
		s += "ArmyType:"+armyType+"\n";
		for( ArmyUnit u : units ) {
			s += u.toString()+"\n";
		}
		s += "AllUnits:"+elements.toString();
		return s;
	}
	
	public int getPoints() {
		int p = 0;
		/*for(ArmyElement el : getElements()) {
			p += Math.round((el.armyValuePoints/el.armyUnit.defaultNumberOfElements));
		}*/
		for( ArmyUnit u : getUnits() ) {
			p += u.getRandomElement().armyValuePoints;
		}
		return p;
	}
	
}
