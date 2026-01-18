package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;
import tanks.gui.screen.leveleditor.selector.SelectorColorAndNoise;

public class OverlaySelectColorAndNoise extends ScreenLevelEditorOverlay
{
    public SelectorColorAndNoise selectorColor;

    public TextBoxSlider colorRed;
    public TextBoxSlider colorGreen;
    public TextBoxSlider colorBlue;
    public TextBoxSlider colorVarRed;
    public TextBoxSlider colorVarGreen;
    public TextBoxSlider colorVarBlue;

    public double[][] randoms = new double[3][400];
    
    public boolean dichromatic;
    
    public String dichromaticText = "Dichromatic: %s";
    
    public Button dichromaticToggle = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "", () ->
    {
        this.dichromatic = !this.dichromatic;
        this.setColor();
    }, "If disabled, the RGB components of noise---will be added separately, leading to---more color variation.------If enabled, they will be added---proportionally to their noise values.");

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Done", this::escape);

    public OverlaySelectColorAndNoise(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorColorAndNoise selector)
    {
        super(previous, screenLevelEditor);

        this.selectorColor = selector;
        long initColor = ((Number) this.selectorColor.getMetadata(screenLevelEditor.mousePlaceable)).longValue();

        this.dichromatic = initColor / (long) Math.pow(256, 6) != 0;

        colorRed = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Red", () ->
        {
            if (colorRed.inputText.length() <= 0)
                colorRed.inputText = colorRed.previousInputText;

            this.setColor();
        }
                , (initColor / (256 * 256)) % 256, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Green", () ->
        {
            if (colorGreen.inputText.length() <= 0)
                colorGreen.inputText = colorGreen.previousInputText;

            this.setColor();
        }
                , (initColor / 256) % 256, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Blue", () ->
        {
            if (colorBlue.inputText.length() <= 0)
                colorBlue.inputText = colorBlue.previousInputText;

            this.setColor();
        }
                , initColor % 256, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;

        colorVarRed = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Red noise", () ->
        {
            if (colorVarRed.inputText.length() <= 0)
                colorVarRed.inputText = colorVarRed.previousInputText;

            this.setColor();
        }
                , (initColor / ((long) Math.pow(256, 5))) % 256, 0, 255, 1);

        colorVarRed.allowLetters = false;
        colorVarRed.allowSpaces = false;
        colorVarRed.maxChars = 3;
        colorVarRed.checkMaxValue = true;

        colorVarGreen = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Green noise", () ->
        {
            if (colorVarGreen.inputText.length() <= 0)
                colorVarGreen.inputText = colorVarGreen.previousInputText;

            this.setColor();
        }
                , (initColor / ((long) Math.pow(256, 4))) % 256, 0, 255, 1);

        colorVarGreen.allowLetters = false;
        colorVarGreen.allowSpaces = false;
        colorVarGreen.maxChars = 3;
        colorVarGreen.checkMaxValue = true;

        colorVarBlue = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Blue noise", () ->
        {
            if (colorVarBlue.inputText.length() <= 0)
                colorVarBlue.inputText = colorVarBlue.previousInputText;

            this.setColor();
        }
                , (initColor / ((long) Math.pow(256, 3))) % 256, 0, 255, 1);

        colorVarBlue.allowLetters = false;
        colorVarBlue.allowSpaces = false;
        colorVarBlue.maxChars = 3;
        colorVarBlue.checkMaxValue = true;

        for (int i = 0; i < this.randoms.length; i++)
        {
            for (int j = 0; j < this.randoms[i].length; j++)
            {
                this.randoms[i][j] = Math.random();
            }
        }
        this.musicInstruments = true;

        this.setColor();
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
        this.colorVarRed.update();
        this.colorVarGreen.update();
        this.colorVarBlue.update();
        this.dichromaticToggle.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        int sX = (int) (800 * this.objWidth / 350);
        int sY = (int) Math.min(700, 600 * this.objHeight / 40);
        int w = (int) (20 * this.objWidth / 350);

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, sX, sY);

        int vr = (int) colorVarRed.value;
        int vg = (int) colorVarGreen.value;
        int vb = (int) colorVarBlue.value;
        int r = (int) colorRed.value;
        int g = (int) colorGreen.value;
        int b = (int) colorBlue.value;
        Drawing.drawing.setColor(r, g, b);
//        Drawing.drawing.drawInterfaceRect(this.centerX + 20, this.centerY + 20, sX, sY, 20, 5);

        int rand = dichromatic ? 0 : 1;
        int i = 0;
        for (double x = this.centerX - sX / 2 + w / 4; x <= this.centerX + sX / 2 - w / 4; x += w / 2)
        {
            Drawing.drawing.setColor(r + vr * randoms[0][i], g + vg * randoms[rand][i], b + vb * randoms[rand * 2][i]);
            Drawing.drawing.fillInterfaceOval(x, this.centerY - sY / 2 + w / 4, w * 0.4, w * 0.4);
            i++;
            Drawing.drawing.setColor(r + vr * randoms[0][i], g + vg * randoms[rand][i], b + vb * randoms[rand * 2][i]);
            Drawing.drawing.fillInterfaceOval(x, this.centerY + sY / 2 - w / 4, w * 0.4, w * 0.4);
            i++;
        }

        for (double y = this.centerY - sY / 2 + w / 4; y <= this.centerY + sY / 2 - w / 4; y += w / 2)
        {
            Drawing.drawing.setColor(r + vr * randoms[0][i], g + vg * randoms[rand][i], b + vb * randoms[rand * 2][i]);
            Drawing.drawing.fillInterfaceOval(this.centerX - sX / 2 + w / 4, y,  w * 0.4, w * 0.4);
            i++;
            Drawing.drawing.setColor(r + vr * randoms[0][i], g + vg * randoms[rand][i], b + vb * randoms[rand * 2][i]);
            Drawing.drawing.fillInterfaceOval(this.centerX + sX / 2 - w / 4, y, w * 0.4, w * 0.4);
            i++;
        }

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

        colorVarRed.r1 = colorRed.value;
        colorVarRed.r2 = 255;
        colorVarRed.g1 = colorGreen.value;
        colorVarRed.g2 = colorGreen.value;
        colorVarRed.b1 = colorBlue.value;
        colorVarRed.b2 = colorBlue.value;

        colorVarGreen.r1 = colorRed.value;
        colorVarGreen.r2 = colorRed.value;
        colorVarGreen.g1 = colorGreen.value;
        colorVarGreen.g2 = 255;
        colorVarGreen.b1 = colorBlue.value;
        colorVarGreen.b2 = colorBlue.value;

        colorVarBlue.r1 = colorRed.value;
        colorVarBlue.r2 = colorRed.value;
        colorVarBlue.g1 = colorGreen.value;
        colorVarBlue.g2 = colorGreen.value;
        colorVarBlue.b1 = colorBlue.value;
        colorVarBlue.b2 = 255;

        colorVarRed.maxValue = 255 - r;
        colorVarRed.max = colorVarRed.maxValue;

        colorVarGreen.maxValue = 255 - g;
        colorVarGreen.max = colorVarGreen.maxValue;

        colorVarBlue.maxValue = 255 - b;
        colorVarBlue.max = colorVarBlue.maxValue;

        if (!colorVarRed.selected)
            colorVarRed.inputText = (int) Math.min(colorVarRed.value, colorVarRed.maxValue) + "";

        if (!colorVarGreen.selected)
            colorVarGreen.inputText = (int) Math.min(colorVarGreen.value, colorVarGreen.maxValue) + "";

        if (!colorVarBlue.selected)
            colorVarBlue.inputText = (int) Math.min(colorVarBlue.value, colorVarBlue.maxValue) + "";

        this.colorBlue.draw();
        this.colorGreen.draw();
        this.colorRed.draw();
        this.colorVarBlue.draw();
        this.colorVarGreen.draw();
        this.colorVarRed.draw();
        this.back.draw();

        this.dichromaticToggle.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Color");
    }

    public void setColor()
    {
        int r = Integer.parseInt(colorRed.inputText);
        int g = Integer.parseInt(colorGreen.inputText);
        int b = Integer.parseInt(colorBlue.inputText);

        if (!colorVarRed.selected)
            colorVarRed.inputText = (int) colorVarRed.value + "";

        if (!colorVarGreen.selected)
            colorVarGreen.inputText = (int) colorVarGreen.value + "";

        if (!colorVarBlue.selected)
            colorVarBlue.inputText = (int) colorVarBlue.value + "";

        colorVarRed.maxValue = 255 - r;
        colorVarRed.max = colorVarRed.maxValue;
        colorVarRed.performValueCheck();

        colorVarGreen.maxValue = 255 - g;
        colorVarGreen.max = colorVarGreen.maxValue;
        colorVarGreen.performValueCheck();

        colorVarBlue.maxValue = 255 - b;
        colorVarBlue.max = colorVarBlue.maxValue;
        colorVarBlue.performValueCheck();

        long vr = Integer.parseInt(colorVarRed.inputText);
        long vg = Integer.parseInt(colorVarGreen.inputText);
        long vb = Integer.parseInt(colorVarBlue.inputText);

        dichromaticToggle.setText(dichromaticText, (Object) (dichromatic ? ScreenOptions.onText : ScreenOptions.offText));
        
        long m = (((((((dichromatic ? 256L : 0L) + vr) * 256 + vg) * 256 + vb) * 256 + r) * 256 + g) * 256 + b);
        this.selectorColor.setMetadata(editor, editor.mousePlaceable, m);
    }
}
