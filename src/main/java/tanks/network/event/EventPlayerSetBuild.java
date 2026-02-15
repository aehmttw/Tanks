package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.Team;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.ServerHandler;
import tanks.tank.TankPlayable;
import tanks.tank.TankPlayer;
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

                            for (ServerHandler h : ScreenPartyHost.server.connections)
                            {
                                if (h.player == p)
                                {
                                    h.queueEvent(new EventUpdateCoins(p));
                                    h.queueEvent(new EventPurchaseBuild(b.name));
                                }
                            }
                        }

                        if (success)
                        {
                            p.buildName = b.name;

                            if (p.tank instanceof TankPlayerRemote && p.clientID.equals(this.clientID))
                                ((TankPlayerRemote) p.tank).buildName = s.builds.get(build).name;

                            for (ServerHandler h : ScreenPartyHost.server.connections)
                            {
                                if (h.player == p)
                                    h.queueEvent(new EventPlayerSetBuild(this.build));
                                else if (h.player != null && h.player.tank != null && Team.isAllied(h.player.tank, p.tank))
                                    h.queueEvent(new EventPlayerRevealBuild(h.player.tank.networkID, build));
                            }

                            if (Team.isAllied(Game.playerTank, p.tank))
                            {
                                TankPlayer.ShopTankBuild stb = ((ScreenGame) Game.screen).builds.get(build);
                                stb.clonePropertiesTo((TankPlayable) p.tank);
                                p.tank.health = stb.baseHealth;
                            }
                        }
                    }
                }
            }
            else if (Game.playerTank != null)
            {
                s.builds.get(build).clonePropertiesTo(Game.playerTank);
                Game.playerTank.health = s.builds.get(build).baseHealth;
                Game.player.buildName = s.builds.get(build).name;
            }
        }
    }
}
