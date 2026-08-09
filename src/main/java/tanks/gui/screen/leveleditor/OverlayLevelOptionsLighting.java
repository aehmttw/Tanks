package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SelectorColor;
import tanks.gui.TextBoxSlider;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsLighting extends ScreenLevelEditorOverlay implements IUnshadedEditorBackgroundScreen
{
    public TextBoxSlider light;
    public TextBoxSlider shadow;

    public SelectorColor lightColor;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", this::escape);

    public OverlayLevelOptionsLighting(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        light = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 1.25, this.objWidth, this.objHeight, "Direct light", () ->
        {
            if (light.inputText.length() <= 0)
                light.inputText = light.previousInputText;

            screenLevelEditor.level.light = Integer.parseInt(light.inputText) / 100.0;
            Level.currentLightIntensity = screenLevelEditor.level.light;
        },
            (int) Math.round(screenLevelEditor.level.light * 100), 0, 200, 1);

        light.allowLetters = false;
        light.allowSpaces = false;
        light.maxChars = 3;
        light.checkMaxValue = true;
        light.integer = true;

        light.r1 = 0;
        light.g1 = 0;
        light.b1 = 0;

        shadow = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 0.25, this.objWidth, this.objHeight, "Shadow light", () ->
        {
            if (shadow.inputText.length() <= 0)
                shadow.inputText = shadow.previousInputText;

            screenLevelEditor.level.shadow = Integer.parseInt(shadow.inputText) / 100.0;
            Level.currentShadowIntensity = screenLevelEditor.level.shadow;
        },
            (int) Math.round(screenLevelEditor.level.shadow * 100), 0, 200, 1);

        shadow.allowLetters = false;
        shadow.allowSpaces = false;
        shadow.maxChars = 3;
        shadow.checkMaxValue = true;
        shadow.integer = true;

        shadow.r1 = 0;
        shadow.g1 = 0;
        shadow.b1 = 0;

        lightColor = new SelectorColor(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight,
                "Light color", this.objYSpace * 1.5, this.editor.level.lightColor, false);
    }

    public void update()
    {
        this.light.update();
        this.shadow.update();
        this.back.update();
        this.lightColor.update();

        super.update();
    }

    public void drawUI()
    {
        super.drawUI();

        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.drawPopup(centerX, centerY - 30, 800 * this.objHeight / 40, 420 * this.objWidth / 350);

        this.light.draw();
        this.shadow.draw();
        this.back.draw();
        this.lightColor.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Lighting");
    }
}
