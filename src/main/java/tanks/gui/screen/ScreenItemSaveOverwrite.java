package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.item.Item;
import tanks.tankson.MonitoredArrayListIndexPointer;

public class ScreenItemSaveOverwrite extends Screen implements IBlankBackgroundScreen
{
    public ScreenEditorItem previous;
    public Item.ItemStack<?> item;
    public Item.ItemStack<?> oldItem;

    public ScreenItemSaveOverwrite(ScreenEditorItem s, Item.ItemStack<?> item)
    {
        this.previous = s;
        this.music = this.previous.music;
        this.musicID = this.previous.musicID;
        this.item = item;

        try
        {
            BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + item.item.name.replace(" ", "_") + ".tanks");
            f.startReading();
            String t = f.nextLine();
            f.stopReading();
            this.oldItem = Item.ItemStack.fromString(null, t);
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public Button replace = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Replace template", () ->
    {
        this.previous.writeItemAndConfirm(item, true);
    }
    );

    public Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = this.previous;
    }
    );

    @Override
    public void update()
    {
        this.replace.update();
        this.quit.update();
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.textSize);

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "An item template with this name already exists!");
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1, "Would you like to replace it?");

        this.replace.draw();
        this.quit.draw();
    }
}
