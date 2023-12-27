package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.TextBoxSlider;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsColor extends ScreenLevelEditorOverlay
{
    public TextBoxSlider colorRed;
    public TextBoxSlider colorGreen;
    public TextBoxSlider colorBlue;
    public TextBox colorVarRed;
    public TextBox colorVarGreen;
    public TextBox colorVarBlue;

    public OverlayLevelOptionsColor(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        colorRed = new TextBoxSlider(this.centerX - objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Red", () ->
        {
            if (colorRed.inputText.length() <= 0)
                colorRed.inputText = colorRed.previousInputText;

            screenLevelEditor.level.colorR = Integer.parseInt(colorRed.inputText);

            colorVarRed.maxValue = 255 - screenLevelEditor.level.colorR;
            colorVarRed.performValueCheck();

            screenLevelEditor.level.colorVarR = Integer.parseInt(colorVarRed.inputText);
            screenLevelEditor.level.reloadTiles();
            Drawing.drawing.terrainRenderer.reset();
        }
                , screenLevelEditor.level.colorR, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Green", () ->
        {
            if (colorGreen.inputText.length() <= 0)
                colorGreen.inputText = colorGreen.previousInputText;

            screenLevelEditor.level.colorG = Integer.parseInt(colorGreen.inputText);

            colorVarGreen.maxValue = 255 - screenLevelEditor.level.colorG;
            colorVarGreen.performValueCheck();

            screenLevelEditor.level.colorVarG = Integer.parseInt(colorVarGreen.inputText);
            screenLevelEditor.level.reloadTiles();
            Drawing.drawing.terrainRenderer.reset();
        }
                , screenLevelEditor.level.colorG, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Blue", () ->
        {
            if (colorBlue.inputText.length() <= 0)
                colorBlue.inputText = colorBlue.previousInputText;

            screenLevelEditor.level.colorB = Integer.parseInt(colorBlue.inputText);

            colorVarBlue.maxValue = 255 - screenLevelEditor.level.colorB;
            colorVarBlue.performValueCheck();

            screenLevelEditor.level.colorVarB = Integer.parseInt(colorVarBlue.inputText);
            screenLevelEditor.level.reloadTiles();
            Drawing.drawing.terrainRenderer.reset();
        }
                , screenLevelEditor.level.colorB, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;

        colorVarRed = new TextBox(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Red noise", () ->
        {
            if (colorVarRed.inputText.length() <= 0)
                colorVarRed.inputText = colorVarRed.previousInputText;

            screenLevelEditor.level.colorVarR = Integer.parseInt(colorVarRed.inputText);

            screenLevelEditor.level.reloadTiles();
            Drawing.drawing.terrainRenderer.reset();
        }
                , screenLevelEditor.level.colorVarR + "");

        colorVarRed.allowLetters = false;
        colorVarRed.allowSpaces = false;
        colorVarRed.maxChars = 3;
        colorVarRed.checkMaxValue = true;

        colorVarGreen = new TextBox(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Green noise", () ->
        {
            if (colorVarGreen.inputText.length() <= 0)
                colorVarGreen.inputText = colorVarGreen.previousInputText;

            screenLevelEditor.level.colorVarG = Integer.parseInt(colorVarGreen.inputText);

            screenLevelEditor.level.reloadTiles();
            Drawing.drawing.terrainRenderer.reset();
        }
                , screenLevelEditor.level.colorVarG + "");

        colorVarGreen.allowLetters = false;
        colorVarGreen.allowSpaces = false;
        colorVarGreen.maxChars = 3;
        colorVarGreen.checkMaxValue = true;

        colorVarBlue = new TextBox(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Blue noise", () ->
        {
            if (colorVarBlue.inputText.length() <= 0)
                colorVarBlue.inputText = colorVarBlue.previousInputText;

            screenLevelEditor.level.colorVarB = Integer.parseInt(colorVarBlue.inputText);

            screenLevelEditor.level.reloadTiles();
            Drawing.drawing.terrainRenderer.reset();
        }
                , screenLevelEditor.level.colorVarB + "");

        colorVarBlue.allowLetters = false;
        colorVarBlue.allowSpaces = false;
        colorVarBlue.maxChars = 3;
        colorVarBlue.checkMaxValue = true;

        colorVarRed.maxValue = 255 - screenLevelEditor.level.colorR;
        colorVarGreen.maxValue = 255 - screenLevelEditor.level.colorG;
        colorVarBlue.maxValue = 255 - screenLevelEditor.level.colorB;
    }

    public Button back = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 3), this.objWidth, this.objHeight, "Back", this::escape);

    public void update()
    {
        this.colorRed.update();
        this.colorGreen.update();
        this.colorBlue.update();
        this.colorVarRed.update();
        this.colorVarGreen.update();
        this.colorVarBlue.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

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

        this.colorBlue.draw();
        this.colorGreen.draw();
        this.colorRed.draw();
        this.colorVarBlue.draw();
        this.colorVarGreen.draw();
        this.colorVarRed.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Background colors");
        this.back.draw();
    }
}
