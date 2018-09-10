package demonworld.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.tree.TreeNode;

import biz.pavonis.hexameter.api.Hexagon;
import biz.pavonis.hexameter.api.exception.HexagonNotFoundException;
import demonworld.controller.server.ControlSource;
import demonworld.map.HexagonPanel;
import demonworld.map.SatelliteData;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyType;
import demonworld.model.ArmyUnit;
import demonworld.model.ArmyUnitMoveTreeNode;
import demonworld.model.ArmyUnitMoveTreeNode.CostClass;
import demonworld.model.ArmyUnitMoveTreeNode.MoveType;
import demonworld.model.Position;
import demonworld.model.UnitOrder;
import demonworld.model.ViewDirection;

public class AIController {
	private MainController mainControl;
	private ControlSource aiControlSource;
	private ControlSource opponentControlSource;
	private Random rand = new Random();
	
	
	
	public AIController(MainController mainControl, ControlSource playerSource) {
		this.mainControl = mainControl;
		this.aiControlSource = playerSource;
		if( aiControlSource.equals(ControlSource.LOCAL_PLAYER) ) {
			opponentControlSource = ControlSource.EXTERNAL_PLAYER;
		} else {
			opponentControlSource = ControlSource.LOCAL_PLAYER;
		}
		
	}
	
	public void doAction() {
		mainControl.serverBridge.sendMessage("AI Action");
		if( mainControl.gameState.currentGameMode == null ) {
			return;
		}
		
		switch( mainControl.gameState.currentGameMode ) {
		case SETUP_ARMYS:
			setupArmysAction();
			break;
			
		case PLACE_ORDERS:
			placeOrdersAction();
			break;
			
		case FIGHT_RANGED_SKIRMISH:
			rangedFightAction( UnitOrder.SKIRMISH );
			break;	
			
		case FIGHT_RANGED_HOLD:
			rangedFightAction( UnitOrder.HOLD );
			break;	

		case MOVE:
			Thread thread = new Thread() {
				public void run() {
					movementAction( getCurrentUnitOrderForMovement() );
				}
			};
			
			thread.start();
		break;
		
		case FIGHT_MEELE:
			meleeFightAction();
		break;

		default:
			System.err.println( "error in AIController.doAction(), unknown gameMode:"+mainControl.gameState.currentGameMode );
			break;
		}
	};
	
	private void meleeFightAction() {
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		
		for( ArmyElement attacker : getCurrentMeleeAttackers() ) {
			// get neighbours by viewdirection
			Hexagon[] neighboursHexes = ArmyElement.getNeighbourHexesInSightOrder(
					attacker.elementState.getMapPosition(hexPanel), 
					attacker.elementState.viewDirection, 
					hexPanel);
			// find out at which front field an enemy is
			for( int i=0; i<2; i++ ) {
				Hexagon actHex = neighboursHexes[i];
				SatelliteData data = actHex.getSatelliteData();
				ArmyElement target = data.element;
				if( target != null ) {
					if( target.armyUnit.army != attacker.armyUnit.army ) { // target from other army?
						// test if another element is attacking this target, if so, support them!
						ArmyElement attackerToSupport = null;
						for( ArmyElement el : attacker.armyUnit.elements ) {
							if( el == attacker ) continue;
							if( el.elementState.attackAt == target ) {
								attackerToSupport = el;
							}
						}
						// TODO: not always support. define strategy
						// TODO: choose weapon if 2 available (eg. cavalry charge)
						if( attackerToSupport != null ) {
							attacker.elementState.supportAttack = attackerToSupport;
							attacker.elementState.attackAt = null;
						} else {
							attacker.attack(target, hexPanel);
						}
					}
				}
			}
		}
		mainControl.mainFrame.repaint();
	}
	
	/** 
	 * get all elements whos turn is it to attack in melee depending on initative
	 * @return
	 */
	public List<ArmyElement> getCurrentMeleeAttackers() {
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		Army myArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		Army opponentArmy = mainControl.gameState.playerArmys.get(opponentControlSource);
		List<ArmyElement> attackers = new ArrayList<ArmyElement>();
		// add all elements with a enemy as neighbour
		// TODO: use initiative and opponentn turns to decide who is valid to attack
		for( ArmyElement el : myArmy.getElements() ) {
			if( el.hasEnemyNeighbour(hexPanel) ) {
				attackers.add(el);
			}
		}
		return attackers;
	}
	
	

	private UnitOrder getCurrentUnitOrderForMovement() {
		// there is a general move order, depending on unit order (move, skirmisch, attack, hold)
		// for every move order, player with lesser orders decides if he wants to move first or secondly 
		// every click of ai-action button check, in which pahse of movements we are
		// we assume, that if opponent hasnt moved, he doesnt wish to move
		
		// get both armys
		Army opponentArmy, aiArmy;
		aiArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		opponentArmy = mainControl.gameState.playerArmys.get(opponentControlSource);	
		
		// for every unitorder-type, check if at least one of this units have moved yet
		HashMap<UnitOrder, Boolean> opponentUnitMovementsByOrder = new HashMap<UnitOrder, Boolean>();
		HashMap<UnitOrder, Boolean> aiUnitMovementsByOrder = new HashMap<UnitOrder, Boolean>();
		for( ArmyUnit unit : opponentArmy.getUnits() ) {
			// set 'not moved' for default, (if a unitorder is not given to any unit, it has no key in the map)
			if( !opponentUnitMovementsByOrder.containsKey(unit.status.order) ) {
				opponentUnitMovementsByOrder.put(unit.status.order, false);
			}
			// if unit has moved, save this fact
			if( unit.status.usedManeuverPoints > 0 || unit.status.usedMovePoints > 0 ) {
				opponentUnitMovementsByOrder.put(unit.status.order, true);
			}
		}
		for( ArmyUnit unit : aiArmy.getUnits() ) {
			// set 'not moved' for default
			if( !aiUnitMovementsByOrder.containsKey(unit.status.order) ) {
				aiUnitMovementsByOrder.put(unit.status.order, false);
			}
			// if unit has moved, save this fact
			if( unit.status.usedManeuverPoints > 0 || unit.status.usedMovePoints > 0 ) {
				aiUnitMovementsByOrder.put(unit.status.order, true);
			}
		}
		
		
		UnitOrder[] orders = new UnitOrder[]{ UnitOrder.MOVE, UnitOrder.SKIRMISH, UnitOrder.ATTACK};
		for( UnitOrder currentOrder : orders ) {
			
			Boolean aiHasDoneMovement = aiUnitMovementsByOrder.get(currentOrder);
			
			if( aiHasDoneMovement == null ) {
				// we have no units with this order, check next unitorder
			} else {
				if( aiHasDoneMovement ) {
					// ai already moved these units, check next unitorder
				} else { 
					// 
					return currentOrder;
				}
			}
		}
		// units with hold-order move at last
		return UnitOrder.HOLD;
	}

	/**
	 * 
	 * @param necessaryOrder move only units with these order
	 */
	private void movementAction( UnitOrder necessaryOrder ) {
		
		// there is a general move order, depending on unit order (move, skirmisch, attack, hold)
		// for every move order, player with lesser orders decides if he wants to move first or secondly 
		// every click of ai-action button check, in which pahse of movements we are
		// we assume, that if opponent hasnt moved, he doesnt wish to move
		Army aiArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		
		for( ArmyUnit unit : aiArmy.getUnits() ) {
			// if unit is in melee, its not allowed to move in movephase
			if( isUnitInMelee(unit.getElementPositions()) ) {
				System.out.println( "unit "+unit.getRandomElement().unitDesignation+" is in melee and not allowed to move in this phase" );
				continue;
			}
			
			// unit needs at least 3 elements
			if( unit.elements.size() < 3 ) {
				System.out.println( "unit "+unit.getRandomElement().unitDesignation+" has size < 3 and is not allowed to move (yet)" );
				// TODO: implement special move rules for small units
				continue;
			}
			
			//TODO. use Armyelement at end of unitline as baseElement
			ArmyElement baseElement = unit.getRandomElement();
			
			ArmyUnitMoveTreeNode root = new ArmyUnitMoveTreeNode( 
					unit, 
					baseElement, 
					baseElement.elementState.position,
					unit.getElementPositions(),
					baseElement.elementState.viewDirection,
					unit.getElementViewDirections(),
					unit.status.usedManeuverPoints,
					unit.status.usedMovePoints,
					unit.isUnitOrderly(),
					MoveType.STAY,
					0,
					null);
			
			// choose movement randomly
			TreeNode[] actionPath = evolveMovementPath( root, true, true, true );
			
			// do and show movement
			apply( actionPath ); 
		}
		
		
	}

	// apply all actions along the actionpath step by step and print result on hexPanel
	private void apply(TreeNode[] actionPath) {
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		
		GameMode_Move_MapController moveController = null;
		if( aiControlSource.equals(ControlSource.LOCAL_PLAYER) ) {
			moveController = mainControl.gm_moveMapControllerLocal;	
		} else {
			moveController = mainControl.gm_moveMapControllerOtherPlayer;
		}
		System.out.println( "unit:"+((ArmyUnitMoveTreeNode)actionPath[0]).baseElement.unitDesignation );
		for( TreeNode actNode : actionPath ) {
			ArmyUnitMoveTreeNode actMoveNode = (ArmyUnitMoveTreeNode)actNode;
			//Hexagon newPos = hexPanel.getHexByGridCoordinate(actMoveNode.posOfBaseElement.x, actMoveNode.posOfBaseElement.z);
			//System.out.println( "newPos:"+newPos );
			
			System.out.println( actMoveNode );
			ArmyUnit unit = actMoveNode.unit;
			unit.setElementPositions(actMoveNode.posOfElements);
			unit.setElementViewDirections(actMoveNode.viewDirectionElements);
			hexPanel.refreshArmyElementPositions();
			mainControl.mainFrame.repaint();
			if( isUnitInMelee(unit.getElementPositions()) ) {
				System.out.println( "unit "+unit.getRandomElement().unitDesignation+" is in melee and not allowed to move further" );
				break;
			}
			/*switch( actMoveNode.moveType ) {
				case UNITMOVE:
					moveController.moveUnit(actMoveNode.baseElement, newPos);	
				break;
				
				// node positions include the already done movement, bat we want to do the move. we need to use the original positions
				case UNITTURN_LEFT:
					List<Position> frontRank = ArmyUnit.getElementPositionsOrderedByRank(unit.getElementPositions(), unit.getElementViewDirections()[0]).get(0);
					 // left turn, use outerLeft as base
					System.out.println( frontRank.get(0) );
					ArmyElement turnBase = getArmyElementAtPosition( frontRank.get(0) );
					moveController.turnUnit(turnBase, -1 * actMoveNode.moveAmount);
				break;

				case UNITTURN_RIGHT:
					frontRank = ArmyUnit.getElementPositionsOrderedByRank(unit.getElementPositions(), unit.getElementViewDirections()[0]).get(0);
					 // right turn, use outerright as base
					turnBase = getArmyElementAtPosition( frontRank.get(frontRank.size()-1) );
					System.out.println( frontRank.get(frontRank.size()-1) );
					moveController.turnUnit(turnBase, actMoveNode.moveAmount);
				break;

				case STAY:
				break;

				default:
					System.out.println( "AIController.createValidMoveNodes() unimplemented unitMove:"+actMoveNode.moveType );
			}*/
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	private TreeNode[] evolveMovementPath(
			ArmyUnitMoveTreeNode parentNode,
			boolean useMovement,
			boolean useUnitTurn,
			boolean useUnitRotate) {
		System.out.println( "--- evolve ---" );
		if( isUnitInMelee( parentNode.posOfElements ) ) {
			System.out.println( "melee stops movement" );
			return parentNode.getPath();
		} else {
			System.out.println( "unit is not in meelee" );
		}
		System.out.println( "parentNode:" +parentNode );
		
		// create unitMove nodes as childs of parentNode (create all possible moves as child nodes)
		if( useMovement ) {
			System.out.println( "search for valid Unit movements..." );
			createUnitMoveToNeighbourFieldNodes(parentNode);
		}

		// create unitTurn nodes as childs of parentNode
		if( useUnitTurn ) {
			System.out.println( "search for valid unitTurns..." );
			createUnitTurnNodes(parentNode);
		}
		
		// create unitTurn nodes as childs of parentNode
		if( useUnitRotate ) {
			System.out.println( "search for valid unitRotations..." );
			createUnitRotateNodes(parentNode);
		}
		
		// TODO: only attack (move near enemy, if orderly, move in front direction and has order attack or skirmish)
		// TODO: create elementsRotate nodes as childs of parentNode
		// TODO: create maneuverMove nodes as childs of parentNode
		// TODO: create expandFrontage nodes as childs of parentNode
		// TODO: create reduceFrontage nodes as childs of parentNode
		
		
		
		// if no more possible actions, do parent action
		if( parentNode.getChildCount() == 0){ 
			System.out.println( "no more childs" );
			return parentNode.getPath();
		}  else { // select randomly one child and evolve tree
			int childNr = rand.nextInt( parentNode.getChildCount() );			
			return evolveMovementPath( (ArmyUnitMoveTreeNode)parentNode.getChildAt(childNr), useMovement, useUnitTurn, useUnitRotate );
		}
	}
	
	/**
	 * perform unit rotate (in rules named turn)
	 */
	private void createUnitRotateNodes(ArmyUnitMoveTreeNode parentNode) {
		/*A unit which was in organized formation before turning AND has turned all its elements in the same direction and angle 
		 * can move single elements by one field, provided the unit is disordered after execution of the turn, and then only as far
		 *  as is necessary to regain an organized formation. This does not count as movement or as an additional maneuver.
		 */
		int necessaryManeuverPts = 1;
		int freeManeuverPts = (parentNode.unit.getBaseManeuverPoints() - parentNode.maneuverPointsUsed);
		
		if( freeManeuverPts < necessaryManeuverPts ) {
			return;
		}
		
		int[] rotateDegrees = new int[] {60,120,180,270};
		for( int rotateStepNr=1; rotateStepNr<5; rotateStepNr++ ) {
			
			ViewDirection[] newViewDirectionOfAllElements = parentNode.unit.getElementViewDirections();
			ViewDirection newViewDirectionOfBaseElement = parentNode.baseElement.elementState.viewDirection;
			
				// rotate x steps to the right for each element ( calc extra for baseElement)
			for( int multipleRotateNr = 1;multipleRotateNr <= rotateStepNr; multipleRotateNr++ ) {
				newViewDirectionOfBaseElement = ArmyElement.getRightRotateViewDirection(newViewDirectionOfBaseElement);
				for( int i=0; i<newViewDirectionOfAllElements.length; i++ ) {
					newViewDirectionOfAllElements[i] = ArmyElement.getRightRotateViewDirection( newViewDirectionOfAllElements[i] );
				}
			}
			
			ArmyUnitMoveTreeNode moveNode = new ArmyUnitMoveTreeNode(
					parentNode.unit, 
					parentNode.baseElement, 
					parentNode.posOfBaseElement, 
					parentNode.posOfElements, 
					newViewDirectionOfBaseElement,
					newViewDirectionOfAllElements,
					parentNode.maneuverPointsUsed + necessaryManeuverPts, 
					parentNode.movePointsUsed,
					ArmyUnit.isUnitOrderly(parentNode.posOfElements, newViewDirectionOfAllElements),
					MoveType.UNITROTATE,
					rotateStepNr,
					CostClass.MANEUVER);
			
			parentNode.add( moveNode );
			
		}
		
	}
	
	/**
	 * perfrom unit turn (in rules named Wheel)
	 * @param parentNode
	 */
	private void createUnitTurnNodes( ArmyUnitMoveTreeNode parentNode ) {
		//HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		// unit need to be in orderly formation to wheel
		if( !parentNode.unitIsOrderly ) return;
		Position turnBasePos = null;
		Position[] newPositions = null;

		// TODO: turn is valid only in front direction!
		
		//these are the valid Movetypes, we create a node for each
		MoveType[] moveTypes = new MoveType[] {
				MoveType.UNITTURN_LEFT, 
				MoveType.UNITTURN_RIGHT};
		
		// get moveController
		GameMode_Move_MapController moveController = null;
		if( aiControlSource.equals(ControlSource.LOCAL_PLAYER) ) {
			moveController = mainControl.gm_moveMapControllerLocal;	
		} else {
			moveController = mainControl.gm_moveMapControllerOtherPlayer;
		}
		
		// create a Node for every moveType, every turn may have 60 degree (1left) or 120 degree (2left)
		for( MoveType actMoveType : moveTypes ) {
			// its not allowed to turn left AND right 
			if( parentNode.countMoveTypeInPath( MoveType.UNITTURN_LEFT ) > 0 && actMoveType.equals(MoveType.UNITTURN_RIGHT) ) {
				continue;
			} else if( parentNode.countMoveTypeInPath( MoveType.UNITTURN_RIGHT ) > 0 && actMoveType.equals(MoveType.UNITTURN_LEFT) ) {
				continue;
			}
			for( int turnAmount=1; turnAmount<=2; turnAmount++ ) {
				ViewDirection newViewDirection = null;
				int turnOffset = 1;
				List<Position> frontRank = ArmyUnit.getElementPositionsOrderedByRank(parentNode.posOfElements, parentNode.viewDirectionOfBaseElement).get(0);
				/*for( Position p : parentNode.posOfElements ) {
					System.out.println( "oldPos:"+p  );
				}
				System.out.println( "frontRank:"+frontRank );*/
				
				switch(actMoveType) {
					case UNITTURN_LEFT:
						turnBasePos = frontRank.get(0); // left turn, use outerLeft as base
						//System.out.println( "turnbaselft:"+turnBasePos );
						newViewDirection = ArmyElement.getLeftRotateViewDirection(parentNode.viewDirectionOfBaseElement);
						if( turnAmount == 2 ) newViewDirection = ArmyElement.getLeftRotateViewDirection(newViewDirection);
						turnOffset = -1;
					break;
					case UNITTURN_RIGHT:
						turnBasePos = frontRank.get( frontRank.size() -1 ); // right turn, use outer right as base
						newViewDirection = ArmyElement.getRightRotateViewDirection(parentNode.viewDirectionOfBaseElement);
						if( turnAmount == 2 ) newViewDirection = ArmyElement.getRightRotateViewDirection(newViewDirection);
						//System.out.println( "turnbasright:"+turnBasePos );
					break;
				}
	
				// get final positions
				Set<Hexagon> marchThroughFields;
				try {
					marchThroughFields = moveController.getMarchThroughFieldsDuringUnitTurn(
						parentNode.posOfElements, 
						turnBasePos, 
						parentNode.viewDirectionOfBaseElement, 
						turnOffset*turnAmount);
				} catch (HexagonNotFoundException e) {
					//System.out.println( "invalid move, hex out of field" );
					continue; // invalid move, next
				}	
				
				// get fields in turnpath (to test if they are blocked)
				newPositions = moveController.getNewElementPositionAfterUnitTurn(
						parentNode.posOfElements, 
						turnBasePos, 
						turnOffset,
						turnAmount);
				
				// test if this turn is valid
				boolean turnIsValid = true;
				for( Hexagon actHex : marchThroughFields ) {
					if( ! isValidToMoveOn(actHex, parentNode.unit) ) { turnIsValid = false; }
				}
				for( Position p : newPositions ) {
					//System.out.println( "newPos:"+p );
					if( ! isValidToMoveOn(p, parentNode.unit) ) { turnIsValid = false; }
					// final position may have a enemy as beighbour, but only at front fields
					Position[] nP = p.getNeighbourPositionsByDirection(newViewDirection);
					if( containsEnemy(nP[2]) || containsEnemy(nP[3]) || containsEnemy(nP[4]) || containsEnemy(nP[5]) ) {
						turnIsValid = false;
					}
				}
				
				// maneuver costs
				int necessaryManeuverPts = 1;
				int necessaryMovePts = Math.round(parentNode.unit.getBaseMovePoints() / 2);
				int freeManeuverPts = (parentNode.unit.getBaseManeuverPoints() - parentNode.maneuverPointsUsed);
				int freeMovePts = (parentNode.unit.getBaseMovePoints() - parentNode.movePointsUsed);
				
				if( turnIsValid && freeMovePts >= necessaryMovePts && freeManeuverPts >= necessaryManeuverPts) {
					System.out.println( "..found valid veighbour" );
					
					ViewDirection[] newViewDirectionOfElements = new ViewDirection[ parentNode.viewDirectionElements.length ];
					for( int i=0; i< newViewDirectionOfElements.length; i++ ) {
						newViewDirectionOfElements[i] = newViewDirection;
					}
					
					ArmyUnitMoveTreeNode moveNode = new ArmyUnitMoveTreeNode(
						parentNode.unit, 
						parentNode.baseElement, 
						parentNode.posOfBaseElement, 
						newPositions, 
						newViewDirection,
						newViewDirectionOfElements,
						parentNode.maneuverPointsUsed + necessaryManeuverPts, 
						parentNode.movePointsUsed + necessaryMovePts,
						ArmyUnit.isUnitOrderly(newPositions, newViewDirectionOfElements),
						actMoveType,
						turnAmount,
						CostClass.MANEUVER);
					
					//System.out.println( moveNode );
					//System.out.println("---");
					parentNode.add( moveNode );
				}
			}
		}
	}
	
	private void createUnitMoveToNeighbourFieldNodes(ArmyUnitMoveTreeNode parentNode) {
		//int maneuversDone = parentNode.countCostClassInPath(CostClass.MANEUVER);
		ViewDirection actDirection = parentNode.viewDirectionOfBaseElement;
		
		// for every neighbourhex of baseElement create a moveNode (if move is valid)
		Position[] neighbourPositions = parentNode.posOfBaseElement.getNeighbourPositionsByDirection(actDirection);
		for( int n=0; n<neighbourPositions.length; n++ ) {
			Position neighbourPos = neighbourPositions[n];
			CostClass costClass = CostClass.MANEUVER;;
			if( parentNode.unitIsOrderly ) {
				// neighbourpositions are ordered to viewdirection
				// if we move in front direction this is an advancement move
				//TODO: may also be an maneuver move
				if( n == 0 || n == 1 ) {
					costClass = CostClass.ADVANCEMENT;
				}
			}
			
			Position[] newElementPositions = new Position[parentNode.posOfElements.length];
			//System.out.println( "newPos:"+newPos );
			//for everey element in unit, test if new position is free (no element of another unit)
			boolean targetFieldValid = true;
			int elNr = 0;
			boolean[] enemy = new boolean[6];
			boolean enemyContact = false;
			for( Position actElPos : parentNode.posOfElements ) {
				
				Position offsetToBaseEl = actElPos.getOffsetTo(parentNode.posOfBaseElement);
				//System.out.println( "offsetToBaseEl:"+offsetToBaseEl );
				Position newElPos = neighbourPos.getCopy();
				newElPos.x += offsetToBaseEl.x;
				newElPos.y += offsetToBaseEl.y;
				newElPos.z += offsetToBaseEl.z;
				newElementPositions[elNr] = newElPos;
				elNr++;
				// get neighbours ordered equally to baseElement. 
				//System.out.println( "newELPos:"+elPos );
				
				// test if target field is off the grid or occupied
				if( !isValidToMoveOn(newElPos, parentNode.unit) ) {
					System.out.println( "not valid to move on:" +newElPos );
					targetFieldValid = false;
				}
					
				// for every newPos of unitelements, check if neighbour field contains enemy (means we would attack)
				Position[] newElPosNeighbours = newElPos.getNeighbourPositionsByDirection(actDirection);
				
				for( int i = 0; i< newElPosNeighbours.length; i++ ) {
					if( containsEnemy(newElPosNeighbours[i]) ) { // enemy contact
						enemy[i] = true;
						enemyContact = true;
						System.out.println( "enemycontact with one element" );
						// unit need skirmish or Attack order to attack other unit
						UnitOrder unitOrder = parentNode.unit.status.order; 
						if( unitOrder == null ) {
							targetFieldValid = false;
						}
						else if( !unitOrder.equals(UnitOrder.SKIRMISH) && !unitOrder.equals(UnitOrder.ATTACK)) {
							targetFieldValid = false;
						}
						
						// unit need to advance(or maneuver) in front direction (not sideways or backwards)
						if( n > 1) {
							targetFieldValid = false;
						}
						
						//its not allowed to attack with left or right side. (make contact by moving in front direction aside a unit)
						if( i > 1 ) {
							targetFieldValid = false;
						}
					}
				}
				
			}
			
			
			switch( costClass ) {
			case MANEUVER: 
				// if unit is not orderly && not already in melee, we may not attack
				Army opponentArmy = mainControl.gameState.playerArmys.get(opponentControlSource);
				boolean isOrderly = parentNode.unitIsOrderly;
				boolean existingMelee = ArmyUnit.isUnitInMelee(parentNode.posOfElements, opponentArmy.getElementPositions());		 
				if( enemyContact && !existingMelee && !isOrderly)  {
					targetFieldValid = false;
				}
				// orderly units, not already in melee may attack, but only at front 
				else if( isOrderly && !existingMelee && (enemy[2] || enemy[3] ||  enemy[4] ||  enemy[5])) {
					targetFieldValid = false;
				} else if( existingMelee && enemyContact ) { // may only valid at end of meelee phase 
					targetFieldValid = false;
				}
				break;
			case ADVANCEMENT: // enemy at front is ok, other not
				if( enemy[2] || enemy[3] ||  enemy[4] ||  enemy[5] ) {
					targetFieldValid = false;
				}
			break;
			}
					
			// calc costs of movement
			int necessaryMovePts = 0;
			int necessaryManeuverPts = 0;
			switch( costClass ) {
				case ADVANCEMENT:
					necessaryMovePts = 3;
					System.out.println( "ADVANCEMENT, necessaryMovPts:"+necessaryMovePts );
				break;
				case MANEUVER:
					necessaryManeuverPts = 1;
					// each maneuver costs half of movepoints
					necessaryMovePts = Math.round(parentNode.unit.getBaseMovePoints() / 2);
					System.out.println( "necessaryMovPts "+necessaryMovePts );
				break;
			}
			
			int freeMovePts = (parentNode.unit.getBaseMovePoints() - parentNode.movePointsUsed);
			int freeManeuverPts = (parentNode.unit.getBaseManeuverPoints() - parentNode.maneuverPointsUsed);
			
			if( targetFieldValid && freeMovePts >= necessaryMovePts && freeManeuverPts >= necessaryManeuverPts) {
				System.out.println( "..found valid veighbour" );
				ArmyUnitMoveTreeNode moveNode = new ArmyUnitMoveTreeNode(
					parentNode.unit, 
					parentNode.baseElement, 
					neighbourPos, 
					newElementPositions, 
					parentNode.viewDirectionOfBaseElement,
					parentNode.viewDirectionElements,
					parentNode.maneuverPointsUsed + necessaryManeuverPts, 
					parentNode.movePointsUsed + necessaryMovePts,
					parentNode.unitIsOrderly,
					MoveType.UNITMOVE,
					1,
					costClass);
				parentNode.add( moveNode );
			}			
		}
	}
	public void rollit() {
		
		switch( mainControl.gameState.currentGameMode ) {
			
		case FIGHT_RANGED_SKIRMISH:
			rangedFightRollIt();
			break;	
		case FIGHT_RANGED_HOLD:
			rangedFightRollIt();
			break;	
		
		case FIGHT_MEELE:
			meleeFightRollIt();
		break;
		default:
			System.err.println( "error in AIController.doAction(), unknown gameMode:"+mainControl.gameState.currentGameMode );
			break;
		}
	}
	
	void meleeFightRollIt() {
		Army myArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;

		for( ArmyElement attacker : myArmy.getElements() ) {
			if( attacker.elementState.attackAt != null ) {
				mainControl.gm_FightMeele_controllerLocal.rollIt(attacker);
			}
		}
		hexPanel.repaint();
	}
	
	private void rangedFightRollIt() {
		Army myArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;

		for( ArmyUnit unit : myArmy.getUnits() ) {
			if( unit.getRandomElement().fightingSkillLongRange != null ) {
				for( ArmyElement attacker : unit.elements ) {
					if( attacker.elementState.aimsAt != null ) {
						mainControl.gm_FightRanged_controllerLocal.rollIt(attacker);
					}
				}
			}
		}
		hexPanel.repaint();
	}

	private void rangedFightAction(UnitOrder necessaryOrder) {
		Army myArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		Army opponentArmy = mainControl.gameState.playerArmys.get(opponentControlSource);
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;

		for( ArmyUnit unit : myArmy.getUnits() ) {
			// shooting while into meelee is only valid if UnitOrder is HOLD and melle began this round
			if( necessaryOrder.equals(UnitOrder.SKIRMISH) && ArmyUnit.isUnitInMelee(unit.getElementPositions(), opponentArmy.getElementPositions()) ) {
				continue;
			}
			if( unit.status.order.equals(necessaryOrder) && unit.getRandomElement().fightingSkillLongRange != null ) {
				for( ArmyElement attacker : unit.elements ) {
					
					// choose one validTarget randomly
					List<ArmyElement> validTargets = mainControl.gm_FightRanged_controllerLocal.getValidTargets(attacker);
					if( !validTargets.isEmpty() ) {
						ArmyElement choosedTarget = validTargets.get( rand.nextInt( validTargets.size() ) );
						shootAtTarget(attacker, choosedTarget);
						hexPanel.repaint();
					}
					
				}
			}
			
		}
		
	}
	
	
	
	public void shootAtTarget(ArmyElement shooterElement, ArmyElement target) {
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		
		//test if someone else from our unit is shooting at this
		ArmyElement attackerToSupport = null;
		for( ArmyElement el : shooterElement.armyUnit.elements ) {
			if( el != shooterElement ) { // ignore attacker
				if( el.elementState.aimsAt == target ) {
					attackerToSupport = el;
				}
			}
		}
		// support attacker
		if( attackerToSupport != null ) {
			// remove my own supporters
			for (ArmyElement supporter : shooterElement.getSupporters()) {
				supporter.elementState.supportAttack = null;
			}

			// remove shooting (if exist from action before)
			shooterElement.elementState.aimsAt = null;
			shooterElement.elementState.aimingDistance = -1;

			// set support
			shooterElement.elementState.supportAttack = attackerToSupport;
		} else {
			// test if target in melee
			shooterElement.elementState.aimsAtTargetInMelee = target.elementIsInMeleeFight(hexPanel);
			
			// shoot
			shooterElement.elementState.supportAttack = null;
			shooterElement.elementState.aimsAt = target;
			shooterElement.elementState.aimingDistance = hexPanel.hexagonalGridCalculator.calculateDistanceBetween(
					shooterElement.elementState.getMapPosition(hexPanel), 
					target.elementState.getMapPosition(hexPanel));
		}		
		
	}

	private void placeOrdersAction() {
		Army myArmy = mainControl.gameState.playerArmys.get(aiControlSource);
		UnitOrder[] orders = UnitOrder.values();
		
		for( ArmyUnit unit: myArmy.getUnits() ) {
			UnitOrder actOrder = UnitOrder.NO_ORDER;
			while( actOrder.equals( UnitOrder.NO_ORDER ) ) {
				actOrder = orders[rand.nextInt(orders.length)];
			}
			unit.status.order = actOrder;
		}
		mainControl.mainFrame.repaint();
	}

	private void setupArmysAction() {
		int availableArmyPoints = -1;
		
		availableArmyPoints = mainControl.gameState.playerArmys.get(opponentControlSource).totalArmyPoints;	
		availableArmyPoints -= mainControl.gameState.playerArmys.get(aiControlSource).totalArmyPoints;
		
		Army sourceArmy = mainControl.fullArmys.get(ArmyType.ORCS);
		List<ArmyElement> sourceElements = new ArrayList<ArmyElement>();
		for( ArmyElement el : sourceArmy.getElements() ) {
			sourceElements.add( el );
		}
		//System.out.println( "avail points:"+availableArmyPoints );
		
		// choose unit type
		int actRand;
		while( sourceElements.size() > 0 ) { // until all units are too expensive...
			// choose unit randomly
			actRand = rand.nextInt( sourceElements.size() );
			// if unit is too expensive , remove
			if( sourceElements.get(actRand).armyValuePoints > availableArmyPoints ) {
				System.out.println( "too expensive, remove element "+sourceElements.get(actRand) );
				sourceElements.remove(actRand);
				continue;
				
			} else {
				
				List<Hexagon> unitPos = getPositionsForNewUnit(sourceElements.get(actRand), sourceElements.get(actRand).armyUnit.defaultNumberOfElements  );
				
				System.out.println( unitPos );
				//List<Hexagon> unitPos = new ArrayList<Hexagon>();
				//unitPos.add( mainControl.mainViewControl.hexPanel.getHexByGridCoordinate(10, 10) );
				for( Hexagon hex: unitPos ) {
					ArmyElement newEl = sourceElements.get(actRand).copy();
					if( aiControlSource.equals(ControlSource.LOCAL_PLAYER) ) {
						mainControl.gm_mapSetupControlLocal.addArmyElement(newEl, hex, ViewDirection.TOP);
					} else {
						mainControl.gm_mapSetupControlOtherPlayer.addArmyElement(newEl, hex, ViewDirection.BOTTOM);
					}
				}
				availableArmyPoints -= sourceElements.get(actRand).armyValuePoints;
			}
			
		}
		mainControl.mainFrame.repaint();
		
	}

	/**
	 * create a list of refs to free hexes for a new unit
	 * @param el
	 * @param amount
	 * @return
	 */
	public List<Hexagon> getPositionsForNewUnit( ArmyElement el, int amount ) {
		ArrayList<Hexagon> unitPos = new ArrayList<Hexagon>();
		HexagonPanel hexPanel = mainControl.mainViewControl.hexPanel;
		int z = 1;
		int x, xIncrement;
		
		if( aiControlSource.equals(ControlSource.LOCAL_PLAYER) ) {
			x = 20;
			xIncrement = -1;
		} else {
			x = 1;
			xIncrement = 1;
		}
		
		boolean notFree = true;
		
		while( notFree ) {
			System.out.println( "notFree" );
			notFree = false;
			for( int i=0; i<amount; i++ ){
				Hexagon actHex = hexPanel.getHexByGridCoordinate(x, z+i);
				if( ((SatelliteData)actHex.getSatelliteData()).element != null ) {
					notFree = true;
				}
			}
			
			if( notFree ) { // try next pos
				z += 2;
				if( z > 30 ) {
					z = 1;
					x += xIncrement;
				}
			} else { // place army
				for( int i=0; i<amount; i++ ){
					unitPos.add( hexPanel.getHexByGridCoordinate(x, z+i) );
				}
				return unitPos;
			}
		}
		
		return null;
		
	}

	
	
	private boolean isUnitInMelee( Position[] unitPositions ) {
		Army opponentArmy = mainControl.gameState.playerArmys.get(opponentControlSource);
		if( ArmyUnit.isUnitInMelee(unitPositions, opponentArmy.getElementPositions()) ) {
			return true;
		}else return false;
	}
	
	private boolean isValidToMoveOn(Hexagon toHex, ArmyUnit myUnit) {
		ArmyElement toHexEl = null;
		try {
			toHexEl = ((SatelliteData)toHex.getSatelliteData()).element;
		}catch( HexagonNotFoundException e ) {
			return false; // hexagon may be off grid
		}
		if( toHex != null && toHexEl != null) {
			if( !toHexEl.armyUnit.equals(myUnit) ) { 
				// if a targetfield contains element of other unit, dont use this move
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isValidToMoveOn(Position actPos, ArmyUnit myUnit) {
		try {
			Hexagon actHex = mainControl.mainViewControl.hexPanel.getHexByPosition(actPos);
			return isValidToMoveOn(actHex, myUnit);
		} catch (HexagonNotFoundException e) {
			System.err.println( "position:"+actPos +" is off the grid or invalid pos");
			return false;
		}
	}
	
	private ArmyElement getArmyElementAtPosition( Position p ) {
		try {
			Hexagon actHex = mainControl.mainViewControl.hexPanel.getHexByPosition(p);
			return ((SatelliteData)actHex.getSatelliteData()).element;
		} catch (HexagonNotFoundException e) {
			return null;
		}
	}
	
	private boolean containsEnemy( Position pos ) {
		Army opponentArmy = mainControl.gameState.playerArmys.get(opponentControlSource);
		for( Position p : opponentArmy.getElementPositions() ) {
			if( p.equals( pos ) ) {
				return true;
			}
		}
		return false;
	}
	
}

