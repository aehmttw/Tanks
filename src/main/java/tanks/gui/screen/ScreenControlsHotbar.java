package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsHotbar extends Screen
{
    public static int page = 0;
    public static final int page_count = 2;

    InputSelector hotbarToggle = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Toggle hotbar visibility", Game.game.input.hotbarToggle);
    InputSelector hotbar1 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Item slot 1", Game.game.input.hotbar1);
    InputSelector hotbar2 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Item slot 2", Game.game.input.hotbar2);
    InputSelector hotbar3 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Item slot 3", Game.game.input.hotbar3);
    InputSelector hotbar4 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Item slot 4", Game.game.input.hotbar4);
    InputSelector hotbar5 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Item slot 5", Game.game.input.hotbar5);
    InputSelector hotbar6 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Item slot 6", Game.game.input.hotbar6);
    InputSelector hotbar7 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Item slot 7", Game.game.input.hotbar7);
    InputSelector hotbar8 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Item slot 8", Game.game.input.hotbar8);
    InputSelector hotbar9 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Item slot 9", Game.game.input.hotbar9);
    InputSelector hotbar10 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Item slot 10", Game.game.input.hotbar10);
    InputSelector hotbarDeselect = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Deselect item slot", Game.game.input.hotbarDeselect);

    Button next = new Button(Drawing.drawing.interfaceSizeX * 2 / 3 + 190, Drawing.drawing.interfaceSizeY / 2 + 350, this.objWidth, this.objHeight, "Next page", () -> page++);

    Button previous = new Button(Drawing.drawing.interfaceSizeX * 2 / 3 - 190, Drawing.drawing.interfaceSizeY / 2 + 350, this.objWidth, this.objHeight, "Previous page", () -> page--);

    public ScreenControlsHotbar()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        next.enabled = page < page_count - 1;
        previous.enabled = page > 0;

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;
    }

    @Override
    public void update()
    {

        if (page == 0)
        {
            hotbarToggle.update();
            hotbar1.update();
            hotbar2.update();
            hotbar3.update();
            hotbar4.update();
            hotbar5.update();
        }
        else
        {
            hotbar6.update();
            hotbar7.update();
            hotbar8.update();
            hotbar9.update();
            hotbar10.update();
            hotbarDeselect.update();
        }

        next.enabled = page < page_count - 1;
        previous.enabled = page > 0;

        next.update();
        previous.update();

        ScreenOverlayControls.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (page == 0)
        {
            hotbar5.draw();
            hotbar4.draw();
            hotbar3.draw();
            hotbar2.draw();
            hotbar1.draw();
            hotbarToggle.draw();
        }
        else
        {
            hotbarDeselect.draw();
            hotbar10.draw();
            hotbar9.draw();
            hotbar8.draw();
            hotbar7.draw();
            hotbar6.draw();
        }

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Hotbar controls");

        next.draw();
        previous.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 310, "Page %d of %d", (page + 1), page_count);

        ScreenOverlayControls.overlay.draw();
    }

}
