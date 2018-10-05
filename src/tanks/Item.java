package tanks;

public abstract class Item 
{
	public boolean isConsumable;
	public int levelUnlock;
	public int price;
	public int maxStackSize;
	public int cooldown = 0;

	public abstract boolean usable();
	
	public abstract void use();
}
