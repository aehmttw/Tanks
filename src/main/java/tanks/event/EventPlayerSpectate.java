package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenPartyHost;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

public class EventPlayerSpectate extends PersonalEvent
{
    public EventPlayerSpectate()
    {

    }

    @Override
    public void execute()
    {
        if (ScreenPartyHost.includedPlayers.remove(this.clientID))
        {
            for (Player p : Game.players)
                if (p.clientID.equals(this.clientID))
                {
                    Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
                    break;
                }
        }

        for (Tank t : Tank.idMap.values())
        {
            if (t instanceof TankPlayer && ((TankPlayer) t).player.clientID == this.clientID ||
                    t instanceof TankPlayerRemote && ((TankPlayerRemote) t).player.clientID == this.clientID)
            {
                EventRemoveTank e = new EventRemoveTank(t);
                e.execute();
                Game.eventsOut.add(e);
                break;
            }
        }
    }

    @Override
    public void write(ByteBuf b)
    {

    }

    @Override
    public void read(ByteBuf b)
    {

    }

}