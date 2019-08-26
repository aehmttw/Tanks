package tanks.event;

import tanks.ChatMessage;
import tanks.ScreenPartyLobby;

public class EventPlayerChat extends PersonalEvent
{
	public String message;
	public String username;
	
	public EventPlayerChat(String s)
	{
		this.username = s.substring(0, s.indexOf("|"));
		this.message = s.substring(1 + s.indexOf("|"));
	}
	
	public EventPlayerChat(String p, String m)
	{
		this.message = m;
		this.username = p;
	}

	@Override
	public String getNetworkString()
	{
		return this.username + "|" + this.message;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
			ScreenPartyLobby.chat.add(0, new ChatMessage(this.username, this.message));
	}

}
