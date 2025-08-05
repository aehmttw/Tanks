package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;
import tanks.network.ServerHandler;

public class EventChat extends PersonalEvent
{
	public String message;

	public EventChat()
	{
		
	}
	
	public EventChat(String s)
	{
		this.message = s;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
			ScreenPartyLobby.chat.add(0, new ChatMessage(this.message));
		else
		{
			boolean invalid = !isStringValid(this.message);

			for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
			{
				ServerHandler s = ScreenPartyHost.server.connections.get(i);

				if (s.clientID != null && s.clientID.equals(this.clientID))
				{
					if (invalid)
						s.sendEventAndClose(new EventKick("Invalid chat message received!"));
                    else if (ScreenPartyHost.activeScreen.mutedPlayers.contains(this.clientID))
                        s.sendEvent(new EventChat("\u00A7255000000255The party host has disabled your ability to chat!"));
					else
					{
						ScreenPartyHost.chat.add(0, new ChatMessage(s.player, this.message));
						Game.eventsOut.add(new EventPlayerChat(s.player, this.message));
					}
				}
			}
		}

	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.message);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.message = NetworkUtils.readString(b);
	}

	public static boolean isStringValid(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			if (" `1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?".indexOf(s.charAt(i)) == -1)
				return false;
		}

		return true;
	}
}
