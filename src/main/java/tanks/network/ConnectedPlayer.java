package tanks.network;

import basewindow.Color;
import tanks.Game;
import tanks.Player;

import java.util.UUID;

public class ConnectedPlayer
{
	public String username;
	public final String rawUsername;
	public final UUID clientId;

	public Color color = new Color();
	public Color color2 = new Color();
	public Color color3 = new Color();
	public Color teamColor = new Color();

	public boolean isBot;

	public ConnectedPlayer(UUID id, String name)
	{
		this.clientId = id;
		this.rawUsername = name;
		this.username = name;
		
		if (Game.enableChatFilter)
			this.username = Game.chatFilter.filterChat(this.rawUsername);
	}

	public ConnectedPlayer(UUID id, String name, boolean bot)
	{
		this(id, name);
		this.isBot = bot;
	}

	public ConnectedPlayer(Player p)
	{
		this.clientId = p.clientID;
		this.username = p.username;
		this.rawUsername = p.username;

		if (Game.enableChatFilter)
			this.username = Game.chatFilter.filterChat(this.rawUsername);

		this.isBot = p.isBot;
		this.setColors(p.color, p.color2, p.color3);

		if (p.tank != null && p.tank.team != null && p.tank.team.enableColor)
			this.teamColor.set(p.tank.team.teamColor);
		else
			this.teamColor.set(255, 255, 255);
	}

	public void setColors(Color c, Color c2, Color c3)
	{
		this.color.set(c);
		this.color2.set(c2);
		this.color3.set(c3);
	}
}
