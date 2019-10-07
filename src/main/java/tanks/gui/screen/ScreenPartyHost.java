package tanks.gui.screen;

import tanks.*;
import tanks.event.EventPlayerChat;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Server;
import tanks.network.SynchronizedList;
import org.lwjgl.glfw.GLFW;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

public class ScreenPartyHost extends Screen implements IPartyMenuScreen
{
	Thread serverThread;
	public static Server server;
	public static boolean isServer = false;
	public static SynchronizedList<UUID> readyPlayers = new SynchronizedList<UUID>();
	public static ScreenPartyHost activeScreen;
	public String ip = "";

	Button[] kickButtons = new Button[entries_per_page];

	public int usernamePage = 0;

	public static int entries_per_page = 10;
	public static int username_spacing = 30;
	public static int username_y_offset = -120;

	public static SynchronizedList<ChatMessage> chat = new SynchronizedList<ChatMessage>();

	public static ChatBox chatbox = new ChatBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 30, 1380, 40, GLFW.GLFW_KEY_T, 
			"\u00A7127127127255Click here or press 'T' to send a chat message", new Runnable()
	{
		@Override
		public void run() 
		{
			chat.add(0, new ChatMessage(Game.username, chatbox.inputText));
			Game.eventsOut.add(new EventPlayerChat(Game.username, chatbox.inputText));
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

	Button nextUsernamePage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190,
			Drawing.drawing.interfaceSizeY / 2 + username_y_offset + username_spacing * (1 + entries_per_page), 300, 30, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			usernamePage++;
		}
	}
	);

	Button previousUsernamePage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + username_y_offset,
			300, 30, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			usernamePage--;
		}
	}
	);

	Button versus = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Versus", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			String s = LevelGeneratorVersus.generateLevelString();
			Level l = new Level(s);
			l.loadLevel();
			ScreenGame.versus = true;

			Game.screen = new ScreenGame();
		}
	}
			, "Fight other players in this party---in a randomly generated level");

	Button crusades = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Crusades", "Crusades are not yet---supported in party mode");

	Button myLevels = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "My levels", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenPlaySavedLevels();
		}
	}
	, "Play levels you have created");

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
		for (int i = 0; i < this.kickButtons.length; i++)
		{
			final int j = i;
			kickButtons[i] = new Button(Drawing.drawing.interfaceSizeX / 2 - 20,
					Drawing.drawing.interfaceSizeY / 2 + (1 + i) * username_spacing + username_y_offset, 25, 25, "x", new Runnable()
			{
				@Override
				public void run()
				{
					Game.screen = new ScreenPartyKick(server.connections.get(j + usernamePage * entries_per_page));
				}
			});

			kickButtons[i].textOffsetY = -1;
			kickButtons[i].textOffsetX = 1;

			kickButtons[i].textColR = 255;
			kickButtons[i].textColG = 255;
			kickButtons[i].textColB = 255;

			kickButtons[i].unselectedColR = 255;
			kickButtons[i].unselectedColG = 0;
			kickButtons[i].unselectedColB = 0;

			kickButtons[i].selectedColR = 255;
			kickButtons[i].selectedColG = 127;
			kickButtons[i].selectedColB = 127;
		}

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
					ip = "Your Local IP Address: " + Inet4Address.getLocalHost().getHostAddress() + " (Port: " + Game.port + ")";
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
		versus.update();
		quit.update();

		if (server != null && server.connections != null)
		{
			if (this.usernamePage > 0)
				this.previousUsernamePage.update();

			if ((this.usernamePage + 1) * 10 < server.connections.size())
				this.nextUsernamePage.update();

			int entries = Math.min(10, server.connections.size() - this.usernamePage * entries_per_page);

			for (int i = 0; i < entries; i++)
			{
				this.kickButtons[i].update();
			}
		}


		chatbox.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();

		myLevels.draw();
		crusades.draw();
		newLevel.draw();
		versus.draw();
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

		if (server != null && server.connections != null)
		{
			if (this.usernamePage > 0)
				this.previousUsernamePage.draw();

			if ((this.usernamePage + 1) * entries_per_page <  server.connections.size())
				this.nextUsernamePage.draw();

			if (this.usernamePage <= 0)
			{
				String n = Game.username;
				if (Game.enableChatFilter)
					n = Game.chatFilter.filterChat(n);

				n = "\u00A7000127255255" + n;

				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + username_y_offset, n);
			}

			if (server.connections != null)
			{
				for (int i = this.usernamePage * entries_per_page; i < Math.min(((this.usernamePage + 1) * entries_per_page), server.connections.size()); i++)
				{
					if (server.connections.get(i).username != null)
					{
						Drawing.drawing.setColor(0, 0, 0);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 - 190,
								Drawing.drawing.interfaceSizeY / 2 + (1 + i - this.usernamePage * entries_per_page) * username_spacing + username_y_offset,
								server.connections.get(i).username);

						this.kickButtons[i - this.usernamePage * entries_per_page].draw();
					}
				}
			}
		}
	}

}
