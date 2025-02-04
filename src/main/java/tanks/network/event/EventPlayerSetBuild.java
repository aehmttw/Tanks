package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
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
        if (this.clientID != null && Game.screen instanceof ScreenGame && Game.currentLevel != null && build > 0 && build < Game.currentLevel.playerBuilds.size())
        {
            ScreenGame s = (ScreenGame) Game.screen;
            for (int i = 0; i < Game.players.size(); i++)
            {
                if (Game.players.get(i).clientID.equals(this.clientID))
                {
                    Player p = Game.players.get(i);
                    p.buildName = Game.currentLevel.playerBuilds.get(build).name;

                    for (Movable m: Game.movables)
                    {
                        if (m instanceof TankPlayerRemote && ((TankPlayerRemote) m).player.clientID.equals(this.clientID))
                            ((TankPlayerRemote) m).buildName = Game.currentLevel.playerBuilds.get(build).name;
                    }
                }
            }
        }
    }
}
