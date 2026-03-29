package tanks.gui;

import tanks.gui.screen.ScreenSelector;
import tanks.item.Item;
import tanks.item.ItemIcon;

import java.util.ArrayList;
import java.util.Collection;

public class SelectorItemIcon extends SelectorImage
{
    public ItemIcon selectedIcon;
    public Item itemBeingEdited;

    public SelectorItemIcon(double x, double y, double sX, double sY, String text, Collection<ItemIcon> o, Runnable f)
    {
        super(x, y, sX, sY, text, null, f);
        this.setItemIcons(o);
    }

    public SelectorItemIcon(double x, double y, double sX, double sY, String text, Collection<ItemIcon> o, Runnable f, String hoverText)
    {
        super(x, y, sX, sY, text, null, f, hoverText);
        this.setItemIcons(o);
    }

    public SelectorItemIcon(double x, double y, double sX, double sY, String text, Collection<ItemIcon> o)
    {
        super(x, y, sX, sY, text, null);
        this.setItemIcons(o);
    }

    public SelectorItemIcon(double x, double y, double sX, double sY, String text, Collection<ItemIcon> o, String hoverText)
    {
        super(x, y, sX, sY, text, null, hoverText);
        this.setItemIcons(o);
    }

    public void setItemIcons(Collection<ItemIcon> in)
    {
        ArrayList<String> icons = new ArrayList<>();
        this.itemIcons = new ItemIcon[in.size()];

        int index = 0;
        for (ItemIcon i: in)
        {
            this.itemIcons[index] = i.getCopy();
            icons.add(i.idName);
            index++;
        }

        String[] iconsArray = new String[icons.size()];
        icons.toArray(iconsArray);
        this.options = iconsArray;
    }

    public void draw()
    {
        super.draw();
        if (selectedIcon != null)
        {
            double m = 0.8;
            this.selectedIcon.drawInterfaceImage(posX - sizeX / 2 + sizeY * 7 / 8, posY - sizeY * 3 / 8, sizeY * (3.0 / 4 + m), sizeY * (3.0 / 4 + m));
        }
    }

    @Override
    public ScreenSelector getSelectorScreen()
    {
        ScreenSelector s = super.getSelectorScreen();
        s.drawItemIcons = true;
        s.beforeItems = 1;
        s.itemBeingEdited = this.itemBeingEdited;
        s.defaultIcon = this.itemBeingEdited.getAutomaticIcon();

        Button b1 = new Button(0, 0, s.objWidth, s.objHeight, "", () ->
        {
            ItemIcon ii = this.itemBeingEdited.getAutomaticIcon();
            ((SelectorItemIcon) (s.selector)).selectedIcon = ii;
            this.itemBeingEdited.autoIcon = true;

            s.selector.selectedOption = ii.registryIndex;
            s.selector.itemIcons[ii.registryIndex] = ii;

            if (s.iconColors != null)
                s.iconColors.refreshButtons();
        }, "Automatically pick an icon for this item---based on what the item might look---like when used ingame.");
        b1.itemIcon = s.defaultIcon;
        b1.setSubtext("Auto");
        b1.fullInfo = true;
        s.buttonList.buttons.add(0, b1);
        s.buttonList.sortButtons();

        return s;
    }

}
