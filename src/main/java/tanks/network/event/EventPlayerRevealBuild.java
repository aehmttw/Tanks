package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;
import tanks.tank.TankPlayable;
import tanks.tank.TankPlayerRemote;
import tanks.tank.TankRemote;

import java.util.UUID;

public class EventPlayerRevealBuild extends PersonalEvent
{
    public int tank;
    public int build;

    public EventPlayerRevealBuild()
    {

    }

    public EventPlayerRevealBuild(int tank, int build)
    {
        this.tank = tank;
        this.build = build;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(tank);
        b.writeInt(build);
    }

    @Override
    public void read(ByteBuf b)
    {
        tank = b.readInt();
        build = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && build >= 0 && build < Game.currentLevel.playerBuilds.size())
        {
            for (Movable m: Game.movables)
            {
                if (m instanceof TankRemote && ((TankRemote) m).tank instanceof TankPlayable && ((Tank) m).networkID == tank)
                {
                    Game.currentLevel.playerBuilds.get(build).clonePropertiesTo((TankPlayable) ((TankRemote) m).tank);
                    ((TankRemote) m).copyTank(((TankRemote) m).tank);
                    ((TankRemote) m).health = ((TankRemote) m).baseHealth;
                }
            }
        }
    }
}
