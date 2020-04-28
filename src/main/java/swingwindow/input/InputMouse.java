package swingwindow.input;

import swingwindow.SwingWindow;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputMouse implements MouseListener, MouseMotionListener
{
	public SwingWindow window;
	public InputMouse(SwingWindow w)
	{
		this.window = w;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{

	}

	@Override
	public void mousePressed(MouseEvent e)
	{				
		window.pressedButtons.add(e.getButton() - 1);
		window.validPressedButtons.add(e.getButton() - 1);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		window.pressedButtons.remove((Integer)(e.getButton() - 1));
		window.validPressedButtons.remove((Integer)(e.getButton() - 1));
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{

	}

	@Override
	public void mouseExited(MouseEvent e)
	{

	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		this.window.absoluteMouseX = e.getPoint().getX();
		this.window.absoluteMouseY = e.getPoint().getY();
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		this.window.absoluteMouseX = e.getPoint().getX();
		this.window.absoluteMouseY = e.getPoint().getY();
	}
}
