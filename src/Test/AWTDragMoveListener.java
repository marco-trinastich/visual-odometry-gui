package Test;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class AWTDragMoveListener implements AWTEventListener {


    private Point startPt;
    private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final JViewport vport;
    private final JComponent comp;
    private Point move  = new Point();
    private Point ptZero, it;
    private final Rectangle rect = new Rectangle();
    private Rectangle vr;
    private int n, w, h;
    private MouseEvent event;
    private boolean rightButtonPressed = false;
 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//Usage
//		long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
//		Toolkit.getDefaultToolkit()
//			.addAWTEventListener( new AWTDragMoveListener(new JViewport(), new JPanel(), eventMask);

		
	}

    
    //"JComponent comp" is in my case the JDesktopPane object
    public AWTDragMoveListener(JViewport vport, JComponent comp) {
        this.comp = comp;
        this.vport = vport;
    }
         
    public void eventDispatched(AWTEvent e) {
        event = (MouseEvent) e;
 
        //catching press of button no. 3 (right button)
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            if (event.getButton() == 3) {
 
                //getting mouse location, when mouse button is pressed
                startPt = event.getLocationOnScreen();
                n = comp.getComponentCount();
 
                //changing mouse cursor on every showing component of my JDesktopPane
                for (int i = 0; i < n; i++) {
                    comp.getComponent(i).setCursor(handCursor);
                }
 
                //changing mouse cursor on my JDesktopPane
                comp.setCursor(handCursor);
                rightButtonPressed = true;
            }
        }
 
        //catching release of button no. 3 (right button)
        //and changing mouse cursor back on all components
        if (event.getID() == MouseEvent.MOUSE_RELEASED) {
            if (event.getButton() == 3) {
                for (int i = 0; i < n; i++) {
                    comp.getComponent(i).setCursor(defaultCursor);
                }
                comp.setCursor(defaultCursor);
                rightButtonPressed = false;
            }
        }
 
        //catching mouse move when the right mouse button is pressed
        if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
            if (rightButtonPressed) {
                it = event.getLocationOnScreen();
 
                //calculation of move
                move.setLocation(it.x-startPt.x, it.y-startPt.y);
                startPt.setLocation(it);
                vr = vport.getViewRect();
                w = vr.width;
                h = vr.height;
 
                //getting zero point in my JDesktopPane coordinates
                ptZero = SwingUtilities.convertPoint(vport, 0, 0, comp);
 
                //setting new rectangle to view
                rect.setRect(ptZero.x-move.x, ptZero.y-move.y, w, h);
 
                //viewing of new rectangle
                comp.scrollRectToVisible(rect);
            }
        }
    }
}

