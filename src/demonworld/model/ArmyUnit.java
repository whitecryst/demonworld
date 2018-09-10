package demonworld.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;


public class ArmyUnit implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5195235267247775508L;
	public Vector<ArmyElement> elements;
	public UnitState status = new UnitState();
	public Army army;
	public int defaultNumberOfElements = 1;
	
	
	
	public ArmyUnit( Army army) {
		elements = new Vector<ArmyElement>();
		this.army = army;
	}

	public void removeElement( ArmyElement e ) {
		if( e.unitLeader && elements.size() > 1) {
			// define new unitleader if someone else is left
			elements.remove(e);
			elements.get(0).unitLeader = true;
		} else {
			elements.remove(e);
		}
		
		
	}
	
	public void addElement( ArmyElement element ) {
		this.elements.add( element );
		element.armyUnit = this;
		if( this.elements.size() == 1 ) {
			element.unitLeader = true;
		} else {
			element.unitLeader = false;
		}
	}
	
	public ArmyElement getRandomElement() {
		if( elements.size() > 0 ) {
			return elements.firstElement();
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		String s = "Unit Status:"+status+"\n";
		s += "AllElements:";
		for( ArmyElement e : elements ) {
			s += e.unitDesignation+e.elementState.useFightingSkillBonus+",";
		}
		s += "\n";
		
		return s;
	}

	public Position[] getElementPositions() {
		Position[] positions = new Position[ elements.size() ]; 
		for( int i=0; i<elements.size(); i++ ) {
			positions[i] = elements.get(i).elementState.position;
		}
		return positions;
	}
	
	public void setElementPositions( Position[] newPos) {
		if( newPos.length == elements.size() ) {
			for( int i=0; i< newPos.length; i++ ) {
				elements.get(i).elementState.position = newPos[i];
			}
		}
	}
	
	public void setElementViewDirections( ViewDirection[] newView) {
		if( newView.length == elements.size() ) {
			for( int i=0; i< newView.length; i++ ) {
				elements.get(i).elementState.viewDirection = newView[i];
			}
		}
	}
	
	public ViewDirection[] getElementViewDirections() {
		ViewDirection[] directions = new ViewDirection[ elements.size() ]; 
		for( int i=0; i<elements.size(); i++ ) {
			directions[i] = elements.get(i).elementState.viewDirection;
		}
		return directions;
	}
	
	/**
	 * all elements have to have same viewDirection
	 * 
	 * @param elementPositions
	 * @param viewDirections
	 * @return first list-value contains all elementPosition of front rank, than second rank etc.. every rank ist ordered from outerleft to outerright
	 */
	public static List<List<Position>> getElementPositionsOrderedByRank(Position[] elementPositions, ViewDirection viewDirections) {

		// find out min and max value
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int actValue = 0;
		// TODO: multiline dont have same axis!
		int axisNr = -1;
		if( viewDirections.equals(ViewDirection.TOP) || viewDirections.equals(ViewDirection.BOTTOM) ) {
			axisNr = 0; //x
		} else if( viewDirections.equals(ViewDirection.TOP_LEFT) || viewDirections.equals(ViewDirection.BOTTOM_RIGHT) ) {
			axisNr = 1; //y
		} if( viewDirections.equals(ViewDirection.TOP_RIGHT) || viewDirections.equals(ViewDirection.BOTTOM_LEFT) ) {
			axisNr = 2; //z
		}
		//System.out.println( "axisNr:"+axisNr );
		for( Position actPos : elementPositions ) {
			if( axisNr == 0 ) {
				actValue = actPos.x;
			} else if( axisNr == 1 ) {
				actValue = Position.calcYFromXZ(actPos.x, actPos.z);
			} else if( axisNr == 2 ) {
				actValue = actPos.z;
			}
			
			if( actValue < min ) min = actValue;
			if( actValue > max ) max = actValue;
		}
		//System.out.println( "min:"+min+" max:"+max );
		
		List<List<Position>> result = new ArrayList<List<Position>>();
		int rankValue, endValue, offsetValue;
		if( viewDirections.equals( ViewDirection.TOP ) || viewDirections.equals(ViewDirection.BOTTOM_RIGHT) || viewDirections.equals(ViewDirection.BOTTOM_LEFT) ) {
			// front rank has lowest value
			rankValue = min;
			endValue = max;
			offsetValue = 1;
		} else { 
			//front rank has highest value
			rankValue = max;
			endValue = min;
			offsetValue = -1;
		} 
		
		boolean abort = false;
		// for every rankvalue found
		while (!abort) {
			// get all positions of this rank and put to result
			List<Position> elPosOfRank = new ArrayList<Position>();
			for( Position actPos : elementPositions ) {
				if( axisNr == 0 ) {
					actValue = actPos.x;
				} else if( axisNr == 1 ) {
					actValue = Position.calcYFromXZ(actPos.x, actPos.z);
				} else if( axisNr == 2 ) {
					actValue = actPos.z;
				}
				if( actValue == rankValue ) {
					elPosOfRank.add(actPos);
				}
			}
			
			// order positions in rank from outerleft to outerright
			orderPositions(elPosOfRank, viewDirections); 

			result.add(elPosOfRank);
			
			// next RankValue or ABORT	
			if( rankValue == endValue ) {
				abort = true;
			} else {
				rankValue += offsetValue;
			}
		}
		return result;
	}
	
	/**
	 * test, if all Positions are on same axis (same x , same y or same z)
	 * @param positions
	 * @return {allSameX, allSameY, allSameZ)
	 */
	public static boolean[] haveSameAxis( List<Position> positions ) {
		
		boolean[] result = new boolean[] {true, true, true};
		
		// all psoitions should be on same axis, test for each axis
		Position firstPos = positions.get(0);
		for( Position actPos : positions ) {
			if( firstPos.x != actPos.x ) result[0] = false;
			if( firstPos.y != actPos.y ) result[1] = false;
			if( firstPos.z != actPos.z ) result[2] = false;
			//if( (firstPos.x + firstPos.z) != (actPos.x + actPos.z) ) result[2] = false;
		}
		return result;
	}
	
	/**
	 * order positions from left to right according to viewdirection
	 * if viewdirect is TOP, the result gives the element at (absolute) outerleft at first.
	 * this means, the element with minimal z value
	 * orderAxis 0: use z axis for order (if all positions have same x, outerleftElement is now first)
	 * orderAxis 1: use x axis for order ( if all positions have same z or same sum(x,z), outerleftElement is now first)
	 * 
	 */
	public static void orderPositions( List<Position> positions, final ViewDirection direction ) {
		if( direction.equals(ViewDirection.TOP) || direction.equals(ViewDirection.BOTTOM) ){
			Collections.sort(positions, new Comparator<Position>() {
		        @Override
		        public int compare(Position pos2, Position pos1)
		        {
		            if(direction.equals(ViewDirection.TOP) ) {
		            	return new Integer(pos2.z).compareTo(pos1.z);
		            }
		            else return new Integer(pos1.z).compareTo(pos2.z);
		        }
		    }); 
		} else if( direction.equals(ViewDirection.TOP_RIGHT) || direction.equals(ViewDirection.BOTTOM_LEFT) ){
			Collections.sort(positions, new Comparator<Position>() {
		        @Override
		        public int compare(Position pos2, Position pos1)
		        {
		        	if( direction.equals(ViewDirection.TOP_LEFT) || direction.equals(ViewDirection.TOP_RIGHT)) {
		        		return new Integer(pos2.x).compareTo(pos1.x);
		        	}
		            else return new Integer(pos1.x).compareTo(pos2.x);
		        }
		    }); 
		} else if( direction.equals(ViewDirection.TOP_LEFT) || direction.equals(ViewDirection.BOTTOM_RIGHT)) {
			Collections.sort(positions, new Comparator<Position>() {
		        @Override
		        public int compare(Position pos2, Position pos1)
		        {
		        	if( direction.equals(ViewDirection.TOP_LEFT) || direction.equals(ViewDirection.TOP_RIGHT)) {
		        		return new Integer(pos1.x).compareTo(pos2.x);
		        	}
		            else return new Integer(pos2.x).compareTo(pos1.x);
		        }
		    });
		}
	}
	
	public static boolean isStraightLine(List<Position> positions) {
		if( positions.size() == 1 ) return true;
		
		boolean allSameX, allSameY, allSameZ;
		boolean[] sameAxisTest = haveSameAxis(positions);
		allSameX = sameAxisTest[0];
		allSameY = sameAxisTest[1];
		allSameZ = sameAxisTest[2];
		
		// if not all pos are aligned on a axis, its no straight line
		if( !allSameX && !allSameY && !allSameZ ) {
			System.out.println("no straight line, not all positions on same axis");
			return false;
		}
		
		// sort positions, then test if there is a gap
		List<Position> positionsCopy = new ArrayList<Position>();
		for( Position p : positions ) { positionsCopy.add(p); }
		
		int firstVal=0, lastVal=0;
		if( allSameX ) {
			orderPositions(positionsCopy, ViewDirection.TOP);
			lastVal = positionsCopy.get(positionsCopy.size()-1).z;
			firstVal = positionsCopy.get(0).z;
		} else if( allSameZ || allSameY) {
			// when allSameSumXZ, we can use x OR z to compare positions, here we use x
			orderPositions(positionsCopy, ViewDirection.TOP_LEFT);
			lastVal = positionsCopy.get(positionsCopy.size()-1).x;
			firstVal = positionsCopy.get(0).x;
		}
		int diff = 0;
		if( lastVal > firstVal ) {
			diff = lastVal - firstVal;
		} else {
			diff = firstVal - lastVal;
		}
		if( (diff + 1)  != positionsCopy.size()) {
			System.out.println("no straight line, there is a gap!");
			return false; // gap identified
		}
		// its a straight line
		return true;
		
	}
	
	public boolean isUnitOrderly() {
		return isUnitOrderly(getElementPositions(), getElementViewDirections());
	}
	
	/**
	 * tests, if any of the unitpositions is directly near any of the opponent positions
	 * @param unitPos
	 * @param opponentPos need to include all army element positions
	 * @return
	 */
	public static boolean isUnitInMelee( Position[] unitPos, Position[] opponentPos ) {
		for( Position elPos : unitPos ) {
			for( Position foePos : opponentPos ) {
				if( elPos.getDistanceTo(foePos) == 1 ) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * should be used with result of getElemetPositionsOrderedByRank() only on ordered units (this is the last test)
	 * test if all ranks are aligned well (one after another ano no rank is too much left or right)
	 * @return
	 */
	private static boolean allRanksAligned (List<List<Position>> positionsByRank) {
		if( positionsByRank.size() == 1 ) return true;
		
		//test if ranks are all aligned well. a rank should be positioned right behind the rank before
		Position frontOuterLeft = positionsByRank.get(0).get(0); // outerLeftFrontRank
		int rankNr = 0;
		for( List<Position> actRank : positionsByRank ) {
			if( rankNr == positionsByRank.size() - 1 ) { // last rank
				// on last rank, we test if there is a distance of 1 to >any< of the frontrank cause last rank may have gaps
				for( Position backPos : actRank ) {
					boolean nearEnough = false;
					for( Position frontPos : positionsByRank.get(rankNr-1) ) {
						if( backPos.getDistanceTo(frontPos) == 1) {
							nearEnough = true;
						}
					}
					if( !nearEnough ) return false;
				}
			} else {
				Position backOuterLeft = actRank.get(0);
				//System.out.println( "front"+frontOuterLeft );
				//System.out.println("back"+backOuterLeft);
				if( backOuterLeft.getDistanceTo(frontOuterLeft) > 1) {
					return false;
				}
				frontOuterLeft = backOuterLeft;
			}
			rankNr++;
		}
		return true;
	}
	public static boolean isUnitOrderly(Position[] elementPositions, ViewDirection[] viewDirections) {
		// all elements need same alignment(viewdirection)
		ViewDirection v1 = viewDirections[0];
		for( ViewDirection v2 : viewDirections ) {
			if( ! v1.equals(v2) ) {
				System.out.println( "not orderly: different alignments" );
				return false; 
			}
		}
		
		List<List<Position>> elPosByRanks = getElementPositionsOrderedByRank(elementPositions, v1);
		
		// straight front row (rank) in viewDirection exists
		// only last rank may has gaps
		// each rank needs same amunt of elements (last rank may have lesser amount)
		int firstRankAmount = elPosByRanks.get(0).size();
		int actRankNr = 0;
		for( List<Position> actRank : elPosByRanks) {
			if( actRank.size() == 0 ) {
				System.out.println( "not Orderly, there is an empty rank" );
				return false;
			}
			if( actRankNr == 0 ) { // dont compare amount of front with itself
				// straight front row (rank) in viewDirection exists
				if( ! isStraightLine(actRank) ) {
					System.out.println( "not orderly: first rank is no straight line" );
					System.out.println(actRank );
					return false;
				}
			} else if( actRankNr < elPosByRanks.size()-1 ) { // if not last rank, needs same amount as front
				// is straight line?
				if( ! isStraightLine(actRank) ) {
					System.out.println( "not orderly: rank "+actRankNr+" is no straight line or have gaps" );
					return false;
				}
				// has same amount as rank before?
				if( actRank.size() != firstRankAmount ) {
					System.out.println("not orderly: rank "+actRankNr+" has different amount to front rank");
					return false;
				}	
				//is positioned right behind the rank before?
				
				
			} else { // last rank may have lesser amount
				if( actRank.size() > firstRankAmount ) {
					System.out.println("not orderly: last rank has too much elements");
					return false;
				}
			}
			
			actRankNr ++;
		}
			
		// viewDirection is perpendicular to rank axis
		boolean[] axisResult = haveSameAxis(elPosByRanks.get(0));
		ViewDirection v = viewDirections[0];
		boolean isPerpendicular = false;
		if( 	 	( axisResult[0] && (v.equals(ViewDirection.TOP) || v.equals(ViewDirection.BOTTOM)))
				|| ( axisResult[1] && (v.equals(ViewDirection.TOP_LEFT) || v.equals(ViewDirection.BOTTOM_RIGHT)))
				|| ( axisResult[2] && (v.equals(ViewDirection.TOP_RIGHT) || v.equals(ViewDirection.BOTTOM_LEFT))) ) {
			isPerpendicular = true;
		} 
		if( !isPerpendicular ) {
			System.out.println( "not orderly: front rank is not perpendicular to viewdirection" );
			return false;
		}
		
		
		if( !allRanksAligned(elPosByRanks) ) {
			System.out.println( "not orderly: ranks are not aligned" );
			return false;
		}
		
		return true;
	}
	
	public int getBaseManeuverPoints() {
		return elements.get(0).maneuverPoints;
	}
	
	public int getBaseMovePoints() {
		ArmyElement element = elements.get(0);
		if( this.status.order == null ) return element.movePointsHold;
		
		switch( this.status.order ) {
		case MOVE:
			return element.movePointsGeneral;
		case ATTACK:
			return element.movePointsAttack;
		case SKIRMISH:
			return element.movePointsSkirmish;
		default:
			return element.movePointsHold;
		}
	}
}
