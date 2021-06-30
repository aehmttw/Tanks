package tanks.gui.screen.levelbuilder;

import tanks.Game;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.Screen;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public abstract class ScreenLevelBuilderOverlay extends Screen implements ILevelPreviewScreen
{
    public Screen previous;
    public ScreenLevelEditor screenLevelEditor;

    public ScreenLevelBuilderOverlay(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        this.previous = previous;
        this.screenLevelEditor = screenLevelEditor;

        this.music = previous.music;
        this.musicID = previous.musicID;

        this.enableMargins = false;
    }

    public void escape()
    {
        Game.screen = previous;

        if (previous instanceof ScreenLevelBuilderOverlay)
            ((ScreenLevelBuilderOverlay) previous).load();

        if (previous == screenLevelEditor)
        {
            screenLevelEditor.clickCooldown = 20;
            screenLevelEditor.paused = false;
        }
    }

    public void load()
    {

    }

    @Override
    public void update()
    {
        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            this.escape();
        }

        if (Game.game.input.editorObjectMenu.isValid() && screenLevelEditor.objectMenu)
        {
            Game.game.input.editorObjectMenu.invalidate();
            Game.screen = screenLevelEditor;
            screenLevelEditor.clickCooldown = 20;
            screenLevelEditor.paused = false;
        }
    }

    @Override
    public void draw()
    {
        this.screenLevelEditor.draw();
    }

    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return screenLevelEditor.spawns;
    }

    @Override
    public double getOffsetX()
    {
        return screenLevelEditor.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        return screenLevelEditor.getOffsetY();
    }

    @Override
    public double getScale()
    {
        return screenLevelEditor.getScale();
    }
}
