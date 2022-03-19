package tanks.hotbar.item;

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
	public boolean usable()
	{
		return false;
	}

	@Override
	public void use()
	{
		
	}
}
