package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

public final class ItemBar
{
	public static final int size = 50; // The slot size.
	public static final int count_margin_right = 27; // Item number's distance from right.
	public static final int count_margin_bottom = 30; // Item number's distance from bottom.
	public static final int gap = 75; // Gap between slots.
	public static final int bar_margin = 50; // Bar's distance from bottom.

	public static final Color slot_bg = new Color(0, 0, 0, 128); // BG color of slots.
	public static final Color slot_selected = new Color(255, 128, 0); // Border color of selected slot.
	public static final Color item_count = new Color(255, 255, 255); // Text color of item count.

	public Item[] slots = new Item[5];
	public int selected = -1;
		
	public Hotbar hotbar;
	
	public ItemBar(Hotbar h)
	{
		this.hotbar = h;
		for (int i = 0; i < slots.length; i++)
			slots[i] = new ItemEmpty();
	}
	
	public boolean addItem(Item item)
	{
		Item i = Item.parseItem(item.toString());
		int emptyAmount = 0;
		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].name.equals(i.name) || this.slots[x] instanceof ItemEmpty)
				emptyAmount += i.maxStackSize - slots[x].stackSize;
		}
		
		if (emptyAmount < i.stackSize)
			return false;
				
		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].name.equals(i.name) && this.slots[x].stackSize >= this.slots[x].maxStackSize)
			{
				
			}
			else if (this.slots[x].name.equals(i.name))
			{
				if (this.slots[x].stackSize + i.stackSize <= this.slots[x].maxStackSize)
				{
					this.slots[x].stackSize += i.stackSize;
					return true;
				}
				else
				{
					int remaining = this.slots[x].stackSize + i.stackSize - this.slots[x].maxStackSize;
					this.slots[x].stackSize = this.slots[x].maxStackSize;
					i.stackSize = remaining;
					this.addItem(i);
					return true;
				}
			}
			else if (this.slots[x] instanceof ItemEmpty)
			{
				if (i.stackSize <= i.maxStackSize)
				{
					this.slots[x] = i;
					return true;
				}
				else
				{
					int remaining = i.stackSize - i.maxStackSize;
					this.slots[x] = Item.parseItem(i.toString());
					this.slots[x].stackSize = this.slots[x].maxStackSize;					
					i.stackSize = remaining;
					this.addItem(i);
					return true;
				}
			}
		}
		return true;
	}

	public boolean useItem(boolean rightClick)
	{
		if (selected == -1)
			return false;
		
		if (slots[selected] instanceof ItemEmpty)
			return false;
		
		if (slots[selected].rightClick != rightClick)
			return false;
		
		if (slots[selected] instanceof ItemEmpty)
			return false;
		
		slots[selected].attemptUse();
		
		if (slots[selected].destroy)
			slots[selected] = new ItemEmpty();
		
		return true;
	}
	
	public void update()
	{
		checkKey(KeyEvent.VK_1, 0);
		checkKey(KeyEvent.VK_2, 1);
		checkKey(KeyEvent.VK_3, 2);
		checkKey(KeyEvent.VK_4, 3);
		checkKey(KeyEvent.VK_5, 4);
	}

	public void checkKey(int key, int index)
	{
		if (InputKeyboard.validKeys.contains(key))
		{
			this.hotbar.hidden = false;
			this.hotbar.hideTimer = 500;
			selected = (selected == index ? -1 : index);
			InputKeyboard.validKeys.remove((Object) key);
		}
	}

	public void draw(Graphics g)
	{
		g.setColor(slot_bg);

		for (int i = -2; i <= 2; i++)
		{
			int x = (int) ((i * gap) + (Drawing.interfaceSizeX / 2));
			int y = (int) (Drawing.interfaceSizeY - bar_margin + this.hotbar.bottomOffset);

			Drawing.window.fillInterfaceRect(g, x, y, size, size);

			if (i + 2 == selected)
			{
				g.setColor(slot_selected);
				Drawing.window.fillInterfaceRect(g, x, y, size, size);
				g.setColor(slot_bg); // Unless you want the rest of the slots to be orange...
			}
			
			if (slots[i + 2] != null)
			{
				Item item = slots[i + 2];
				if (item.stackSize > 1)
				{
					g.setColor(item_count);
					Drawing.setFontSize(g, 12);
					Drawing.window.drawInterfaceText(g, x + size - count_margin_right, y + size - count_margin_bottom, Integer.toString(item.stackSize), true);
					g.setColor(slot_bg); // You saw nothing...
				}
			}
			
			if (slots[i + 2].icon != null)
				Drawing.window.drawInterfaceImage(g, Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/" + slots[i + 2].icon)), x, y, size, size);
		}
	}
}
