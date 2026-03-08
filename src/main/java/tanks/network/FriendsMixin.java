package tanks.network;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.ScreenElement;
import tanks.gui.screen.ScreenOverlayChat;
import tanks.translation.Translation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsMixin
{
    public SteamFriends friends;
    public Map<Integer, SteamID> friendUserIDs = new ConcurrentHashMap<>();
    public Map<Integer, Long> friendGameIDs = new ConcurrentHashMap<>();
    public Map<Integer, String> knownUsernamesByID = new ConcurrentHashMap<>();
    public Map<Integer, SteamFriends.PersonaState> friendStatuses = new ConcurrentHashMap<>();

    public SteamFriendsCallback friendsCallback = new SteamFriendsCallback()
    {
        @Override
        public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend)
        {
            Game.steamLobbyInvite = Long.parseLong(steamIDLobby.toString(), 16);
            Panel.notifications.add(new ScreenElement.Notification("Head over to the 'Join a party' menu under 'Multiplayer' to join the party you were invited to!", 2000, 400));
        }

        @Override
        public void onPersonaStateChange(SteamID steamID, SteamFriends.PersonaChange change)
        {
            if (change == SteamFriends.PersonaChange.NameFirstSet)
                knownUsernamesByID.put(steamID.getAccountID(), friends.getFriendPersonaName(steamID));
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
            knownUsernamesByID.put(steamIDUser.getAccountID(), friends.getFriendPersonaName(steamIDUser));
        }

        knownUsernamesByID.put(Game.steamNetworkHandler.user.getSteamID().getAccountID(), friends.getPersonaName());
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
