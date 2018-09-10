package demonworld.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import demonworld.model.ArmyElement;

public class InfoBox extends JPanel {
	
	public Vector<JLabel> keyLabels;
	public Vector<JTextArea> valueLabels;
	private Color textColor = Color.LIGHT_GRAY ;
	private Font textFont;
	private SpringLayout springLayout;
		private Image backgroundImage;
	
	public InfoBox() {
		keyLabels = new Vector<JLabel>();
		valueLabels = new Vector<JTextArea>();
		

		//this.setBackground( Color.blue );
		this.setOpaque( false );
		
		
		//this.setLayout( new GridLayout(0,1) );
		this.springLayout = new SpringLayout();
		this.setLayout( springLayout );
		textFont = new Font("Trattatello", Font.PLAIN, 24);
		//this.add( );
		//this.add( new JLabel( "" ) );
		initLabels();
		
		
		
		//this.setBackground( null );
		//updateElementInfo(null);
		
		try {
			backgroundImage = ImageIO.read(new File("resources/images/background/putz.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private void initLabels() {
		int numberOfRows = 5;
		
		JLabel key;
		JTextArea val;
		for( int x = 0; x < numberOfRows; x++  ) {
			
			key = new JLabel("x");
			key.setForeground( textColor );
			key.setFont( textFont );
			//key.setOpaque(false);
			
			val = new JTextArea("y");
			val.setFont( textFont );
			val.setLineWrap(true);
			val.setWrapStyleWord(true);
			val.setEditable(false);
			val.setOpaque(false);
			
			
			keyLabels.add( key );
			valueLabels.add( val );
			
			this.add( keyLabels.get(x) );
			this.add( valueLabels.get(x) );
			
			
			// connect left corner of Mainpanel to keyLabel
			if( x == 0 ) { 
				springLayout.putConstraint(SpringLayout.WEST, key, 0, SpringLayout.WEST, this);
				springLayout.putConstraint(SpringLayout.NORTH, key, 5, SpringLayout.NORTH, this);
			} else {
				// connect key label to last added value label
				springLayout.putConstraint(SpringLayout.WEST, key, 0, SpringLayout.WEST, valueLabels.get(x-1));
				springLayout.putConstraint(SpringLayout.NORTH, key, 2, SpringLayout.SOUTH, valueLabels.get(x-1));
			}
			// connect valuelabel to keyLabel
			springLayout.putConstraint(SpringLayout.WEST, val, 0, SpringLayout.WEST, key);
			springLayout.putConstraint(SpringLayout.NORTH, val, 2, SpringLayout.SOUTH, key);
			
			// connect right corner of panel to valuelabels
			if( x == (numberOfRows-1) ) {
				springLayout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, val);
				springLayout.putConstraint(SpringLayout.SOUTH, this, 5, SpringLayout.SOUTH, val);
			}
			
		}
		/*
		SpringUtilities.makeCompactGrid(this,
                numberOfRows, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad*/
	}
	
	public void updateElementInfo( ArmyElement e ) {
		String[] keyLabelText = new String [] {
				"Unit:",
				"Army:",
				"Move points:",
				"Closed combat:",
				"Armor:"};
		String[] valueLabelText = new String [] {"","","","",""};
		if( e != null ) {
		valueLabelText = new String [] {
				e.unitDesignation,
				e.armyMembership.toString(),
				e.movePointsGeneral+"/"+e.movePointsAttack+"/"+e.movePointsSkirmish+"/"+e.movePointsHold+"/"+e.maneuverPoints,
				""+e.fightingSkillClosedCombat,
				""+e.armorPoints};
		} 
		int i = 0;
		for( String s : keyLabelText ) {
			keyLabels.get( i ).setText( keyLabelText[i] );
			valueLabels.get( i ).setText( valueLabelText[i] );
			i++;
		
		}
		 
	}
	
	public void paint(Graphics g) {
		
		//backgroundImage = backgroundImage.getScaledInstance( this.getWidth() , this.getHeight(), 0);
		
		
		
		g.drawImage(backgroundImage, 0, 0, null);
		super.paint(g);
	}
	
}
