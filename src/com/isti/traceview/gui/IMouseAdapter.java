package com.isti.traceview.gui;

/**
 * Abstract representation of mouse behavior. Concrete realizations of this interface
 * can be assigned to Slot or GraphPanel to customize its.
 */
import javax.swing.JPanel;

public interface IMouseAdapter {
	public void mouseClickedButton1(int x, int y, int clickCount, int modifiers, JPanel clickedAt);

	public void mouseClickedButton2(int x, int y, int clickCount, int modifiers, JPanel clickedAt);

	public void mouseClickedButton3(int x, int y, int clickCount, int modifiers, JPanel clickedAt);

	public void mouseMoved(int x, int y, int modifiers, JPanel clickedAt);
	
	public void mouseEntered(int x, int y, int modifiers, JPanel clickedAt);
	
	public void mouseExited(int x, int y, int modifiers, JPanel clickedAt);

	public void mouseDragged(int x, int y, int modifiers, JPanel clickedAt);

	public void mouseReleasedButton1(int x, int y, int modifiers, JPanel clickedAt);

	public void mouseReleasedButton3(int x, int y, int modifiers, JPanel clickedAt);
}
