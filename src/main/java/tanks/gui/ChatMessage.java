package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.Player;

import java.util.ArrayList;

public class ChatMessage 
{
	public long time = System.currentTimeMillis();
	
	public String message;
	public String rawMessage;
	public ArrayList<String> lines;

	public boolean enableTankIcon = false;
	public double r1;
	public double g1;
	public double b1;
	public double r2;
	public double g2;
	public double b2;
	public double r3 = -1;
	public double g3 = -1;
	public double b3 = -1;

	public ChatMessage(String s)
	{
		this.rawMessage = s;
		this.message = s;
		
		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(s);

		this.lines = Drawing.drawing.wrapText(this.message, Drawing.drawing.interfaceSizeX - 40, 24);
	}
	
	public ChatMessage(String u, String s)
	{
		String n = u;

		if (u.startsWith("\u00A7"))
		{
			n = "    " + u.split("\\|")[1];

			String[] c = u.substring(1).split("\\|")[0].split(",");

			this.enableTankIcon = true;
			this.r1 = Integer.parseInt(c[0]);
			this.g1 = Integer.parseInt(c[1]);
			this.b1 = Integer.parseInt(c[2]);
			this.r2 = Integer.parseInt(c[3]);
			this.g2 = Integer.parseInt(c[4]);
			this.b2 = Integer.parseInt(c[5]);
			this.r3 = Integer.parseInt(c[6]);
			this.g3 = Integer.parseInt(c[7]);
			this.b3 = Integer.parseInt(c[8]);
		}

		this.rawMessage = n + ": " + s;
		this.message = n + ": " + s;
		
		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(n) + ": " + Game.chatFilter.filterChat(s);

		this.lines = Drawing.drawing.wrapText(this.message, Drawing.drawing.interfaceSizeX - 40, 24);
	}

	public ChatMessage(Player p, String s)
	{
		this.r1 = p.colorR;
		this.g1 = p.colorG;
		this.b1 = p.colorB;
		this.r2 = p.colorR2;
		this.g2 = p.colorG2;
		this.b2 = p.colorB2;
		this.r3 = p.colorR3;
		this.g3 = p.colorG3;
		this.b3 = p.colorB3;
		this.enableTankIcon = true;

		String n = "    " + p.username;
		this.rawMessage = n + ": " + s;
		this.message = n + ": " + s;

		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(n) + ": " + Game.chatFilter.filterChat(s);

		this.lines = Drawing.drawing.wrapText(this.message, Drawing.drawing.interfaceSizeX - 40, 24);
	}
}
