package tanks.network.event;

import tanks.*;
import tanks.tank.*;

public class EventArcadeClearMovables extends PersonalEvent
{
    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof Crate && ((((Crate) m).tank instanceof TankRemote && ((TankRemote) ((Crate) m).tank).name.equals("player")) || ((Crate) m).tank instanceof TankPlayerController))
                    continue;

                m.destroy = true;
            }
        }
    }
}
