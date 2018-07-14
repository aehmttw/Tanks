package tanks;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ScrollInputListener implements MouseWheelListener
{
	static boolean validScrollUp = false;
	static boolean validScrollDown = false;

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getPreciseWheelRotation() > 0)
			validScrollDown = true;
		else if (e.getPreciseWheelRotation() < 0)
			validScrollUp = true;
	}

}
