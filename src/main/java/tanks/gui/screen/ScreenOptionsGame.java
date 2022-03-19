package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsGame extends Screen
{
    public static final String autostartText = "Autostart: ";
    public static final String fullStatsText = "Stats animations: ";

    Button autostart = new Button(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
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

    Button fullStats = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
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

    Button transparentTallTiles = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable() {
        @Override
        public void run() {
            Game.transparentTallTiles = !Game.transparentTallTiles;

            transparentTallTiles.setText("Transparent tiles: " + (Game.transparentTallTiles ? ScreenOptions.onText : ScreenOptions.offText));
        }
    }, "If enabled, any obstacles that are---tall enough so a tank can---fit under are rendered transparent.");

    Button speedrunOptions = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Speedrunning options", () -> Game.screen = new ScreenOptionsSpeedrun());

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

        transparentTallTiles.setText("Transparent tiles: " + (Game.transparentTallTiles ? ScreenOptions.onText : ScreenOptions.offText));
    }

    @Override
    public void update()
    {
        back.update();
        transparentTallTiles.update();
        speedrunOptions.update();
        autostart.update();
        fullStats.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        transparentTallTiles.draw();
        speedrunOptions.draw();
        fullStats.draw();
        autostart.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Game options");
    }
}
