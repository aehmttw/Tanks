package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.item.Item;
import tanks.item.ItemEmpty;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventSetItem extends PersonalEvent
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
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeInt(this.slot);
        NetworkUtils.writeString(b, this.itemStackString);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.slot = b.readInt();
        this.itemStackString = NetworkUtils.readString(b);
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
}
