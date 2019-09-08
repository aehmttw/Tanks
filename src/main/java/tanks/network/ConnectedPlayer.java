package tanks.network;

import tanks.Game;

import java.util.UUID;

public class ConnectedPlayer
{
	public String username;
	public final String rawUsername;
	public final UUID clientId;
	
	public ConnectedPlayer(UUID id, String name)
	{
		this.clientId = id;
		this.rawUsername = name;
		this.username = name;
		
		if (Game.enableChatFilter)
			this.username = Game.chatFilter.filterChat(this.rawUsername);
	}
}
