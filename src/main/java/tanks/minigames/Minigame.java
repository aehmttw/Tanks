package tanks.minigames;

import tanks.Game;
import tanks.Level;
import tanks.ModAPI;
import tanks.bullet.Bullet;
import tanks.gui.screen.*;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

public abstract class Minigame extends Level
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
    public boolean customLevelEnd = false;
    public boolean hideSpeedrunTimer = false;
    public boolean noLose = false;
    public boolean disableEndMusic = false;
    public boolean customIntroMusic = false;
    public boolean enableItemBar = false;
    public boolean flashBackground = false;

    public String name;

    public String introMusic;

    /**
     * If you used getLevelString(), make sure to switch it with the actual level string before publishing it!
     */
    public Minigame(String levelString)
    {
        super(levelString);
        for (String s: Game.registryMinigame.minigames.keySet())
        {
            if (Game.registryMinigame.minigames.get(s).equals(this.getClass()))
                this.name = s;
        }
    }

    @Override
    public void loadLevel()
    {
        ModAPI.menuGroup.clear();
        ScreenInterlevel.fromMinigames = true;

        super.loadLevel();
        setUp();

        Game.screen = new ScreenGame();
        Game.screen.splitTiles = flashBackground;

        if (this.flashBackground)
        {
            Game.screen.tiles = new Screen.FlashingTile[Game.currentSizeX][Game.currentSizeY];

            for (int i = 0; i < Game.screen.tiles.length; i++)
            {
                for (int j = 0; j < Game.screen.tiles[i].length; j++)
                {
                    Game.screen.tiles[i][j] = new Screen.FlashingTile(i, j);
                }
            }
        }
    }

    /**
     * Add custom scoreboards, text, etc.
     */
    public void setUp()
    {

    }

    /**
     * Do any per-frame updating here
     */
    public void update()
    {

    }

    /**
     * Draw any HUD things here
     */
    public void draw()
    {

    }

    /**
     * Override this method to do something when the level has started to end
     */
    public void onLevelEndQuick()
    {

    }

    /**
     * Override this method to do something when the level finished ending
     */
    public void onLevelEnd(boolean levelWon)
    {

    }

    public void loadInterlevelScreen()
    {
        if (ScreenPartyHost.isServer)
            Game.screen = new ScreenPartyInterlevel();
        else
            Game.screen = new ScreenInterlevel();
    }

    /**
     *  Override and set customLevelEnd to true to set a custom level end condition
     */
    public boolean levelEnded()
    {
        return true;
    }

    /**
     * Override to do something when a bullet is fired
     * @param b
     */
    public void onBulletFire(Bullet b)
    {

    }

    /**
     * Override to do something when a tank destroys another tank
     * @param attacker
     * @param target
     */
    public void onKill(Tank attacker, Tank target)
    {

    }

    public String generateKillMessage(Tank killed, Tank killer, boolean isBullet)
    {
        StringBuilder message = new StringBuilder();

        String killedR;
        String killedG;
        String killedB;

        String killR;
        String killG;
        String killB;

        if (killed.team != null && killed.team.enableColor)
        {
            killedR = String.format("%03d", (int) killed.team.teamColorR);
            killedG = String.format("%03d", (int) killed.team.teamColorG);
            killedB = String.format("%03d", (int) killed.team.teamColorB);
        }
        else
        {
            killedR = String.format("%03d", (int) killed.colorR);
            killedG = String.format("%03d", (int) killed.colorG);
            killedB = String.format("%03d", (int) killed.colorB);
        }

        if (killer.team != null && killer.team.enableColor)
        {
            killR = String.format("%03d", (int) killer.team.teamColorR);
            killB = String.format("%03d", (int) killer.team.teamColorG);
            killG = String.format("%03d", (int) killer.team.teamColorB);
        }
        else
        {
            killR = String.format("%03d", (int) killer.colorR);
            killG = String.format("%03d", (int) killer.colorG);
            killB = String.format("%03d", (int) killer.colorB);
        }

        message.append("\u00a7").append(killedR).append(killedG).append(killedB).append("255");

        if (killed instanceof TankPlayer)
            message.append(((TankPlayer) killed).player.username);
        else if (killed instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) killed).player.username);
        else
        {
            String name = killed.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName.toString());
        }
        message.append("\u00a7000000000255 was ").append(isBullet ? "shot" : "blown up").append(" by ").append("\u00a7").append(killR).append(killG).append(killB).append("255");

        if (killer instanceof TankPlayer)
            message.append(((TankPlayer) killer).player.username);

        else if (killer instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) killer).player.username);

        else
        {
            String name = killer.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName.toString());
        }

        return message.toString();
    }

    public String generateDrownMessage(Tank killed)
    {
        StringBuilder message = new StringBuilder();

        String killedR;
        String killedG;
        String killedB;

        if (killed.team != null && killed.team.enableColor)
        {
            killedR = String.format("%03d", (int) killed.team.teamColorR);
            killedG = String.format("%03d", (int) killed.team.teamColorG);
            killedB = String.format("%03d", (int) killed.team.teamColorB);
        }
        else
        {
            killedR = String.format("%03d", (int) killed.colorR);
            killedG = String.format("%03d", (int) killed.colorG);
            killedB = String.format("%03d", (int) killed.colorB);
        }

        message.append("\u00a7").append(killedR).append(killedG).append(killedB).append("255");

        if (killed instanceof TankPlayer)
            message.append(((TankPlayer) killed).player.username);

        else if (killed instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) killed).player.username);

        else
        {
            String name = killed.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName.toString());
        }

        message.append("\u00a7000000000255 drowned");
        return message.toString();
    }
}