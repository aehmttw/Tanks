package tanks.gui;

import basewindow.BaseFile;
import basewindow.BaseWindow;
import basewindow.Camera;
import tanks.*;
import tanks.gui.screen.*;
import tanks.obstacle.Obstacle;

public class ScreenIntro extends Screen
{
    public long startTime = System.currentTimeMillis();
    public long lastTime = System.currentTimeMillis();

    double introTime = 500;
    double introAnimationTime = 500;

    Screen nextScreen;

    public ScreenIntro()
    {
        this.musicID = "menu";
        this.music = "menu_1.ogg";
        this.drawBehindInfoBar = true;

        if (Game.fancyTerrain && Game.enable3d)
            introAnimationTime = 1000;

        if (Game.usernameInvalid(Game.player.username))
            nextScreen = new ScreenUsernameInvalid();
        else
        {
            if (Game.cinematic)
                nextScreen = new ScreenCinematicTitle();
            else if (Game.steamLobbyInvite != -1)
            {
                if (Game.player.username.isEmpty())
                    nextScreen = new ScreenUsernamePrompt();
                else
                    nextScreen = new ScreenAcceptSteamInvite();
            }
            else
            {
                nextScreen = new ScreenTitle();

                ScreenChangelog.Changelog.setupLogs();

                ScreenChangelog s = new ScreenChangelog();
                s.setup();
                s.prev = nextScreen;

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
    public void setBasePerspective()
    {
        super.setBasePerspective();
        if (Game.enable3d && Game.fancyTerrain && !Game.game.window.mainRenderPasses.drawingShadow)
        {
            double frac = this.getAnimationProgress();
            Game.game.window.currentCamera.worldAdjustment.setTranslation(0, 0, -0.08 * (1 - frac) * Game.game.window.absoluteDepth);
            Game.game.window.loadPerspective();
        }
    }

    @Override
    public void draw()
    {
        this.viewProjection = null;
        double frac = this.getAnimationProgress();

        if (lastTime - startTime >= introTime && Game.fancyTerrain)
        {
            Obstacle.draw_size = frac * Game.tile_size;
            Game.screen.drawDefaultBackground(frac);

            if (!Game.game.window.mainRenderPasses.drawingShadow)
            {
                Camera.Bounds bounds = Game.game.window.currentCamera.getBoundsAtDepth(-1);
                Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue);

                // The intro backdrop is a presentation layer, not a shadow receiver.
                Game.game.window.shaderDefault.shaderBase.shadow.set(false);
                Drawing.drawing.fillRect(
                        (bounds.left + bounds.right) / 2,
                        (bounds.top + bounds.bottom) / 2,
                        -1,
                        bounds.right - bounds.left + Game.tile_size * 4,
                        bounds.bottom - bounds.top + Game.tile_size * 4,
                        true
                );
                Game.game.window.shaderDefault.shaderBase.shadow.set(Game.game.window.mainRenderPasses.shadowsEnabled);
            }
        }

        if (!(nextScreen instanceof ScreenGame))
        {
            nextScreen.setupLayoutParameters();

            // Preserve title framing; only the intro's extra area below it differs.
            nextScreen.setBasePerspective();
            if (!Game.game.window.mainRenderPasses.drawingShadow)
                Game.game.window.currentCamera.worldAdjustment.setTranslation(0, 0, (1 - frac) * 2 * Game.game.window.absoluteDepth);
            Game.game.window.loadPerspective();

            if (nextScreen instanceof ISeparateBackgroundScreen)
                ((ISeparateBackgroundScreen) nextScreen).drawWithoutBackground();
            else
                nextScreen.draw();

            this.viewProjection = Game.game.window.mainRenderPasses.sceneCamera.projection.multiply(Game.game.window.mainRenderPasses.sceneCamera.view);
        }
    }

    private double getAnimationProgress()
    {
        return Math.min(1, (lastTime - startTime - introTime) / introAnimationTime);
    }

    @Override
    public void drawUI()
    {
        double frac = this.getAnimationProgress();
        double barOffset = (1 - frac) * 40;
        BaseWindow window = Game.game.window;

        // No terrain is drawn during the short intro delay, so provide the same
        // level-colored backdrop that the terrain will replace once it begins.
        if (lastTime - startTime < introTime || !Game.fancyTerrain)
        {
            window.bindCamera(window.screenCamera, 0, 0, window.frameBufferWidth, window.frameBufferHeight);
            Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue);
            window.shapeRenderer.fillRect(0, 0, window.absoluteWidth, window.absoluteHeight);
        }

        Drawing.drawing.bindInterfaceCamera();

        if (!(nextScreen instanceof ScreenGame))
        {
            nextScreen.setupLayoutParameters();
            window.interfaceCamera.worldAdjustment.setTranslation(0, 0, (1 - frac) * 2 * window.absoluteDepth);
            window.interfaceCamera.updateView();
            Drawing.drawing.bindInterfaceCamera();
            try
            {
                nextScreen.drawUI();
            }
            finally
            {
                window.interfaceCamera.clearAdjustments();
                Drawing.drawing.bindInterfaceCamera();
            }
        }

        if (Drawing.drawing.enableStats)
        {
            window.bindCamera(window.screenCamera, 0, 0, window.frameBufferWidth, window.frameBufferHeight);
            Panel.panel.drawBar(barOffset);
        }

        Drawing.drawing.bindInterfaceCamera();
    }
}
