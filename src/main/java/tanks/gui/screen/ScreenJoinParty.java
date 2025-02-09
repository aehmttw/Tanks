package tanks.gui.screen;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamNetworking;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.Client;
import tanks.network.SteamNetworkHandler;
import tanks.network.event.EventSendClientDetails;

import java.util.UUID;

public class ScreenJoinParty extends Screen
{
	public Thread clientThread;

	public ScreenJoinParty()
	{
		this.music = "menu_2.ogg";
		this.musicID = "menu";

		ip.allowDots = true;
		ip.maxChars = 100;
		ip.allowColons = true;
		ip.lowerCase = true;

		if (Game.steamNetworkHandler.initialized)
		{
			ip.posY += this.objYSpace;
			join.posY += this.objYSpace;
		}
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
					clientThread.interrupt();
			}
			catch (Exception ignored) {}

			Game.screen = new ScreenParty();
		}
	}
	);

//	Button steam = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "Join Steam friends", () -> Game.screen = new ScreenJoinSteamFriends((ScreenJoinParty) Game.screen));

	Button steamLobbies = new Button(this.centerX, this.centerY - this.objYSpace * 1.75, this.objWidth, this.objHeight, "Browse public parties", () ->
	{
		Game.steamNetworkHandler.requestLobbies(this);
		Game.screen = new ScreenWaitingLobbyList();
	});

	Button acceptInvite = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 1.75, this.objWidth, this.objHeight, "Accept party invite!", () ->
	{
		ScreenJoinParty s = this;
		String s1 = s.ip.inputText;
		s.ip.inputText = "lobby:" + Long.toHexString(Game.steamLobbyInvite);
		Game.steamLobbyInvite = -1;
		s.join.function.run();
		s.ip.inputText = s1;
		Game.lastOfflineScreen = new ScreenTitle();
	});


	public Button join = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Join", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.lastOfflineScreen = Game.screen;

			ScreenSharedLevels.page = 0;
			ScreenPartyLobby.chat.clear();
			ScreenPartyLobby.sharedLevels.clear();

			ScreenPartyLobby.connections.clear();
			ScreenPartyLobby.connectedBots = 0;
			Game.eventsOut.clear();

			if (ip.inputText.startsWith("lobby:") && Game.steamNetworkHandler.initialized)
			{
				Game.steamNetworkHandler.joinParty(Long.parseLong(ip.inputText.split(":")[1], 16));
				ScreenConnecting s = new ScreenConnecting(clientThread);
				Game.screen = s;
				Client.connectionID = UUID.randomUUID();

				clientThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						UUID connectionID = Client.connectionID;

						while (Game.steamNetworkHandler.currentLobby == null)
						{
							try
							{
								Thread.sleep(25);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}

						SteamID target = Game.steamNetworkHandler.send(Game.steamNetworkHandler.currentLobby, new EventSendClientDetails(Game.network_protocol, Game.clientID, Game.player.username), SteamNetworking.P2PSend.Reliable);

						while (true)
						{
							if (target == null)
								break;

							SteamNetworking.P2PSessionState state = new SteamNetworking.P2PSessionState();
							Game.steamNetworkHandler.networking.getP2PSessionState(target, state);

							if (!state.isConnecting() && !state.isConnectionActive())
							{
								sendToFail(s, connectionID);
								break;
							}

							if (state.isConnectionActive())
							{
								break;
							}
						}
					}

					public void sendToFail(ScreenConnecting s, UUID connectionID)
					{
						if (Game.screen == s && Client.connectionID == connectionID)
						{
							s.text = "Failed to connect";
							s.finished = true;

							s.music = "menu_1.ogg";
							Drawing.drawing.playSound("leave.ogg");

							Panel.forceRefreshMusic = true;
						}
					}
				});
				clientThread.setDaemon(true);
				clientThread.start();
				return;
			}

			if (ip.inputText.startsWith("steam:") && Game.steamNetworkHandler.initialized)
			{
				Client.connectionID = UUID.randomUUID();
				int id = 0;

				try
				{
					id = Integer.parseInt(ip.inputText.substring("steam:".length()));
				}
				catch (Exception e)
				{
					ScreenConnecting s = new ScreenConnecting(clientThread);
					Game.screen = s;

					s.text = "Failed to connect";
					s.exception = e.getLocalizedMessage();
					s.finished = true;

					s.music = "menu_1.ogg";
					Drawing.drawing.playSound("leave.ogg");

					Panel.forceRefreshMusic = true;
				}

				SteamID target = Game.steamNetworkHandler.send(id, new EventSendClientDetails(Game.network_protocol, Game.clientID, Game.player.username), SteamNetworking.P2PSend.Reliable);

				clientThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						UUID connectionID = Client.connectionID;
						ScreenConnecting s = new ScreenConnecting(clientThread);
						Game.screen = s;
						s.steamID = target;

						while (true)
						{
							if (target == null)
							{
								sendToFail(s, connectionID);
								break;
							}

							SteamNetworking.P2PSessionState state = new SteamNetworking.P2PSessionState();
							Game.steamNetworkHandler.networking.getP2PSessionState(target, state);

							if (!state.isConnecting() && !state.isConnectionActive())
							{
								sendToFail(s, connectionID);
								break;
							}

							if (state.isConnectionActive())
								break;
						}
					}

					public void sendToFail(ScreenConnecting s, UUID connectionID)
					{
						if (Game.screen == s && Client.connectionID == connectionID)
						{
							s.text = "Failed to connect";
							s.finished = true;

							s.music = "menu_1.ogg";
							Drawing.drawing.playSound("leave.ogg");

							Panel.forceRefreshMusic = true;
						}
					}

				});

				clientThread.setDaemon(true);
				clientThread.start();

				return;
			}

			clientThread = new Thread(() ->
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

						s.music = "menu_1.ogg";
						Drawing.drawing.playSound("leave.ogg");

						Panel.forceRefreshMusic = true;

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
	
	public TextBox ip = new TextBox(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth * 16 / 7, this.objHeight, "Party IP Address", new Runnable()
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

		if (Game.steamNetworkHandler.initialized)
		{
//			steam.update();

			if (Game.steamLobbyInvite != -1)
			{
				steamLobbies.posX = this.centerX - this.objXSpace / 2;
				acceptInvite.update();
			}

			steamLobbies.update();
		}
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();
		join.draw();
		ip.draw();
		back.draw();

		if (Game.steamNetworkHandler.initialized)
		{
			steamLobbies.draw();
//			steam.draw();

			if (Game.steamLobbyInvite != -1)
				acceptInvite.draw();

			Drawing.drawing.setInterfaceFontSize(this.textSize);
			Drawing.drawing.displayInterfaceText(this.centerX, this.steamLobbies.posY - this.objYSpace * 0.75, "Join Steam parties");
		}

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Join a party");

		Drawing.drawing.setInterfaceFontSize(this.textSize);
		Drawing.drawing.displayInterfaceText(this.ip.posX, this.ip.posY - this.objYSpace * 1.25, "Join by IP");
	}
}
