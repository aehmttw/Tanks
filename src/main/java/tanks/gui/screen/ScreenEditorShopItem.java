package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tankson.FieldPointer;
import tanks.tankson.MonitoredFieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.lang.reflect.Field;

public class ScreenEditorShopItem extends ScreenEditorItem
{
    public ScreenEditorShopItem(Pointer<Item.ShopItem> item, Screen screen) throws NoSuchFieldException
    {
        super(new MonitoredFieldPointer<>(item.get(), item.get().getClass().getField("itemStack"), () ->
        {
            if (item.get().itemStack == null)
                item.set(null);
        }), screen);
        Field price = item.get().getClass().getField("price");
        this.itemProperties.uiElements.add(getUIElementForField(price, price.getAnnotation(Property.class), item));
        this.itemProperties.sortUIElements();
    }
}
