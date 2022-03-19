package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenOptionsEffects extends Screen
{
    public static final String effectsText = "Particle effects: ";

    Button effects = new Button(this.centerX, this.centerY - this.objYSpace * 0.75, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.effectsEnabled = !Game.effectsEnabled;

            if (Game.effectsEnabled)
                effects.setText(effectsText, ScreenOptions.onText);
            else
                effects.setText(effectsText, ScreenOptions.offText);
        }
    },
            "Particle effects may significantly---impact performance");


    TextBoxSlider effectMultiplier = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 0.75, this.objWidth, this.objHeight, "Particle percentage", new Runnable()
    {
        @Override
        public void run()
        {
            if (effectMultiplier.inputText.length() <= 0)
                effectMultiplier.inputText = effectMultiplier.previousInputText;

            Game.effectMultiplier = Integer.parseInt(effectMultiplier.inputText) / 100.0;
        }
    }
            , (int) Math.round(Game.effectMultiplier * 100), 10, 100, 10);

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptionsGraphics()
    );

    public ScreenOptionsEffects()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        effectMultiplier.allowLetters = false;
        effectMultiplier.allowSpaces = false;
        effectMultiplier.maxChars = 3;
        effectMultiplier.max = 100;
        effectMultiplier.checkMaxValue = true;
        effectMultiplier.checkMinValue = true;
        effectMultiplier.integer = true;

        effectMultiplier.r1 = 210;
        effectMultiplier.g1 = 210;
        effectMultiplier.b1 = 210;

        if (Game.effectsEnabled)
            effects.setText(effectsText, ScreenOptions.onText);
        else
            effects.setText(effectsText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        effectMultiplier.update();
        effects.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        effectMultiplier.draw();
        effects.draw();
        back.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Effect options");
    }
}
