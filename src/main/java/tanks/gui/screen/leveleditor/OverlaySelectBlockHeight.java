package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.leveleditor.selector.SelectorStackHeight;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.obstacle.ObstacleStackable;

public class OverlaySelectBlockHeight extends ScreenLevelEditorOverlay
{
    public SelectorStackHeight selector;

    public Button increaseHeight = new Button(this.centerX + 100, this.centerY, 60, 60, "+", () -> selector.changeMetadata(editor, editor.mousePlaceable, 1));

    public Button decreaseHeight = new Button(this.centerX - 100, this.centerY, 60, 60, "-", () -> selector.changeMetadata(editor, editor.mousePlaceable, -1));

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, 350, 40, "Done", this::escape);

    public Button staggering = new Button(this.centerX + 200, this.centerY, 60, 60, "", () ->
    {
        if (!editor.stagger)
        {
            selector.setMetadata(editor, editor.mousePlaceable, Math.max(1, (double) selector.getMetadata(editor.mousePlaceable)));
            editor.stagger = true;
        }
        else if (!editor.oddStagger)
        {
            selector.setMetadata(editor, editor.mousePlaceable, Math.max(1, (double) selector.getMetadata(editor.mousePlaceable)));
            editor.oddStagger = true;
        }
        else
        {
            editor.oddStagger = false;
            editor.stagger = false;
        }
    }, " --- "
    );

    public OverlaySelectBlockHeight(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorStackHeight selector)
    {
        super(previous, screenLevelEditor);

        this.selector = selector;
        screenLevelEditor.paused = true;

        staggering.imageSizeX = 40;
        staggering.imageSizeY = 40;
        staggering.fullInfo = true;
    }

    public void update()
    {
        InputBindingGroup ig = Game.game.inputBindings.get(this.selector.metadataProperty.keybind());
        if (ig.isValid())
        {
            ig.invalidate();
            this.escape();
        }

        this.increaseHeight.enabled = (double) selector.getMetadata(editor.mousePlaceable) < ObstacleStackable.default_max_height;
        this.decreaseHeight.enabled = (double) selector.getMetadata(editor.mousePlaceable) > 0.5;

        if (editor.stagger)
            this.decreaseHeight.enabled = (double) selector.getMetadata(editor.mousePlaceable) > 1;

        this.increaseHeight.update();
        this.decreaseHeight.update();
        this.staggering.update();

        if (!editor.stagger)
        {
            this.staggering.image = "icons/nostagger.png";
            this.staggering.setHoverText("Blocks will all be placed---with the same height");
        }
        else if (editor.oddStagger)
        {
            this.staggering.image = "icons/oddstagger.png";
            this.staggering.setHoverText("Every other block on the grid---will be half a block shorter");
        }
        else
        {
            this.staggering.image = "icons/evenstagger.png";
            this.staggering.setHoverText("Every other block on the grid---will be half a block shorter");
        }

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY + 25, 800, 600);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Block height");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 500, 150);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(36);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, selector.getMetadata(editor.mousePlaceable) + "");

        this.increaseHeight.draw();
        this.decreaseHeight.draw();
        this.staggering.draw();

        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(12);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(staggering.posX, staggering.posY - 40, "Staggering");
    }
}
