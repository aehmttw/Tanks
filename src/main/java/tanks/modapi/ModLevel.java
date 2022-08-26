package tanks.modapi;

import tanks.Game;
import tanks.Level;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenInterlevel;
import tanks.modapi.events.EventDisableMinimap;
import tanks.tank.Tank;

public abstract class ModLevel extends Level
{
    /**
     * The amount of coins one gets from killing players.
     */
    public int playerKillCoins = 0;

    /**
     * Forcibly disable the minimap. Useful for games like hide and seek.
     */
    public boolean forceDisableMinimap = false;

    public boolean enableKillMessages = false;

    public String name;
    public String description = null;

    /**
     * If you used getLevelString(), make sure to switch it with the actual level string before publishing it!
     */
    public ModLevel(String levelString)
    {
        super(levelString);
        this.name = this.getClass().getSimpleName().replace("_", " ");
    }

    @Override
    public void loadLevel()
    {
        ModAPI.menuGroup.clear();
        ScreenInterlevel.fromModdedLevels = true;

        super.loadLevel();
        setUp();

        if (this.forceDisableMinimap)
            Game.eventsOut.add(new EventDisableMinimap());

        Game.screen = new ScreenGame();
    }

    /**
     * Add custom scoreboards, text, etc.
     */
    public void setUp()
    {

    }

    /**
     * Update the custom items here
     */
    public void update()
    {

    }

    /**
     * Override this method to do something when the level ends
     */
    public void onLevelEnd(boolean levelWon)
    {

    }

    public void onKill(Tank killer, Tank killed)
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