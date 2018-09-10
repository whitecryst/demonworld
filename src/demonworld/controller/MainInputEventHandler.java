package demonworld.controller;


/**
 * inputEventHandler works as a middleware between Hardware and GameController.
 * Its goal is, to take all Events from user input and send it 1. to the local gameclient controller, 2. to the server
 * to synch the events with other player. 
 * To achieve this, this class takes all events and sends it to all registered subHandlers
 */
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainInputEventHandler implements InputHandlerInterface{
	private HashMap<InputHandlerInterface, Boolean> isHandlerActive = new HashMap<InputHandlerInterface, Boolean>();
	private List<InputHandlerInterface> subHandler = new ArrayList<InputHandlerInterface>();
	
	public void addSubHandler( InputHandlerInterface newSubHandler, boolean isActive ) {
		//System.out.println( "addSubHandler" );
		subHandler.add(newSubHandler);
		isHandlerActive.put(newSubHandler, new Boolean(isActive));
	}
	
	public void setHandlerActivation( InputHandlerInterface newSubHandler, boolean isActive ) {
		//System.out.println( "addSubHandler" );
		isHandlerActive.put(newSubHandler, new Boolean(isActive));
	}
	
	public void deactivateAllHandler() {
		for( InputHandlerInterface i : isHandlerActive.keySet() ) {
			isHandlerActive.put(i, new Boolean(false));
		}
	}
	
	public void removeSubHandler( InputHandlerInterface delSubHandler ) {
		subHandler.remove(delSubHandler);
		isHandlerActive.remove(delSubHandler);
	}
	
	public void removeAllSubHandler(  ) {
		subHandler.clear();
		isHandlerActive.clear();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//System.out.println( "MainEventHandler mouseclick, subHandlerCount:"+subHandler.size() );
		for( InputHandlerInterface handler : subHandler ) {
			//System.out.println("mouseclick to subHandler"+handler.getClass());
			if( isHandlerActive.get(handler) ) handler.mouseClicked(e);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mouseReleased(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mouseEntered(e);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mouseExited(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mouseDragged(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mouseMoved(e);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.keyTyped(e);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.keyReleased(e);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for( InputHandlerInterface handler : subHandler ) {
			if( isHandlerActive.get(handler) ) handler.mouseWheelMoved(e);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//System.out.println(e);
		for( InputHandlerInterface handler : subHandler ) {
			//System.out.println( "MainInputEventHandler, push ActionPerformed to"+handler+e );
			//System.out.println( "isActive:"+isHandlerActive );
			if( isHandlerActive.get(handler) ) handler.actionPerformed(e);
		}
	}
	
	public void addListenerToComponent( Component c, boolean requestFocus ) {
		c.addMouseListener( this );
		c.addMouseMotionListener(this);
		c.addMouseWheelListener( this );
		c.addKeyListener(this);
		// request focus to enable the keyListener
		if( requestFocus ) { c.requestFocus();}
	}
		
	public void removeListenerFromComponent(Component c) {
		c.removeMouseListener(this);
		c.removeMouseMotionListener(this);
		c.removeMouseWheelListener(this);
		c.removeKeyListener(this);
	}
	

	
	
	public static void publishEvent(InputHandlerInterface targetHandler, InputEvent e) {
		//System.out.println( "MainInputEventHandler publish"+targetHandler+e.toString() );
		
		if( e instanceof KeyEvent ) {
			switch( ((KeyEvent)e).getID() ) {
				case KeyEvent.KEY_TYPED:
					targetHandler.keyTyped((KeyEvent)e);
				break;
				case KeyEvent.KEY_PRESSED:
					targetHandler.keyPressed((KeyEvent)e);
				break;
				case KeyEvent.KEY_RELEASED:
					targetHandler.keyReleased((KeyEvent)e);
				break;
			}
		} else if( e instanceof MouseWheelEvent ) {
			targetHandler.mouseWheelMoved( (MouseWheelEvent) e);
		} else if( e instanceof MouseEvent) {
			switch( ((MouseEvent)e).getID() ) {
				case MouseEvent.MOUSE_CLICKED:
					targetHandler.mouseClicked((MouseEvent)e);
				break;
				case MouseEvent.MOUSE_PRESSED:
					targetHandler.mousePressed((MouseEvent)e);
				break;
				case MouseEvent.MOUSE_RELEASED:
					targetHandler.mouseReleased((MouseEvent)e);
				break;
				case MouseEvent.MOUSE_ENTERED:
					targetHandler.mouseEntered((MouseEvent)e);
				break;
				case MouseEvent.MOUSE_EXITED:
					targetHandler.mouseExited((MouseEvent)e);
				break;
				case MouseEvent.MOUSE_DRAGGED:
					targetHandler.mouseDragged((MouseEvent)e);
				break;
				case MouseEvent.MOUSE_MOVED:
					targetHandler.mouseMoved((MouseEvent)e);
					
				break;
			}
		} 
	}

	
}
