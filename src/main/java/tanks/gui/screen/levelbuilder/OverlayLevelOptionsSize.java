package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsSize extends ScreenLevelBuilderOverlay
{
    public TextBox sizeX;
    public TextBox sizeY;

    public Button back3 = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public OverlayLevelOptionsSize(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

        sizeX = new TextBox(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Width", new Runnable()
        {
            @Override
            public void run()
            {
                if (sizeX.inputText.length() <= 0)
                    sizeX.inputText = screenLevelBuilder.level.sizeX + "";
                else
                {
                    screenLevelBuilder.level.sizeX = Integer.parseInt(sizeX.inputText);
                    Game.currentSizeX = screenLevelBuilder.level.sizeX;
                }

                screenLevelBuilder.level.reloadTiles();
            }

        }
                , screenLevelBuilder.level.sizeX + "");

        sizeX.allowLetters = false;
        sizeX.allowSpaces = false;
        sizeX.maxChars = 3;
        sizeX.maxValue = 400;
        sizeX.minValue = 1;
        sizeX.checkMaxValue = true;
        sizeX.checkMinValue = true;

        sizeY = new TextBox(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Height", new Runnable()
        {
            @Override
            public void run()
            {
                if (sizeY.inputText.length() <= 0)
                    sizeY.inputText = screenLevelBuilder.level.sizeY + "";
                else
                {
                    screenLevelBuilder.level.sizeY = Integer.parseInt(sizeY.inputText);
                    Game.currentSizeY = screenLevelBuilder.level.sizeY;
                }

                screenLevelBuilder.level.reloadTiles();
            }

        }
                , screenLevelBuilder.level.sizeY + "");

        sizeY.allowLetters = false;
        sizeY.allowSpaces = false;
        sizeY.maxChars = 3;
        sizeY.maxValue = 400;
        sizeY.minValue = 1;
        sizeY.checkMaxValue = true;
        sizeY.checkMinValue = true;
    }

    public void update()
    {
        this.sizeX.update();
        this.sizeY.update();
        this.back3.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        this.sizeY.draw();
        this.sizeX.draw();
        this.back3.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Level size");
    }
}
