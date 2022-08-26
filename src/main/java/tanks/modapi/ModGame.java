package tanks.modapi;

import tanks.Game;
import tanks.Level;
import tanks.event.INetworkEvent;
import tanks.modapi.events.EventCustomLevelEndCondition;
import tanks.modapi.events.EventCustomLevelEndConditionMet;
import tanks.modapi.events.EventDisableMinimap;
import tanks.tank.Tank;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class ModGame
{
    public boolean customLevelEndCondition = false;
    public boolean customRestart = false;
    public boolean enableShooting = true;
    public boolean enableLayingMines = true;
    public boolean forceDisableMinimap = false;
    public boolean enableKillMessages = false;
    public double playerKillCoins = 0;

    public String name;
    public String description = null;

    public boolean listeningForEvents = false;
    public HashSet<Class<? extends INetworkEvent>> currentEventClasses = new HashSet<>();
    public ArrayList<INetworkEvent> currentEvents = new ArrayList<>();

    public ModGame()
    {
        this.name = this.getClass().getSimpleName().replace("_", " ");
    }

    public void start()
    {
        if (this.forceDisableMinimap)
            Game.eventsOut.add(new EventDisableMinimap());

        if (this.customLevelEndCondition)
            Game.eventsOut.add(new EventCustomLevelEndCondition());
    }

    /**
     * Called when the <code>Restart this Level</code> button is clicked
     */
    public void onLevelRestart()
    {
        Game.cleanUp();
        ModAPI.loadLevel(Game.currentLevelString);
    }

    public void onKill(Tank killer, Tank killed)
    {

    }

    public boolean levelEndCondition()
    {
        return false;
    }

    public void onLevelEnd(boolean levelWon)
    {
        Game.eventsOut.add(new EventCustomLevelEndConditionMet());
    }

    public void update()
    {

    }

    public void draw()
    {

    }

    public String generateKillMessage(Tank killed, Tank killer, boolean isBullet)
    {
        return Level.genKillMessage(killed, killer, isBullet);
    }

    public String generateDrownMessage(Tank killed)
    {
        return Level.genDrownMessage(killed);
    }
}
