package tanks.event;

import tanks.Crusade;
import tanks.Game;

public class EventPlayerAutoReady extends EventPlayerReady
{
    @Override
    public void execute()
    {
        if (!Game.currentLevel.shop.isEmpty() || (Crusade.crusadeMode && !Crusade.currentCrusade.getShop().isEmpty()))
            return;

        super.execute();
        Game.eventsOut.add(new EventPlayerAutoReadyConfirm(this.clientID));
    }
}
