package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Player;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventMutePlayer extends PersonalEvent
{
	public boolean muted;

	public EventMutePlayer()
	{

	}

	public EventMutePlayer(boolean muted)
	{
		this.muted = muted;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
        {
            if (muted)
                ScreenPartyLobby.chat.add(0, new ChatMessage("\u00A7255000000255The party host has disabled your ability to chat!"));
            else
                ScreenPartyLobby.chat.add(0, new ChatMessage("\u00A7000200000255The party host has enabled your ability to chat!"));

            ScreenPartyLobby.muted = muted;
        }
	}

}
