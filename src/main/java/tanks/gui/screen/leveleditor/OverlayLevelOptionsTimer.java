package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsTimer extends ScreenLevelEditorOverlay
{
    public TextBox minutes;
    public TextBox seconds;

    public Button back = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Back", this::escape
    );

    public OverlayLevelOptionsTimer(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        minutes = new TextBox(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Minutes", () ->
        {
            try
            {
                screenLevelEditor.level.timer = Integer.parseInt(minutes.inputText) * 6000 + Integer.parseInt(seconds.inputText) * 100;
            }
            catch (Exception e)
            {
                minutes.inputText = "" + screenLevelEditor.level.timer / 6000;
            }
        }
                , (int) (screenLevelEditor.level.timer / 6000) + "", "Set minutes and seconds to 0---to disable the time limit");

        minutes.allowLetters = false;
        minutes.allowSpaces = false;
        minutes.maxChars = 2;
        minutes.minValue = 0;
        minutes.maxValue = 59;
        minutes.checkMaxValue = true;
        minutes.checkMinValue = true;

        seconds = new TextBox(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Seconds", () ->
        {
            try
            {
                screenLevelEditor.level.timer = Integer.parseInt(minutes.inputText) * 6000 + Integer.parseInt(seconds.inputText) * 100;
            }
            catch (Exception e)
            {
                seconds.inputText = "" + (screenLevelEditor.level.timer % 6000) / 100;
            }
        }
                , (int)(screenLevelEditor.level.timer % 6000) / 100 + "", "Set minutes and seconds to 0---to disable the time limit");

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
        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Time limit");
    }
}
