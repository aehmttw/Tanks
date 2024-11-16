package tanks.network;

import com.codedisaster.steamworks.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import tanks.Game;
import tanks.gui.screen.ScreenConnecting;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.event.INetworkEvent;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SteamNetworkHandler
{
	public boolean initialized = false;
	protected SteamUtils clientUtils;
	public int[] msgSize = new int[1];

	protected HashMap<Integer, Long> toClose = new HashMap<>();

	public HashMap<Integer, ServerHandler> serverHandlersBySteamID = new HashMap<>();

	protected SteamAPIWarningMessageHook clMessageHook = (severity, message) -> System.err.println("[client debug message] (" + severity + ") " + message);

	protected SteamUtilsCallback clUtilsCallback = new SteamUtilsCallback() {};

	protected static final int defaultChannel = 1;

	protected static final int readBufferCapacity = MessageReader.max_event_size;

	public FriendsMixin friends;
	public SteamNetworking networking;

	protected ByteBuffer packetReadBuffer = ByteBuffer.allocateDirect(readBufferCapacity);

	public ByteBuf sendBuf = Unpooled.buffer();

	public Map<Integer, SteamID> remoteUserIDs = new ConcurrentHashMap<>();

	protected SteamNetworkingCallback peer2peerCallback = new SteamNetworkingCallback()
	{
		@Override
		public void onP2PSessionConnectFail(SteamID steamIDRemote, SteamNetworking.P2PSessionError sessionError)
		{
			unregisterRemoteSteamID(steamIDRemote);
		}

		@Override
		public void onP2PSessionRequest(SteamID steamIDRemote)
		{
			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				networking.closeP2PSessionWithUser(steamIDRemote);
			else
			{
				registerRemoteSteamID(steamIDRemote);
				networking.acceptP2PSessionWithUser(steamIDRemote);
			}
		}
	};

	protected void registerInterfaces()
	{
		friends = new FriendsMixin();
		networking = new SteamNetworking(peer2peerCallback);
		networking.allowP2PPacketRelay(true);
	}

	protected void unregisterInterfaces()
	{
		friends.dispose();
		networking.dispose();
	}

	public void update()
	{
		try
		{
			if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			{
				synchronized (ScreenPartyHost.server.connections)
				{
					for (int i = 0; i < ScreenPartyHost.server.connections.size(); i++)
					{
						ServerHandler s = ScreenPartyHost.server.connections.get(i);

						if (s.steamID != null && serverHandlersBySteamID.get(s.steamID.getAccountID()) == null && !toClose.containsKey(s.steamID.getAccountID()))
						{
							s.channelInactive(null);
							toClose.put(s.steamID.getAccountID(), System.currentTimeMillis());
						}
					}
				}
			}

			ArrayList<Integer> remove = new ArrayList<>();
			for (int i: toClose.keySet())
			{
				if (System.currentTimeMillis() - toClose.get(i) > 100)
				{
					close(i);
					remove.add(i);
				}
			}

			for (int i: remove)
				toClose.remove(i);

			SteamAPI.runCallbacks();
			friends.updateFriends();

			while (networking.isP2PPacketAvailable(defaultChannel, msgSize))
			{
				SteamID steamIDSender = new SteamID();

				packetReadBuffer.clear();

				if (networking.readP2PPacket(steamIDSender, packetReadBuffer, defaultChannel) > 0)
				{
					ByteBuf readBuf = Unpooled.buffer();

					// register, if unknown
					registerRemoteSteamID(steamIDSender);

					int bytesReceived = packetReadBuffer.getInt();

					byte[] bytes = new byte[bytesReceived];
					packetReadBuffer.get(bytes);
					readBuf.writeInt(bytesReceived);
					readBuf.writeBytes(bytes);

					if (ScreenPartyHost.isServer)
					{
						ServerHandler h = serverHandlersBySteamID.get(steamIDSender.getAccountID());

						if (h != null)
							h.channelRead(null, readBuf);
					}
					else if (ScreenPartyLobby.isClient)
						Client.handler.channelRead(null, readBuf);
				}
			}

			for (int id: remoteUserIDs.keySet())
			{
				SteamNetworking.P2PSessionState state = new SteamNetworking.P2PSessionState();
				networking.getP2PSessionState(remoteUserIDs.get(id), state);

				if (!state.isConnectionActive() && !state.isConnecting())
					queueClose(id);
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public SteamID send(int target, INetworkEvent event, SteamNetworking.P2PSend type)
	{
		try
		{
			SteamID steamIDReceiver;

			if (remoteUserIDs.containsKey(target))
				steamIDReceiver = remoteUserIDs.get(target);
			else if (friends.isFriendAccountID(target))
				steamIDReceiver = friends.getFriendSteamID(target);
			else
			{
				Game.exitToCrash(new RuntimeException(";("));
				return null;
			}

			if (steamIDReceiver != null)
			{
				sendBuf.clear();
				event.write(sendBuf);

				ByteBuffer packetSendBuffer = ByteBuffer.allocateDirect(sendBuf.readableBytes() + 8);
				packetSendBuffer.putInt(sendBuf.readableBytes() + 4);

				int i = NetworkEventMap.get(event.getClass());
				if (i == -1)
					throw new RuntimeException("The network event " + event.getClass() + " has not been registered!");

				packetSendBuffer.putInt(i);

				byte[] bytes = new byte[sendBuf.readableBytes()];
				sendBuf.getBytes(0, bytes);
				packetSendBuffer.put(bytes);

				packetSendBuffer.flip(); // limit=pos, pos=0

				networking.sendP2PPacket(steamIDReceiver, packetSendBuffer, type, defaultChannel);
				return steamIDReceiver;
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		return null;
	}

	public void queueClose(int remoteID)
	{
		if (toClose.containsKey(remoteID))
			return;

		toClose.put(remoteID, System.currentTimeMillis());

		synchronized (serverHandlersBySteamID)
		{
			SteamID steamIDRemote = null;

			if (remoteUserIDs.containsKey(remoteID))
				steamIDRemote = remoteUserIDs.get(remoteID);

			if (steamIDRemote != null)
			{
				if (ScreenPartyHost.isServer)
					serverHandlersBySteamID.get(remoteID).channelInactive(null);
				else if (ScreenPartyLobby.isClient)
					Client.handler.channelInactive(null);

				unregisterRemoteSteamID(steamIDRemote);
			}
		}
	}

	public boolean close(int remoteID)
	{
		SteamID steamIDRemote;

		if (remoteUserIDs.containsKey(remoteID))
			steamIDRemote = remoteUserIDs.get(remoteID);
		else
			return false;

		if (steamIDRemote != null)
		{
			networking.closeP2PSessionWithUser(steamIDRemote);

			return true;
		}

		return true;
	}

	protected void registerRemoteSteamID(SteamID steamIDUser)
	{
		synchronized (serverHandlersBySteamID)
		{
			if (!remoteUserIDs.containsKey(steamIDUser.getAccountID()))
			{
				if (ScreenPartyHost.isServer)
				{
					ServerHandler s = new ServerHandler(ScreenPartyHost.server);
					s.steamID = steamIDUser;
					s.reader.useQueue = false;
					serverHandlersBySteamID.put(steamIDUser.getAccountID(), s);
					s.channelActive(null);

					synchronized (ScreenPartyHost.server.connections)
					{
						ScreenPartyHost.server.connections.add(s);
					}
				}
				else if (Game.screen instanceof ScreenConnecting || ScreenPartyLobby.isClient)
				{
					Client.handler = new ClientHandler(false, Client.connectionID);
					Client.handler.steamID = steamIDUser;
					Client.handler.reader.useQueue = false;
					Client.handler.channelActive(null);
				}
				else
				{
					networking.closeP2PSessionWithUser(steamIDUser);
					return;
				}

				remoteUserIDs.put(steamIDUser.getAccountID(), steamIDUser);
			}
		}
	}

	protected void unregisterRemoteSteamID(SteamID steamIDUser)
	{
		synchronized (serverHandlersBySteamID)
		{
			serverHandlersBySteamID.remove(steamIDUser.getAccountID());
			remoteUserIDs.remove(steamIDUser.getAccountID());
		}
	}

	public boolean load()
	{
		if (Game.framework == Game.Framework.libgdx)
			return false;

		try
		{
			System.setProperty("com.codedisaster.steamworks.Debug", "true");

			SteamAPI.loadLibraries(new SteamLibraryLoaderLwjgl3());

			if (!SteamAPI.init())
				return false;

			registerInterfaces();

			clientUtils = new SteamUtils(clUtilsCallback);
			clientUtils.setWarningMessageHook(clMessageHook);

			// doesn't make much sense here, as normally you would call this before
			// SteamAPI.init() with your (kn)own app ID
			if (SteamAPI.restartAppIfNecessary(clientUtils.getAppID()))
			{
				System.out.println("SteamAPI_RestartAppIfNecessary returned 'false'");
			}

			this.initialized = true;
			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void exit()
	{
		clientUtils.dispose();
		unregisterInterfaces();
		SteamAPI.shutdown();
	}
}
