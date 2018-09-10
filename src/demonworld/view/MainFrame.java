package demonworld.view;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicBorders.RadioButtonBorder;

import demonworld.controller.GameMode;
import demonworld.controller.MainInputEventHandler;
import demonworld.controller.MainViewController;
import demonworld.controller.MapScollingController;
import demonworld.controller.MenuController;
import demonworld.controller.ToolBarController;
import demonworld.controller.server.ControlSource;
import demonworld.map.GeneralMapDrawer;
import demonworld.map.HexagonPanel;
import demonworld.tools.TextManager.ContentID;

public class MainFrame extends JFrame {
	private Image backgroundImage = null;
	public HexagonPanel hexMap;
	public MainViewController mainViewControl;
	//private MenuController menuControl;
	private ToolBarController toolBarControl;
	public MainMenu mainMenu;
	public CustomToolBar toolBar;
	public JLabel diceResultLabel;
	public JLabel msgLabel;
	public ArrayList<String> msgHistory = new ArrayList<String>();
	public JLabel infoLabel, miniMapLabel, announcementLabel;
	public ImageIcon infoIcon, miniMapIcon;
	public final String DEFAULT_DICE_RESULT = " Roll it! "; 
	public static final String fontFamily = "Trattatello";
	public MainInputEventHandler mainInputHandler;
	public HashMap<String, JRadioButton> gameModeButtons = new HashMap<String, JRadioButton>();
	private BufferedImage pergamentImg;
	public JScrollPane mapScrollPane;
	public ArmyPointsView armyPointsView;
	
	
	public MainFrame( 
			MainViewController mainViewControl, 
			/*MenuController menuControl,*/ 
			ToolBarController toolBarControl,
			String username, 
			MainInputEventHandler mainInputHandler ) throws IOException {
		super("DemonWorld - "+username);
		this.mainInputHandler = mainInputHandler;
		this.mainViewControl = mainViewControl;
		//this.menuControl = menuControl;
		this.toolBarControl = toolBarControl;
		
		init();
		ToolTipManager.sharedInstance().setDismissDelay(60000);
        try {
			pergamentImg = ImageIO.read(new File("resources/images/pergament.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        

	}
	

	public void init() throws IOException {
		//backgroundImage = ImageIO.read(new File("resources/images/background/putz.jpg"));
//		try {
//			backgroundImage = ImageIO.read(new File("resources/images/background/background.jpg"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		final int tableWidth = 1500;//800;//1280;
		final int tableHeight = 430;//740;//600;//1024;
		final int mapWidth = 1410;//800;//1280;
		final int mapHeight = 820;//700;//600;//1024;
		
		hexMap = new HexagonPanel( mapWidth, mapHeight );
		mainInputHandler.addListenerToComponent(hexMap, true);
		hexMap.mapDrawer = new GeneralMapDrawer(hexMap);
		// define Infobox
		//InfoBox info = new InfoBox();
		//mainViewControl.infoBox = info;

		mainViewControl.hexPanel = hexMap ;
		hexMap.mainViewControl = mainViewControl;
		hexMap.addMouseListener(mainViewControl);
		hexMap.addMouseMotionListener(mainViewControl);
		
		JMenuBar menuBar = new JMenuBar();
		mainMenu = new MainMenu( mainInputHandler, mainViewControl.mainControl.menuControl );
		menuBar.add( mainMenu );

		// toolbar
		toolBar = new CustomToolBar();
		
		 //first button
	    JButton button = makeNavigationButton(
	    		"wuerfel.png", 
	    		ToolBarController.ActionCommand.ROLL_DICE.name(),
	            "Roll Dice, receive Random Number",
	           "Roll it!");
        
	    diceResultLabel = new JLabel( DEFAULT_DICE_RESULT );
	    diceResultLabel.setFont( new Font("Trattatello", Font.PLAIN, 24) );
	    diceResultLabel.setToolTipText("Dice roll result is shown here");
	    msgLabel = new JLabel(  );
	    
	    msgLabel.setToolTipText("Other players Dice roll is shown here");

	    toolBar.add(button);
	    toolBar.add(diceResultLabel);
	    toolBar.addSeparator();
	    addGameModeButtons(toolBar);
	    toolBar.addSeparator();
	    /*
	    JSlider hexagonRadiusSlider = new JSlider(JSlider.HORIZONTAL,10, 30, 5);
	    // TODO identify slider in mainVieControl
	    hexagonRadiusSlider.addChangeListener(mainViewControl);
	    toolBar.addSeparator();*/
	    
	    try {
			miniMapIcon = new ImageIcon(ImageIO.read(new File ("resources/images/toolbar/miniMap2.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    miniMapLabel = new JLabel();
	    miniMapLabel.setName("miniMap");
	    miniMapLabel.addMouseListener( toolBarControl );
	    miniMapLabel.setIcon(miniMapIcon);
	    toolBar.add(miniMapLabel);
	    armyPointsView = new ArmyPointsView();
	    
	    announcementLabel = new JLabel();
	    announcementLabel.setName("announcements");
	    try {
	    	announcementLabel.setIcon( new ImageIcon(ImageIO.read(new File ("resources/images/toolbar/book.png"))) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    announcementLabel.addMouseListener( toolBarControl );
	    toolBar.add(announcementLabel);
	    ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(500);
	    
	    
	    toolBar.add(armyPointsView);
	    final ImageIcon icon = new ImageIcon(ImageIO.read(new File ("resources/images/toolbar/chip.png")));
	    final ImageIcon icon2 = new ImageIcon(ImageIO.read(new File ("resources/images/toolbar/chip2.png")));
	    final ImageIcon icon3 = new ImageIcon(ImageIO.read(new File ("resources/images/toolbar/chip3.png")));
		final JButton aiActionButton = new JButton(icon);
	    aiActionButton.addActionListener(toolBarControl);
	    aiActionButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				aiActionButton.setIcon(icon);
			}
			@Override
			public void mousePressed(MouseEvent e) {
				aiActionButton.setIcon(icon2);
			}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
	    aiActionButton.setActionCommand(ToolBarController.ActionCommand.AI_ACTION.name());
	    toolBar.add(aiActionButton);
	    
	    final JButton aiRollItButton = new JButton(icon3);
	    aiRollItButton.addActionListener(toolBarControl);
	    aiRollItButton.setActionCommand( ToolBarController.ActionCommand.AI_ROLLIT.name() );
	    toolBar.add(aiRollItButton);
	    
	   
	    
	    toolBar.add(msgLabel);

	    this.setJMenuBar( menuBar );
	    
		this.setLayout( new BorderLayout() );
		this.add(toolBar, BorderLayout.PAGE_START);
		//this.add(menuBar, BorderLayout.NORTH);
		
		
		
		BackgroundImagePanel tablePanel = new BackgroundImagePanel( "resources/images/background/background.jpg" );
		tablePanel.setBorder( new EmptyBorder(10, 10, 10, 10) );
		BackgroundImagePanel pergamentPanel = new BackgroundImagePanel( "resources/images/pergament.png");
		pergamentPanel.setBorder( new EmptyBorder(10, 25, 10, 25) );
		
		// grassBackground via panel
		/*BackgroundImagePanel grassPanel = new BackgroundImagePanel( "resources/images/background/gras2.jpg");
		grassPanel.add(hexMap, BorderLayout.CENTER);
		pergamentPanel.add( grassPanel, BorderLayout.CENTER);
		*/
		mapScrollPane = new JScrollPane(hexMap, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mapScrollPane.setOpaque(false);
		mapScrollPane.setBorder(null);
		mapScrollPane.getViewport().setOpaque(false);
		InputMap im = mapScrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke("UP"), "none");
        im.put(KeyStroke.getKeyStroke("DOWN"), "none");
        im.put(KeyStroke.getKeyStroke("LEFT"), "none");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "none");
		
		//JScrollPane scrollPane = new JScrollPane(hexMap);
		MapScollingController scrollControl = new MapScollingController(mapScrollPane, hexMap);
		hexMap.addMouseMotionListener(scrollControl);
		hexMap.addMouseListener(scrollControl);
		//scrollPane.setVisible(false);
		
		pergamentPanel.add( mapScrollPane, BorderLayout.CENTER);
		
		tablePanel.add( pergamentPanel, BorderLayout.CENTER);
		infoLabel = new JLabel("<html><body><table width=270><tr><td>&nbsp;</td></tr></table></body></html>");
		infoIcon = new ImageIcon();
		infoLabel.setIcon(infoIcon);
		infoLabel.setVerticalTextPosition(JLabel.BOTTOM);
		infoLabel.setHorizontalTextPosition(JLabel.CENTER);
		BackgroundImagePanel infoPanel = new BackgroundImagePanel( "resources/images/pergament.png");
		infoPanel.add(infoLabel, BorderLayout.CENTER );
		infoPanel.setPreferredSize(new Dimension(333, 600));
		
		JScrollPane infoScrollPane = new JScrollPane(infoPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScrollPane.setOpaque(false);
		infoScrollPane.setBorder(null);
		infoScrollPane.getViewport().setOpaque(false);
		//JScrollPane scrollPane = new JScrollPane(hexMap);
		MapScollingController scrollControl2 = new MapScollingController(infoScrollPane, infoPanel);
		infoPanel.addMouseMotionListener(scrollControl2);
		infoPanel.addMouseListener(scrollControl2);

		
		//infoPanel.add(new JLabel(infoIcon), BorderLayout.NORTH);
		
		
		tablePanel.add(infoScrollPane, BorderLayout.EAST );
		
		
		this.add(tablePanel, BorderLayout.CENTER );
		
		
		//frame.setBackground( Color.white );
		this.setSize(tableWidth, tableHeight);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
        
	}
	
	private ButtonGroup addGameModeButtons(JToolBar toolBar) {
		
		ButtonGroup group = new ButtonGroup();
		int x = 0;
	    for( GameMode mode : GameMode.values() ) {
	    	
	    	//JRadioButton button = new JRadioButton(""+x);
	    	JRadioButton button;
	    	ImageIcon icon = null;
	    	String fileName = "resources/images/toolbar/gameMode_"+x+".png";
			try {
				icon = new ImageIcon(ImageIO.read(new File(fileName)));
				button = new JRadioButton(icon);
			} catch (IOException e) {
				System.err.println("ToolBar: unable to load img: "+fileName);
				button = new JRadioButton(""+x);
			}
			button.setBorder(new LineBorder(Color.black));
			
	    	gameModeButtons.put(mode.name(), button);
	    	for( ContentID id : ContentID.values() ) {
	    		if( id.name().equals( "Mode_"+mode.name() ) ) {
	    			button.setToolTipText(mainViewControl.mainControl.textManager.getText(id));
	    		}
	    	}
	    	button.setActionCommand(mode.name());
	    	button.addActionListener( mainInputHandler );
	    	group.add(button);
	    	x++;
	    	toolBar.add(button);
	    	
	    	if( mode.equals(GameMode.SETUP_ARMYS) ) {
	    		button.setSelected(true);
	    		toolBar.addSeparator();
	    	}
	    }
	    
	   
	    return group;
	}
	
	public void paint( Graphics g ) {
		super.paint(g);
		for( JRadioButton b : gameModeButtons.values() ) {
			if( mainViewControl.mainControl.gameState.currentGameMode != null && b.getActionCommand() != null) {
				if( b.getActionCommand().equals( mainViewControl.mainControl.gameState.currentGameMode.name()) ) {
					b.setBorderPainted(true);
				} else {
					b.setBorderPainted(false);
				}
				
			}
			
		}
		
	    
	    /*
	    g.drawImage(backgroundImage, 0, 0, null);
	    Font f = new Font("Trattatello", Font.PLAIN, 120);
	    g.setFont( f );
	    g.drawString("DemonWorld", (this.getWidth()/2)-300, (this.getHeight()/2)-50);
	    
	    f = new Font("Trattatello", Font.PLAIN, 40);
	    g.setFont( f );
	    g.drawString("by BasicAlb", (this.getWidth()/2)-85, (this.getHeight()/2)+50);
	    */
	    if( toolBarControl.showMiniMap ) {
	    	
	    	BufferedImage miniMap = ((GeneralMapDrawer)mainViewControl.hexPanel.mapDrawer).miniMap;
	 		int portraitHeight = 300;
	 		double scaleFactor =  (double)portraitHeight / (double)miniMap.getHeight(null);
	 		
	 		AffineTransform at = new AffineTransform();	
	 		at.scale(scaleFactor, scaleFactor);
	 		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
	 		miniMap = op.filter(miniMap, null);

	 		if( pergamentImg.getHeight() != miniMap.getHeight() || pergamentImg.getWidth() != miniMap.getWidth() ) {
		 		//at = new AffineTransform();	
		 		//at.scale((pergamentImg.getHeight()/miniMap.getHeight()), (pergamentImg.getWidth()/miniMap.getWidth()));
		 		//op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		 		//pergamentImg = op.filter(pergamentImg, null);
	 		}
	 		//g.fillRect(miniMapLabel.getX(), miniMapLabel.getY(), miniMap.getWidth(null), portraitHeight);
	 		//g.drawImage(pergamentImg, miniMapLabel.getX(), miniMapLabel.getY(), null);
	 		int x = 480;
	 		int y = 80;		
	 		g.drawImage(miniMap, x, y, null);
	 		g.drawRect(x, y, miniMap.getWidth(), miniMap.getHeight());
	 		g.setColor(Color.darkGray);
	 		//draw scrollWindow
	 		g.drawRect(
	 				(int)Math.round( x+(mapScrollPane.getViewport().getViewPosition().getX()*scaleFactor) ), 
	 				(int)Math.round( y+(mapScrollPane.getViewport().getViewPosition().getY()*scaleFactor) ), 
	 				(int)Math.round( mapScrollPane.getVisibleRect().getWidth()*scaleFactor), 
	 				(int)Math.round( mapScrollPane.getVisibleRect().getHeight()*scaleFactor));
	    }
	    
	    
	  }


	protected JButton makeNavigationButton(
			String imageFileName,
			String actionCommand, 
			String toolTipText, 
			String altText) {
		// Look for the image.
		String imgLocation = "resources/images/" + imageFileName ;
		//URL imageURL = MainFrame.class.getResource(imgLocation);

		// Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(toolBarControl);

		if (new File(imgLocation).exists() ) { // image found
			button.setIcon(new ImageIcon(imgLocation, altText));
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}

		return button;
	}


	
		
	

	
	
}


