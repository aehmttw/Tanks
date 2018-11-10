package tanks.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import tanks.InputKeyboard;
import tanks.Panel;

public final class ItemBar  {
	private static final int SIZE = 50; // The slot size.
	private static final int GAP = 75; // Gap between slots
	private static final int MARGIN = 100; // Distance from bottom
	
	private static final Color SLOT_BG = new Color(0,0,0,128);
	private static final Color SLOT_BORDER = new Color(255,128,0);
	
	public ItemStack[] slots = new ItemStack[5];
	private int selected = -1;
	
	public ItemBar() {
		slots[0] = new ItemStack(new ItemBullet(), 3);
	}
	
	public void update() {
		checkKey(KeyEvent.VK_1, 0);
		checkKey(KeyEvent.VK_2, 1);
		checkKey(KeyEvent.VK_3, 2);
		checkKey(KeyEvent.VK_4, 3);
		checkKey(KeyEvent.VK_5, 4);
	}
	
	private void checkKey(int key, int index) {
		if (InputKeyboard.validKeys.contains(key)) {
			selected = (selected == index ? -1 : index);
			InputKeyboard.validKeys.remove((Object) key);
		}
	}
	
	public void draw(Graphics g) {
		g.setColor(SLOT_BG);
		
		for (int i = -2; i <= 2; i++) {
			int x = (int) ((i*GAP)+(Panel.windowWidth/2));
			int y = (int) (Panel.windowHeight-MARGIN);
			
			g.fillRect(x, y, SIZE, SIZE);
			
			if (i+2 == selected) {
				g.setColor(SLOT_BORDER);
				g.drawRect(x, y, SIZE, SIZE);
				g.setColor(SLOT_BG); // Unless you want the rest of the slots to be orange...
			}
		}
	}
}
