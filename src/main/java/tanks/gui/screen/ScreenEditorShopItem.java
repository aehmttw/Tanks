package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Game;
import tanks.gui.Button;
import tanks.item.Item;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tankson.FieldPointer;
import tanks.tankson.MonitoredFieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.io.IOException;
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
        this.itemProperties.uiElements.add(getUIElementForField(new FieldPointer<>(item.get(), price), price.getAnnotation(Property.class)));
        this.itemProperties.sortUIElements();
    }
}
