package tanks.network.event;

import tanks.*;
import tanks.item.*;

import java.util.UUID;

public class EventSetItem extends PersonalEvent implements IStackableEvent
{
    public String name;
    public UUID playerID;
    public int slot;

    public String itemStackString;

    public EventSetItem()
    {

    }

    public EventSetItem(Player p, int slot, Item.ItemStack<?> item)
    {
        this.playerID = p.clientID;
        this.slot = slot;

        this.itemStackString = item.toString();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Item.ItemStack<?> s = Item.ItemStack.fromString(Game.player, this.itemStackString);

            if (s.stackSize < 0)
                s = new ItemEmpty.ItemStackEmpty();

            Game.player.hotbar.itemBar.slots[slot] = s;
        }
    }

    @Override
    public int getIdentifier()
    {
        return IStackableEvent.f(playerID.hashCode()) + slot;
    }
}
