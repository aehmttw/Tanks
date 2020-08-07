package tanks.hotbar.item;

import tanks.hotbar.item.property.ItemProperty;

import java.util.ArrayList;

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
	public boolean usable()
	{
		return false;
	}

	@Override
	public void use()
	{
		
	}
}
