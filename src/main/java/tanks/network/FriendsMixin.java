package tanks.network;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsMixin
{
	public SteamFriends friends;
	public Map<Integer, SteamID> friendUserIDs = new ConcurrentHashMap<>();
	public Map<Integer, Long> friendGameIDs = new ConcurrentHashMap<>();
	public Map<Integer, String> friendNames = new ConcurrentHashMap<>();

	public SteamFriendsCallback friendsCallback = new SteamFriendsCallback()
	{
		@Override
		public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result)
		{
			/*System.out.println("Set persona name response: " +
					"success=" + success +
					", localSuccess=" + localSuccess +
					", result=" + result.name());*/
		}

		@Override
		public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change)
		{
			/*switch (change)
			{
				case Name:
					System.out.println("Persona name received: " +
							"accountID=" + steamID.getAccountID() +
							", name='" + friends.getFriendPersonaName(steamID) + "'");
					break;

				default:
					System.out.println("Persona state changed (unhandled): " +
							"accountID=" + steamID.getAccountID() +
							", change=" + change.name());
					break;
			}*/
		}

		//@Override
		public void onGameOverlayActivated(boolean active)
		{

		}

		@Override
		public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend)
		{

		}

		@Override
		public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height)
		{

		}

		@Override
		public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID)
		{

		}

		@Override
		public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect)
		{

		}

		@Override
		public void onGameServerChangeRequested(String server, String password)
		{

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

	/*public void processInput(String input)
	{
		if (input.equals("persona get"))
		{
			System.out.println("persona name: " + friends.getPersonaName());
		}
		else if (input.startsWith("persona set "))
		{
			String personaName = input.substring("persona set ".length());
			friends.setPersonaName(personaName);
		}
		else if (input.equals("friends list"))
		{

			int friendsCount = friends.getFriendCount(SteamFriends.FriendFlags.Immediate);
			System.out.println(friendsCount + " friends");

			for (int i = 0; i < friendsCount; i++)
			{
				SteamID steamIDUser = friends.getFriendByIndex(i, SteamFriends.FriendFlags.Immediate);
				friendUserIDs.put(steamIDUser.getAccountID(), steamIDUser);

				String personaName = friends.getFriendPersonaName(steamIDUser);
				SteamFriends.PersonaState personaState = friends.getFriendPersonaState(steamIDUser);

				System.out.println("  - " + steamIDUser.getAccountID() + " (" +
						personaName + ", " + personaState.name() + ")");
			}
		}
	}*/

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
