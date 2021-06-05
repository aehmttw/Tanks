package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.screen.Screen;

public class OverlayBlockHeight extends ScreenLevelBuilderOverlay
{
    public Button increaseHeight = new Button(this.centerX + 100, this.centerY, 60, 60, "+", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.mouseObstacleHeight += 0.5;
        }
    }
    );

    public Button decreaseHeight = new Button(this.centerX - 100, this.centerY, 60, 60, "-", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.mouseObstacleHeight -= 0.5;
        }
    }
    );

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public Button staggering = new Button(this.centerX + 200, this.centerY, 60, 60, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (!screenLevelBuilder.stagger)
            {
                screenLevelBuilder.mouseObstacleHeight = Math.max(screenLevelBuilder.mouseObstacleHeight, 1);
                screenLevelBuilder.stagger = true;
            }
            else if (!screenLevelBuilder.oddStagger)
            {
                screenLevelBuilder.mouseObstacleHeight = Math.max(screenLevelBuilder.mouseObstacleHeight, 1);
                screenLevelBuilder.oddStagger = true;
            }
            else
            {
                screenLevelBuilder.oddStagger = false;
                screenLevelBuilder.stagger = false;
            }
        }
    }, " --- "
    );

    public OverlayBlockHeight(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

        staggering.imageSizeX = 40;
        staggering.imageSizeY = 40;
        staggering.fullInfo = true;

        increaseHeight.textOffsetX = 1.5;
        increaseHeight.textOffsetY = 1.5;

        decreaseHeight.textOffsetX = 1.5;
        decreaseHeight.textOffsetY = 1.5;
    }

    public void update()
    {
        this.increaseHeight.enabled = screenLevelBuilder.mouseObstacleHeight < 4;
        this.decreaseHeight.enabled = screenLevelBuilder.mouseObstacleHeight > 0.5;

        if (screenLevelBuilder.stagger)
            this.decreaseHeight.enabled = screenLevelBuilder.mouseObstacleHeight > 1;

        this.increaseHeight.update();
        this.decreaseHeight.update();
        this.staggering.update();

        if (!screenLevelBuilder.stagger)
        {
            this.staggering.image = "nostagger.png";
            this.staggering.hoverText[0] = "Blocks will all be placed";
            this.staggering.hoverText[1] = "with the same height";
        }
        else if (screenLevelBuilder.oddStagger)
        {
            this.staggering.image = "oddstagger.png";
            this.staggering.hoverText[0] = "Every other block on the grid";
            this.staggering.hoverText[1] = "will be half a block shorter";
        }
        else
        {
            this.staggering.image = "evenstagger.png";
            this.staggering.hoverText[0] = "Every other block on the grid";
            this.staggering.hoverText[1] = "will be half a block shorter";
        }

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Block height");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 500, 150);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(36);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, screenLevelBuilder.mouseObstacleHeight + "");

        this.increaseHeight.draw();
        this.decreaseHeight.draw();
        this.staggering.draw();

        this.back.draw();

        Drawing.drawing.setInterfaceFontSize(12);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(staggering.posX, staggering.posY - 40, "Staggering");
    }
}
