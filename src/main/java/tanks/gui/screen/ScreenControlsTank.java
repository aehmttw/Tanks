package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsTank extends Screen
{
    public static int page = 0;
    public static final int page_count = 2;

    InputSelector moveUp = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 270, 700, 40, "Move up", Game.game.input.moveUp);
    InputSelector moveDown = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 180, 700, 40, "Move down", Game.game.input.moveDown);
    InputSelector moveLeft = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 90, 700, 40, "Move left", Game.game.input.moveLeft);
    InputSelector moveRight = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 0, 700, 40, "Move right", Game.game.input.moveRight);
    InputSelector shoot = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 90, 700, 40, "Shoot bullet", Game.game.input.shoot);
    InputSelector mine = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 180, 700, 40, "Lay mine", Game.game.input.mine);
    InputSelector aim = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 270, 700, 40, "Trace aim", Game.game.input.aim);

    InputSelector ability1 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 180, 700, 40, "Quick use ability 1", Game.game.input.ability1);
    InputSelector ability2 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 90, 700, 40, "Quick use ability 2", Game.game.input.ability2);
    InputSelector ability3 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 0, 700, 40, "Quick use ability 3", Game.game.input.ability3);
    InputSelector ability4 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 90, 700, 40, "Quick use ability 4", Game.game.input.ability4);
    InputSelector ability5 = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 180, 700, 40, "Quick use ability 5", Game.game.input.ability5);

    Button next = new Button(Drawing.drawing.interfaceSizeX * 2 / 3 + 190, Drawing.drawing.interfaceSizeY / 2 + 350, this.objWidth, this.objHeight, "Next page", () -> page++);

    Button previous = new Button(Drawing.drawing.interfaceSizeX * 2 / 3 - 190, Drawing.drawing.interfaceSizeY / 2 + 350, this.objWidth, this.objHeight, "Previous page", () -> page--);

    public ScreenControlsTank()
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
            moveUp.update();
            moveDown.update();
            moveLeft.update();
            moveRight.update();
            shoot.update();
            mine.update();
            aim.update();
        }
        else
        {
            ability1.update();
            ability2.update();
            ability3.update();
            ability4.update();
            ability5.update();
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
            aim.draw();
            mine.draw();
            shoot.draw();
            moveRight.draw();
            moveLeft.draw();
            moveDown.draw();
            moveUp.draw();
        }
        else
        {
            ability1.draw();
            ability2.draw();
            ability3.draw();
            ability4.draw();
            ability5.draw();
        }

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Tank controls");

        next.draw();
        previous.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 310, "Page %d of %d", (page + 1), page_count);

        ScreenOverlayControls.overlay.draw();
    }

}
