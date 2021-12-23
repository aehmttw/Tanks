package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.screen.Screen;

public class OverlayRotateTank extends ScreenLevelEditorOverlay
{
    public Button rotateUp = new Button(this.centerX, this.centerY - 100, 75, 75, "Up", () -> screenLevelEditor.mouseTankOrientation = 3);

    public Button rotateRight = new Button(this.centerX + 100, this.centerY, 75, 75, "Right", () -> screenLevelEditor.mouseTankOrientation = 0);

    public Button rotateDown = new Button(this.centerX, this.centerY + 100, 75, 75, "Down", () -> screenLevelEditor.mouseTankOrientation = 1);

    public Button rotateLeft = new Button(this.centerX - 100, this.centerY, 75, 75, "Left", () -> screenLevelEditor.mouseTankOrientation = 2);

    public Button back = new Button(this.centerX, this.centerY, 75, 75, "Done", this::escape);

    public OverlayRotateTank(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        this.rotateDown.fontSize = 24;
        this.rotateRight.fontSize = 24;
        this.rotateLeft.fontSize = 24;
        this.rotateUp.fontSize = 24;
        this.back.fontSize = 24;
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

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select tank orientation");

        this.rotateUp.draw();
        this.rotateLeft.draw();
        this.rotateDown.draw();
        this.rotateRight.draw();

        this.back.draw();
    }
}
