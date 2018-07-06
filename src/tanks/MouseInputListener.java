package tanks;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseInputListener implements MouseListener
{
	static boolean lClick = false;
	static boolean rClick = false;

	static boolean lClickValid = false;


	@Override
	public void mouseClicked(MouseEvent e) 
	{
		
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		if (e.getButton() == 1)
		{
			lClick = true;
			lClickValid = true;
		}
		else if (e.getButton() == 3)
			rClick = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		if (e.getButton() == 1)
		{
			lClick = false;
			lClickValid = false;
		}
		else if (e.getButton() == 3)
			rClick = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) 
	{
		
	}

	@Override
	public void mouseExited(MouseEvent e) 
	{
		
	}

}
