package tanks.network;

import tanks.Game;
import tanks.Player;

import java.util.UUID;

public class ConnectedPlayer
{
	public String username;
	public final String rawUsername;
	public final UUID clientId;

	public double teamColorR = 255;
	public double teamColorG = 255;
	public double teamColorB = 255;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorR2;
	public double colorG2;
	public double colorB2;
	public double colorR3;
	public double colorG3;
	public double colorB3;

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
		this.setColors(p.colorR, p.colorG, p.colorB, p.colorR2, p.colorG2, p.colorB2, p.colorR3, p.colorG3, p.colorB3);

		if (p.tank != null && p.tank.team != null && p.tank.team.enableColor)
		{
			this.teamColorR = p.tank.team.teamColorR;
			this.teamColorG = p.tank.team.teamColorG;
			this.teamColorB = p.tank.team.teamColorB;
		}
	}

	public void setColors(double r, double g, double b, double r2, double g2, double b2, double r3, double g3, double b3)
	{
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		this.colorR2 = r2;
		this.colorG2 = g2;
		this.colorB2 = b2;
		this.colorR3 = r3;
		this.colorG3 = g3;
		this.colorB3 = b3;
	}
}
