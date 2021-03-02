package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsGame extends Screen
{
    public static final String autostartText = "Autostart: ";
    public static final String timerText = "Timer: ";

    Button autostart = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.autostart = !Game.autostart;

            if (Game.autostart)
                autostart.text = autostartText + ScreenOptions.onText;
            else
                autostart.text = autostartText + ScreenOptions.offText;
        }
    },
            "When enabled, levels will---start playing automatically---4 seconds after they are---loaded (if the play button---isn't clicked earlier)");

    Button timer = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showSpeedrunTimer = !Game.showSpeedrunTimer;

            if (Game.showSpeedrunTimer)
                timer.text = timerText + ScreenOptions.onText;
            else
                timer.text = timerText + ScreenOptions.offText;
        }
    },
            "When enabled, time spent---in the current level attempt---and crusade will be displayed");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    }
    );

    public ScreenOptionsGame()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";

        if (Game.autostart)
            autostart.text = autostartText + ScreenOptions.onText;
        else
            autostart.text = autostartText + ScreenOptions.offText;

        if (Game.showSpeedrunTimer)
            timer.text = timerText + ScreenOptions.onText;
        else
            timer.text = timerText + ScreenOptions.offText;
    }

    @Override
    public void update()
    {
        back.update();
        timer.update();
        autostart.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        timer.draw();
        autostart.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Game options");
    }
}
