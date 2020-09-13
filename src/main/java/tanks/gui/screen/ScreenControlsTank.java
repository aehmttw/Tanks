package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
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
        this.music = "tomato_feast_1_options.ogg";
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

        ScreenOptionsInputDesktop.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        moveUp.draw();
        moveDown.draw();
        moveLeft.draw();
        moveRight.draw();
        shoot.draw();
        mine.draw();
        aim.draw();

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Tank controls");

        ScreenOptionsInputDesktop.overlay.draw();
    }

}
