package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.InputSelector;

public class ScreenControlsHotbar extends Screen
{
    InputSelector hotbarToggle = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 270, 700, 40, "Toggle hotbar visibility", Game.game.input.hotbarToggle);
    InputSelector hotbar1 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 180, 700, 40, "Item slot 1", Game.game.input.hotbar1);
    InputSelector hotbar2 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 90, 700, 40, "Item slot 2", Game.game.input.hotbar2);
    InputSelector hotbar3 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 0, 700, 40, "Item slot 3", Game.game.input.hotbar3);
    InputSelector hotbar4 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 90, 700, 40, "Item slot 4", Game.game.input.hotbar4);
    InputSelector hotbar5 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 180, 700, 40, "Item slot 5", Game.game.input.hotbar5);
    InputSelector hotbarDeselect = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 270, 700, 40, "Deselect item slot", Game.game.input.hotbarDeselect);

    public ScreenControlsHotbar()
    {
        this.music = "menu_options.ogg";
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
        hotbarDeselect.update();

        ScreenOverlayControls.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        hotbarDeselect.draw();
        hotbar5.draw();
        hotbar4.draw();
        hotbar3.draw();
        hotbar2.draw();
        hotbar1.draw();
        hotbarToggle.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Hotbar controls");

        ScreenOverlayControls.overlay.draw();
    }

}
