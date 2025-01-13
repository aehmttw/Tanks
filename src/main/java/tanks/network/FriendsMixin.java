package tanks.network;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenOverlayChat;
import tanks.translation.Translation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsMixin
{
	public SteamFriends friends;
	public Map<Integer, SteamID> friendUserIDs = new ConcurrentHashMap<>();
	public Map<Integer, Long> friendGameIDs = new ConcurrentHashMap<>();
	public Map<Integer, String> friendNames = new ConcurrentHashMap<>();
	public Map<Integer, SteamFriends.PersonaState> friendStatuses = new ConcurrentHashMap<>();

	public SteamFriendsCallback friendsCallback = new SteamFriendsCallback()
	{
		@Override
		public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend)
		{
			Game.steamLobbyInvite = Long.parseLong(steamIDLobby.toString(), 16);
			Drawing.drawing.playSound("join.ogg", 2f);
			ScreenOverlayChat.addChat(Translation.translate("\u00A7000200000255Head over to the 'Join a party' menu under 'Multiplayer' to join the party you were invited to!"));
		}
	};

	public FriendsMixin()
	{
		friends = new SteamFriends(friendsCallback);
	}

	public void dispose()
	{
		friends.dispose();
	}

	public void updateFriends()
	{
		int friendsCount = friends.getFriendCount(SteamFriends.FriendFlags.Immediate);

		for (int i = 0; i < friendsCount; i++)
		{
			SteamID steamIDUser = friends.getFriendByIndex(i, SteamFriends.FriendFlags.Immediate);
			friendUserIDs.put(steamIDUser.getAccountID(), steamIDUser);

			SteamFriends.FriendGameInfo friendGameInfo = new SteamFriends.FriendGameInfo();
			friends.getFriendGamePlayed(steamIDUser, friendGameInfo);
			long id = friendGameInfo.getGameID();
			friendGameIDs.put(steamIDUser.getAccountID(), id);
			friendStatuses.put(steamIDUser.getAccountID(), friends.getFriendPersonaState(steamIDUser));
			friendNames.put(steamIDUser.getAccountID(), friends.getFriendPersonaName(steamIDUser));
		}
	}

	public boolean isFriendAccountID(int accountID)
	{
		return friendUserIDs.containsKey(accountID);
	}

	public SteamID getFriendSteamID(int accountID)
	{
		return friendUserIDs.get(accountID);
	}
}
