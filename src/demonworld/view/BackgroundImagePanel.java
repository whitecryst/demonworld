package demonworld.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class BackgroundImagePanel extends JPanel {
	private Image backgroundImage;
	
	public BackgroundImagePanel(String imgFile) {
		
		
		try {
			backgroundImage = ImageIO.read(new File(imgFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.add(new JLabel("XXX"));
		this.setLayout( new BorderLayout());
		
		this.setOpaque(false);
		//this.setVisible(true);
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g); 
		//Image backgroundImage;
		try {
			//backgroundImage = ImageIO.read(new File("resources/images/background/gras2.jpg"));
			//backgroundImage = ImageIO.read(new File("resources/images/background/background.jpg"));
			if( backgroundImage.getWidth( null) != this.getWidth() || backgroundImage.getHeight(null) != this.getHeight() ) {
				backgroundImage = backgroundImage.getScaledInstance( this.getWidth() , this.getHeight(), 0);
			}
			g.drawImage(backgroundImage, 0, 0, null);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
