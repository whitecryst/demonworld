package demonworld.controller;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import demonworld.model.ArmyElement;
import demonworld.model.ViewDirection;

public class MainImageController {

	private HashMap<String, ArrayList<BufferedImage>> armyElementIconsByDesignation;
	private HashMap<String, BufferedImage> armyElementPortraits;
	private HashMap<String, BufferedImage> cursorIcons;
	public Image emptyImage;
	int hexPanelRadius = -1;
	
	
	public MainImageController( int hexPanelRadius ) {
		armyElementIconsByDesignation = new HashMap<String, ArrayList<BufferedImage>>();
		armyElementPortraits = new HashMap<String, BufferedImage>();
		cursorIcons = new HashMap<String, BufferedImage>();
		this.hexPanelRadius = hexPanelRadius;
		emptyImage = (new JLabel()).createImage(1, 1);
	}
	
	public void addArmyElementImage( String unitDesignation, File icon ) {
		
		if( !armyElementIconsByDesignation.containsKey(unitDesignation) ) {
			BufferedImage iconImg = null;
			if (icon.exists()) {
				try {
					iconImg = ImageIO.read( icon );
					
				} catch (IOException e) {
				}
			} else {
				try {
					iconImg = ImageIO.read( new File("resources/images/defaultElement.gif") );
				} catch (IOException e) {
				}
			}

			ArrayList<BufferedImage> imagesByViewDirection = new ArrayList<BufferedImage>();
			int[] degrees = new int[] {0, 60, 120, 180, 240, 300};
			
			//double scaleFactor = hexPanelRadius / ((double)iconImg.getHeight() * 0.51); //20=hexPanel.radius
			double scaleFactor = hexPanelRadius / ((double)iconImg.getHeight() * 0.48); //20=hexPanel.radius
			//scaleFactor = 0.4;
			//System.out.println( "scaleFactor:"+scaleFactor+" ImgHeight:"+iconImg.getHeight() );
			for( int rotateDegree : degrees ) {
				BufferedImage newImage = deepCopy(iconImg);
				
				AffineTransform at = new AffineTransform();	
				at.scale(scaleFactor, scaleFactor);
				//at.rotate(Math.toRadians(rotateDegree), newImage.getWidth()/2, newImage.getHeight()/2);
				AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				imagesByViewDirection.add( op.filter(newImage, null) );
				//imagesByViewDirection.add( newImage );
				
			}
			armyElementIconsByDesignation.put(unitDesignation, imagesByViewDirection);
		}
	}
	
	public void addCursorIcon(String cursorName, BufferedImage img) {
		if( ! cursorIcons.containsKey(cursorName) ) {
			cursorIcons.put(cursorName, img);
		}
	}
	
	
	public void addArmyElementPortrait( String unitDesignation, File portrait ) {
		
		if( !armyElementPortraits.containsKey(unitDesignation) ) {
			BufferedImage portraitImg = null;
			if (portrait.exists()) {
				try {
					portraitImg = ImageIO.read( portrait );
					
				} catch (IOException e) {
				}
			} else {
				/*try {
					img = ImageIO.read( new File("resources/images/defaultElement.gif") );
				} catch (IOException e) {
					
				}*/
			}
			
			
			int portraitWidth = 300;
			//int portraitHeight = 200;
			double scaleFactor =  (double)portraitWidth / (double)portraitImg.getWidth(null);
			//System.out.println( "scakeFactoer:"+scaleFactor+"/"+portraitImg.getWidth(null) );
			AffineTransform at = new AffineTransform();	
			at.scale(scaleFactor, scaleFactor);
			AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			
			armyElementPortraits.put(unitDesignation, op.filter(portraitImg, null));
		}
	}

	
	public BufferedImage getArmyElementIcon( ArmyElement el ) {
		String unitDesignation = el.unitDesignation; 
		ViewDirection direction = el.elementState.viewDirection;
		int offset=0;
		if( direction.equals( ViewDirection.TOP ) ) {
			offset = 0;
		} else if ( direction.equals( ViewDirection.TOP_RIGHT ) ) {
			offset = 1;
		} else if ( direction.equals( ViewDirection.BOTTOM_RIGHT ) ) {
			offset = 2;
		} else if ( direction.equals( ViewDirection.BOTTOM ) ) {
			offset = 3;
		} else if ( direction.equals( ViewDirection.BOTTOM_LEFT ) ) {
			offset = 4;
		} else if ( direction.equals( ViewDirection.TOP_LEFT ) ) {
			offset = 5;
		}
		
		if( armyElementIconsByDesignation.containsKey(unitDesignation) ) {
			return armyElementIconsByDesignation.get(unitDesignation).get(offset);
		} else {
			System.err.println( "MainImageController.getArmyElementIcon: unkown unitDesignation:"+unitDesignation );
		}
		return null;
		
	}
	
	public BufferedImage getArmyElementPortrait( ArmyElement el ) {
		if( armyElementPortraits.containsKey(el.unitDesignation) ) {
			return armyElementPortraits.get(el.unitDesignation);
		} else {
			System.err.println( "MainImageController.getArmyElementPortrait: unkown unitDesignation:"+el.unitDesignation );
		}
		return null;

	}
	
	public BufferedImage getCursorIcon(String cursorName) {
		if( cursorIcons.containsKey(cursorName) ) {
			return cursorIcons.get(cursorName);
		} else {
			System.err.println( "MainImageController.getCursorIcon: unkown CursorName:"+cursorName );
		}
		return null;
	}

	
	public static BufferedImage deepCopy(BufferedImage bi) {
	    ColorModel cm = bi.getColorModel();
	    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	    WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
	    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}


	
}
