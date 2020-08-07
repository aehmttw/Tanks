package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.event.EventChat;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Client;
import tanks.network.ConnectedPlayer;

import java.util.ArrayList;
import java.util.UUID;

public class ScreenPartyLobby extends Screen
{
	public static ArrayList<ConnectedPlayer> connections = new ArrayList<ConnectedPlayer>();
	public static boolean isClient = false;
	public static ArrayList<UUID> includedPlayers = new ArrayList<UUID>();
	public static ArrayList<String> readyPlayers = new ArrayList<String>();
	public static int remainingLives = 0;

	public static ArrayList<ChatMessage> chat = new ArrayList<ChatMessage>();

	public int usernamePage = 0;

	public static int entries_per_page = 10;
	public static int username_spacing = 30;
	public static int username_y_offset = -230;
	public static int username_x_offset = 0;

	public static ChatBox chatbox;

	public ScreenPartyLobby()
	{
		this.music = "tomato_feast_4.ogg";
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

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, 350, 40, "Leave party", new Runnable()
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

	@Override
	public void update()
	{
		exit.update();

		if (this.usernamePage > 0)
			this.previousUsernamePage.update();

		if ((this.usernamePage + 1) * 10 < connections.size())
			this.nextUsernamePage.update();

		chatbox.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(24);

		//Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 400, Panel.winlose);

		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 265, "Players in this party:");

		if (connections != null)
		{
			if (this.usernamePage > 0)
				this.previousUsernamePage.draw();

			if ((this.usernamePage + 1) * entries_per_page < connections.size())
				this.nextUsernamePage.draw();

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

			long time = System.currentTimeMillis();
			for (int i = 0; i < chat.size(); i++)
			{
				ChatMessage c = chat.get(i);
				if (time - c.time <= 30000 || chatbox.selected)
				{
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
				}
			}


			exit.draw();
			chatbox.draw();
		}
	}

}
