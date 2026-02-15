package tanks.gui.screen;

import tanks.Drawing;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenImportingFiles extends Screen
{
    public Screen prevScreen;
    public long start = System.currentTimeMillis();
    public boolean musicStarted = false;

    public static int numberCompleted = 0;
    public static int numberTotal = 0;
    public static volatile boolean cancelable = false;
    public static volatile boolean canceled = false;

    public Button cancel = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Cancel", () ->
    {
       if (cancelable)
           canceled = true;
    });

    public ScreenImportingFiles(Screen prev)
    {
        this.prevScreen = prev;
        this.music = this.prevScreen.music;
        this.musicID = this.prevScreen.musicID;
    }

    @Override
    public void update()
    {
        if (System.currentTimeMillis() - start > 500 && !musicStarted)
        {
            this.music = "waiting_music_2.ogg";
            musicStarted = true;
            Panel.forceRefreshMusic = true;
        }

        cancel.enabled = cancelable;
        cancel.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        cancel.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 30, "Importing files (%d / %d)", numberCompleted, numberTotal);
        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY + this.objYSpace / 2, 500, 5);
        Drawing.drawing.setColor(255, 127, 80);
        Drawing.drawing.fillInterfaceProgressRect(this.centerX, this.centerY + 30, 500, 5, numberCompleted * 1.0 / numberTotal);
    }
}
