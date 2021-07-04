package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsSpeedrun extends Screen
{
    public static final String useSeedText = "Use seed: ";
    public static final String fixedFrameFrequencyText = "Fixed frames: ";

    Button useSeed = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.useSeed = !Game.useSeed;

            if (Game.useSeed)
                useSeed.text = useSeedText + ScreenOptions.onText;
            else
                useSeed.text = useSeedText + ScreenOptions.offText;
        }
    }, "Use the seed below for Tank AI rng");

    TextBox seedEntry = new TextBox(this.centerX, this.centerY, this.objWidth, this.objHeight, "Seed", new Runnable() {
        @Override
        public void run() {
            Game.seed = Integer.parseInt(seedEntry.inputText);
        }
    }, "0", "Seed for random number generator");

    Button fixedFrames = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fixedFrameFrequency = !Game.fixedFrameFrequency;

            if (Game.fixedFrameFrequency)
                fixedFrames.text = fixedFrameFrequencyText + ScreenOptions.onText;
            else
                fixedFrames.text = fixedFrameFrequencyText + ScreenOptions.offText;
        }
    }, "Make the frame frequency fixed---so that Tank AI is consistent.---Can cause timing issues.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptionsGame();
        }
    });

    public ScreenOptionsSpeedrun()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.useSeed)
            useSeed.text = useSeedText + ScreenOptions.onText;
        else
            useSeed.text = useSeedText + ScreenOptions.offText;

        if (Game.fixedFrameFrequency)
            fixedFrames.text = fixedFrameFrequencyText + ScreenOptions.onText;
        else
            fixedFrames.text = fixedFrameFrequencyText + ScreenOptions.offText;

        seedEntry.inputText = Long.toString(Game.seed);

        seedEntry.allowLetters = false;
        seedEntry.allowDots = false;
    }

    @Override
    public void update()
    {
        back.update();
        fixedFrames.update();
        seedEntry.update();
        useSeed.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        fixedFrames.draw();
        seedEntry.draw();
        useSeed.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Speedrunning options");
    }
}
