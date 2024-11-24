package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ITrigger;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tank.Explosion;
import tanks.tankson.FieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.io.IOException;
import java.lang.reflect.Field;

public class ScreenEditorItem extends ScreenEditorTanksONable<Item.ItemStack<?>>
{
    public TabItemProperties itemProperties;
    public ScreenEditorTanksONable<?> objectEditorScreen = null;
    public Button itemTabButton = new Button(this.centerX, 175, 350, 40, "Item properties", () ->
    {
        this.setTab(itemProperties);
        this.objectEditorScreen.currentTab = null;
    });

    public boolean writeItem(Item.ItemStack<?> t)
    {
        return this.writeItem(t, false);
    }

    public boolean writeItem(Item.ItemStack<?> t, boolean overwrite)
    {
        BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.itemDir + "/" + t.item.name.replace(" ", "_") + ".tanks");

        if (!f.exists() || overwrite)
        {
            try
            {
                if (!f.exists())
                    f.create();

                f.startWriting();
                f.println(t.toString());
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

    public void writeItemAndConfirm(Item.ItemStack<?> i, boolean overwrite)
    {
        if (this.writeItem(i, overwrite))
            Game.screen = new ScreenItemSavedInfo(this, i);
        else
            Game.screen = new ScreenItemSaveOverwrite(this, i);
    }

    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template", () ->
    {
        Item.ItemStack<?> t = target.get();
        this.writeItemAndConfirm(t, false);
    }
    );

    public ScreenEditorItem(Pointer<Item.ItemStack<?>> itemStack, Screen screen)
    {
        super(itemStack, screen);

        this.title = "Edit %s";

        this.itemTabButton.image = "item.png";
        this.itemTabButton.drawImageShadow = true;
        this.itemTabButton.imageSizeX = 40;
        this.itemTabButton.imageSizeY = 40;
        this.itemTabButton.imageXOffset = -145;

        try
        {
            this.objName = itemStack.get().item.getClass().getField("item_class_name").get(null) + " item";

            Item.ItemStack<?> is = itemStack.get();
            Item item = is.item;

            if (is instanceof ItemBullet.ItemStackBullet)
            {
                this.objectEditorScreen = new ScreenEditorBullet(new FieldPointer<>(item, item.getClass().getField("bullet"), false), this.prevScreen);
                ((ScreenEditorBullet) this.objectEditorScreen).bulletTypes.posX += 20;
            }
            else if (is instanceof ItemMine.ItemStackMine)
            {
                this.objectEditorScreen = new ScreenEditorMine(new FieldPointer<>(item, item.getClass().getField("mine"), false), this.prevScreen);
                this.objectEditorScreen.forceDisplayTabs = true;
                Button b = this.objectEditorScreen.topLevelButtons.get(0);
                b.posX = this.itemTabButton.posX;
                b.posY = this.itemTabButton.posY - 60;
                b.sizeX = this.itemTabButton.sizeX;
                b.imageXOffset = this.itemTabButton.imageXOffset;
            }

            if (this.objectEditorScreen != null)
            {
                this.objectEditorScreen.currentTab = null;
                this.objectEditorScreen.title += " item";
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        this.delete.function = () ->
        {
            setTarget(null);
            this.quit.function.run();
        };

        this.deleteText = "Delete item";
        this.showDeleteObj = false;
        this.delete.setText("Delete item");
    }

    public class TabItemProperties extends Tab
    {
        public TabItemProperties(ScreenEditorTanksONable<Item.ItemStack<?>> screen, String name, String category)
        {
            super(screen, name, category);
        }

        public void addFields()
        {
            try
            {
                this.uiElements.clear();

                Item i = this.screen.target.get().item;

                // Item name, icon, cooldown
                FieldPointer<Item> ip = new FieldPointer<>(screen.target.get(), screen.target.getType().getField("item"));
                for (Field f : i.getClass().getFields())
                {
                    if (f.getDeclaringClass().equals(Item.class))
                    {
                        Property p = f.getAnnotation(Property.class);
                        if (p != null && p.category().equals(this.category))
                        {
                            this.uiElements.add(screen.getUIElementForField(f, p, ip));
                        }
                    }
                }

                // Move the cooldown to be after stack size and max stack size
                ITrigger cooldown = this.uiElements.remove(this.uiElements.size() - 1);
                for (Field f : this.screen.fields)
                {
                    Property p = f.getAnnotation(Property.class);
                    if (p != null && p.category().equals(this.category) && !p.id().equals("item"))
                    {
                        this.uiElements.add(screen.getUIElementForField(f, p, screen.target));
                    }
                }
                this.uiElements.add(cooldown);

                // Other per-item settings
                for (Field f : i.getClass().getFields())
                {
                    if (!f.getDeclaringClass().equals(Item.class))
                    {
                        Property p = f.getAnnotation(Property.class);
                        if (p != null && p.category().equals(this.category))
                        {
                            this.uiElements.add(screen.getUIElementForField(f, p, ip));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }
    }

    @Override
    public void setupTabs()
    {
        this.itemProperties = new TabItemProperties(this, "Item properties", "");
        this.setTab(this.itemProperties);
    }

    @Override
    public void draw()
    {
        if (this.objectEditorScreen != null)
        {
            this.objectEditorScreen.draw();

            if (this.objectEditorScreen.currentTab == null)
            {
                Drawing.drawing.setInterfaceFontSize(this.titleSize);

                if (Level.isDark())
                    Drawing.drawing.setColor(255, 255, 255);
                else
                    Drawing.drawing.setColor(0, 0, 0);

                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 200, this.currentTab.name);
                this.currentTab.draw();
            }

            this.itemTabButton.draw();
        }
        else
            super.draw();

        this.delete.draw();
        this.save.draw();
    }

    @Override
    public void update()
    {
        if (this.objectEditorScreen != null)
        {
            this.objectEditorScreen.onComplete = this.onComplete;
            this.objectEditorScreen.update();

            if (this.objectEditorScreen.currentTab == null)
                this.currentTab.update();

            this.itemTabButton.enabled = this.objectEditorScreen.currentTab != null;
            this.itemTabButton.update();
        }
        else
            super.update();

        this.delete.update();
        this.save.update();
    }
}
