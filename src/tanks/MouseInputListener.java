package tanks;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseInputListener implements MouseListener
{
	public static boolean lClick = false;
	public static boolean rClick = false;
	
	public static boolean lClickValid = false;
	public static boolean rClickValid = false;


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
		else if (e.getButton() == 3) {
			rClick = false;
			rClickValid = false;
		}
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
