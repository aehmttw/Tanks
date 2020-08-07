package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsHotbar extends Screen
{
    InputSelector hotbarToggle = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Toggle hotbar visibility", Game.game.input.hotbarToggle);
    InputSelector hotbar1 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Item slot 1", Game.game.input.hotbar1);
    InputSelector hotbar2 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Item slot 2", Game.game.input.hotbar2);
    InputSelector hotbar3 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Item slot 3", Game.game.input.hotbar3);
    InputSelector hotbar4 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Item slot 4", Game.game.input.hotbar4);
    InputSelector hotbar5 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Item slot 5", Game.game.input.hotbar5);

    public ScreenControlsHotbar()
    {
        this.music = "tomato_feast_1.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        hotbarToggle.update();
        hotbar1.update();
        hotbar2.update();
        hotbar3.update();
        hotbar4.update();
        hotbar5.update();

        ScreenOptionsInputDesktop.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        hotbarToggle.draw();
        hotbar1.draw();
        hotbar2.draw();
        hotbar3.draw();
        hotbar4.draw();
        hotbar5.draw();

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Hotbar controls");

        ScreenOptionsInputDesktop.overlay.draw();
    }

}
