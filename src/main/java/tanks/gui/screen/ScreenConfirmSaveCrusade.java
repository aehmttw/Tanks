package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenConfirmSaveCrusade extends Screen
{
    public ScreenCrusadeEditor screenCrusadeEditor;
    public Screen previous;

    Button saveExit = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Save and exit", () ->
    {
        screenCrusadeEditor.save();

        System.exit(0);
    });

    Button noSaveExit = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Exit without saving", () ->
    {
        System.exit(0);
    });


    Button cancel = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Don't exit", () ->
    {
        Game.screen = previous;
    });


    public ScreenConfirmSaveCrusade(Screen previous, ScreenCrusadeEditor s)
    {
        this.previous = previous;
        this.screenCrusadeEditor = s;
        this.allowClose = false;

        this.music = previous.music;
        this.musicID = previous.musicID;
    }

    @Override
    public void update()
    {
        saveExit.update();
        noSaveExit.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
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