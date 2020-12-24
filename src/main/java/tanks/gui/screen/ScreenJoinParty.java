package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.Client;

import java.util.UUID;

public class ScreenJoinParty extends Screen
{
	public Thread clientThread;

	public ScreenJoinParty()
	{
		this.music = "tomato_feast_2.ogg";
		this.musicID = "menu";

		ip.allowDots = true;
		ip.maxChars = 43;
		ip.allowColons = true;
		ip.lowerCase = true;
	}
	
	Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@SuppressWarnings("deprecation")
		@Override
		public void run() 
		{
			try
			{
				if (clientThread != null && clientThread.isAlive())
					clientThread.stop();
			}
			catch (Exception ignored) {}

			Game.screen = new ScreenParty();
		}
	}
	);
	
	Button join = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Join", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.lastOfflineScreen = Game.screen;

			ScreenSharedLevels.page = 0;
			ScreenPartyLobby.chat.clear();
			ScreenPartyLobby.sharedLevels.clear();

			Game.eventsOut.clear();
			clientThread = new Thread(new Runnable()
			{

				@Override
				public void run() 
				{
					ScreenConnecting s = new ScreenConnecting(clientThread);
					Game.screen = s;

					UUID connectionID = UUID.randomUUID();
					Client.connectionID = connectionID;

					try 
					{
						String ipaddress = ip.inputText;
						int port = Game.port;
						
						if (ip.inputText.contains(":"))
						{
							int colon = ip.inputText.lastIndexOf(":");
							ipaddress = ip.inputText.substring(0, colon);
							port = Integer.parseInt(ip.inputText.substring(colon + 1));
						}
						
						if (ip.inputText.equals(""))
							Client.connect("localhost", Game.port, false, connectionID);
						else
							Client.connect(ipaddress, port, false, connectionID);
					} 
					catch (Exception e) 
					{
						if (Game.screen == s && Client.connectionID == connectionID)
						{
							s.text = "Failed to connect";
							s.exception = e.getLocalizedMessage();
							s.finished = true;

							s.music = "tomato_feast_1.ogg";
							Drawing.drawing.playSound("leave.ogg");

							Panel.forceRefreshMusic = true;

							e.printStackTrace(Game.logger);
							e.printStackTrace();
						}
					}
				}

			});
			
			clientThread.setDaemon(true);
			clientThread.start();		
		}
	}
	);
	
	TextBox ip = new TextBox(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth * 16 / 7, this.objHeight, "Party IP Address", new Runnable()
	{

		@Override
		public void run() 
		{
			Game.lastParty = ip.inputText;
			ScreenOptions.saveOptions(Game.homedir);
		}
		
	}	
			, Game.lastParty, "You can find this on the---party host's screen");
	
	@Override
	public void update() 
	{
		ip.update();
		join.update();
		back.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		join.draw();
		ip.draw();
		back.draw();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Join a party");
	}
}
