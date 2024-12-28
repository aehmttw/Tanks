package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.network.NetworkUtils;
import tanks.tank.TankPlayable;

import java.util.UUID;

public class EventUpdateTankAbility extends PersonalEvent
{
    public UUID playerID;
    public int slot;
    public int count;

    public EventUpdateTankAbility()
    {

    }

    public EventUpdateTankAbility(Player p, int slot)
    {
        this.playerID = p.clientID;
        this.slot = slot;

        this.count = ((TankPlayable)(p.tank)).abilities.get(slot).stackSize;
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
            int c = Game.playerTank.abilities.get(slot).stackSize;
            Game.playerTank.abilities.get(slot).stackSize = this.count;
            if (c > 0 && this.count <= 0)
            {
                Game.playerTank.abilities.get(slot).destroy = true;
                if (Game.player.hotbar.itemBar != null)
                    Game.player.hotbar.itemBar.setItem(slot);
            }
        }
    }
}
