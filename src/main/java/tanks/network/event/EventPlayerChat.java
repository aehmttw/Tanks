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
		this.username = "\u00A7" + (int) p.color.red + "," + (int) p.color.green + "," + (int) p.color.blue + "," +
            (int) p.color2.red + "," + (int) p.color2.green + "," + (int) p.color2.blue + "," +
            (int) p.color3.red + "," + (int) p.color3.green + "," + (int) p.color3.blue + "|" + p.username;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
			ScreenPartyLobby.chat.add(0, new ChatMessage(this.username, this.message));
	}
}
