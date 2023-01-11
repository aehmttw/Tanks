package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenOptionsGame extends Screen
{
    public static final String autostartText = "Autostart: ";
    public static final String fullStatsText = "Stats animations: ";
    public static final String pauseText = "Pause on defocus: ";

    Button autostart = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.autostart = !Game.autostart;

            if (Game.autostart)
                autostart.setText(autostartText, ScreenOptions.onText);
            else
                autostart.setText(autostartText, ScreenOptions.offText);
        }
    },
            "When enabled, levels will---start playing automatically---4 seconds after they are---loaded (if the play button---isn't clicked earlier)");

    Button fullStats = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fullStats = !Game.fullStats;

            if (Game.fullStats)
                fullStats.setText(fullStatsText, ScreenOptions.onText);
            else
                fullStats.setText(fullStatsText, ScreenOptions.offText);
        }
    },
            "When off, skips directly to the summary tab---of the crusade end stats screen");

    Button pauseOnDefocus = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Panel.pauseOnDefocus = !Panel.pauseOnDefocus;

            if (Panel.pauseOnDefocus)
                pauseOnDefocus.setText(pauseText, ScreenOptions.onText);
            else
                pauseOnDefocus.setText(pauseText, ScreenOptions.offText);
        }
    });

    Button speedrunOptions = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Speedrunning options", () -> Game.screen = new ScreenOptionsSpeedrun());

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions());

    public ScreenOptionsGame()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.autostart)
            autostart.setText(autostartText, ScreenOptions.onText);
        else
            autostart.setText(autostartText, ScreenOptions.offText);

        if (Game.fullStats)
            fullStats.setText(fullStatsText, ScreenOptions.onText);
        else
            fullStats.setText(fullStatsText, ScreenOptions.offText);

        if (Panel.pauseOnDefocus)
            pauseOnDefocus.setText(pauseText, ScreenOptions.onText);
        else
            pauseOnDefocus.setText(pauseText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        back.update();
        speedrunOptions.update();
        pauseOnDefocus.update();
        fullStats.update();
        autostart.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        speedrunOptions.draw();
        pauseOnDefocus.draw();
        fullStats.draw();
        autostart.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Game options");
    }
}
