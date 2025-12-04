package tanks.network.event;

import tanks.Game;
import tanks.tank.Mine;

public class EventMineRemove extends PersonalEvent
{
    public int mine;

    public EventMineRemove()
    {

    }

    public EventMineRemove(Mine m)
    {
        this.mine = m.networkID;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        Mine m = Mine.idMap.get(mine);

        if (m == null)
            return;

        Game.removeMovables.add(m);
        Mine.idMap.remove(m.networkID);
    }
}
