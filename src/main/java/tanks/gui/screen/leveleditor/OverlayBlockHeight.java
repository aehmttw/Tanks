package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.obstacle.Obstacle;

public class OverlayBlockHeight extends ScreenLevelEditorOverlay
{
    public Button increaseHeight = new Button(this.centerX + 100, this.centerY, 60, 60, "+", () -> screenLevelEditor.mouseObstacleHeight += 0.5);

    public Button decreaseHeight = new Button(this.centerX - 100, this.centerY, 60, 60, "-", () -> screenLevelEditor.mouseObstacleHeight -= 0.5);

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape);

    public Button staggering = new Button(this.centerX + 200, this.centerY, 60, 60, "", () ->
    {
        if (!screenLevelEditor.stagger)
        {
            screenLevelEditor.mouseObstacleHeight = Math.max(screenLevelEditor.mouseObstacleHeight, 1);
            screenLevelEditor.stagger = true;
        }
        else if (!screenLevelEditor.oddStagger)
        {
            screenLevelEditor.mouseObstacleHeight = Math.max(screenLevelEditor.mouseObstacleHeight, 1);
            screenLevelEditor.oddStagger = true;
        }
        else
        {
            screenLevelEditor.oddStagger = false;
            screenLevelEditor.stagger = false;
        }
    }, " --- "
    );

    public OverlayBlockHeight(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        staggering.imageSizeX = 40;
        staggering.imageSizeY = 40;
        staggering.fullInfo = true;
    }

    public void update()
    {
        this.increaseHeight.enabled = screenLevelEditor.mouseObstacleHeight < Obstacle.default_max_height;
        this.decreaseHeight.enabled = screenLevelEditor.mouseObstacleHeight > 0.5;

        if (screenLevelEditor.stagger)
            this.decreaseHeight.enabled = screenLevelEditor.mouseObstacleHeight > 1;

        this.increaseHeight.update();
        this.decreaseHeight.update();
        this.staggering.update();

        if (!screenLevelEditor.stagger)
        {
            this.staggering.image = "icons/nostagger.png";
            this.staggering.setHoverText("Blocks will all be placed---with the same height");
        }
        else if (screenLevelEditor.oddStagger)
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
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Block height");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 500, 150);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(36);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, screenLevelEditor.mouseObstacleHeight + "");

        this.increaseHeight.draw();
        this.decreaseHeight.draw();
        this.staggering.draw();

        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(12);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(staggering.posX, staggering.posY - 40, "Staggering");
    }
}
