package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.event.EventChat;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Client;
import tanks.network.ConnectedPlayer;
import tanks.network.SynchronizedList;

import java.util.ArrayList;
import java.util.UUID;

public class ScreenPartyLobby extends Screen
{
	public static ArrayList<ConnectedPlayer> connections = new ArrayList<ConnectedPlayer>();
	public static boolean isClient = false;
	public static ArrayList<UUID> includedPlayers = new ArrayList<UUID>();
	public static ArrayList<String> readyPlayers = new ArrayList<String>();
	public static int remainingLives = 0;

	public static SynchronizedList<ChatMessage> chat = new SynchronizedList<ChatMessage>();
	public static SynchronizedList<ScreenPartyHost.SharedLevel> sharedLevels = new SynchronizedList<>();
	public static SynchronizedList<ScreenPartyHost.SharedCrusade> sharedCrusades = new SynchronizedList<>();

	public int usernamePage = 0;

	public static int entries_per_page = 10;
	public static int username_spacing = 30;
	public static int username_y_offset = -180;
	public static int username_x_offset = -190;

	public static ChatBox chatbox;

	public ScreenPartyLobby()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		ScreenPartyLobby.chatbox = new ChatBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 30, Drawing.drawing.interfaceSizeX - 20, 40, Game.game.input.chat, new Runnable()
		{
			@Override
			public void run()
			{
				Game.eventsOut.add(new EventChat(ScreenPartyLobby.chatbox.inputText));
			}
		});
	}

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 270, this.objWidth, this.objHeight, "Leave party", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenConfirmLeaveParty();
		}
	}
	);

	Button nextUsernamePage = new Button(Drawing.drawing.interfaceSizeX / 2 + username_x_offset,
			Drawing.drawing.interfaceSizeY / 2 + username_y_offset + username_spacing * (1 + entries_per_page), 300, 30, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			usernamePage++;
		}
	}
	);

	Button previousUsernamePage = new Button(Drawing.drawing.interfaceSizeX / 2 + username_x_offset, Drawing.drawing.interfaceSizeY / 2 + username_y_offset,
			300, 30, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			usernamePage--;
		}
	}
	);

	Button share = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 180, this.objWidth, this.objHeight, "Upload", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenShareSelect();
		}
	});

	Button shared = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 120, this.objWidth, this.objHeight, "Download", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenSharedSummary(sharedLevels, sharedCrusades);
		}
	}
    );

	@Override
	public void update()
	{
		exit.update();

		if (this.usernamePage > 0)
			this.previousUsernamePage.update();

		if ((this.usernamePage + 1) * 10 < connections.size())
			this.nextUsernamePage.update();

		share.update();
		shared.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.textSize);

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Party IP Address: " + Client.currentHost + " (Port: " + Client.currentPort + ")");

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + username_x_offset, Drawing.drawing.interfaceSizeY / 2 - 220, "Players in this party:");

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 220, "Level and crusade sharing:");

		if (connections != null)
		{
			if (this.usernamePage > 0)
				this.previousUsernamePage.draw();

			if ((this.usernamePage + 1) * entries_per_page < connections.size())
				this.nextUsernamePage.draw();


			exit.draw();
			shared.draw();
			share.draw();

			if (connections != null)
			{
				for (int i = this.usernamePage * entries_per_page; i < Math.min(((this.usernamePage + 1) * entries_per_page), connections.size()); i++)
				{
					if (connections.get(i).username != null)
					{
						String n = connections.get(i).username;
						if (connections.get(i).clientId.equals(Game.clientID))
							n = "\u00A7000127255255" + n;
						else if (i == 0)
							n = "\u00A7000200000255" + n;

						Drawing.drawing.setColor(0, 0, 0);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + username_x_offset,
								Drawing.drawing.interfaceSizeY / 2 + (1 + i - this.usernamePage * entries_per_page) * username_spacing + username_y_offset,
								n);
					}
				}
			}
		}
	}

}
