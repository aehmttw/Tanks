package tanks;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class InputKeyboard implements KeyListener
{
	public static ArrayList<Integer> keys = new ArrayList<Integer>();
	public static ArrayList<Integer> validKeys = new ArrayList<Integer>();
	//public static ArrayList<Character> validChars = new ArrayList<Character>();

	@Override
	public void keyTyped(KeyEvent e) 
	{
		
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (!keys.contains(e.getKeyCode()))
		{
			keys.add(e.getKeyCode());
			validKeys.add(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		if (keys.contains(e.getKeyCode()))
		{
			keys.remove((Integer)e.getKeyCode());
			validKeys.remove((Integer)e.getKeyCode());
		}
	}

}
