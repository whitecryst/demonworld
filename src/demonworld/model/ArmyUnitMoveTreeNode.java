package demonworld.model;

import javax.swing.tree.DefaultMutableTreeNode;

public class ArmyUnitMoveTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = -7060581024026515333L;
	public ArmyUnit unit;
	public ArmyElement baseElement;
	public Position posOfBaseElement;
	public ViewDirection viewDirectionOfBaseElement;
	public Position[] posOfElements;
	public ViewDirection[] viewDirectionElements;
	public int maneuverPointsUsed, movePointsUsed;
	public int moveAmount;
	public boolean unitIsOrderly;
	//public boolean unitIsInMelee;
	public MoveType moveType;
	public CostClass costClass;
	public static enum MoveType {UNITMOVE, UNITTURN_LEFT, UNITTURN_RIGHT, UNITROTATE, SINGLEROTATE, STAY};
	public static enum CostClass {MANEUVER, ADVANCEMENT};
	
	public ArmyUnitMoveTreeNode(
			ArmyUnit unit, 
			ArmyElement baseElement, 
			Position newPosOfBaseElement,
			Position[] newPosOfElements,
			ViewDirection newViewDirectionOfBaseElement,
			ViewDirection[] newViewDirectionOfElements,
			int maneuverPointsUsed,
			int movePointsUsed,
			boolean unitIsOrderly,
			//boolean unitIsInMelee,
			MoveType moveType,
			int moveAmount,
			CostClass costClass) {
		
		this.unit 							= unit;
		this.baseElement 					= baseElement;
		this.posOfBaseElement 				= newPosOfBaseElement;
		this.posOfElements					= newPosOfElements;
		this.viewDirectionOfBaseElement 		= newViewDirectionOfBaseElement;
		this.viewDirectionElements			= newViewDirectionOfElements;
		this.maneuverPointsUsed 				= maneuverPointsUsed;
		this.movePointsUsed 					= movePointsUsed;
		this.unitIsOrderly					= unitIsOrderly;
		//this.unitIsInMelee					= unitIsInMelee;
		this.moveType 						= moveType;
		this.costClass						= costClass;
		this.moveAmount						= moveAmount;
		
		
		
	}
	
	public int countCostClassInPath(CostClass searchFor) {
		ArmyUnitMoveTreeNode actNode = this;
		int count = 0;
		if( this.costClass != null ) {
			if( this.costClass.equals(searchFor) ) count++;	
		}
		
		while( actNode.getParent() != null ) {
			actNode = (ArmyUnitMoveTreeNode)actNode.getParent();
			if( actNode.costClass != null ) {
				if( actNode.costClass.equals(searchFor) ) count++;
			}
		}
		return count;
	}
	
	public int countMoveTypeInPath(MoveType searchFor) {
		ArmyUnitMoveTreeNode actNode = this;
		int count = 0;
		if( this.moveType != null ) {
			if( this.moveType.equals(searchFor) ) count++;	
		}
		
		while( actNode.getParent() != null ) {
			actNode = (ArmyUnitMoveTreeNode)actNode.getParent();
			if( actNode.moveType != null ) {
				if( actNode.moveType.equals(searchFor) ) count++;
			}
		}
		return count;
	}
	
	/*public ArmyUnitMoveTreeNode getCopy() {
		return new ArmyUnitMoveTreeNode(
				unit, 
				baseElement, 
				newPosOfElement.getCopy(), 
				offsetToOriginalPosition.getCopy(), 
				new Integer(maneuverPointsUsed), 
				new Integer(movePointsUsed),
				moveType);
	}*/
	
	@Override
	public String toString() {
		String str = "";
		str += costClass+"\n";
		str += moveType+" ("+moveAmount+")"+"\n";
		str += "maneuverPts used:"+maneuverPointsUsed+"\n";
		str += "movePts used:"+movePointsUsed+"\n";
		str += "unit orderly:"+unitIsOrderly+"\n";
		
		return str;
	}
}
