package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.screen.Screen;

public class OverlayConfirmSave extends ScreenLevelEditorOverlay
{
    public double opacity = 100;

    Button saveExit = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Save and exit", () ->
    {
        screenLevelEditor.save();

        System.exit(0);
    });

    Button noSaveExit = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Exit without saving", () ->
    {
        System.exit(0);
    });


    Button cancel = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Don't exit", this::escape);


    public OverlayConfirmSave(Screen previous, ScreenLevelEditor s)
    {
        super(previous, s);
        Drawing.drawing.playSound("timer.ogg");
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

        saveExit.update();
        noSaveExit.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(255, 127, 0, this.opacity);
        this.opacity = Math.max(0, this.opacity - Panel.frameFrequency * 2);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

        Drawing.drawing.setColor(this.screenLevelEditor.fontBrightness, this.screenLevelEditor.fontBrightness, this.screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Save before exiting?");

        saveExit.draw();
        noSaveExit.draw();
        cancel.draw();
    }

    @Override
    public void onAttemptClose()
    {
        Drawing.drawing.playSound("timer.ogg");
        opacity = 100;
    }
}