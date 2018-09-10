package demonworld.model;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.io.Serializable;


/**
 * defines a position on the board
 * @author kbailly
 *
 */
public class Position implements Serializable{

	private static final long serialVersionUID = -2446186886872581743L;
	public int x;
	public int y;
	public int z;
	
	public Position(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Position getCopy() {
		return new Position(x,y,z);
	}
	
	public Position getOffsetTo( Position otherPos ){
		return new Position( 
				(x - otherPos.x),
				(y - otherPos.y), 
				(z - otherPos.z) );
	}
	
	public String toString() {
		return "("+x+","+y+","+z+")";
	}
	
	public double getDistanceTo(Position pos1) {
        return getDistanceBetween(this, pos1);
    }
	
	public static double getDistanceBetween(Position pos0, Position pos1) {
		//System.out.println( pos0+" vs "+pos1 );
        double absX = abs(pos0.x - pos1.x);
        double absY = abs(pos0.y - pos1.y);
        double absZ = abs(pos0.z - pos1.z);
        return (int) max(max(absX, absY), absZ);
        
    }
	
	/** 
	 * get Neighbour positions relative to viewdirection in the following order:
	 * top_left(v), top_right(v), right(v), bottom_right(v), bottom_left(v), left(v) 
	 * @param v
	 * @return
	 */
	public Position[] getNeighbourPositionsByDirection( ViewDirection v ) {
		Position[] absN = getNeighbourPositions();
		
		switch (v) {
		case TOP:
			return absN;
		case TOP_RIGHT:
			return new Position[] { absN[1], absN[2], absN[3], absN[4], absN[5], absN[0] };
		case BOTTOM_RIGHT:
			return new Position[] { absN[2], absN[3], absN[4], absN[5], absN[0], absN[1] };
		case BOTTOM:
			return new Position[] { absN[3], absN[4], absN[5], absN[0], absN[1], absN[2] };
		case BOTTOM_LEFT:
			return new Position[] { absN[4], absN[5], absN[0], absN[1], absN[2], absN[3] };
		case TOP_LEFT:	
			return new Position[] { absN[5], absN[0], absN[1], absN[2], absN[3], absN[4] };
		default:
			return null;
		}
			
	}
	
	/** get Neighbour positions ordered to absolute orientation on grid
	 * top_left(center), top_right(center), right(center), bottom_right(center), bottom_left(center), left(center)
	 * @return
	 */
	public Position[] getNeighbourPositions() {
		Position[] result = new Position[6];
		result[0] = new Position( this.x - 1, this.y +1, this.z ); // top_left (absolute)
		result[1] = new Position( this.x - 1, this.y, this.z+1 ); // top_right (absolute)
		result[2] = new Position( this.x, this.y-1, this.z+1 ); // right (absolute)
		result[3] = new Position( this.x + 1, this.y-1, this.z ); // bottom_right (absolute)
		result[4] = new Position( this.x + 1, this.y, this.z-1 ); // bottom_left (absolute)
		result[5] = new Position( this.x, this.y+1, this.z-1 ); // left (absolute)
		return result;
	}

	public static int calcYFromXZ(int x, int z) {
		
		return -1 * (x+z);
	}
	
	@Override
	public boolean equals( Object o ) {
		if( o instanceof Position ) {
			if( this.getDistanceTo((Position)o) == 0 ) return true;
		}
		return false;
	}
}
