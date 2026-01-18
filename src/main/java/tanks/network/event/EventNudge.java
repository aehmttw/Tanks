package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenOverlayChat;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.NetworkUtils;
import tanks.network.ServerHandler;
import tanks.translation.Translation;

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

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.username);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.username = NetworkUtils.readString(b);
    }

}
