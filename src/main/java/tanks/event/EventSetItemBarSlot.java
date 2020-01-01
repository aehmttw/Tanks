package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;

public class EventSetItemBarSlot extends PersonalEvent
{
    public int slot;

    public EventSetItemBarSlot()
    {

    }

    public EventSetItemBarSlot(int slot)
    {
        this.slot = slot;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.slot);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.slot = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            for (int i = 0; i < Game.players.size(); i++)
            {
                Player p = Game.players.get(i);
                if (p.clientID.equals(this.clientID))
                {
                    if (p.crusadeItemBar != null)
                        p.crusadeItemBar.selected = this.slot;
                }
            }
        }
    }
}
