package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsInterface extends Screen
{
    public static final String infoBarText = "Info bar: ";
    public static final String warnText = "Warn before exit: ";

    Button showStats = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Drawing.drawing.showStats(!Drawing.drawing.enableStats);

            if (Drawing.drawing.enableStats)
                showStats.setText(infoBarText, ScreenOptions.onText);
            else
                showStats.setText(infoBarText, ScreenOptions.offText);
        }
    },
            "Shows the following information---" +
                    "at the bottom of the screen:---" +
                    "---" +
                    "Game version---" +
                    "Framerate---" +
                    "Network latency (if in a party)---" +
                    "Memory usage");

    Button confirmClose = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.warnBeforeClosing = !Game.warnBeforeClosing;

            if (Game.warnBeforeClosing)
                confirmClose.setText(warnText, ScreenOptions.onText);
            else
                confirmClose.setText(warnText, ScreenOptions.offText);
        }
    },
            "Warn before closing the game---while in an editor");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
    );

    public ScreenOptionsInterface()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Drawing.drawing.enableStats)
            showStats.setText(infoBarText, ScreenOptions.onText);
        else
            showStats.setText(infoBarText, ScreenOptions.offText);

        if (Game.warnBeforeClosing)
            confirmClose.setText(warnText, ScreenOptions.onText);
        else
            confirmClose.setText(warnText, ScreenOptions.offText);

        if (Game.framework == Game.Framework.libgdx)
        {
            confirmClose.enabled = false;
            confirmClose.setText(warnText, ScreenOptions.offText);
        }
    }

    @Override
    public void update()
    {
        back.update();
        showStats.update();
        confirmClose.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        confirmClose.draw();
        showStats.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Interface options");
    }
}
