package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.network.event.EventChat;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Client;
import tanks.network.ConnectedPlayer;
import tanks.network.SynchronizedList;
import tanks.tank.Tank;
import tanks.tank.TankModels;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ScreenPartyLobby extends Screen
{
	public static ArrayList<ConnectedPlayer> connections = new ArrayList<>();
	public static boolean isClient = false;
	public static ArrayList<UUID> includedPlayers = new ArrayList<>();
	public static ArrayList<ConnectedPlayer> readyPlayers = new ArrayList<>();
	public static int remainingLives = 0;
	public static HashMap<UUID, String> stats = new HashMap<>();

	public static SynchronizedList<ChatMessage> chat = new SynchronizedList<>();
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

		toggleIP.fullInfo = true;

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		ScreenPartyLobby.chatbox = new ChatBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.getInterfaceEdgeY(true) - 30, Drawing.drawing.interfaceSizeX - 20, 40, Game.game.input.chat, () -> Game.eventsOut.add(new EventChat(ScreenPartyLobby.chatbox.inputText)));
	}

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 270, this.objWidth, this.objHeight, "Leave party", () -> Game.screen = new ScreenConfirmLeaveParty());
	Button options = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, this.objWidth, this.objHeight, "Options", () -> Game.screen = new ScreenOptions());

	Button nextUsernamePage = new Button(Drawing.drawing.interfaceSizeX / 2 + username_x_offset,
			Drawing.drawing.interfaceSizeY / 2 + username_y_offset + username_spacing * (1 + entries_per_page), 300, 30, "Next page", () -> usernamePage++
	);

	Button previousUsernamePage = new Button(Drawing.drawing.interfaceSizeX / 2 + username_x_offset, Drawing.drawing.interfaceSizeY / 2 + username_y_offset,
			300, 30, "Previous page", () -> usernamePage--
	);

	Button share = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 180, this.objWidth, this.objHeight, "Upload", () -> Game.screen = new ScreenShareSelect());

	Button shared = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 120, this.objWidth, this.objHeight, "Download", () -> Game.screen = new ScreenSharedSummary(sharedLevels, sharedCrusades));

	Button toggleIP = new Button(-1000, -1000, this.objHeight, this.objHeight, "", () -> Game.showIP = !Game.showIP, "Toggle showing IP address");

	@Override
	public void update()
	{
		exit.update();
		options.update();

		if (this.usernamePage > 0)
			this.previousUsernamePage.update();

		if ((this.usernamePage + 1) * 10 < connections.size())
			this.nextUsernamePage.update();

		share.update();
		shared.update();
		toggleIP.update();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.textSize);

		String title = Translation.translate("Party IP Address: %s (Port: %d)", Client.currentHost, Client.currentPort);

		if (Client.handler.steamID != null)
			title = "Connected to party via Steam Peer-to-Peer";

		if (!Game.showIP)
			title = Translation.translate("Connected to party");

		this.toggleIP.posX = this.centerX + Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, title) / Drawing.drawing.interfaceScale / 2 + 30;
		this.toggleIP.posY = this.centerY - 270;

		if (Game.showIP)
			this.toggleIP.setText("-");
		else
			this.toggleIP.setText("+");

		this.toggleIP.draw();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.textSize);

		Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, title);

		Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + username_x_offset, Drawing.drawing.interfaceSizeY / 2 - 220, "Players in this party:");

		Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 220, "Level and crusade sharing:");

		if (connections != null)
		{
			if (this.usernamePage > 0)
				this.previousUsernamePage.draw();

			if ((this.usernamePage + 1) * entries_per_page < connections.size())
				this.nextUsernamePage.draw();


			exit.draw();
			options.draw();
			shared.draw();
			share.draw();

			if (connections != null)
			{
				for (int i = (int) (this.usernamePage * entries_per_page + Math.signum(this.usernamePage)); i < Math.min(((this.usernamePage + 1) * entries_per_page + 1), connections.size()); i++)
				{
					ConnectedPlayer c = connections.get(i);
					if (c.username != null)
					{
						String n = connections.get(i).username;
						if (connections.get(i).clientId.equals(Game.clientID))
							n = "\u00A7000127255255" + n;
						else if (i == 0)
							n = "\u00A7000200000255" + n;

						Drawing.drawing.setBoundedInterfaceFontSize(this.textSize, 250, n);
						double y = Drawing.drawing.interfaceSizeY / 2 + (i - this.usernamePage * entries_per_page) * username_spacing + username_y_offset;
						Drawing.drawing.setColor(0, 0, 0);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + username_x_offset, y, n);

						Tank.drawTank(this.centerX - Drawing.drawing.getStringWidth(n) / 2 - 230, y, c.colorR, c.colorG, c.colorB, c.colorR2, c.colorG2, c.colorB2);
					}
				}
			}
		}
	}
}
