package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;
import tanks.obstacle.ObstacleBeatBlock;

public class OverlayBeatBlockPattern extends ScreenLevelEditorOverlay
{
    public Button back = new Button(this.centerX, this.centerY + 240, 350, 40, "Done", this::escape);

    public int buttonSize = 60;

    public Button[] groups = new Button[8];

    public OverlayBeatBlockPattern(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        for (int i = 0; i < 8; i++)
        {
            int j = i;
            groups[i] = new Button(this.centerX - 135 + i % 4 * 90, this.centerY - 45 + i / 4 * 90, buttonSize, buttonSize, "", new Runnable()
            {
                @Override
                public void run()
                {
                    screenLevelEditor.mouseObstacleBeatPattern = (j % 4) * 2 + j / 4;
                    screenLevelEditor.mouseObstacle.setMetadata(screenLevelEditor.mouseObstacleBeatPattern + "");
                }
            }
            );
        }

        this.musicInstruments = true;
    }

    public void update()
    {
        for (int i = 0; i < groups.length; i++)
        {
            groups[i].enabled = screenLevelEditor.mouseObstacleBeatPattern != (i % 4) * 2 + i / 4;
            groups[i].update();
        }

        this.back.update();

        screenLevelEditor.overlayMusics.add("beatblocks/beat_blocks.ogg");
        screenLevelEditor.overlayMusics.add("beatblocks/beat_beeps_" + (int) Math.pow(2, screenLevelEditor.mouseObstacleBeatPattern / 2) + ".ogg");

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 800, 600);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 780, 580);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Beat pattern");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 450, 200);

        for (int i = 0; i < groups.length; i++)
        {
            double sx = groups[i].sizeX;;
            double sy = groups[i].sizeY;

            if (!groups[i].enabled)
            {
                groups[i].sizeX *= 1.2;
                groups[i].sizeY *= 1.2;
            }

            groups[i].draw();

            groups[i].sizeX = sx;
            groups[i].sizeY = sy;
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
