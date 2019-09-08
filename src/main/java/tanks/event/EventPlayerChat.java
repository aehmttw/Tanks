package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

public class EventPlayerChat extends PersonalEvent
{
	public String message;
	public String username;
	
	public EventPlayerChat()
	{
		
	}
	
	public EventPlayerChat(String p, String m)
	{
		this.message = m;
		this.username = p;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
			ScreenPartyLobby.chat.add(0, new ChatMessage(this.username, this.message));
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.username);
		NetworkUtils.writeString(b, this.message);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.username = NetworkUtils.readString(b);
		this.message = NetworkUtils.readString(b);
	}

}
