package tanks.hotbar;

public class ItemEmpty extends Item
{
	public ItemEmpty()
	{
		this.name = "";
		this.stackSize = 0;
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
