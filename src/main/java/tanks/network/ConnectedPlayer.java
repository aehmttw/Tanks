package tanks.network;

import tanks.Game;

import java.util.UUID;

public class ConnectedPlayer
{
	public String username;
	public final String rawUsername;
	public final UUID clientID;
	
	public ConnectedPlayer(UUID id, String name)
	{
		this.clientID = id;
		this.rawUsername = name;
		this.username = name;
		
		if (Game.enableChatFilter)
			this.username = Game.chatFilter.filterChat(this.rawUsername);
	}
}
