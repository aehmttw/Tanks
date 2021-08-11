package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.gui.Firework;
import tanks.gui.SpeedrunTimer;

import java.util.ArrayList;

public class ScreenPartyInterlevel extends Screen implements IDarkScreen
{
    boolean odd = false;

    ArrayList<Firework> fireworks1 = new ArrayList<Firework>();
    ArrayList<Firework> fireworks2 = new ArrayList<Firework>();

    Button newLevel = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Generate a new level", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenGame.versus)
            {
                Game.cleanUp();
                new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
                Game.screen = new ScreenGame();
            }
            else
            {
                Game.reset();
                Game.screen = new ScreenGame();
            }
        }
    }
    );

    Button replay = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Replay the level", new Runnable()
    {
        @Override
        public void run()
        {
            Level level = new Level(Game.currentLevelString);
            level.loadLevel();
            Game.screen = new ScreenGame();
        }
    }
    );

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.resetTiles();
            Game.screen = ScreenPartyHost.activeScreen;
            ScreenGame.versus = false;
            ScreenInterlevel.fromSavedLevels = false;
        }
    }
    );

    Button replayHigherPos = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Replay the level", new Runnable()
    {
        @Override
        public void run()
        {
            Level level = new Level(Game.currentLevelString);
            level.loadLevel();
            Game.screen = new ScreenGame();
        }
    }
    );

    Button quitHigherPos = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.resetTiles();
            Game.screen = ScreenPartyHost.activeScreen;
            ScreenGame.versus = false;
            ScreenInterlevel.fromSavedLevels = false;
        }
    }
    );

    Button next = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Continue", new Runnable()
    {
        @Override
        public void run()
        {
            Game.resetTiles();
            Game.screen = new ScreenPartyLobby();
            ScreenGame.versus = false;
        }
    }
    );

    public ScreenPartyInterlevel()
    {
        Game.player.hotbar.percentHidden = 100;

        if (Panel.win)
        {
            Drawing.drawing.playSound("win.ogg");
            this.music = "win_music.ogg";
        }
        else
        {
            Drawing.drawing.playSound("lose.ogg");
            this.music = "lose_music.ogg";
        }

        if (Panel.win && Game.effectsEnabled)
        {
            for (int i = 0; i < 5; i++)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, getFireworkArray());
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }
        }
    }


    @Override
    public void update()
    {
        if (Panel.win && Game.effectsEnabled)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.update();
        }
        else if (ScreenInterlevel.fromSavedLevels)
        {
            quitHigherPos.update();
            replayHigherPos.update();
        }
        else
        {
            quit.update();
            replay.update();
            newLevel.update();
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (Panel.win && Game.effectsEnabled && !Game.game.window.drawingShadow)
        {
            ArrayList<Firework> fireworks = getFireworkArray();
            if (Math.random() < Panel.frameFrequency * ScreenInterlevel.firework_frequency * Game.effectMultiplier)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, fireworks);
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }

            for (int i = 0; i < getFireworkArray().size(); i++)
            {
                fireworks.get(i).drawUpdate(fireworks, getOtherFireworkArray());
            }

            if (Game.glowEnabled)
            {
                for (int i = 0; i < getFireworkArray().size(); i++)
                {
                    fireworks.get(i).drawGlow();
                }
            }

            //A fix to some glitchiness on ios
            Drawing.drawing.setColor(0, 0, 0, 0);
            Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

            fireworks.clear();
            odd = !odd;
        }

        if (Panel.win && Game.effectsEnabled)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.draw();
        }
        else if (ScreenInterlevel.fromSavedLevels)
        {
            quitHigherPos.draw();
            replayHigherPos.draw();
        }
        else
        {
            quit.draw();
            replay.draw();
            newLevel.draw();
        }

        if (Game.showSpeedrunTimer)
            SpeedrunTimer.draw();

        if ((Panel.win && Game.effectsEnabled) || Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, Panel.winlose);
    }

    public ArrayList<Firework> getFireworkArray()
    {
        if (odd)
            return fireworks2;
        else
            return fireworks1;
    }

    public ArrayList<Firework> getOtherFireworkArray()
    {
        if (odd)
            return fireworks1;
        else
            return fireworks2;
    }
}
