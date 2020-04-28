package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.Client;

public class ScreenJoinParty extends Screen
{
	public Thread clientThread;

	public ScreenJoinParty()
	{
		ip.allowDots = true;
		ip.maxChars = 43;
		ip.allowColons = true;
		ip.lowerCase = true;
	}
	
	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, 350, 40, "Back", new Runnable()
	{
		@SuppressWarnings("deprecation")
		@Override
		public void run() 
		{
			if (clientThread != null && clientThread.isAlive())
				clientThread.stop();
			Game.screen = new ScreenParty();
		}
	}
	);
	
	Button join = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Join", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.lastOfflineScreen = Game.screen;

			Game.eventsOut.clear();
			clientThread = new Thread(new Runnable()
			{

				@Override
				public void run() 
				{
					ScreenConnecting s = new ScreenConnecting(clientThread);
					Game.screen = s;
						
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
							Client.connect("localhost", Game.port, false);
						else
							Client.connect(ipaddress, port, false);
					} 
					catch (Exception e) 
					{
						s.text = "Failed to connect";
						s.exception = e.getLocalizedMessage();
						s.finished = true;

						e.printStackTrace(Game.logger);
						e.printStackTrace();
					}
				}

			});
			
			clientThread.setDaemon(true);
			clientThread.start();		
		}
	}
	);
	
	TextBox ip = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 800, 40, "Party IP Address", new Runnable()
	{

		@Override
		public void run() 
		{
			Game.lastParty = ip.inputText;
			ScreenOptions.saveOptions(Game.homedir);
		}
		
	}	
			,Game.lastParty, "You can find this on the---party host's screen");
	
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
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 210, "Join a party");
	}
}
