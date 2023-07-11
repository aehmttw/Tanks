package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptionsItems extends ScreenLevelEditorOverlay
{
    public TextBox editCoins;

    public Button editShop = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Shop", () -> Game.screen = new OverlayEditLevelShop(Game.screen, screenLevelEditor));

    public Button editStartingItems = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Starting items", () -> Game.screen = new OverlayEditLevelStartingItems(Game.screen, screenLevelEditor));

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", this::escape);

    public OverlayLevelOptionsItems(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        editCoins = new TextBox(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Starting coins", () ->
        {
            if (editCoins.inputText.length() <= 0)
                editCoins.inputText = "0";

            screenLevelEditor.level.startingCoins = Integer.parseInt(editCoins.inputText);
        }
                ,  screenLevelEditor.level.startingCoins + "");

        editCoins.allowLetters = false;
        editCoins.allowSpaces = false;
        editCoins.maxChars = 9;
        editCoins.checkMaxValue = true;
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
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Items");
        this.editCoins.draw();
        this.editShop.draw();
        this.editStartingItems.draw();
        this.back.draw();
    }
}
