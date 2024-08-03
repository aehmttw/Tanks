package tanks.item;

import tanks.Player;
import tanks.tank.Tank;

public class ItemEmpty2 extends Item2
{
	public ItemEmpty2()
	{
		this.name = "";
	}

	@Override
	public ItemStack<?> getStack(Player p)
	{
		return new ItemStackEmpty();
	}

	public static class ItemStackEmpty extends ItemStack<ItemEmpty2>
	{
		public ItemStackEmpty()
		{
			super(null, new ItemEmpty2(), 0);
			this.isEmpty = true;
		}

		@Override
		public boolean usable(Tank t)
		{
			return false;
		}
	}
}
