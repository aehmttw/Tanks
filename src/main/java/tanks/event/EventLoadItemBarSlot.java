package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventLoadItemBarSlot extends PersonalEvent
{
    public int slot;
    public UUID targetID;

    public EventLoadItemBarSlot()
    {

    }

    public EventLoadItemBarSlot(UUID clientID, int slot)
    {
        this.slot = slot;
        this.targetID = clientID;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, targetID.toString());
        b.writeInt(this.slot);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.targetID = UUID.fromString(NetworkUtils.readString(b));
        this.slot = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.targetID.equals(Game.clientID))
        {
            Game.player.hotbar.itemBar.selected = slot;
        }
    }
}
