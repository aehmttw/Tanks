package tanks.event;

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
			for (int i = 0; i < this.message.length(); i++)
			{
				if (" `1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?".indexOf(this.message.charAt(i)) == -1)
					return;
			}

			for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
			{
				ServerHandler s = ScreenPartyHost.server.connections.get(i);

				if (s.clientID != null && s.clientID.equals(this.clientID))
				{
					ScreenPartyHost.chat.add(0, new ChatMessage(s.rawUsername, this.message));
					Game.eventsOut.add(new EventPlayerChat(s.rawUsername, this.message));
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
}
