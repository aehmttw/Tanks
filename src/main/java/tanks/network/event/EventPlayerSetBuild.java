package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.Server;
import tanks.network.ServerHandler;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerController;
import tanks.tank.TankPlayerRemote;

public class EventPlayerSetBuild extends PersonalEvent
{
    public int build;

    public EventPlayerSetBuild()
    {

    }

    public EventPlayerSetBuild(int build)
    {
        this.build = build;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(build);
    }

    @Override
    public void read(ByteBuf b)
    {
        build = b.readInt();
    }

    @Override
    public void execute()
    {
        if (Game.screen instanceof ScreenGame && Game.currentLevel != null && build >= 0 && build < ((ScreenGame) (Game.screen)).builds.size())
        {
            ScreenGame s = (ScreenGame) Game.screen;
            if (s.playing)
                return;

            if (this.clientID != null)
            {
                for (int i = 0; i < Game.players.size(); i++)
                {
                    if (Game.players.get(i).clientID.equals(this.clientID))
                    {
                        Player p = Game.players.get(i);
                        TankPlayer.ShopTankBuild b = s.builds.get(build);

                        boolean success = false;
                        if (p.ownedBuilds.contains(b.name))
                            success = true;
                        else if (p.hotbar.coins >= b.price)
                        {
                            p.ownedBuilds.add(b.name);
                            p.hotbar.coins -= b.price;
                            success = true;
                            Game.eventsOut.add(new EventUpdateCoins(p));

                            for (ServerHandler h : ScreenPartyHost.server.connections)
                            {
                                if (h.player == p)
                                    h.queueEvent(new EventPurchaseBuild(b.name));
                            }
                        }

                        if (success)
                        {
                            p.buildName = b.name;

                            for (Movable m : Game.movables)
                            {
                                if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player.clientID.equals(this.clientID))
                                    ((TankPlayerRemote) m).buildName = s.builds.get(build).name;
                            }

                            for (ServerHandler h : ScreenPartyHost.server.connections)
                            {
                                if (h.player == p)
                                    h.queueEvent(new EventPlayerSetBuild(this.build));
                            }
                        }
                    }
                }
            }
            else if (Game.playerTank != null)
            {
                s.builds.get(build).clonePropertiesTo(Game.playerTank);
                Game.player.buildName = s.builds.get(build).name;
            }
        }
    }
}
