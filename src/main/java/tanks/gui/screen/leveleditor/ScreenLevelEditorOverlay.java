package tanks.gui.screen.leveleditor;

import tanks.Game;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.Screen;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public abstract class ScreenLevelEditorOverlay extends Screen implements ILevelPreviewScreen
{
    public Screen previous;
    public ScreenLevelEditor screenLevelEditor;
    public boolean musicInstruments = false;
    public InputBindingGroup triggerKeybind = null;

    public ScreenLevelEditorOverlay(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        this.allowClose = false;

        this.previous = previous;
        this.screenLevelEditor = screenLevelEditor;

        this.music = previous.music;
        this.musicID = previous.musicID;

        this.enableMargins = false;

        if (previous instanceof ScreenLevelEditorOverlay)
            this.musicInstruments = ((ScreenLevelEditorOverlay) previous).musicInstruments;
    }

    public void escape()
    {
        this.onExitScreen();
        Game.screen = previous;

        if (previous instanceof ScreenLevelEditorOverlay)
            ((ScreenLevelEditorOverlay) previous).load();

        if (previous == screenLevelEditor)
        {
            screenLevelEditor.clickCooldown = 20;
            screenLevelEditor.paused = false;
        }
    }

    public void onExitScreen()
    {

    }

    public void load()
    {

    }

    @Override
    public void update()
    {
        Game.recomputeHeightGrid();
        this.screenLevelEditor.updateMusic(this.musicInstruments);

        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            this.escape();
        }

        if (Game.game.input.editorObjectMenu.isValid() || (triggerKeybind != null && triggerKeybind.isValid()))
        {
            this.onExitScreen();
            Game.game.input.editorObjectMenu.invalidate();

            if (triggerKeybind != null)
                triggerKeybind.invalidate();

            Game.screen = screenLevelEditor;
            screenLevelEditor.clickCooldown = 20;
            screenLevelEditor.paused = false;
        }
    }

    @Override
    public void setupLights()
    {
        this.screenLevelEditor.setupLights();
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

    @Override
    public void onAttemptClose()
    {
        Game.screen = new OverlayConfirmSave(Game.screen, this.screenLevelEditor);
    }
}
