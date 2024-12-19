package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.TextBoxSlider;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.Screen;
import tanks.gui.screen.leveleditor.selector.SelectorColor;
import tanks.gui.screen.leveleditor.selector.SelectorNumber;

public class OverlaySelectColor extends ScreenLevelEditorOverlay
{
    public SelectorColor selectorColor;

    public TextBoxSlider colorRed;
    public TextBoxSlider colorGreen;
    public TextBoxSlider colorBlue;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, 350, 40, "Done", this::escape);

    public OverlaySelectColor(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorColor selector)
    {
        super(previous, screenLevelEditor);

        this.selectorColor = selector;
        int initColor = ((Number) this.selectorColor.getMetadata(screenLevelEditor.mousePlaceable)).intValue();

        colorRed = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Red", () ->
        {
            if (colorRed.inputText.length() <= 0)
                colorRed.inputText = colorRed.previousInputText;

            int color = ((Number) this.selectorColor.getMetadata(screenLevelEditor.mousePlaceable)).intValue();
            int r = Integer.parseInt(colorRed.inputText);
            int g = (color / (256)) % 256;
            int b = color % 256;
            this.selectorColor.setMetadata(screenLevelEditor, screenLevelEditor.mousePlaceable, r * 256 * 256 + g * 256 + b);
        }
                , (initColor / (256 * 256)) % 256, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Green", () ->
        {
            if (colorGreen.inputText.length() <= 0)
                colorGreen.inputText = colorGreen.previousInputText;

            int color = ((Number) this.selectorColor.getMetadata(screenLevelEditor.mousePlaceable)).intValue();
            int r = (color / (256 * 256)) % 256;
            int g = Integer.parseInt(colorGreen.inputText);
            int b = color % 256;
            this.selectorColor.setMetadata(screenLevelEditor, screenLevelEditor.mousePlaceable, r * 256 * 256 + g * 256 + b);
        }
                , (initColor / 256) % 256, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Blue", () ->
        {
            if (colorBlue.inputText.length() <= 0)
                colorBlue.inputText = colorBlue.previousInputText;

            int color = ((Number) this.selectorColor.getMetadata(screenLevelEditor.mousePlaceable)).intValue();
            int r = (color / (256 * 256)) % 256;
            int g = (color / (256)) % 256;
            int b = Integer.parseInt(colorBlue.inputText);
            this.selectorColor.setMetadata(screenLevelEditor, screenLevelEditor.mousePlaceable, r * 256 * 256 + g * 256 + b);
        }
                , initColor % 256, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;
    }

    public void update()
    {
        InputBindingGroup ig = Game.game.inputBindings.get(this.selectorColor.metadataProperty.keybind());
        if (ig.isValid())
        {
            ig.invalidate();
            this.escape();
        }

        this.colorRed.update();
        this.colorGreen.update();
        this.colorBlue.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 800, 600);

        int color = ((Number) this.selectorColor.getMetadata(this.editor.mousePlaceable)).intValue();
        int r = (color / (256 * 256)) % 256;
        int g = (color / (256)) % 256;
        int b = color % 256;
        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.drawInterfaceRect(this.centerX + 20, this.centerY + 20, 800, 600, 20, 5);

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

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Color");
        this.back.draw();
    }
}
