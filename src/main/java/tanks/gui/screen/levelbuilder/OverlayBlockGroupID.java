package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayBlockGroupID extends ScreenLevelBuilderOverlay
{
    public TextBox groupID;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public Button increaseID = new Button(this.centerX + 250, this.centerY, 60, 60, "+", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.mouseObstacleGroup += 1;
            groupID.inputText = screenLevelBuilder.mouseObstacleGroup + "";
            groupID.previousInputText = screenLevelBuilder.mouseObstacleGroup + "";
            screenLevelBuilder.mouseObstacle.setMetadata(screenLevelBuilder.mouseObstacleGroup + "");
        }
    }
    );

    public Button decreaseID = new Button(this.centerX - 250, this.centerY, 60, 60, "-", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.mouseObstacleGroup -= 1;
            groupID.inputText = screenLevelBuilder.mouseObstacleGroup + "";
            groupID.previousInputText = screenLevelBuilder.mouseObstacleGroup + "";
            screenLevelBuilder.mouseObstacle.setMetadata(screenLevelBuilder.mouseObstacleGroup + "");
        }
    }
    );

    public OverlayBlockGroupID(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

        groupID = new TextBox(this.centerX, this.centerY + 15, 350, 40, "Group ID", new Runnable()
        {
            @Override
            public void run()
            {
                if (groupID.inputText.length() <= 0)
                    groupID.inputText = screenLevelBuilder.mouseObstacleGroup + "";
                else
                    screenLevelBuilder.mouseObstacleGroup = Integer.parseInt(groupID.inputText);

                screenLevelBuilder.mouseObstacle.setMetadata(screenLevelBuilder.mouseObstacleGroup + "");
            }

        }
                , screenLevelBuilder.mouseObstacleGroup + "");

        groupID.allowLetters = false;
        groupID.allowSpaces = false;
        groupID.maxChars = 9;
        groupID.minValue = 0;
        groupID.checkMaxValue = true;
        groupID.checkMinValue = true;

        increaseID.textOffsetX = 1.5;
        increaseID.textOffsetY = 1.5;

        decreaseID.textOffsetX = 1.5;
        decreaseID.textOffsetY = 1.5;
    }

    public void update()
    {
        this.increaseID.enabled = screenLevelBuilder.mouseObstacleGroup < 999999999;
        this.decreaseID.enabled = screenLevelBuilder.mouseObstacleGroup > 0;

        this.increaseID.update();
        this.decreaseID.update();
        this.groupID.update();

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Group ID");

        Drawing.drawing.setColor(0, 0, 0, 127);

        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 600, 75);

        this.increaseID.draw();
        this.decreaseID.draw();
        this.groupID.draw();

        this.back.draw();
    }
}
