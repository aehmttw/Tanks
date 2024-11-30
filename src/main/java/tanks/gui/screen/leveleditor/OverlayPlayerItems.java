package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayPlayerItems extends ScreenLevelEditorOverlay
{
    public TextBox editCoins;

    public Button editShop = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Shop", () -> Game.screen = new OverlayShop(Game.screen, editor));

    public Button editStartingItems = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Starting items", () -> Game.screen = new OverlayStartingItems(Game.screen, editor));

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", this::escape);

    public OverlayPlayerItems(Screen previous, ScreenLevelEditor editor)
    {
        super(previous, editor);

        editCoins = new TextBox(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Starting coins", () ->
        {
            if (editCoins.inputText.isEmpty())
                editCoins.inputText = "0";

            editor.level.startingCoins = Integer.parseInt(editCoins.inputText);
        }
                ,  editor.level.startingCoins + "");

        editCoins.allowLetters = false;
        editCoins.allowSpaces = false;
        editCoins.maxChars = 9;
        editCoins.checkMaxValue = true;
        editor.modified = true;
    }

    public void update()
    {
        this.editCoins.update();
        this.editShop.update();
        this.editStartingItems.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY - 20, 800, 550, 20, 5);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Items");
        this.editCoins.draw();
        this.editShop.draw();
        this.editStartingItems.draw();
        this.back.draw();
    }
}
