package tanks.gui;

import basewindow.Color;
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
	public Color color1 = new Color();
	public Color color2 = new Color();
	public Color color3 = new Color();

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
			this.color1.red = Integer.parseInt(c[0]);
			this.color1.green = Integer.parseInt(c[1]);
			this.color1.blue = Integer.parseInt(c[2]);
			this.color2.red = Integer.parseInt(c[3]);
			this.color2.green = Integer.parseInt(c[4]);
			this.color2.blue = Integer.parseInt(c[5]);
			this.color3.red = Integer.parseInt(c[6]);
			this.color3.green = Integer.parseInt(c[7]);
			this.color3.blue = Integer.parseInt(c[8]);
		}

		this.rawMessage = n + ": " + s;
		this.message = n + ": " + s;
		
		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(n) + ": " + Game.chatFilter.filterChat(s);

		this.lines = Drawing.drawing.wrapText(this.message, Drawing.drawing.interfaceSizeX - 40, 24);
	}

	public ChatMessage(Player p, String s)
	{
		this.color1.set(p.color);
		this.color2.set(p.color2);
		this.color3.set(p.color3);

		this.enableTankIcon = true;

		String n = "    " + p.username;
		this.rawMessage = n + ": " + s;
		this.message = n + ": " + s;

		if (Game.enableChatFilter)
			this.message = Game.chatFilter.filterChat(n) + ": " + Game.chatFilter.filterChat(s);

		this.lines = Drawing.drawing.wrapText(this.message, Drawing.drawing.interfaceSizeX - 40, 24);
	}
}
