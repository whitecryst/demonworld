package demonworld.view;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import demonworld.controller.GameMode;
import demonworld.controller.MainInputEventHandler;
import demonworld.controller.MenuController;
import demonworld.controller.ToolBarController;
import demonworld.model.ArmyType;

public class MainMenu extends JMenu {
	
	
	
	public List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
	
	public MainMenu( MainInputEventHandler inputEventHandler, MenuController menuControl ) {
		super( "Game" );
		
		
		this.setMnemonic(KeyEvent.VK_G);
		this.getAccessibleContext().setAccessibleDescription(
		        "Game Menü");
		/*
		//select Army
		JMenu submenu = new JMenu("Select Army");
		for( ArmyType type : ArmyType.values()) {
			JMenuItem menuItem = new JMenuItem( type.name() );
			menuItem.setActionCommand( type.name() );
			menuItem.addActionListener( menuControl );
			submenu.add(menuItem);
		}
		this.add(submenu);
		*/
		
		JMenuItem menuItem5 = new JMenuItem("Undo last action");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(	KeyEvent.VK_1, ActionEvent.ALT_MASK));
		//menuItem.getAccessibleContext().setAccessibleDescription("Start playing");
		menuItem5.setActionCommand( "undo" );
		menuItem5.addActionListener( menuControl );
		this.add(menuItem5);
		
		
		JMenuItem menuItem4 = new JMenuItem("New game");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(	KeyEvent.VK_1, ActionEvent.ALT_MASK));
		//menuItem.getAccessibleContext().setAccessibleDescription("Start playing");
		menuItem4.setActionCommand( "newGame" );
		menuItem4.addActionListener( menuControl );
		this.add(menuItem4);
		
		JMenuItem menuItem2 = new JMenuItem("Save game");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(	KeyEvent.VK_1, ActionEvent.ALT_MASK));
		//menuItem.getAccessibleContext().setAccessibleDescription("Start playing");
		menuItem2.setActionCommand( "saveGame" );
		menuItem2.addActionListener( menuControl );
		this.add(menuItem2);

		JMenuItem menuItem3 = new JMenuItem("Load game");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(	KeyEvent.VK_1, ActionEvent.ALT_MASK));
		//menuItem.getAccessibleContext().setAccessibleDescription("Start playing");
		menuItem3.setActionCommand( "loadGame" );
		menuItem3.addActionListener( menuControl );
		this.add(menuItem3);
		
		// select gamne Mode
		
		
		/*
		int modeNr = 0;
		for( GameMode mode : GameMode.values() ) {
			JMenuItem menuItem = new JMenuItem(mode.name());
			//menuItem.setAccelerator(KeyStroke.getKeyStroke(	KeyEvent.VK_1, ActionEvent.ALT_MASK));
			//menuItem.getAccessibleContext().setAccessibleDescription("Start playing");
			menuItem.setActionCommand( mode.name() );
			menuItem.addActionListener( inputEventHandler );
			this.add(menuItem);
			menuItems.add(menuItem);
			modeNr ++;
		}*/
		
		JMenuItem menuItem = new JMenuItem("Show Rules");
		//menuItem.setAccelerator(KeyStroke.getKeyStroke(	KeyEvent.VK_1, ActionEvent.ALT_MASK));
		//menuItem.getAccessibleContext().setAccessibleDescription("Start playing");
		menuItem.setActionCommand( "showRules" );
		menuItem.addActionListener( menuControl );
		this.add(menuItem);
		//menuItems.add(menuItem); //dont add to menuItems to ensure that no one can open pdf on other clients

		JMenu opponentMenu = new JMenu("AI");
		JMenuItem opMenuItem1 = new JMenuItem("New Opponent");
		opMenuItem1.setActionCommand( "opponent_new" );
		opMenuItem1.addActionListener( menuControl );
		opponentMenu.add(opMenuItem1);
		this.add(opponentMenu);
		
		JMenu toolsMenu = new JMenu("Tools");
		JCheckBoxMenuItem opMenuItem2 = new JCheckBoxMenuItem("Show GridCoords");
		opMenuItem2.setActionCommand( "tools_gridcoords" );
		opMenuItem2.addActionListener( menuControl );
		toolsMenu.add(opMenuItem2);
		this.add(toolsMenu);
		
		
	}
	
	
}
