package tanks;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseInputListener implements MouseListener, MouseMotionListener
{
	public static boolean lClick = false;
	public static boolean rClick = false;
	public static boolean mClick = false;
	public static boolean b4Click = false;
	public static boolean b5Click = false;

	public static boolean lClickValid = false;
	public static boolean rClickValid = false;
	public static boolean mClickValid = false;
	public static boolean b4ClickValid = false;
	public static boolean b5ClickValid = false;

	public static long clickTime = 0;

	@Override
	public void mouseClicked(MouseEvent e) 
	{

	}

	@Override
	public void mousePressed(MouseEvent e) 
	{				
		if (e.getButton() == 1)
		{
			clickTime = System.currentTimeMillis();

			lClick = true;
			lClickValid = true;
		}
		else if (e.getButton() == 3)
		{
			rClick = true;
			rClickValid = true;
		}
		else if (e.getButton() == 2)
		{
			mClick = true;
			mClickValid = true;
		}
		else if (e.getButton() == 4)
		{
			b4Click = true;
			b4ClickValid = true;
		}
		else if (e.getButton() == 5)
		{
			b5Click = true;
			b5ClickValid = true;
		}
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
		{
			rClick = false;
			rClickValid = false;
		}
		else if (e.getButton() == 2)
		{
			mClick = false;
			mClickValid = false;
		}
		else if (e.getButton() == 4)
		{
			b4Click = false;
			b4ClickValid = false;
		}
		else if (e.getButton() == 5)
		{
			b5Click = false;
			b5ClickValid = false;
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

	@Override
	public void mouseDragged(MouseEvent e) 
	{		
		if (System.currentTimeMillis() - clickTime > 100)
		{
			lClickValid = false;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		if (System.currentTimeMillis() - clickTime > 100)
		{
			lClickValid = false;
		}
	}
}
