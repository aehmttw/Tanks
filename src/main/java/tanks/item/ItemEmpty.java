package tanks.item;

import tanks.Player;
import tanks.tank.Tank;

public class ItemEmpty extends Item
{
	public ItemEmpty()
	{
		this.name = "";
		this.icon = null;
	}

	@Override
	public ItemStack<?> getStack(Player p)
	{
		return new ItemStackEmpty();
	}

	public static class ItemStackEmpty extends ItemStack<ItemEmpty>
	{
		public ItemStackEmpty()
		{
			super(null, new ItemEmpty(), 0);
			this.isEmpty = true;
		}

		@Override
		public boolean usable(Tank t)
		{
			return false;
		}
	}
}
