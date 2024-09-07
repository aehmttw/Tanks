package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tank.Explosion;
import tanks.tankson.FieldPointer;
import tanks.tankson.Property;

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

    public ScreenEditorItem(FieldPointer<Item.ItemStack<?>> itemStack, Screen screen)
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
                this.objectEditorScreen = new ScreenEditorBullet(new FieldPointer<>(item, item.getClass().getField("bullet"), false), Game.screen);
                ((ScreenEditorBullet) this.objectEditorScreen).bulletTypes.posX += 20;
            }
            else if (is instanceof ItemMine.ItemStackMine)
                this.objectEditorScreen = new ScreenEditorMine(new FieldPointer<>(item, item.getClass().getField("mine"), false), Game.screen);

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
                for (Field f : this.screen.fields)
                {
                    Property p = f.getAnnotation(Property.class);
                    if (p != null && p.category().equals(this.category))
                    {
                        this.uiElements.add(screen.getUIElementForField(f, p, screen.target));
                    }
                }

                Item i = this.screen.target.get().item;
                FieldPointer<Item> ip = new FieldPointer<>(screen.target.get(), screen.target.getType().getField("item"));
                for (Field f : i.getClass().getFields())
                {
                    Property p = f.getAnnotation(Property.class);
                    if (p != null && p.category().equals(this.category))
                    {
                        this.uiElements.add(screen.getUIElementForField(f, p, ip));
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

    }

    @Override
    public void update()
    {
        if (this.objectEditorScreen != null)
        {
            this.objectEditorScreen.update();

            if (this.objectEditorScreen.currentTab == null)
                this.currentTab.update();

            this.itemTabButton.enabled = this.objectEditorScreen.currentTab != null;
            this.itemTabButton.update();
        }
        else
            super.update();
    }
}
