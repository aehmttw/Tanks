package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.screen.Screen;

public class OverlayRotateTank extends ScreenLevelEditorOverlay
{
    public Button rotateUp = new Button(this.centerX, this.centerY - 100, 75, 75, "", () -> screenLevelEditor.mouseTankOrientation = 3);

    public Button rotateRight = new Button(this.centerX + 100, this.centerY, 75, 75, "", () -> screenLevelEditor.mouseTankOrientation = 0);

    public Button rotateDown = new Button(this.centerX, this.centerY + 100, 75, 75, "", () -> screenLevelEditor.mouseTankOrientation = 1);

    public Button rotateLeft = new Button(this.centerX - 100, this.centerY, 75, 75, "", () -> screenLevelEditor.mouseTankOrientation = 2);

    public Button back = new Button(this.centerX, this.centerY, 75, 75, "Done", this::escape);

    public OverlayRotateTank(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        this.rotateDown.fontSize = 24;
        this.rotateRight.fontSize = 24;
        this.rotateLeft.fontSize = 24;
        this.rotateUp.fontSize = 24;
        this.back.fontSize = 24;

        rotateUp.image = "icons/arrow_up.png";
        rotateUp.imageSizeX = 50;
        rotateUp.imageSizeY = 50;
        rotateUp.imageYOffset = -5;

        rotateDown.image = "icons/arrow_down.png";
        rotateDown.imageSizeX = 50;
        rotateDown.imageSizeY = 50;
        rotateDown.imageYOffset = 5;

        rotateLeft.image = "icons/back.png";
        rotateLeft.imageSizeX = 50;
        rotateLeft.imageSizeY = 50;
        rotateLeft.imageXOffset = -5;

        rotateRight.image = "icons/forward.png";
        rotateRight.imageSizeX = 50;
        rotateRight.imageSizeY = 50;
        rotateRight.imageXOffset = 5;

        this.musicInstruments = true;
    }

    public void update()
    {
        this.rotateUp.enabled = true;
        this.rotateDown.enabled = true;
        this.rotateLeft.enabled = true;
        this.rotateRight.enabled = true;

        if (screenLevelEditor.mouseTankOrientation == 0)
            this.rotateRight.enabled = false;
        else if (screenLevelEditor.mouseTankOrientation == 1)
            this.rotateDown.enabled = false;
        else if (screenLevelEditor.mouseTankOrientation == 2)
            this.rotateLeft.enabled = false;
        else
            this.rotateUp.enabled = false;

        this.rotateUp.update();
        this.rotateLeft.update();
        this.rotateDown.update();
        this.rotateRight.update();

        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
//        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 600, 450);
//        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 580, 430);
        Drawing.drawing.drawPopup(centerX, centerY - 15,550, 420, 10, 5);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select tank orientation");

        this.rotateUp.draw();
        this.rotateLeft.draw();
        this.rotateDown.draw();
        this.rotateRight.draw();

        this.back.draw();
    }
}
