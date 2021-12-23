package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsSpeedrun extends Screen
{
    public static final String deterministicText = "Deterministic: ";
    public static final String timerText = "Timer: ";

    Button timer = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showSpeedrunTimer = !Game.showSpeedrunTimer;

            if (Game.showSpeedrunTimer)
                timer.setText(timerText, ScreenOptions.onText);
            else
                timer.setText(timerText, ScreenOptions.offText);
        }
    },
            "When enabled, time spent---in the current level attempt---and crusade will be displayed");

    Button deterministic = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.deterministicMode = !Game.deterministicMode;

            if (Game.deterministicMode)
                deterministic.setText(deterministicText, ScreenOptions.onText);
            else
                deterministic.setText(deterministicText, ScreenOptions.offText);
        }
    },
            "Deterministic mode changes the random number---generation to be fixed based on a seed, and---the game speed to be locked and independent---of framerate." +
                    "------This is useful for fair speedruns but may---provide for a less smooth experience.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptionsGame()
    );

    public ScreenOptionsSpeedrun()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.showSpeedrunTimer)
            timer.setText(timerText, ScreenOptions.onText);
        else
            timer.setText(timerText, ScreenOptions.offText);

        if (Game.deterministicMode)
            deterministic.setText(deterministicText, ScreenOptions.onText);
        else
            deterministic.setText(deterministicText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        back.update();
        timer.update();
        deterministic.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        deterministic.draw();
        timer.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Speedrunning options");
    }
}
