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
    public ScreenLevelEditor editor;
    public boolean musicInstruments = false;
    public InputBindingGroup triggerKeybind = null;

    public ScreenLevelEditorOverlay(Screen previous, ScreenLevelEditor editor)
    {
        this.allowClose = false;

        this.previous = previous;
        this.editor = editor;

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

        if (previous == editor)
        {
            editor.clickCooldown = 20;
            editor.paused = false;
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
        this.editor.updateMusic(this.musicInstruments);

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

            Game.screen = editor;
            editor.clickCooldown = 20;
            editor.paused = false;
        }
    }

    @Override
    public void setupLights()
    {
        this.editor.setupLights();
    }


    @Override
    public void draw()
    {
//        windowTitle = (this.editor.allowClose ? "" : "*");
        this.editor.draw();
    }

    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return editor.spawns;
    }

    @Override
    public double getOffsetX()
    {
        return editor.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        return editor.getOffsetY();
    }

    @Override
    public double getScale()
    {
        return editor.getScale();
    }

    @Override
    public void onAttemptClose()
    {
        Game.screen = new OverlayConfirmSave(Game.screen, this.editor);
    }
}
