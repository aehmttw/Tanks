package tanks.network.event;

import tanks.*;

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
