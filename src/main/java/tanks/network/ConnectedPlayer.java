package tanks.network;

import tanks.Game;

import java.util.UUID;

public class ConnectedPlayer
{
	public String username;
	public final String rawUsername;
	public final UUID clientId;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorR2;
	public double colorG2;
	public double colorB2;
	public double colorR3;
	public double colorG3;
	public double colorB3;

	public ConnectedPlayer(UUID id, String name)
	{
		this.clientId = id;
		this.rawUsername = name;
		this.username = name;
		
		if (Game.enableChatFilter)
			this.username = Game.chatFilter.filterChat(this.rawUsername);
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
