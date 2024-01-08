package tanks.gui;

import basewindow.BaseFile;
import basewindow.transformation.Translation;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.screen.*;
import tanks.obstacle.Obstacle;

public class ScreenIntro extends Screen
{
    public long startTime = System.currentTimeMillis();
    public long lastTime = System.currentTimeMillis();

    double introTime = 500;
    double introAnimationTime = 500;

    Translation zoomTranslation = new Translation(Game.game.window, 0, 0, 0);

    Screen nextScreen;

    public ScreenIntro()
    {
        this.musicID = "menu";
        this.music = "menu_1.ogg";

        zoomTranslation.window = Game.game.window;
        zoomTranslation.applyAsShadow = true;

        if (Game.fancyTerrain && Game.enable3d)
            introAnimationTime = 1000;

        if (Game.usernameInvalid(Game.player.username))
            nextScreen = new ScreenUsernameInvalid();
        else
        {
            if (Game.cinematic)
                nextScreen = new ScreenCinematicTitle();
            else
            {
                nextScreen = new ScreenTitle();

                ScreenChangelog.Changelog.setupLogs();

                ScreenChangelog s = new ScreenChangelog();
                s.setup();

                if (!s.pages.isEmpty())
                    nextScreen = s;
            }
        }

        BaseFile tutorialFile = Game.game.fileManager.getFile(Game.homedir + Game.tutorialPath);
        if (!tutorialFile.exists())
        {
            Game.silentCleanUp();
            Game.lastVersion = Game.version;
            ScreenOptions.saveOptions(Game.homedir);
            new Tutorial().loadTutorial(true, Game.game.window.touchscreen);
            nextScreen = Game.screen;
            Game.screen = this;
            ((ScreenGame) nextScreen).introBattleMusicEnd = 0;
        }

        Panel.panel.introMusicEnd = System.currentTimeMillis() + Long.parseLong(Game.game.fileManager.getInternalFileContents("/music/intro_length.txt").get(0));
        Panel.panel.introMusicEnd -= 40;

        if (Game.framework == Game.Framework.libgdx)
            Panel.panel.introMusicEnd -= 100;

        if (!tutorialFile.exists())
            Drawing.drawing.playSound("battle_intro.ogg", 1.0f, true);
        else
            Drawing.drawing.playMusic("menu_intro.ogg", Game.musicVolume, false, "intro", 0, false);
    }

    @Override
    public void update()
    {
        lastTime = System.currentTimeMillis();

        if (lastTime - startTime >= introTime + introAnimationTime)
        {
            Game.screen = nextScreen;
        }
    }

    @Override
    public void draw()
    {
        Game.game.window.clipMultiplier = 2;
        double frac = Math.min(1, ((lastTime - startTime - introTime) / introAnimationTime));

        if (Game.enable3d && Game.fancyTerrain)
        {
            zoomTranslation.z = -0.08 * (1 - frac);
            Game.game.window.transformations.add(zoomTranslation);
            Game.game.window.loadPerspective();
        }

        Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth * 1.2 / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight * 1.2 / Drawing.drawing.interfaceScale);

        if (lastTime - startTime >= introTime && Game.fancyTerrain)
        {
            Obstacle.draw_size = frac;
            Game.screen.drawDefaultBackground(frac);
        }

        if (!(nextScreen instanceof ScreenGame))
        {
            zoomTranslation.z = (1 - frac);
            Game.game.window.transformations.add(zoomTranslation);
            Game.game.window.loadPerspective();

            nextScreen.draw();
        }

        Game.game.window.transformations.clear();
        Game.game.window.loadPerspective();

        Panel.panel.drawBar((1 - frac) * 40);

        Game.game.window.clipMultiplier = 100;
    }
}
