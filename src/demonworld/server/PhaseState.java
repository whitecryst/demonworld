package demonworld.server;

import java.awt.Image;
import java.io.Serializable;

public class PhaseState implements Serializable{
	private int cursorOffset = -1; // defines the offset of cusor in cursorlist of current controller
	private Image cursorImage;
	
	public PhaseState( int cursorOffset, Image cusorImage ) {
		this.cursorImage = cusorImage;
		this.cursorOffset = cursorOffset;
	}

	public int getCursorOffset() {
		return cursorOffset;
	}

	public void setCursorOffset(int cursorOffset) {
		this.cursorOffset = cursorOffset;
	}

	public Image getCursorImage() {
		return cursorImage;
	}

	public void setCursorImage(Image cursorImage) {
		this.cursorImage = cursorImage;
	}
	
	
}
