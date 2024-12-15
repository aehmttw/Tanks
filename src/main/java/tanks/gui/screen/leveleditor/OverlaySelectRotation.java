package tanks.gui.screen.leveleditor;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.leveleditor.selector.SelectorRotation;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.obstacle.Obstacle;

public class OverlaySelectRotation extends ScreenLevelEditorOverlay
{
    public SelectorRotation<?> selector;

    public Button rotateUp = new Button(this.centerX, this.centerY - 100, 75, 75, "", () -> selector.number = 3);

    public Button rotateRight = new Button(this.centerX + 100, this.centerY, 75, 75, "", () -> selector.number = 0);

    public Button rotateDown = new Button(this.centerX, this.centerY + 100, 75, 75, "", () -> selector.number = 1);

    public Button rotateLeft = new Button(this.centerX - 100, this.centerY, 75, 75, "", () -> selector.number = 2);

    public Button back = new Button(this.centerX, this.centerY, 75, 75, "Done", this::escape);

    public OverlaySelectRotation(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorRotation<?> selector)
    {
        super(previous, screenLevelEditor);

        this.selector = selector;
        this.selector.modified = true;

        this.rotateDown.fontSize = 24;
        this.rotateRight.fontSize = 24;
        this.rotateLeft.fontSize = 24;
        this.rotateUp.fontSize = 24;
        this.back.fontSize = 24;

        rotateUp.image = "icons/arrow_up.png";
        rotateUp.imageSizeX = 50;
        rotateUp.imageSizeY = 50;
        rotateUp.imageYOffset = -5;
        rotateUp.keybind = Game.game.input.moveUp;

        rotateDown.image = "icons/arrow_down.png";
        rotateDown.imageSizeX = 50;
        rotateDown.imageSizeY = 50;
        rotateDown.imageYOffset = 5;
        rotateDown.keybind = Game.game.input.moveDown;

        rotateLeft.image = "icons/back.png";
        rotateLeft.imageSizeX = 50;
        rotateLeft.imageSizeY = 50;
        rotateLeft.imageXOffset = -5;
        rotateLeft.keybind = Game.game.input.moveLeft;

        rotateRight.image = "icons/forward.png";
        rotateRight.imageSizeX = 50;
        rotateRight.imageSizeY = 50;
        rotateRight.imageXOffset = 5;
        rotateRight.keybind = Game.game.input.moveRight;
    }

    public void update()
    {
        this.rotateUp.enabled = true;
        this.rotateDown.enabled = true;
        this.rotateLeft.enabled = true;
        this.rotateRight.enabled = true;

        if (selector.number == 0)
            this.rotateRight.enabled = false;
        else if (selector.number == 1)
            this.rotateDown.enabled = false;
        else if (selector.number == 2)
            this.rotateLeft.enabled = false;
        else
            this.rotateUp.enabled = false;

        this.rotateUp.update();
        this.rotateLeft.update();
        this.rotateDown.update();
        this.rotateRight.update();

        this.back.update();

        if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER))
        {
            Game.game.window.validPressedKeys.remove(((Integer) InputCodes.KEY_ENTER));
            this.escape();
        }

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.drawPopup(centerX, centerY - 15,550, 420);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select " + (selector.gameObject instanceof Obstacle ? "obstacle" : "tank") + " orientation");

        this.rotateUp.draw();
        this.rotateLeft.draw();
        this.rotateDown.draw();
        this.rotateRight.draw();

        this.back.draw();
    }

    public void escape()
    {
        super.escape();
    }
}
