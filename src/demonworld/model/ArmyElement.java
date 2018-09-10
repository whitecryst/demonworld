package demonworld.model;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import biz.pavonis.hexameter.api.Hexagon;
import demonworld.calculation.FightCalculator;
import demonworld.map.HexagonPanel;
import demonworld.map.SatelliteData;
import demonworld.tools.WinkelApp;
import demonworld.view.MainFrame;


public class ArmyElement extends Object implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9117884690528055195L;
	public ArmyUnit armyUnit;
	public boolean unitLeader = false;
	public String unitDesignation;
	public File elementIcon;
	public File elementPortrait;
	public int movePointsGeneral;
	public int movePointsSkirmish;
	public int movePointsAttack;
	public int movePointsHold;
	public int maneuverPoints;
	public int armorPoints;
	public int[] fightingSkillLongRange;
	public int[] distancesForLongRangeSkill; 
	public int[] fightingSkillClosedCombat;
	public String[] meleeWeaponType;
	public int fightingSkillBonus;
	public int hitPoints;
	public int initiative;
	public int size;
	public int figureAmount; // Anzahl Figuren auf der Base
	public ArmyType armyMembership;
	public int armyValuePoints; // zur Zusammenstellung von Armeen gleicher Stärke 
	public ElementState elementState;
	private String elementInfoFormatted;
	
	
	public ArmyElement(
			String unitType,
			boolean unitLeader,
			File icon,
			File portrait,
			int movePointsGeneral, 
			int movePointsSkirmish, 
			int movePointsAttack,
			int movePointsHold,
			int maneuverPoints,
			int armorPoints,
			int[] fightingSkillLongRange,
			int[] distancesForLongRangeSkill,
			int[] fightingSkillClosedCombat,
			String[] meleeWeaponType,
			int fightingSkillBonus,
			int hitPoints,
			int initiative,
			int size,
			int figureAmount,
			ArmyType armyMembership,
			int armyValuePoints) {
		
		this.unitDesignation 			= unitType;
		this.unitLeader					= unitLeader;
		this.elementIcon				= icon;
		this.elementPortrait			= portrait;
		this.movePointsGeneral 			= movePointsGeneral;
		this.movePointsSkirmish 		= movePointsSkirmish;
		this.movePointsAttack 			= movePointsAttack;
		this.movePointsHold 			= movePointsHold;
		this.maneuverPoints				= maneuverPoints;
		this.armorPoints 				= armorPoints;
		this.fightingSkillLongRange 	= fightingSkillLongRange;
		this.distancesForLongRangeSkill = distancesForLongRangeSkill;
		this.fightingSkillClosedCombat 	= fightingSkillClosedCombat;
		this.meleeWeaponType			= meleeWeaponType;
		this.fightingSkillBonus 		= fightingSkillBonus;
		this.hitPoints					= hitPoints;
		this.initiative					= initiative;
		this.size						= size;
		this.figureAmount				= figureAmount;
		this.armyMembership 			= armyMembership;
		this.armyValuePoints			= armyValuePoints;
		this.elementState 				= new ElementState();
	}
	
	public ArmyElement copy() {
		
		ArmyElement copy = new ArmyElement(
				this.unitDesignation,
				this.unitLeader,
				this.elementIcon,
				this.elementPortrait,
				this.movePointsGeneral,
				this.movePointsSkirmish,
				this.movePointsAttack,
				this.movePointsHold,
				this.maneuverPoints,
				this.armorPoints,
				this.fightingSkillLongRange,
				this.distancesForLongRangeSkill,
				this.fightingSkillClosedCombat,
				this.meleeWeaponType,
				this.fightingSkillBonus,
				this.hitPoints,
				this.initiative,
				this.size,
				this.figureAmount,
				this.armyMembership,
				this.armyValuePoints
				
				);
		
		copy.elementState = this.elementState.copy();
		
		return copy;
	}
	
	public String toString() {
		String s = "";
		s += this.unitDesignation;
		s += "("+this.armyUnit.elements.size()+")";
		return s;
	}
	
	public UnitOrder getOrder() {
		if( armyUnit != null ) {
			if( armyUnit.status != null ) {
				if( armyUnit.status.order != null ) {
					return armyUnit.status.order;
				}
			}
		}
		return UnitOrder.NO_ORDER;
	}
	
	public String getHtmlFormattedInfo() {
		//File background = new File( "resources/images/pergament.png" );
		
		
		/*try {
			Image portraitImg = ImageIO.read(portrait);
			double factor = portraitImg.getWidth(null) / portraitWidth;
			portraitWidth = (int)Math.round((double)portraitImg.getWidth(null) / factor);
			portraitHeight =  (int)Math.round((double)portraitImg.getHeight(null) / factor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		elementInfoFormatted = "<html>"
				+ "<head>"
				+ "<style>"
				+ "td {font-family:"+MainFrame.fontFamily+"; font-style: normal; font-size:12px; text-align:center}"
				+ "img { width: 20%; height: auto; }"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"259px\">"
				+ "<tr><td colspan=\"2\" style=\"background:"+String.format("#%06X", (0xFFFFFF & armyUnit.army.armyColor.getRGB()))+";\"><b>"+unitDesignation+elementState.position+" ( "+armyUnit.elements.size()+"/"+armyUnit.defaultNumberOfElements+" elements )</b></td></tr>";
		if( unitLeader ) { 		
			elementInfoFormatted += "<tr><td colspan=\"2\" >( Unitleader )</td></tr>"; 
		}
		elementInfoFormatted += "<tr><td colspan=\"2\"></td></tr>";/*<img width=\""+portraitWidth+"\" height=\""+portraitHeight+"\" src=\"file://"+elementPortrait.getAbsolutePath()+ "\" >";*/
		elementInfoFormatted += "<tr style=\"background:#AAAAAA;\"><td colspan=\"2\">Movepoints: M:"+movePointsGeneral+" / A:"+movePointsAttack+" / S:"+movePointsSkirmish+/*" / H:"+movePointsHold+*/" Maneuver: "+maneuverPoints+"</td></tr>";
		String meeleWeapons = "";
		for( int w = 0 ; w < meleeWeaponType.length; w++ ) {
			
			if( meleeWeaponType.length > 1 && w == this.elementState.equippedWeaponOffset) {
				String fightingskill = ""+fightingSkillClosedCombat[w];
				if( elementState.useFightingSkillBonus ) {
					fightingskill = (fightingSkillClosedCombat[w]+fightingSkillBonus)+"("+fightingSkillClosedCombat[w]+"+"+fightingSkillBonus+")";
				}
				meeleWeapons += "<span style=\"text-decoration: underline\">"+meleeWeaponType[w]+":"+fightingskill+"</span> ";
			} else { 
				meeleWeapons += meleeWeaponType[w]+":"+fightingSkillClosedCombat[w]+" ";
			}
			
		}
				
		elementInfoFormatted += "<tr><td  colspan=\"2\">Meele: "+meeleWeapons+"</td></tr>";
		if( fightingSkillLongRange != null ) {
			elementInfoFormatted += "<tr><td  colspan=\"2\">Range attack: ";
			boolean firstEntry = true;
			for( int i=0 ; i<fightingSkillLongRange.length; i++ ) {
				if( firstEntry ) {
					firstEntry = false; 
				} else { elementInfoFormatted += " / "; }
				elementInfoFormatted += distancesForLongRangeSkill[i]+" fields: "+fightingSkillLongRange[i]+" ";
			}
			elementInfoFormatted += "</td></tr>";
		}		
		String initiativeString = ""+this.initiative;
		if( initiative != getInitiative(true) ) {
			initiativeString += "("+getInitiative(true)+")";
		}
		elementInfoFormatted += "<tr><td  colspan=\"2\">Size: "+size+" Figures: "+figureAmount+" Points: "+armyValuePoints+"</td></tr>"
		+ "<tr style=\"background:#AAAAAA;\"><td  colspan=\"2\">Initiative: "+initiativeString+" Armor: "+armorPoints+" Attack Bonus: "+fightingSkillBonus+"</td></tr>";
		//s += "<tr ><td  colspan=\"2\"> UnitSize: "+armyUnit.elements.size()+"</td></tr>";
		elementInfoFormatted += "<tr ><td  colspan=\"2\"> Current UnitOrder: "+getOrder()+"</td></tr>";
		elementInfoFormatted += "<tr ><td  colspan=\"2\"> Current action: ";
		if( elementState.aimsAt != null ) {
			elementInfoFormatted+="Aiming at '"+elementState.aimsAt.unitDesignation+"'";
		} else if( elementState.attackAt != null ) {
			elementInfoFormatted+="Attack '"+elementState.attackAt.unitDesignation+"' from "+elementState.attackFromDirection;
		} else if( elementState.supportAttack != null ) {
			elementInfoFormatted+="Attack support";
		} else {
			elementInfoFormatted += "Nothing";
		}
		elementInfoFormatted += "<tr ><td  colspan=\"2\"> Unit is orderly: "+armyUnit.isUnitOrderly()+"</td></tr>";
		elementInfoFormatted+= "</td></tr>";
		if( elementState.aimsAt != null ) {
			elementInfoFormatted += "<tr ><td  colspan=\"2\">Shoot distance: "+elementState.aimingDistance+" Supporters: "+getSupporters().size()+"</td></tr>";	
			elementInfoFormatted += "<tr ><td  colspan=\"2\"> Need dice roll: "+getNeededDiceRoll()+"</td></tr>";
		}else if( elementState.attackAt != null ) {
			elementInfoFormatted += "<tr ><td  colspan=\"2\">Supporters: "+getSupporters().size()+"</td></tr>";	
			elementInfoFormatted += "<tr ><td  colspan=\"2\"> Need dice roll: "+getNeededDiceRoll()+"</td></tr>";
		}
		
		
		
		elementInfoFormatted+= "</table>";
		elementInfoFormatted+= "</body></html>";
		
		//System.out.println( s );
		return elementInfoFormatted;
		
		
	}
	
	public List<ArmyElement> getSupporters() {
		ArrayList<ArmyElement> supporters = new ArrayList<ArmyElement>();
		for( ArmyElement e : armyUnit.elements ) {
			if( e.elementState != null ) {
				if( e.elementState.supportAttack == this) {
					supporters.add(e);
				}
			}
		}
		return supporters;
	}
	
	public boolean hasEnemyNeighbour( HexagonPanel map ) {
		// test if one of neighbour fields contains element of other army 
		for( Hexagon hex : map.getHexNeighbours( this.elementState.getMapPosition(map) )){
			if( ((SatelliteData)hex.getSatelliteData()).element != null ) {
				if( ((SatelliteData)hex.getSatelliteData()).element.armyUnit.army != this.armyUnit.army ) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * returns the neighbour hexagons in the following order:
	 * front_left, front_right, right, bottom right, bottom left, left
	 * @return
	 */
	public static Hexagon[] getNeighbourHexesInSightOrder(Hexagon centerHex, ViewDirection direction, HexagonPanel hexPanel) {
		Hexagon[] result = new Hexagon[6];
		
		Set<Hexagon> neighbourHexes = hexPanel.getHexNeighbours( centerHex );
		// point in center of centerHex
		Point2D.Double p1 = new Point2D.Double(
				centerHex.getCenterY(), 
				centerHex.getCenterX());

		biz.pavonis.hexameter.api.Point[] edgePoints = centerHex.getPoints();
		biz.pavonis.hexameter.api.Point viewDirectionPoint = null;
		int[] anglesToNeighbours = null;
		if (direction.equals(ViewDirection.TOP)) {
			anglesToNeighbours = new int[] {-30,30,90,150,210,270};
		} else if (direction.equals(ViewDirection.TOP_RIGHT)) {
			anglesToNeighbours = new int[] {30,90,150,210,270,-30};
		} else if (direction.equals(ViewDirection.BOTTOM_RIGHT)) {
			anglesToNeighbours = new int[] {90,150,210,270,-30,30};
		} else if (direction.equals(ViewDirection.BOTTOM)) {
			anglesToNeighbours = new int[] {150,210,270,-30,30,90};
		} else if (direction.equals(ViewDirection.BOTTOM_LEFT)) {
			anglesToNeighbours = new int[] {210,270,-30,30,90,150};
		} else if (direction.equals(ViewDirection.TOP_LEFT)) {
			anglesToNeighbours = new int[] {270,-30,30,90,150,210};			
		}
		viewDirectionPoint = edgePoints[3]; // always use top point
		//point at edge of hex in viewdirection
		Point2D.Double p2 = new Point2D.Double(
				viewDirectionPoint.y, 
				viewDirectionPoint.x);
		
		for( Hexagon neighbour : neighbourHexes ) {
			if( neighbour != null ) {
				// point at center of neighbour
				Point2D.Double p3 = new Point2D.Double(
						neighbour.getCenterY(), 
						neighbour.getCenterX());
				
				// calc degree between viewDirection and neighbour
				
				double winkel360 = Math.round(WinkelApp.get360GradWinkel(p1, p2, p3));
				
				for( int i=0; i<anglesToNeighbours.length; i++ ) {
					if (winkel360 == anglesToNeighbours[i]) {
						result[i] = neighbour;
						//System.out.println(  "identified neigbour "+i);
					}
				}
			}
		}
		/*System.out.println( "centerHex:"+centerHex );
		for( Hexagon h : result ) {
			System.out.println( "neighbour:"+h );
		}*/
		
		return result;
		
	}
	
	public int getNeededDiceRoll() {
		if( elementState.aimsAt != null ) {
			return getNeededDiceRollForRangeAttack();
		} else if( elementState.attackAt != null ) {
			return getNeededDiceRollForMeleeAttack();
		}
		return 0;
	}
	private int getNeededDiceRollForRangeAttack() {
		if( elementState.aimsAt != null ) {
			ArmyElement defender = elementState.aimsAt;
			int supporter = getSupporters().size();
			int bonusSaldo = FightCalculator.calculateShootingOffset(
					this, 
					defender, 
					elementState.aimingDistance, 
					supporter);
			return FightCalculator.getMinimumDieValueForDestroy( bonusSaldo );
		}
		return -1;
	}
	
	private int getNeededDiceRollForMeleeAttack() {
		
		if( elementState.attackAt != null ) {
			ArmyElement defender = elementState.attackAt;
			int supporter = getSupporters().size();
			
			boolean chargeBonus = false;
			
			int bonusSaldo = FightCalculator.calculateMeleeOffset(
					this, 
					defender, 
					supporter, 
					elementState.attackFromDirection, 
					elementState.useFightingSkillBonus);
			//TODO use chargebonus of cvalry
			return FightCalculator.getMinimumDieValueForDestroy( bonusSaldo );
		}
		return -1;
	}

	/**
	 * return current initiative value of element
	 * @param includeBonus if true, bonus for current weapon(e.g. lance,spear) and order is included into base-initiative
	 * @return
	 */
	public int getInitiative( boolean includeBonus ) {
		// TODO Auto-generated method stub
		int i = this.initiative;
		if( includeBonus ) {
			if( meleeWeaponType[elementState.equippedWeaponOffset].equals("Lance")
					|| meleeWeaponType[elementState.equippedWeaponOffset].equals("Spear")) {
				i++;
			} else if( meleeWeaponType[elementState.equippedWeaponOffset].equals("Pike")) {
				i = i + 2;
			}
				
			// if unit is in first meele phase, get bonus for initiative
			if( !armyUnit.status.isEngagedInMeele ) {
				if( armyUnit.status.order == UnitOrder.ATTACK || armyUnit.status.order == UnitOrder.HOLD) {
					i = i + 2;
				} else if( armyUnit.status.order == UnitOrder.SKIRMISH ) {
					i++;
				}
			}
		}
		return i;
	}
	
	/**
	 * ViewDirection, rotated 60 degree to right
	 * @param offset
	 */
	public static ViewDirection getRightRotateViewDirection( ViewDirection viewBefore ) {
		switch( viewBefore ) {
			case BOTTOM_LEFT:
				return ViewDirection.TOP_LEFT;
			case TOP_LEFT:
				return ViewDirection.TOP;
			case TOP:
				return ViewDirection.TOP_RIGHT;
			case TOP_RIGHT:
				return ViewDirection.BOTTOM_RIGHT;
			case BOTTOM_RIGHT:
				return ViewDirection.BOTTOM;
			case BOTTOM:
				return ViewDirection.BOTTOM_LEFT;
			default:
				return null;
		}
	}
	
	/**
	 * ViewDirection, rotated 60 degree to left
	 * @param offset
	 */
	public static ViewDirection getLeftRotateViewDirection( ViewDirection viewBefore ) {
		switch( viewBefore ) {
		case BOTTOM:
			return ViewDirection.BOTTOM_RIGHT;
		case BOTTOM_RIGHT:
			return ViewDirection.TOP_RIGHT;
		case TOP_RIGHT:
			return ViewDirection.TOP;
		case TOP:
			return ViewDirection.TOP_LEFT;
		case TOP_LEFT:
			return ViewDirection.BOTTOM_LEFT;
		case BOTTOM_LEFT:
			return ViewDirection.BOTTOM;
		default:
			return null;
		}
	}
	
	public void rotateRight() {
		this.elementState.viewDirection = getRightRotateViewDirection( elementState.viewDirection);
		//updateElementIcon();
	}

	public void rotateLeft() {
		this.elementState.viewDirection = getLeftRotateViewDirection( elementState.viewDirection);
	}
	
	
	

	/**
	 * returns scaled and rotated ElementIcon due to actual Viewdirection
	 * @return
	 */
	/*
	public BufferedImage updateElementIcon( ) {
		
		//System.out.println( "updateElementIcon()" );
		ViewDirection direction = elementState.viewDirection;
		
		if( !elementIconByDirection.containsKey(direction) ) {
			
		    // rotate scaledElementIcon and put to directionMap
			int degree = 0;
			if( direction.equals( ViewDirection.TOP ) ) {
				degree = 0;
			} else if ( direction.equals( ViewDirection.TOP_RIGHT ) ) {
				degree = 60;
			} else if ( direction.equals( ViewDirection.BOTTOM_RIGHT ) ) {
				degree = 120;
			} else if ( direction.equals( ViewDirection.BOTTOM ) ) {
				degree = 180;
			} else if ( direction.equals( ViewDirection.BOTTOM_LEFT ) ) {
				degree = 240;
			} else if ( direction.equals( ViewDirection.TOP_LEFT ) ) {
				degree = 300;
			}
			
			AffineTransform at = new AffineTransform();
			at.rotate(Math.toRadians(degree), elementIconScaled.getWidth()/2, elementIconScaled.getHeight()/2);
			AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			elementIconByDirection.put(direction, op.filter(elementIconScaled, null));
		}
		
		return elementIconByDirection.get(direction);
	}*/
	
	public boolean elementIsInMeleeFight( HexagonPanel hexPanel ) {
		// test if one of neighbour fields of target contains element of other army 
		for( Hexagon neighbourHex : hexPanel.getHexNeighbours( this.elementState.getMapPosition(hexPanel) )){
			// neighbour is ArmyElement?
			if( ((SatelliteData)neighbourHex.getSatelliteData()).element != null ) {
				// neighbour armyElement is from different army than target
				if( ((SatelliteData)neighbourHex.getSatelliteData()).element.armyUnit.army !=  this.armyUnit.army) {
					// neighbour armyElement is not from our own unit (in case, we have a hold order and enemy engaged us in melee, we are allowed to shoot and this is no "shoot into melee")
					if( ((SatelliteData)neighbourHex.getSatelliteData()).element.armyUnit != this.armyUnit ) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean attack( ArmyElement target, HexagonPanel hexPanel) {
		if( target != null ) {
			if( target.armyUnit.army != this.armyUnit.army ) {
				
				// to calculate the Value of 'AttackFrom' Direction, we calculate the angle between 3 points:
				// p1: attacker_center, p2: target_center, p3: attacker_viewDirection_edgeOfHexagon
				Point2D.Double p1 = new Point2D.Double(
						target.elementState.getMapPosition(hexPanel).getCenterY(), 
						target.elementState.getMapPosition(hexPanel).getCenterX());

				Point2D.Double p2 = new Point2D.Double(
						this.elementState.getMapPosition(hexPanel).getCenterY(), 
						this.elementState.getMapPosition(hexPanel).getCenterX());

				ViewDirection direction = target.elementState.viewDirection;
				biz.pavonis.hexameter.api.Point[] edgePoints = target.elementState.getMapPosition(hexPanel).getPoints();
				biz.pavonis.hexameter.api.Point viewDirectionPoint = null;
				if (direction.equals(ViewDirection.TOP)) {
					viewDirectionPoint = edgePoints[3];
				} else if (direction.equals(ViewDirection.TOP_RIGHT)) {
					viewDirectionPoint = edgePoints[2];
				} else if (direction.equals(ViewDirection.BOTTOM_RIGHT)) {
					viewDirectionPoint = edgePoints[1];
				} else if (direction.equals(ViewDirection.BOTTOM)) {
					viewDirectionPoint = edgePoints[0];
				} else if (direction.equals(ViewDirection.BOTTOM_LEFT)) {
					viewDirectionPoint = edgePoints[5];
				} else if (direction.equals(ViewDirection.TOP_LEFT)) {
					viewDirectionPoint = edgePoints[4];
				}
				Point2D.Double p3 = new Point2D.Double(
						viewDirectionPoint.y, 
						viewDirectionPoint.x);
				double winkel = WinkelApp.getWinkel(p1, p2, p3);
				FightCalculator.AttackFromDirection d = null;
				//System.out.println( "winkel:"+winkel );
				if ((winkel - 30 ) <= 0.01) {
					d = FightCalculator.AttackFromDirection.FRONT;
				} else if ((winkel - 90) <= 0.01) {
					d = FightCalculator.AttackFromDirection.SIDE;
				} else if ((winkel - 180) <= 0.01) {
					d = FightCalculator.AttackFromDirection.BACK;
				}
				
				this.elementState.attackAt = target;
				this.elementState.supportAttack = null;
				this.elementState.attackFromDirection = d;

			}
		}
		return false;
	}

	public int getMaxShootingDistance() {
		return this.distancesForLongRangeSkill[ distancesForLongRangeSkill.length-1 ];
	}
}
