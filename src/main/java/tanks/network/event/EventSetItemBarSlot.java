package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.tank.TankPlayerRemote;

public class EventSetItemBarSlot extends PersonalEvent implements IStackableEvent
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
                    if (p.hotbar.itemBar != null)
                        p.hotbar.itemBar.selected = this.slot;

                    if (p.tank instanceof TankPlayerRemote)
                        ((TankPlayerRemote) p.tank).refreshAmmo();
                }
            }
        }
        else
        {
            if (Game.player != null && Game.player.hotbar != null && Game.player.hotbar.enabledItemBar)
                Game.player.hotbar.itemBar.selected = this.slot;
        }
    }

    @Override
    public int getIdentifier()
    {
        return slot;
    }
}
