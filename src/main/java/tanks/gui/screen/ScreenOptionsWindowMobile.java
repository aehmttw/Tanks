package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsWindowMobile extends Screen
{
    public static final String infoBarText = "Info bar: ";

    Button showStats = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
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

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions());

    public ScreenOptionsWindowMobile()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Drawing.drawing.enableStats)
            showStats.setText(infoBarText, ScreenOptions.onText);
        else
            showStats.setText(infoBarText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        back.update();
        showStats.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        showStats.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Interface options");
    }
}
