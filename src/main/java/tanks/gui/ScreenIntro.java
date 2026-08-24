package tanks.gui;

import basewindow.BaseFile;
import basewindow.Camera;
import basewindow.transformation.Translation;
import tanks.*;
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
        // This is a presentation-only nudge.  Applying it to the shadow pass
        // shifts geometry after the shadow projection has been captured.
        zoomTranslation.applyAsShadow = false;

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
    public void draw()
    {
        Game.game.window.clipMultiplier = 2;
        Game.game.window.clipDistMultiplier = 50;

        double frac = Math.min(1, ((lastTime - startTime - introTime) / introAnimationTime));
        int previousStatsHeight = Drawing.drawing.statsHeight;

        if (Game.enable3d && Game.fancyTerrain)
        {
            zoomTranslation.z = -0.08 * (1 - frac);
            Game.game.window.transformations.add(zoomTranslation);
            Game.game.window.loadPerspective();
            Game.game.window.transformations.remove(zoomTranslation);
        }

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

            zoomTranslation.z = (1 - frac) * 2;
            Game.game.window.transformations.add(zoomTranslation);
            // Match the title's normal world pass: its camera and viewport must use
            // the same usable height while the intro previews its movables.
            nextScreen.setBasePerspective();

            if (nextScreen instanceof ISeparateBackgroundScreen)
                ((ISeparateBackgroundScreen) nextScreen).drawWithoutBackground();
            else
                nextScreen.draw();

        }

        Game.game.window.transformations.clear();
        Game.game.window.loadPerspective();
        Drawing.drawing.statsHeight = previousStatsHeight;

        Game.game.window.clipMultiplier = 100;
        Game.game.window.clipDistMultiplier = 0.5;
    }

    @Override
    public void drawUI()
    {
        double frac = Math.min(1, ((lastTime - startTime - introTime) / introAnimationTime));
        double barOffset = (1 - frac) * 40;

        int previousStatsHeight = Drawing.drawing.statsHeight;
        int previewViewportY = 0;
        if (Game.game.window.absoluteHeight > 0)
            previewViewportY = (int) Math.round(previousStatsHeight * Game.game.window.frameBufferHeight / (double) Game.game.window.absoluteHeight);

        int previewViewportHeight = Math.max(1, Game.game.window.frameBufferHeight - previewViewportY);

        Drawing.drawing.statsHeight = previousStatsHeight;

        Game.game.window.interfaceCameraLogicalWidth = Drawing.drawing.baseInterfaceSizeX;
        Game.game.window.interfaceCameraLogicalHeight = Drawing.drawing.baseInterfaceSizeY;
        Game.game.window.interfaceCameraHeightOffset = previousStatsHeight;
        Game.game.window.interfaceCamera.resetToBounds(0, Game.game.window.interfaceCameraLogicalWidth, 0, Game.game.window.interfaceCameraLogicalHeight, Game.game.window.absoluteDepth);
        Game.game.window.interfaceCamera.setAspectRatio(Game.game.window.absoluteWidth / Math.max(1, Game.game.window.absoluteHeight - previousStatsHeight));
        Game.game.window.currentCamera = Game.game.window.interfaceCamera;
        Game.game.window.loadPerspective();
        Game.game.window.setViewport(0, previewViewportY, Game.game.window.frameBufferWidth, previewViewportHeight);

        // No terrain is drawn during the short intro delay, so provide the same
        // level-colored backdrop that the terrain will replace once it begins.
        if (lastTime - startTime < introTime || !Game.fancyTerrain)
        {
            Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue);
            Drawing.drawing.fillInterfaceViewportRect();
        }

        if (!(nextScreen instanceof ScreenGame))
        {
            nextScreen.setupLayoutParameters();
            zoomTranslation.z = (1 - frac) * 2;
            Game.game.window.transformations.add(zoomTranslation);
            Game.game.window.loadPerspective();
            nextScreen.drawUI();
            Game.game.window.transformations.clear();
            Game.game.window.loadPerspective();
        }

        Game.game.window.currentCamera = Game.game.window.screenCamera;
        Game.game.window.loadPerspective();
        Game.game.window.setViewport(0, 0, Game.game.window.frameBufferWidth, Game.game.window.frameBufferHeight);

        if (Drawing.drawing.enableStats)
        {
            Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue);
            Game.game.window.shapeRenderer.fillRect(0, Game.game.window.absoluteHeight - 40, Game.game.window.absoluteWidth, 40);
            Panel.panel.drawBar(barOffset);
        }

        Drawing.drawing.statsHeight = previousStatsHeight;
        Game.game.window.transformations.clear();
        // Panel draws the cursor after this method returns.  Leave the normal UI
        // camera and its reduced viewport active so its mouse coordinates match.
        Game.game.window.currentCamera = Game.game.window.interfaceCamera;
        Game.game.window.loadPerspective();
        Game.game.window.setViewport(0, previewViewportY, Game.game.window.frameBufferWidth, previewViewportHeight);
    }
}
