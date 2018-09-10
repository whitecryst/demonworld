package demonworld.tools;

import java.awt.geom.Point2D;

public class WinkelApp {
	/**
	 * Annahme: Winkel wird zwischen P1P2 und P1P3 eingeschlossen
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public static double getWinkel(Point2D.Double p1, Point2D.Double p2,
			Point2D.Double p3) {

		// Vektoren P1P2 und P1P3 berechnen

		Point2D.Double vek1 = new Point2D.Double(p2.getX() - p1.getX(),
				p2.getY() - p1.getY());

		Point2D.Double vek2 = new Point2D.Double(p3.getX() - p1.getX(),
				p3.getY() - p1.getY());

		// Laenge der Vektoren berechnen

		double norm1 = Math.sqrt(vek1.getX() * vek1.getX() + vek1.getY()
				* vek1.getY());

		double norm2 = Math.sqrt(vek2.getX() * vek2.getX() + vek2.getY()
				* vek2.getY());

		// Skalarprodukt der Vektoren P1P2 und P1P3 bilden

		// Gibt's dafür in der API Spezifikation eig ne offizielle Methode???

		double skpr = vek1.getX() * vek2.getX() + vek1.getY() * vek2.getY();

		// Da gilt: <v1,v2> = |v1|*|v2|*cos(alpha)

		double alpha = Math.acos(skpr / (norm1 * norm2));

		// alpha liegt im Bogenmass vor, daher folgende Umrechnung:

		double winkel = 180 * alpha / Math.PI;
		//System.out.println("Der Winkel lautet: alpha = " +  winkel);
		return winkel;
	}
	
	public static double get360GradWinkel(Point2D.Double p1, Point2D.Double p2,
			Point2D.Double p3) {

		// Vektoren P1P2 und P1P3 berechnen

		Point2D.Double vek1 = new Point2D.Double(p2.getX() - p1.getX(),
				p2.getY() - p1.getY());

		Point2D.Double vek2 = new Point2D.Double(p3.getX() - p1.getX(),
				p3.getY() - p1.getY());

		double alpha = Math.atan2(vek2.getY(), vek2.getX()) - Math.atan2(vek1.getY(), vek1.getX());

		// alpha liegt im Bogenmass -p, pi vor, daher folgende Umrechnung:

		double winkel = 180 * (alpha / Math.PI);
		//System.out.println("Der Winkel lautet: alpha = " +  winkel);
		return winkel;
	}

}
