package tanks.network.event;

import tanks.Game;

public class EventPurchaseBuild extends PersonalEvent
{
    public String name;

    public EventPurchaseBuild()
    {

    }

    public EventPurchaseBuild(String name)
    {
        this.name = name;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Game.player.ownedBuilds.add(name);
        }
    }
}
