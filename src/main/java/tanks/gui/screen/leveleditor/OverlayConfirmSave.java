package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;

public class OverlayConfirmSave extends ScreenLevelEditorOverlay
{
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
        super.update();

        saveExit.update();
        noSaveExit.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        super.draw();

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

    }
}