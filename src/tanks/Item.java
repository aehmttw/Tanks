package tanks;

public abstract class Item 
{
	public boolean isConsumable;
	public int levelUnlock;
	public int price;
	public int maxStackSize = 100;
	public int stackSize = 1;
	public boolean inUse = false;
	public String name;

	public abstract boolean usable();
	
	public abstract void use();
	
	public static Item parseItem(String s)
	{
		//String[] p = s.split("-");
		/*String name = p[0];
		int price = Integer.parseInt(p[1]);
		int level = Integer.parseInt(p[2]);
		int maxStack = Integer.parseInt(p[3]);
		*/
		Item i = new ItemBullet();
		
		
		
		return i;
	}
}
