package tanks.gui.screen;

import tanks.item.Item;
import tanks.tankson.FieldPointer;
import tanks.tankson.MonitoredFieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.lang.reflect.Field;

public class ScreenEditorCrusadeShopItem extends ScreenEditorItem
{
    public ScreenEditorCrusadeShopItem(Pointer<Item.CrusadeShopItem> item, Screen screen) throws NoSuchFieldException
    {
        super(new MonitoredFieldPointer<>(item.get(), item.get().getClass().getField("itemStack"), () ->
        {
            if (item.get().itemStack == null)
                item.set(null);
        }), screen);

        Field price = item.get().getClass().getField("price");
        this.itemProperties.uiElements.add(getUIElementForField(new FieldPointer<>(item.get(), price), price.getAnnotation(Property.class)));

        Field level = item.get().getClass().getField("levelUnlock");
        this.itemProperties.uiElements.add(getUIElementForField(new FieldPointer<>(item.get(), level), level.getAnnotation(Property.class)));

        this.itemProperties.sortUIElements();
    }
}
