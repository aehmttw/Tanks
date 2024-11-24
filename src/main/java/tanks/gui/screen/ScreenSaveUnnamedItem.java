package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.bullet.Bullet;
import tanks.gui.Button;
import tanks.gui.SelectorImage;
import tanks.gui.TextBox;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tank.Mine;
import tanks.tankson.FieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.io.IOException;
import java.lang.reflect.Field;

public class ScreenSaveUnnamedItem extends Screen implements IBlankBackgroundScreen
{
    public ScreenEditorTanksONable<?> previous;
    public TextBox itemName;
    public Item.ItemStack<?> itemStack;

    public SelectorImage image;
    public TextBox cooldown;
    public TextBox amount;
    public TextBox maxAmount;

    public Button save = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            writeItem(true);
            Game.screen = new ScreenItemSavedInfo(previous);
        }
    });


    public Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = this.previous;
    }
    );

    public boolean writeItem(boolean overwrite)
    {
        BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + itemName.inputText.replace(" ", "_") + ".tanks");

        if (!f.exists() || overwrite)
        {
            try
            {
                if (!f.exists())
                    f.create();

                f.startWriting();
                f.println(this.itemStack.toString());
                f.stopWriting();

                return true;
            }
            catch (IOException e)
            {
                Game.exitToCrash(e);
            }
        }

        return false;
    }

    public ScreenSaveUnnamedItem(ScreenEditorTanksONable<?> previous)
    {
        this.previous = previous;
        this.music = this.previous.music;
        this.musicID = this.previous.musicID;

        if (previous.target.get() instanceof Bullet)
            this.itemStack = new ItemBullet((Bullet) previous.target.get()).getStack(null);
        else if (previous.target.get() instanceof Mine)
            this.itemStack = new ItemMine((Mine) previous.target.get()).getStack(null);

        try
        {
            Pointer<Item.ItemStack<?>> p = new FieldPointer<>(this.itemStack, Item.ItemStack.class.getField("item"));
            Field f = Item.class.getField("icon");
            this.image = (SelectorImage) previous.getUIElementForField(f, f.getAnnotation(Property.class), p);

            f = Item.class.getField("cooldownBase");
            this.cooldown = (TextBox) previous.getUIElementForField(f, f.getAnnotation(Property.class), p);

            p = new FieldPointer<>(this, this.getClass().getField("itemStack"));
            f = Item.ItemStack.class.getField("stackSize");
            this.amount = (TextBox) previous.getUIElementForField(f, f.getAnnotation(Property.class), p);

            f = Item.ItemStack.class.getField("maxStackSize");
            this.maxAmount = (TextBox) previous.getUIElementForField(f, f.getAnnotation(Property.class), p);

            this.image.posX = this.centerX - this.objXSpace * 0.5;
            this.image.posY = this.centerY - this.objYSpace * 0.5;

            this.cooldown.posX = this.centerX + this.objXSpace * 0.5;
            this.cooldown.posY = this.centerY - this.objYSpace * 0.5;

            this.amount.posX = this.centerX - this.objXSpace * 0.5;
            this.amount.posY = this.centerY + this.objYSpace * 1;

            this.maxAmount.posX = this.centerX + this.objXSpace * 0.5;
            this.maxAmount.posY = this.centerY + this.objYSpace * 1;
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        itemName = new TextBox(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Item save name", () ->
        {
            if (itemName.inputText.equals(""))
                itemName.inputText = itemName.previousInputText;
            updateSaveButton();
        }
                , itemStack.item.name);

        itemName.enableCaps = true;
        updateSaveButton();
    }

    public void updateSaveButton()
    {
        String n = itemName.inputText.replace(" ", "_") + ".tanks";
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + n);

        if (file.exists())
            save.setText("Overwrite item");
        else
            save.setText("Save item");
    }

    public void update()
    {
        this.quit.update();
        this.save.update();
        this.itemName.update();
        this.image.update();
        this.cooldown.update();
        this.amount.update();
        this.maxAmount.update();
    }

    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Set item properties");

        this.quit.draw();
        this.save.draw();
        this.itemName.draw();

        this.maxAmount.draw();
        this.amount.draw();
        this.cooldown.draw();
        this.image.draw();
    }
}
