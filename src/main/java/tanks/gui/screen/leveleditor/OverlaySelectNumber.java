package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.Screen;
import tanks.gui.screen.leveleditor.selector.SelectorNumber;

import java.util.Locale;

public class OverlaySelectNumber extends ScreenLevelEditorOverlay
{
    public TextBox textBox;
    public SelectorNumber selector;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, 350, 40, "Done", this::escape);

    public Button increase = new Button(this.centerX + 250, this.centerY, 60, 60, "+", () ->
    {
        try
        {
            selector.changeMetadata(editor, editor.mousePlaceable, 1);
            textBox.inputText = String.format(Locale.ROOT, selector.format, ((Number) selector.metadataField.get(editor.mousePlaceable)).doubleValue());
        }
        catch (IllegalAccessException e)
        {
            Game.exitToCrash(e);
        }
    }
    );

    public Button decrease = new Button(this.centerX - 250, this.centerY, 60, 60, "-", () ->
    {
        try
        {
            selector.changeMetadata(editor, editor.mousePlaceable, -1);
            textBox.inputText = String.format(Locale.ROOT, selector.format, ((Number) selector.metadataField.get(editor.mousePlaceable)).doubleValue());
        }
        catch (IllegalAccessException e)
        {
            Game.exitToCrash(e);
        }
    }
    );

    public OverlaySelectNumber(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorNumber selector)
    {
        super(previous, screenLevelEditor);

        screenLevelEditor.paused = true;

        this.selector = selector;
        textBox = new TextBox(this.centerX, this.centerY + 15, 350, 40, this.selector.metadataProperty.name(), this::submit, this.selector.numberString(screenLevelEditor.mousePlaceable));

        textBox.allowLetters = false;
        textBox.allowSpaces = false;
        textBox.allowDoubles = this.selector.allowDecimals;
        textBox.maxChars = 9;
        textBox.minValue = selector.min;
        textBox.maxValue = selector.max;
        textBox.checkMaxValue = true;
        textBox.checkMinValue = true;
        this.musicInstruments = true;
    }

    public void update()
    {
        InputBindingGroup ig = Game.game.inputBindings.get(this.selector.metadataProperty.keybind());
        if (ig.isValid())
        {
            ig.invalidate();
            this.escape();
        }

        this.increase.enabled = ((Number) this.selector.getMetadata(editor.mousePlaceable)).doubleValue() < selector.max;
        this.decrease.enabled = ((Number) this.selector.getMetadata(editor.mousePlaceable)).doubleValue() > selector.min;

        this.increase.update();
        this.decrease.update();
        this.textBox.update();

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY + 15, 800, 600);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, selector.metadataProperty.name());

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 600, 100);

        this.increase.draw();
        this.decrease.draw();
        this.textBox.draw();

        this.back.draw();
    }

    public void submit()
    {
        if (textBox.inputText.isEmpty() || textBox.inputText.equals(textBox.previousInputText))
        {
            textBox.inputText = textBox.previousInputText;
            return;
        }

        if (selector.forceStep)
            textBox.inputText = String.format(Locale.ROOT, selector.format, Math.round(Double.parseDouble(textBox.inputText) * selector.step) / selector.step);

        this.selector.setMetadata(editor, editor.mousePlaceable, Double.parseDouble(textBox.inputText));
    }
}
