package tanks.network.event;

import tanks.*;
import tanks.gui.screen.*;
import tanks.item.Item;
import tanks.minigames.Minigame;
import tanks.tank.*;
import tanks.tankson.Serializer;

import java.util.ArrayList;

public class EventLoadLevel extends PersonalEvent
{
    public String level;

    public double startTime;
    public boolean disableFriendlyFire;

    public EventLoadLevel()
    {

    }

    public EventLoadLevel(Level l)
    {
        this.level = l.levelString;

        if (Crusade.crusadeMode)
        {
            // A crusade level leaves the shop and the builds to the crusade, so send the
            // crusade's in their place - the level is sent with them as if they were its own.
            ArrayList<Item.ShopItem> shop = l.shop;
            ArrayList<TankPlayer.ShopTankBuild> builds = l.playerBuilds;

            l.shop = Crusade.currentCrusade.getShop();
            l.playerBuilds = Crusade.currentCrusade.getBuildsShop();

            this.level = Serializer.toTanksON(l);

            l.shop = shop;
            l.playerBuilds = builds;
        }

        this.startTime = l.startTime;
        this.disableFriendlyFire = l.disableFriendlyFire;

        if (l instanceof Minigame)
            this.level = "minigame=" + ((Minigame) l).name;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        if (Game.playerTank != null)
            Game.playerTank.team = null;

        try
        {
            ScreenPartyLobby.readyPlayers.clear();
            ScreenPartyLobby.includedPlayers.clear();
            Game.cleanUp();

            if (level.startsWith("minigame="))
                Game.currentLevel = Game.registryMinigame.minigames.get(level.substring(level.indexOf("=") + 1)).getConstructor().newInstance();
            else
                Game.currentLevel = Level.fromString(level, new ArrayList<>(), true, disableFriendlyFire);

            Game.currentLevel.startTime = startTime;
            Game.currentLevel.loadLevel();
        }
        catch (Exception e)
        {
            Game.screen = new ScreenFailedToLoadLevel("Level is remote!", level, e, new ScreenPartyLobby());
        }
    }
}
