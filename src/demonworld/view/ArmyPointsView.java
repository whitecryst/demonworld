package demonworld.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;

import demonworld.controller.server.ControlSource;
import demonworld.model.Army;

public class ArmyPointsView extends JPanel {
	private int localArmyPointsAtStart, externalArmyPointsAtStart, localArmyPointsCurrent, externalArmyPointsCurrent;
	private JLabel armyPointsLabel;
	private JProgressBar[] pointBars;
	private int maxArmyPoints = 1;
	
	public ArmyPointsView() {
		super(new BorderLayout());
		this.setMaximumSize(new Dimension(100,50));
		armyPointsLabel = new JLabel();
		//this.add(armyPointsLabel);
		pointBars = new JProgressBar[2];
		
		pointBars[0] = new JProgressBar(0, maxArmyPoints);
		pointBars[1] = new JProgressBar(0, maxArmyPoints);
		
		pointBars[0].setStringPainted(true);
		pointBars[1].setStringPainted(true);
		
		pointBars[0].setForeground(Color.decode("#2B65EC"));
		pointBars[1].setForeground(Color.decode("#F62217"));
		
		pointBars[0].setBorder(new LineBorder(Color.black));
		pointBars[1].setBorder(new LineBorder(Color.black));
		
		pointBars[0].setUI(new BasicProgressBarUI() {
		      protected Color getSelectionBackground() { return Color.black; }
		      protected Color getSelectionForeground() { return Color.white; }
		    });
		pointBars[1].setUI(new BasicProgressBarUI() {
		      protected Color getSelectionBackground() { return Color.black; }
		      protected Color getSelectionForeground() { return Color.white; }
		    });
		
		
		this.add(pointBars[0], BorderLayout.NORTH);
		this.add(pointBars[1], BorderLayout.SOUTH);
	}
	
	
	
	public void updatePoints(ControlSource source, Army army) {
		if( army == null) return;
		
		if( army.totalArmyPoints > maxArmyPoints ) {
			maxArmyPoints = army.totalArmyPoints;
			pointBars[0].setMaximum(maxArmyPoints);
			pointBars[1].setMaximum(maxArmyPoints);
		}
		
		if( source == ControlSource.LOCAL_PLAYER ) {
			localArmyPointsAtStart = army.totalArmyPoints;
			localArmyPointsCurrent = army.getPoints();
			pointBars[0].setValue(localArmyPointsCurrent);
			pointBars[0].setString(localArmyPointsCurrent+"/"+localArmyPointsAtStart);
		} else if( source == ControlSource.EXTERNAL_PLAYER ) {
			externalArmyPointsAtStart = army.totalArmyPoints;
			externalArmyPointsCurrent = army.getPoints();
			pointBars[1].setValue(externalArmyPointsCurrent);
			pointBars[1].setString(externalArmyPointsCurrent+"/"+externalArmyPointsAtStart);
		}
		
		
	}

}
