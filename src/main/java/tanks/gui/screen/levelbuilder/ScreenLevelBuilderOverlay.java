package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.Screen;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public abstract class ScreenLevelBuilderOverlay extends Screen implements ILevelPreviewScreen
{
    public Screen previous;
    public ScreenLevelBuilder screenLevelBuilder;

    public ScreenLevelBuilderOverlay(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        this.previous = previous;
        this.screenLevelBuilder = screenLevelBuilder;

        this.music = previous.music;
        this.musicID = previous.musicID;

        this.enableMargins = false;
    }

    public void escape()
    {
        Game.screen = previous;

        if (previous instanceof ScreenLevelBuilderOverlay)
            ((ScreenLevelBuilderOverlay) previous).load();

        if (previous == screenLevelBuilder)
        {
            screenLevelBuilder.clickCooldown = 20;
            screenLevelBuilder.paused = false;
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

        if (Game.game.input.editorObjectMenu.isValid() && screenLevelBuilder.objectMenu)
        {
            Game.game.input.editorObjectMenu.invalidate();
            Game.screen = screenLevelBuilder;
            screenLevelBuilder.clickCooldown = 20;
            screenLevelBuilder.paused = false;
        }
    }

    @Override
    public void draw()
    {
        this.screenLevelBuilder.draw();
    }

    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return screenLevelBuilder.spawns;
    }

    @Override
    public double getOffsetX()
    {
        return screenLevelBuilder.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        return screenLevelBuilder.getOffsetY();
    }

    @Override
    public double getScale()
    {
        return screenLevelBuilder.getScale();
    }
}
