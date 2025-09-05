package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.CrusadePlayer;
import tanks.Player;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;

import java.util.ArrayList;
import java.util.UUID;

public class EventShowCrusadeStats extends PersonalEvent
{
    public String name;
    public String levels;
    public String stats;
    public String crusade;

    public EventShowCrusadeStats()
    {
        if (ScreenPartyLobby.isClient)
            return;

        this.name = Crusade.currentCrusade.name;
        this.crusade = Crusade.currentCrusade.contents;

        StringBuilder l = new StringBuilder();

        for (Crusade.LevelPerformance p: Crusade.currentCrusade.performances)
        {
            l.append(p.toString()).append("\n");
        }

        this.levels = l.toString();


        StringBuilder s = new StringBuilder();

        ArrayList<CrusadePlayer> players = new ArrayList<>();
        players.addAll(Crusade.currentCrusade.crusadePlayers.values());
        players.addAll(Crusade.currentCrusade.disconnectedPlayers);

        int levelExtra = 0;

        if (Crusade.currentCrusade.win)
            levelExtra = 1;

        for (CrusadePlayer cp: players)
        {
            if (cp != null)
            {
                s.append(cp.player.clientID).append("/").append(cp.player.username).append(":")
                        .append(cp.tankKills.toString()).append("/").append(cp.tankDeaths.toString()).append("/")
                        .append(cp.itemUses.toString()).append("/").append(cp.itemHits.toString()).append("/")
                        .append(cp.coins).append("/").append(cp.player.remainingLives).append("/").append(Crusade.currentCrusade.currentLevel + levelExtra).append("/").append(Crusade.currentCrusade.timePassed).append("\n");
            }
        }

        this.stats = s.toString();
    }

    @Override
    public void execute()
    {
        Crusade.currentCrusade = new Crusade(this.crusade, this.name);

        String[] levels = this.levels.split("\n");

        for (String level: levels)
        {
            Player.parseLevelPerformances(Crusade.currentCrusade.performances, level);
        }

        String[] players = this.stats.split("\n");

        for (String p: players)
        {
            String[] parts1 = p.split(":");
            String[] parts2 = parts1[0].split("/");
            UUID id = UUID.fromString(parts2[0]);

            CrusadePlayer cp = new CrusadePlayer(new Player(id, parts2[1]));

            String[] parts3 = parts1[1].split("/");
            Player.parseStringIntHashMap(cp.tankKills, parts3[0]);
            Player.parseStringIntHashMap(cp.tankDeaths, parts3[1]);
            Player.parseStringIntHashMap(cp.itemUses, parts3[2]);
            Player.parseStringIntHashMap(cp.itemHits, parts3[3]);

            cp.coins = Integer.parseInt(parts3[4]);
            cp.player.remainingLives = Integer.parseInt(parts3[5]);
            Crusade.currentCrusade.currentLevel = Integer.parseInt(parts3[6]);
            Crusade.currentCrusade.timePassed = Double.parseDouble(parts3[7]);

            Crusade.currentCrusade.crusadePlayers.put(cp.player, cp);
        }
    }
}
