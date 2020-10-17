package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.Player;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemEmpty;
import tanks.hotbar.item.ItemRemote;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventSetItem extends PersonalEvent
{
    public String name;
    public UUID playerID;
    public int slot;
    public String texture;
    public int count;
    public int bounces = -1;

    public EventSetItem()
    {

    }

    public EventSetItem(Player p, int slot, Item item)
    {
        this.playerID = p.clientID;
        this.slot = slot;

        if (item.icon == null)
            this.texture = "";
        else
            this.texture = item.icon;

        this.count = item.stackSize;
        this.name = item.name;

        if (item instanceof ItemBullet)
        {
            bounces = ((ItemBullet) item).bounces;

            if (((ItemBullet) item).className.equals("electric"))
                bounces = 0;
        }

    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeInt(this.slot);
        NetworkUtils.writeString(b, this.texture);
        b.writeInt(this.count);
        NetworkUtils.writeString(b, this.name);
        b.writeInt(this.bounces);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.slot = b.readInt();
        this.texture = NetworkUtils.readString(b);
        this.count = b.readInt();
        this.name = NetworkUtils.readString(b);
        this.bounces = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Item i = new ItemRemote();
            i.stackSize = this.count;
            i.icon = this.texture;
            i.name = this.name;
            ((ItemRemote) i).bounces = this.bounces;

            if (i.stackSize == 0)
                i = new ItemEmpty();

            Game.player.hotbar.itemBar.slots[slot] = i;
        }
    }
}
