package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.Player;
import tanks.hotbar.Item;
import tanks.hotbar.ItemEmpty;
import tanks.hotbar.ItemRemote;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventSetItem extends PersonalEvent
{
    public UUID playerID;
    public int slot;
    public String texture;
    public int count;

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
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeInt(this.slot);
        NetworkUtils.writeString(b, this.texture);
        b.writeInt(this.count);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.slot = b.readInt();
        this.texture = NetworkUtils.readString(b);
        this.count = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Item i = new ItemRemote();
            i.stackSize = this.count;
            i.icon = this.texture;

            if (i.stackSize == 0)
                i = new ItemEmpty();

            Panel.panel.hotbar.currentItemBar.slots[slot] = i;
        }
    }
}
