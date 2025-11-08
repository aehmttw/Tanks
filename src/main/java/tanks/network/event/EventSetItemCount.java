package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.item.Item;
import tanks.item.ItemEmpty;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventSetItemCount extends PersonalEvent
{
    public String name;
    public UUID playerID;
    public int slot;
    public int count;

    public EventSetItemCount()
    {

    }

    public EventSetItemCount(Player p, int slot, int count)
    {
        this.playerID = p.clientID;
        this.slot = slot;
        this.count = count;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeInt(this.slot);
        b.writeInt(this.count);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.slot = b.readInt();
        this.count = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            Game.player.hotbar.itemBar.slots[slot].stackSize = this.count;
        }
    }
}
