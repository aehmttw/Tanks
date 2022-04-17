package tanks.hotbar.item;

import tanks.tank.Tank;

public class ItemEmpty extends Item
{
	public ItemEmpty()
	{
		this.name = "";
		this.stackSize = 0;
	}

	@Override
	public void fromString(String s)
	{

	}

	@Override
	public String getTypeName()
	{
		return "Nothing";
	}

	@Override
	public boolean usable(Tank t)
	{
		return false;
	}

	@Override
	public void use(Tank t)
	{
		
	}
}
