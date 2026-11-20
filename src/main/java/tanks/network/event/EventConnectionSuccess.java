package tanks.network.event;

import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyLobby;

import io.netty.buffer.ByteBuf;

public class EventConnectionSuccess extends PersonalEvent
{
    public EventConnectionSuccess()
    {

    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            Panel.menuMusicStartTime = -1;
            Game.screen = new ScreenPartyLobby();
            Game.eventsOut.add(new EventSendTankColors(Game.player));
        }
    }
}
