package tanks.modapi.modlevels.Battle_Tanks_3;

import basewindow.InputCodes;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.modapi.ModAPI;
import tanks.modapi.ModGame;
import tanks.modapi.TankNPC;
import tanks.modapi.events.EventCustomLevelEndCondition;
import tanks.modapi.menus.FixedMenu;
import tanks.modapi.menus.FixedText;
import tanks.modapi.menus.TransitionEffect;
import tanks.modapi.modlevels.Battle_Tanks_3.settings.Setting1;
import tanks.modapi.modlevels.Battle_Tanks_3.settings.Setting2;
import tanks.modapi.modlevels.Battle_Tanks_3.settings.Setting3;
import tanks.modapi.modlevels.Battle_Tanks_3.settings.Setting4;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

import java.util.ArrayList;

public class Battle_Tanks_3 extends ModGame
{
    public int levelNum = 1;
    public double timer = 0;
    public double waitTimer = -50;
    public TankNPC generalTank = new TankNPC("", Double.MAX_VALUE, Double.MAX_VALUE, Math.toRadians(215), null, null, 85, 107, 47, new ArrayList<>());

    public boolean levelEnd = false;
    public boolean levelWon = false;
    public boolean levelLost = false;
    public boolean firstFrame = true;
    public boolean fadeDone = false;

    public boolean debug = false;

    private boolean added = false;

    public Battle_Tanks_3()
    {
        this.customLevelEndCondition = true;
        this.customRestart = true;
        this.description = "The sequel to Battle Tanks,---but more exciting, and better.";
    }

    @Override
    public void start()
    {
        super.start();

        setUpLevel();

        ModAPI.displayText(FixedText.types.title, "Battle Tanks 3", false, 3000, 100, 100, 100);
        ModAPI.displayText(FixedText.types.subtitle, "An enemy has arisen.", false, 3000, 255, 255, 255);
    }

    public void setUpLevel()
    {
        Game.cleanUp();
        levelNum++;
        levelEnd = false;
        levelWon = false;
        levelLost = false;
        firstFrame = true;
        fadeDone = false;
        added = false;
        timer = 0;
        waitTimer = -50;
        enableShooting = true;
        enableLayingMines = true;
        listeningForEvents = false;

        Game.eventsOut.add(new EventCustomLevelEndCondition());

        if (levelNum == 1)
            Setting1.setUp(this);
        else if (levelNum == 2)
            Setting2.setUp(this);
        else if (levelNum == 3)
            Setting3.setUp(this);
        else if (levelNum == 4)
            Setting4.setUp(this);

        Game.playerTank.cooldown = 20;

        ModAPI.skipCountdown();

        if (debug)
        {
            Game.playerTank.addUnduplicateAttribute(new AttributeModifier("max_speed", AttributeModifier.Operation.multiply, 1));
            Game.playerTank.addUnduplicateAttribute(new AttributeModifier("friction", AttributeModifier.Operation.multiply, 1));
            Game.playerTank.addUnduplicateAttribute(new AttributeModifier("acceleration", AttributeModifier.Operation.multiply, 1));

            ((TankPlayer) Game.playerTank).enableDestroyCheat = true;
        }
    }

    @Override
    public void update()
    {
        if (!(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing && !((ScreenGame) Game.screen).paused))
            return;

        if (levelNum == 1)
            Setting1.update(this);
        else if (levelNum == 2)
            Setting2.update(this);
        else if (levelNum == 3)
            Setting3.update(this);
        else if (levelNum == 4)
            Setting4.update(this);

        if (!playersAlive())
        {
            levelLost = true;

            if (waitTimer == -50)
                waitTimer = 100;
        }

        timer += Panel.frameFrequency;

        if (waitTimer > 0)
        {
            waitTimer -= Panel.frameFrequency;
            return;
        }

        if (levelEnd)
        {
            if (!added)
            {
                ModAPI.clearMenuGroup();
                ModAPI.addTransitionEffect(TransitionEffect.types.fadeOut);
                added = true;
            }

            if (fadeIsDone() || Game.game.window.pressedKeys.contains(InputCodes.KEY_ESCAPE))
            {
                setUpLevel();
                ModAPI.addTransitionEffect(TransitionEffect.types.fadeIn);
            }
        }

        else if (levelWon)
        {
            if (firstFrame)
            {
                for (Movable m : Game.movables)
                {
                    if (m instanceof Tank)
                        ((Tank) m).invulnerable = true;
                }

                ModAPI.clearMenuGroup();

                ModAPI.displayText(FixedText.types.title, "Mission Complete!", 3000, 255, 255, 0);
                ModAPI.displayText(FixedText.types.subtitle, "Good job!", 3000, 255, 255, 255);

                Drawing.drawing.playGlobalSound("win.ogg");

                Drawing.drawing.movingCamera = false;
                firstFrame = false;
            }

            if (fadeIsDone() || Game.game.window.pressedKeys.contains(InputCodes.KEY_ESCAPE))
            {
                if (!fadeDone)
                {
                    ModAPI.addTransitionEffect(TransitionEffect.types.fadeOut);
                    fadeDone = true;
                }
                else
                {
                    setUpLevel();
                    ModAPI.addTransitionEffect(TransitionEffect.types.fadeIn);
                }
            }
        }

        else if (levelLost)
        {
            if (firstFrame)
            {
                for (Movable m : Game.movables)
                {
                    if (m instanceof Tank)
                        ((Tank) m).invulnerable = true;
                }

                ModAPI.clearMenuGroup();

                ModAPI.displayText(FixedText.types.title, "Mission Failed!", 3000, 255, 0, 0);
                ModAPI.displayText(FixedText.types.subtitle, "Try again!", 3000, 255, 255, 255);

                Drawing.drawing.playGlobalSound("lose.ogg");

                Drawing.drawing.movingCamera = false;
                firstFrame = false;
            }

            if (fadeIsDone() || Game.game.window.pressedKeys.contains(InputCodes.KEY_ESCAPE))
            {
                if (!fadeDone)
                {
                    ModAPI.addTransitionEffect(TransitionEffect.types.fadeOut);
                    fadeDone = true;
                }
                else
                {
                    levelNum--;
                    setUpLevel();
                    ModAPI.addTransitionEffect(TransitionEffect.types.fadeIn);
                }
            }
        }
    }

    public boolean fadeIsDone()
    {
        for (FixedMenu menu : ModAPI.menuGroup)
        {
            if ((menu instanceof FixedText && ((FixedText) menu).location == FixedText.types.title) ||
                    (menu instanceof TransitionEffect && ((TransitionEffect) menu).colorA < 254))
                return false;
        }

        return true;
    }

    @Override
    public void draw()
    {
        if (!(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing))
            return;

        if (levelNum == 1)
            Setting1.draw(this);
        else if (levelNum == 2)
            Setting2.draw(this);
        else if (levelNum == 3)
            Setting3.draw(this);
        else if (levelNum == 4)
            Setting4.draw(this);
    }

    @Override
    public void onLevelRestart()
    {
        levelNum--;
        setUpLevel();
    }

    public void createTree(int x, int y, double height)
    {
        createTree(x,y, height, 0);
    }

    public void createTree(int x, int y, double height, double startHeight)
    {
        ModAPI.setObstacle(x, y, "normal", height, startHeight);
        ModAPI.fillObstacle(x - 2, y - 2, x + 2, y + 2, "shrub", 1, height + startHeight);
        ModAPI.fillObstacle(x - 1, y - 1, x + 1, y + 1, "shrub", 1, height + startHeight + 1);
    }

    public void initGeneralTank(int x, int y, int angle)
    {
        generalTank = new TankNPC("npc", x, y, Math.toRadians((angle - 90) % 360), null, null, 85, 107, 47, new ArrayList<>());
        ModAPI.addObject(generalTank);
    }

    public boolean playersAlive()
    {
        boolean alive = false;

        if (ScreenPartyHost.isServer)
        {
            for (Player p : Game.players)
            {
                if (!p.tank.destroy) {
                    alive = true;
                    break;
                }
            }
        }
        else
            alive = !Game.playerTank.destroy;

        return alive;
    }
}
