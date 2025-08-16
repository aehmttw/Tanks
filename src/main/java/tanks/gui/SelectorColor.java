package tanks.gui;

import basewindow.Color;
import tanks.IDrawable;

public class SelectorColor implements IDrawable, ITrigger
{
    public TextBoxSlider colorRed;
    public TextBoxSlider colorGreen;
    public TextBoxSlider colorBlue;
    public TextBoxSlider colorAlpha;

    public double spacing;
    public boolean enableAlpha;
    public Color color;

    public SelectorColor(double x, double y, double sizeX, double sizeY, String name, double space, Color toEdit, boolean alpha)
    {
        this.color = toEdit;
        this.spacing = space;
        this.enableAlpha = alpha;

        colorRed = new TextBoxSlider(x, y, sizeX, sizeY, name.isEmpty() ? "Red" : (name + " red"), () ->
        {
            if (colorRed.inputText.length() <= 0)
                colorRed.inputText = colorRed.previousInputText;

            color.red = Double.parseDouble(colorRed.inputText);
        }
                , (int) color.red, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(x, y + space, sizeX, sizeY, name.isEmpty() ? "Green" : (name + " green"), () ->
        {
            if (colorGreen.inputText.length() <= 0)
                colorGreen.inputText = colorGreen.previousInputText;

            color.green = Double.parseDouble(colorGreen.inputText);
        }
                , (int) color.green, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(x, y + space * 2, sizeX, sizeY, name.isEmpty() ? "Blue" : (name + " blue"), () ->
        {
            if (colorBlue.inputText.length() <= 0)
                colorBlue.inputText = colorBlue.previousInputText;

            color.blue = Double.parseDouble(colorBlue.inputText);
        }
                , (int) color.blue, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;

        colorAlpha = new TextBoxSlider(x, y + space * 3, sizeX, sizeY, name.isEmpty() ? "Opacity" : (name + " opacity"), () ->
        {
            if (colorAlpha.inputText.length() <= 0)
                colorAlpha.inputText = colorAlpha.previousInputText;

            color.alpha = Double.parseDouble(colorAlpha.inputText);
        }
                , (int) color.alpha, 0, 255, 1);

        colorAlpha.allowLetters = false;
        colorAlpha.allowSpaces = false;
        colorAlpha.maxChars = 3;
        colorAlpha.maxValue = 255;
        colorAlpha.checkMaxValue = true;
        colorAlpha.integer = true;
    }

    public void update()
    {
        this.colorRed.update();
        this.colorGreen.update();
        this.colorBlue.update();
        this.color.red = this.colorRed.value;
        this.color.green = this.colorGreen.value;
        this.color.blue = this.colorBlue.value;

        if (enableAlpha)
        {
            this.colorAlpha.update();
            this.color.alpha = this.colorAlpha.value;
        }
    }

    public void draw()
    {
        colorRed.r1 = 0;
        colorRed.r2 = 255;
        colorRed.g1 = colorGreen.value;
        colorRed.g2 = colorGreen.value;
        colorRed.b1 = colorBlue.value;
        colorRed.b2 = colorBlue.value;

        colorGreen.r1 = colorRed.value;
        colorGreen.r2 = colorRed.value;
        colorGreen.g1 = 0;
        colorGreen.g2 = 255;
        colorGreen.b1 = colorBlue.value;
        colorGreen.b2 = colorBlue.value;

        colorBlue.r1 = colorRed.value;
        colorBlue.r2 = colorRed.value;
        colorBlue.g1 = colorGreen.value;
        colorBlue.g2 = colorGreen.value;
        colorBlue.b1 = 0;
        colorBlue.b2 = 255;

        colorAlpha.r1 = 127;
        colorAlpha.g1 = 127;
        colorAlpha.b1 = 127;

        this.colorBlue.draw();
        this.colorGreen.draw();
        this.colorRed.draw();

        if (enableAlpha)
            this.colorAlpha.draw();
    }

    @Override
    public void setPosition(double x, double y)
    {
        this.colorRed.posX = x;
        this.colorGreen.posX = x;
        this.colorBlue.posX = x;
        this.colorAlpha.posX = x;

        this.colorRed.posY = y;
        this.colorGreen.posY = y + spacing;
        this.colorBlue.posY = y + 2 * spacing;
        this.colorAlpha.posY = y + 3 * spacing;
    }

    @Override
    public double getPositionX()
    {
        return this.colorRed.posX;
    }

    @Override
    public double getPositionY()
    {
        return this.colorRed.posY;
    }

    @Override
    public int getSize()
    {
        return this.enableAlpha ? 4 : 3;
    }

    public void updateColors()
    {
        this.colorRed.value = (int) this.color.red;
        this.colorGreen.value = (int) this.color.green;
        this.colorBlue.value = (int) this.color.blue;
        this.colorAlpha.value = (int) this.color.alpha;

        this.colorRed.inputText = "" + (int) this.colorRed.value;
        this.colorGreen.inputText = "" + (int) this.colorGreen.value;
        this.colorBlue.inputText = "" + (int) this.colorBlue.value;
        this.colorAlpha.inputText = "" + (int) this.colorAlpha.value;
    }
}
