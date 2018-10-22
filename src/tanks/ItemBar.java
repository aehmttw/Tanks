package tanks;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ItemBar 
{
	public Item[] slots = new Item[5];
	
	public ItemBar()
	{
		slots[0] = new ItemBullet();
		slots[0].stackSize = 3;
	}
	
	public void update()
	{
		if (InputKeyboard.validKeys.contains(KeyEvent.VK_1) && slots[0].usable())
			slots[0].use();
	}
	
	public void draw(Graphics g)
	{
		
	}
}
