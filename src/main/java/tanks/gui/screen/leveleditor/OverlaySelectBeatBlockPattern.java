package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.leveleditor.selector.SelectorBeatPattern;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.obstacle.ObstacleBeatBlock;

public class OverlaySelectBeatBlockPattern extends ScreenLevelEditorOverlay
{
    public Button back = new Button(this.centerX, this.centerY + 240, 350, 40, "Done", this::escape);

    public int buttonSize = 60;
    public SelectorBeatPattern selector;
    public Button[] groups = new Button[8];

    public OverlaySelectBeatBlockPattern(Screen previous, ScreenLevelEditor editor, SelectorBeatPattern selector)
    {
        super(previous, editor);

        this.selector = selector;

        for (int i = 0; i < 8; i++)
        {
            int j = i;
            groups[i] = new Button(this.centerX - 135 + i % 4 * 90, this.centerY - 45 + i / 4 * 90, buttonSize, buttonSize,
                    "", () -> selector.setMetadata(editor, editor.mousePlaceable, (j % 4) * 2 + j / 4));
        }

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

        int val = (int) selector.getMetadata(editor.mousePlaceable);
        for (int i = 0; i < groups.length; i++)
        {
            groups[i].enabled = val != (i % 4) * 2 + i / 4;
            groups[i].update();
        }

        this.back.update();

        editor.overlayMusics.add("beatblocks/beat_blocks.ogg");
        editor.overlayMusics.add("beatblocks/beat_beeps_" + (int) Math.pow(2, val / 2) + ".ogg");

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 800, 600);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Beat pattern");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 450, 200);

        for (Button group : groups)
        {
            double sx = group.sizeX;
            double sy = group.sizeY;

            if (!group.enabled)
            {
                group.sizeX *= 1.2;
                group.sizeY *= 1.2;
            }

            group.draw();

            group.sizeX = sx;
            group.sizeY = sy;
        }

        for (int i = 0; i < 8; i++)
        {
            double f = ((i % 4) / 8.0 + 0.4 + (i / 4) * 0.5) % 1.0;
            double[] col = Game.getRainbowColor(f);
            Drawing.drawing.setColor(col[0] * 0.75 + 255 * 0.25, col[1] * 0.75 + 255 * 0.25, col[2] * 0.75 + 255 * 0.25);

            double s = ObstacleBeatBlock.isOn(Math.pow(2, (i % 4)), i / 4 == 0) ? 0.5 : 1;
            Drawing.drawing.fillInterfaceOval(groups[i].posX, groups[i].posY, 50 * s, 50 * s);
            Drawing.drawing.setColor(0, 0, 0);

            col = Game.getRainbowColor((f + 0.1) % 1.0);

            if (groups[i].enabled)
            {
                Drawing.drawing.setColor(0, 0, 0, 127);
                Drawing.drawing.setInterfaceFontSize(24);
            }
            else
                Drawing.drawing.setInterfaceFontSize(40);

            Drawing.drawing.drawInterfaceText(groups[i].posX, groups[i].posY, (int) Math.pow(2, i % 4) + (i / 4 == 0 ? "a" : "b"));

            groups[i].disabledColR = col[0] * 0.75 + 0 * 0.25;
            groups[i].disabledColG = col[1] * 0.75 + 0 * 0.25;
            groups[i].disabledColB = col[2] * 0.75 + 0 * 0.25;
        }

        this.back.draw();
    }
}
