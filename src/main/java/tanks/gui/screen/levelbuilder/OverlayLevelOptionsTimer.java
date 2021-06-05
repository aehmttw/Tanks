package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsTimer extends ScreenLevelBuilderOverlay
{
    public TextBox minutes;
    public TextBox seconds;

    public Button back = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public OverlayLevelOptionsTimer(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

        minutes = new TextBox(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Minutes", new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    screenLevelBuilder.level.timer = Integer.parseInt(minutes.inputText) * 6000 + Integer.parseInt(seconds.inputText) * 100;
                }
                catch (Exception e)
                {
                    minutes.inputText = "" + screenLevelBuilder.level.timer / 6000;
                }
            }

        }
                , (int) (screenLevelBuilder.level.timer / 6000) + "", "Set minutes and seconds to 0---to disable the time limit");

        minutes.allowLetters = false;
        minutes.allowSpaces = false;
        minutes.maxChars = 2;
        minutes.minValue = 0;
        minutes.maxValue = 59;
        minutes.checkMaxValue = true;
        minutes.checkMinValue = true;

        seconds = new TextBox(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Seconds", new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    screenLevelBuilder.level.timer = Integer.parseInt(minutes.inputText) * 6000 + Integer.parseInt(seconds.inputText) * 100;
                }
                catch (Exception e)
                {
                    seconds.inputText = "" + (screenLevelBuilder.level.timer % 6000) / 100;
                }
            }
        }
                , (int)(screenLevelBuilder.level.timer % 6000) / 100 + "", "Set minutes and seconds to 0---to disable the time limit");

        seconds.allowLetters = false;
        seconds.allowSpaces = false;
        seconds.maxChars = 2;
        seconds.minValue = 0;
        seconds.maxValue = 59;
        seconds.checkMaxValue = true;
        seconds.checkMinValue = true;
    }

    public void update()
    {
        this.minutes.update();
        this.seconds.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        this.seconds.draw();
        this.minutes.draw();
        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Time limit");
    }
}
