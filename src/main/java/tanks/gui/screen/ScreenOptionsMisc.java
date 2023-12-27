package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsMisc extends Screen
{
    public static final String autostartText = "Autostart: ";
    public static final String fullStatsText = "Stats animations: ";
    public static final String previewCrusadesText = "Crusade preview: ";

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

    Button previewCrusades = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.previewCrusades = !Game.previewCrusades;

            if (Game.previewCrusades)
                previewCrusades.setText(previewCrusadesText, ScreenOptions.onText);
            else
                previewCrusades.setText(previewCrusadesText, ScreenOptions.offText);
        }
    },
            "When enabled, the backgrounds of---the crusade preview and stats---screens display an animation of all---the crusade levels scrolling by.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions());

    public ScreenOptionsMisc()
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

        if (Game.previewCrusades)
            previewCrusades.setText(previewCrusadesText, ScreenOptions.onText);
        else
            previewCrusades.setText(previewCrusadesText, ScreenOptions.offText);

        if (Game.framework == Game.Framework.libgdx)
            previewCrusades.enabled = false;
    }

    @Override
    public void update()
    {
        back.update();
        autostart.update();
        fullStats.update();
        previewCrusades.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        previewCrusades.draw();
        fullStats.draw();
        autostart.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Miscellaneous options");
    }
}
