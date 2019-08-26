package tanks.gui.screen;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import tanks.ChatMessage;
import tanks.Drawing;
import tanks.Game;
import tanks.IPartyMenuScreen;
import tanks.event.EventPlayerChat;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.Panel;
import tanks.network.Server;

public class ScreenPartyHost extends Screen implements IPartyMenuScreen
{
	Thread serverThread;
	public static Server server;
	public static boolean isServer = false;
	public static ArrayList<UUID> readyPlayers = new ArrayList<UUID>();
	public static ScreenPartyHost activeScreen;
	public String ip = "";

	public static ArrayList<ChatMessage> chat = new ArrayList<ChatMessage>();

	ChatBox chatbox = new ChatBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 30, 1380, 40, GLFW.GLFW_KEY_SPACE, 
			"\u00A7127127127255Click here or press space to send a chat message", new Runnable()
	{
		@Override
		public void run() 
		{
			chat.add(0, new ChatMessage(Game.username, chatbox.inputText));
			Game.events.add(new EventPlayerChat(Game.username, chatbox.inputText));
		}

	});

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Random level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.reset();
			Game.screen = new ScreenGame();
		}
	}
	, "Generate a random level to play");

	Button crusades = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Crusades", "Crusades are not yet---supported in party mode");

	Button myLevels = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "My levels", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlaySavedLevels();
		}
	}
	, "Play levels you have created!");

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "End party", new Runnable()
	{
		@Override
		public void run() 
		{
			isServer = false;
			server.close();
			activeScreen = null;
			Game.screen = new ScreenParty();
		}
	}
			);

	public ScreenPartyHost()
	{
		activeScreen = this;
		isServer = true;
		serverThread = new Thread(new Runnable()
		{

			@Override
			public void run() 
			{
				try 
				{
					server = new Server(Game.port);
					server.run();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					return;
				}
			}

		});

		serverThread.setDaemon(true);
		serverThread.start();

		new Thread(new Runnable()
		{

			@Override
			public void run() 
			{
				ip = "Getting your IP Address...";
				try 
				{
					ip = "Your Local IP Address: " + Inet4Address.getLocalHost().getHostAddress();
				} 
				catch (UnknownHostException e) 
				{
					ip = "Failed to get your IP Address";
				}

				if (ip.contains("%"))
					ip = "You are not connected to a network; connect to one to play with others!";

			}
		}
				).start();
	}	

	@Override
	public void update()
	{
		newLevel.update();
		crusades.update();
		myLevels.update();
		quit.update();

		chatbox.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();


		myLevels.draw();
		crusades.draw();
		newLevel.draw();
		quit.draw();

		chatbox.draw();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, this.ip);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 400, Panel.winlose);

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 160, "Play:");

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 160, "Players in this party:");


		long time = System.currentTimeMillis();
		for (int i = 0; i < chat.size(); i++)
		{
			ChatMessage c = chat.get(i);
			if (time - c.time <= 30000 || chatbox.selected)
			{
				Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
			}
		}

		String n = Game.username;
		if (Game.enableChatFilter)
			n = Game.chatFilter.filterChat(n);

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 120, n);


		if (server != null && server.connections != null)
		{
			//synchronized(server.connections)
			{
				for (int i = 0; i < server.connections.size(); i++)
				{
					if (server.connections.get(i).username != null)
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + (i + 1) * 30 - 120, server.connections.get(i).username);
				}
			}
		}
	}

}
