package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Player;
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
	
	public EventPlayerChat(Player p, String m)
	{
		this.message = m;
		this.username = "\u00A7" + p.color.red + "," + p.color.green + "," + p.color.blue + "," +
				p.color2.red + "," + p.color.green + "," + p.color.blue + "," +
				p.color3.red + "," + p.color3.green + "," + p.color3.blue + "|" + p.username;
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
