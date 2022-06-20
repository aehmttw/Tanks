package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenOptionsShadows extends Screen
{
    public static final String shadowsText = "Fancy lighting: ";

    Button shadows = new Button(this.centerX, this.centerY - this.objYSpace * 0.75, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.shadowsEnabled = !Game.shadowsEnabled;

            if (Game.shadowsEnabled)
                shadows.setText(shadowsText + ScreenOptions.onText);
            else
                shadows.setText(shadowsText + ScreenOptions.offText);
        }
    },
            "Fancy lighting enables shadows and---allows for custom lighting in levels------Fancy lighting is quite graphically intense---and may significantly reduce framerate");


    TextBoxSlider shadowQuality = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 0.75, this.objWidth, this.objHeight, "Shadow quality", new Runnable()
    {
        @Override
        public void run()
        {
            if (shadowQuality.inputText.length() <= 0)
                shadowQuality.inputText = shadowQuality.previousInputText;

            Game.shadowQuality = Integer.parseInt(shadowQuality.inputText);
        }
    }
            , Game.shadowQuality, 1, 20, 1);

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptionsGraphics()
    );

    public ScreenOptionsShadows()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        shadowQuality.allowLetters = false;
        shadowQuality.allowSpaces = false;
        shadowQuality.maxChars = 2;
        shadowQuality.checkMaxValue = true;
        shadowQuality.checkMinValue = true;
        shadowQuality.integer = true;

        shadowQuality.r1 = 210;
        shadowQuality.g1 = 210;
        shadowQuality.b1 = 210;

        if (Game.shadowsEnabled)
            shadows.setText(shadowsText, ScreenOptions.onText);
        else
            shadows.setText(shadowsText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        shadowQuality.update();
        shadows.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        shadowQuality.draw();
        shadows.draw();
        back.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Lighting options");
    }
}
