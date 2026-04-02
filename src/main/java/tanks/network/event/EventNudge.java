package tanks.network.event;

import tanks.*;
import tanks.gui.screen.*;
import tanks.network.NetworkUtils;
import tanks.network.ServerHandler;
import tanks.translation.Translation;

import io.netty.buffer.ByteBuf;

public class EventNudge extends PersonalEvent
{
    public String username;

    public EventNudge()
    {

    }

    public EventNudge(Player p)
    {
        this.username = p.username;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).ready)
        {
            Drawing.drawing.playSound("obliterate.ogg");
            ((ScreenGame) Game.screen).nudgeTimer = 50;
            ScreenOverlayChat.addChat(Translation.translate("\u00A7255000000255%s is nudging you to click the ready button!", username));
        }
        else if (this.clientID != null)
        {
            for (ServerHandler serverHandler: ScreenPartyHost.server.connections)
            {
                Player p = serverHandler.player;
                if (p.clientID != null && p.clientID.equals(this.clientID))
                {
                    if (System.currentTimeMillis() - p.lastNudge < ScreenGame.ready_time_to_nudge * 9)
                    {
                        serverHandler.sendEvent(new EventPlaySound("obliterate.ogg", 1, 1));
                        serverHandler.sendEventAndClose(new EventKick("Don't exploit the nudge system!"));
                        return;
                    }
                    p.lastNudge = System.currentTimeMillis();
                    Game.eventsOut.add(new EventNudge(p));

                    if (Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).ready)
                    {
                        Drawing.drawing.playSound("obliterate.ogg");
                        ((ScreenGame) Game.screen).nudgeTimer = 50;
                        ScreenOverlayChat.addChat(Translation.translate("\u00A7255000000255%s is nudging you to click the ready button!", username));
                    }
                }
            }
        }
    }
}
