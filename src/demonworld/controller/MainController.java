package demonworld.controller;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import demonworld.controller.server.ControlSource;
import demonworld.init.ArmyFactory;
import demonworld.map.GeneralMapDrawer;
import demonworld.model.Army;
import demonworld.model.ArmyElement;
import demonworld.model.ArmyType;
import demonworld.model.GameState;
import demonworld.server.InputEventBridgeToServer;
import demonworld.tools.TextManager;
import demonworld.view.MainFrame;


public class MainController {
	private boolean gameStart = true; //used to skip .endPhase() method in first phase
	public GameState gameState;
	public Vector<GameState> gameStateHistory = new Vector<GameState>();
	public Integer currentHistoryPointer = null;
	public MainFrame mainFrame; 
	
	// gameModeControllers
	public GameMode_SetupArmys_MapController gm_mapSetupControlLocal, gm_mapSetupControlOtherPlayer; // every player have its own controller
	public GameMode_Move_MapController gm_moveMapControllerLocal, gm_moveMapControllerOtherPlayer;
	public GameMode_PlaceOrders_MapController gm_PlaceOrdersMapControllerLocal, gm_PlaceOrdersMapControllerOtherPlayer;
	public GameMode_FightRanged_MapController gm_FightRanged_controllerLocal, gm_FightRanged_controllerOtherPlayer;
	public GameMode_FightMeele_MapController gm_FightMeele_controllerLocal, gm_FightMeele_controllerOtherPlayer;
	public MainViewController mainViewControl;
	public MenuController menuControl;
	public ToolBarController toolBarControl;
	public InputEventBridgeToServer serverBridge;
	public MainImageController mainImageControl ;
	private MainInputEventHandler mainInputHandler = new MainInputEventHandler();
	public String username;
	public TextManager textManager = new TextManager();
	/** all available Armys including all available Elements
	 * 
	 */
	public HashMap<ArmyType, Army> fullArmys; 
	public AIController aiController = new AIController(this, ControlSource.EXTERNAL_PLAYER);
	
	public void initGame(String username, String serverHost) throws IOException {
		this.username = username;
		// init GameState
		gameState = new GameState();
		
		//init View
		mainViewControl = new MainViewController(this);
		menuControl = new MenuController( this );
		toolBarControl = new ToolBarController( this );
		mainFrame = new MainFrame(mainViewControl, toolBarControl, username, mainInputHandler);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	            menuControl.saveGame(menuControl.autoSaveFile);
	            System.exit(0);
	        }
	    });
	
		mainImageControl = new MainImageController(mainViewControl.hexPanel.radius);
		((GeneralMapDrawer)mainFrame.hexMap.mapDrawer).initCursorImages();;
		
		// init Armys
		ArmyFactory armyFactory = new ArmyFactory(mainImageControl);
		File armyFile = new File("resources/armys.csv");
		if( !armyFile.exists() ) {
			System.out.println("file doesnt exist"+armyFile.getAbsolutePath());
			ClassLoader classLoader = MainController.class.getClassLoader();
			armyFile = new File(classLoader.getResource("armys.csv").getFile());
		}
		fullArmys = armyFactory.initFromFile( armyFile );
		
		//TODO choose army by selectlist through player
		gameState.availableArmyElements = fullArmys.get( ArmyType.IMPERIAL ).getElements();
		gameState.availableArmyElements.addAll( fullArmys.get( ArmyType.ORCS ).getElements() );
		
		gameState.playerArmys.put(ControlSource.LOCAL_PLAYER, new Army(ArmyType.IMPERIAL));
		gameState.playerArmys.put(ControlSource.EXTERNAL_PLAYER, new Army(ArmyType.ORCS));
		
		// set Army Colors
		gameState.playerArmys.get(ControlSource.LOCAL_PLAYER).armyColor = Color.decode("#2B65EC");
		gameState.playerArmys.get(ControlSource.EXTERNAL_PLAYER).armyColor = Color.decode("#F62217");
		
		


		
		
		// first gamecontroller is to prepare initial map
		gm_mapSetupControlLocal = new GameMode_SetupArmys_MapController( mainFrame.hexMap, ControlSource.LOCAL_PLAYER);
		gm_mapSetupControlOtherPlayer = new GameMode_SetupArmys_MapController( mainFrame.hexMap, ControlSource.EXTERNAL_PLAYER);
		gm_PlaceOrdersMapControllerLocal = new GameMode_PlaceOrders_MapController( mainFrame.hexMap, ControlSource.LOCAL_PLAYER);
		gm_PlaceOrdersMapControllerOtherPlayer = new GameMode_PlaceOrders_MapController( mainFrame.hexMap, ControlSource.EXTERNAL_PLAYER);
		gm_FightRanged_controllerLocal = new GameMode_FightRanged_MapController( mainFrame.hexMap, ControlSource.LOCAL_PLAYER);
		gm_FightRanged_controllerOtherPlayer = new GameMode_FightRanged_MapController( mainFrame.hexMap, ControlSource.EXTERNAL_PLAYER);
		gm_moveMapControllerLocal = new GameMode_Move_MapController( mainFrame.hexMap, ControlSource.LOCAL_PLAYER);
		gm_moveMapControllerOtherPlayer = new GameMode_Move_MapController( mainFrame.hexMap, ControlSource.EXTERNAL_PLAYER);
		gm_FightMeele_controllerLocal = new GameMode_FightMeele_MapController( mainFrame.hexMap, ControlSource.LOCAL_PLAYER);
		gm_FightMeele_controllerOtherPlayer = new GameMode_FightMeele_MapController( mainFrame.hexMap, ControlSource.EXTERNAL_PLAYER);
		
		ArrayList<InputHandlerInterface> eventHandler = new ArrayList<InputHandlerInterface>();
		eventHandler.add(gm_mapSetupControlOtherPlayer);
		eventHandler.add(menuControl);
		
			serverBridge = new InputEventBridgeToServer( 
				serverHost, 
				8087, 
				username, 
				eventHandler,
				toolBarControl, this);
		


		
		// create main inputHandler (gets all inputs (e.g. from hexMap), sends it to local conroller and server)
		
		mainInputHandler.addSubHandler(menuControl, true);
		mainInputHandler.addSubHandler(serverBridge, true);
		mainInputHandler.addSubHandler(gm_mapSetupControlLocal, true);
		mainInputHandler.addSubHandler(gm_PlaceOrdersMapControllerLocal, false);
		mainInputHandler.addSubHandler(gm_FightRanged_controllerLocal, false);
		mainInputHandler.addSubHandler(gm_moveMapControllerLocal, false);
		mainInputHandler.addSubHandler(gm_FightMeele_controllerLocal, false);
		
		setGameMode( GameMode.SETUP_ARMYS );
		
		gameState.map = gm_mapSetupControlLocal.getHexPanel().getHexagonalGrid();
		
		serverBridge.start();
		mainFrame.repaint();
		// load last saved game
		//menuControl.loadGame(menuControl.autoSaveFile);
		menuControl.startAutoSave();
		mainFrame.hexMap.requestFocus();

	}
	
	//TODO: optimize the code below
	public void setGameMode( GameMode newMode ) {
		if( newMode != gameState.currentGameMode ) {
			if(gameStart) {
				gameStart = false;
			} else {
				endCurrentPhase();
				serverBridge.sendMessage("switched to GameMode:"+newMode.name());
			}
			mainInputHandler.deactivateAllHandler();
			mainInputHandler.setHandlerActivation(menuControl, true);
			mainInputHandler.setHandlerActivation(serverBridge, true);
			
			ArrayList<InputHandlerInterface> eventHandler = new ArrayList<InputHandlerInterface>();
			eventHandler.add(menuControl);
			
			switch( newMode ) {
				case SETUP_ARMYS:
					eventHandler.add(gm_mapSetupControlOtherPlayer);
					mainInputHandler.setHandlerActivation(gm_mapSetupControlLocal, true);
					gm_mapSetupControlLocal.init();
					gm_mapSetupControlOtherPlayer.init();

				break;
				case PLACE_ORDERS:
					eventHandler.add(gm_PlaceOrdersMapControllerOtherPlayer);
					mainInputHandler.setHandlerActivation(gm_PlaceOrdersMapControllerLocal, true);
					gm_PlaceOrdersMapControllerLocal.init();
					gm_PlaceOrdersMapControllerOtherPlayer.init();
				break;

				case FIGHT_RANGED_SKIRMISH:
					eventHandler.add(gm_FightRanged_controllerOtherPlayer);
					mainInputHandler.setHandlerActivation(gm_FightRanged_controllerLocal, true);
					gm_FightRanged_controllerLocal.init();
					gm_FightRanged_controllerOtherPlayer.init();
				break;

				case MOVE:
					eventHandler.add(gm_moveMapControllerOtherPlayer);
					mainInputHandler.setHandlerActivation(gm_moveMapControllerLocal, true);
					gm_moveMapControllerLocal.init();
					gm_moveMapControllerOtherPlayer.init();
				break;

				case FIGHT_RANGED_HOLD:
					eventHandler.add(gm_FightRanged_controllerOtherPlayer);
					mainInputHandler.setHandlerActivation(gm_FightRanged_controllerLocal, true);
					gm_FightRanged_controllerLocal.init();
					gm_FightRanged_controllerOtherPlayer.init();
				break;

				case FIGHT_MEELE:
					eventHandler.add(gm_FightMeele_controllerOtherPlayer);
					mainInputHandler.setHandlerActivation(gm_FightMeele_controllerLocal, true);
					gm_FightMeele_controllerLocal.init();
					gm_FightMeele_controllerOtherPlayer.init();
				break;

				case PREPARE_NEXTROUND: {
					for( ControlSource source : ControlSource.values() ) {
						for( ArmyElement e : gameState.playerArmys.get(source).getElements() ){
							e.elementState.aimingDistance = 0;
							e.elementState.aimsAt = null;
							e.elementState.attackAt = null;
							e.elementState.supportAttack = null;
							//	unit
							//e.armyUnit.status.order = null;
							e.armyUnit.status.isEngagedInMeele = false; // first set to false, then test if it is again in melee
							if( e.hasEnemyNeighbour(mainFrame.hexMap) ) {
								e.armyUnit.status.isEngagedInMeele = true;
							}
							
							e.armyUnit.status.engagedInMeleeThisRound = false;
						}
						
					}
				}
				
			}
			serverBridge.setInputEventSubHandler(eventHandler);
			gameState.currentGameMode = newMode;
			mainViewControl.currentGameMode = newMode;
			
		}
		mainFrame.repaint();
	}
	
	private void endCurrentPhase() {
		switch (gameState.currentGameMode) {
		case SETUP_ARMYS:
			gm_mapSetupControlLocal.endPhase();
			gm_mapSetupControlOtherPlayer.endPhase();

		break;
		case PLACE_ORDERS:
			gm_PlaceOrdersMapControllerLocal.endPhase();
			gm_PlaceOrdersMapControllerOtherPlayer.endPhase();
		break;

		case FIGHT_RANGED_SKIRMISH:
			gm_FightRanged_controllerLocal.endPhase();
			gm_FightRanged_controllerOtherPlayer.endPhase();
		break;

		case MOVE:
			gm_moveMapControllerLocal.endPhase();
			gm_moveMapControllerOtherPlayer.endPhase();
		break;

		case FIGHT_RANGED_HOLD:
			gm_FightRanged_controllerLocal.endPhase();
			gm_FightRanged_controllerOtherPlayer.endPhase();
		break;

		case FIGHT_MEELE:
			gm_FightMeele_controllerLocal.endPhase();
			gm_FightMeele_controllerOtherPlayer.endPhase();
		break;
		default:
		break;
		}
		
		mainFrame.hexMap.repaint();
	}


	public static ControlSource getOpponentControlSource (ControlSource myself) {
		if( myself.equals( ControlSource.LOCAL_PLAYER ) ) {
			return ControlSource.EXTERNAL_PLAYER;
		} else {
			return ControlSource.LOCAL_PLAYER;
		}
	}
	
	public void updateGameStateHistory() {
		// deep clone gameState using serializable
		GameState copy = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this.gameState);

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			copy = (GameState) ois.readObject();
			
			// If pointer is before the end of history, we delete all gameStates after and thenn add the new Gamestate at the end.
			if( currentHistoryPointer != null) {
				if (currentHistoryPointer < gameStateHistory.size()-1 && gameStateHistory.size() > 0){
					System.out.println( "remove from history" );
					while( currentHistoryPointer < gameStateHistory.size() ) {
						gameStateHistory.removeElement( gameStateHistory.size()-1 ); // remove last element until we rech currentPointer
					}
				}
			} 
			gameStateHistory.addElement( copy );	
			currentHistoryPointer = gameStateHistory.size()-1;
			System.out.println( "historyPointer:"+currentHistoryPointer );
		} catch (Exception e) {
			System.err.println( "unable to save gameState" );
			e.printStackTrace();
		}
	}
	
	

	
}
