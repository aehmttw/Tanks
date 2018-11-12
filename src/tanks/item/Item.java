package tanks.item;

import tanks.Bullet;

public abstract class Item  {
	public boolean isConsumable;
	public int levelUnlock;
	public int price;
	public int maxStackSize = 100;
	public boolean inUse = false;
	public String name;

	public boolean usable() {
		return false; // For things like ammo
	}
	
	public void use() {
		// NOOP
	}
	
	public boolean isSingleStack() {
		return false;
	}
	
	public static Item parseItem(String s) throws Exception
	{
		//String[] p = s.split("-");
		/*String name = p[0];
		int price = Integer.parseInt(p[1]);
		int level = Integer.parseInt(p[2]);
		int maxStack = Integer.parseInt(p[3]);
		*/
		Item i = new ItemBullet(Bullet.class);
		
		
		
		return i;
	}
}
