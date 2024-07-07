package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.InputSelector;

public class ScreenControlsTank extends Screen
{


    InputSelector moveUp = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 270, 700, 40, "Move up", Game.game.input.moveUp);
    InputSelector moveDown = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 180, 700, 40, "Move down", Game.game.input.moveDown);
    InputSelector moveLeft = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 90, 700, 40, "Move left", Game.game.input.moveLeft);
    InputSelector moveRight = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 0, 700, 40, "Move right", Game.game.input.moveRight);
    InputSelector shoot = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 90, 700, 40, "Shoot bullet", Game.game.input.shoot);
    InputSelector mine = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 180, 700, 40, "Lay mine", Game.game.input.mine);
    InputSelector aim = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 270, 700, 40, "Trace aim", Game.game.input.aim);

    public ScreenControlsTank()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        moveUp.update();
        moveDown.update();
        moveLeft.update();
        moveRight.update();
        shoot.update();
        mine.update();
        aim.update();

        ScreenOverlayControls.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        aim.draw();
        mine.draw();
        shoot.draw();
        moveRight.draw();
        moveLeft.draw();
        moveDown.draw();
        moveUp.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Tank controls");

        ScreenOverlayControls.overlay.draw();
    }

}
