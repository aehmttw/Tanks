package tanks.item;

import basewindow.Color;
import tanks.Drawing;
import tanks.Game;
import tanks.tankson.ICopyable;
import tanks.tankson.Property;
import tanks.tankson.TanksONable;

import java.util.ArrayList;
import java.util.Arrays;

@TanksONable("item_icon")
public class ItemIcon implements ICopyable<ItemIcon>
{
    public int registryIndex = -1;

    @Property(id="id")
    public String idName;
    public String baseName;

    public ArrayList<Boolean> alphas;
    @Property(id="colors")
    public ArrayList<Color> colors;

    public ItemIcon()
    {

    }

    public ItemIcon(String idName, String baseName, Color[] colors, Boolean[] alphas)
    {
        this.baseName = baseName;
        this.idName = idName;

        this.colors = new ArrayList<>();
        this.colors.addAll(Arrays.asList(colors));

        this.alphas = new ArrayList<>();
        this.alphas.addAll(Arrays.asList(alphas));
    }

    public ItemIcon(String idName, String iconFileName)
    {
        this.baseName = iconFileName;
        this.idName = idName;
    }

    public void drawInterfaceImage(double x, double y, double sX, double sY)
    {
        this.draw(x, y, 0, sX, sY, false, true);
    }

    public void drawImage(double x, double y, double sX, double sY)
    {
        this.draw(x, y, 0, sX, sY, false, false);
    }

    public void drawImage(double x, double y, double z, double sX, double sY)
    {
        this.draw(x, y, z, sX, sY, true, false);
    }

    protected void draw(double x, double y, double z, double sX, double sY, boolean d3, boolean forInterface)
    {
        double a = Drawing.drawing.currentColorA;
        if (this.colors == null)
        {
            Drawing.drawing.setColor(255, 255, 255, a);

            if (forInterface)
                Drawing.drawing.drawInterfaceImage(this.baseName, x, y, sX, sY);
            else if (!d3)
                Drawing.drawing.drawImage(this.baseName, x, y, sX, sY);
            else
                Drawing.drawing.drawImage(this.baseName, x, y, z, sX, sY);

        }
        else
        {
            for (int i = 0; i < this.colors.size(); i++)
            {
                Color c = colors.get(i);
                Drawing.drawing.setColor(c.red, c.green, c.blue, c.alpha * a / 255);
                if (forInterface)
                    Drawing.drawing.drawInterfaceImage(this.baseName + "_" + (i + 1) + ".png", x, y, sX, sY);
                else if (!d3)
                    Drawing.drawing.drawImage(this.baseName + "_" + (i + 1) + ".png", x, y, sX, sY);
                else
                    Drawing.drawing.drawImage(this.baseName + "_" + (i + 1) + ".png", x, y, z, sX, sY);
            }
        }
        Drawing.drawing.setColor(255, 255, 255, a);
    }

    @Override
    public ItemIcon clonePropertiesTo(ItemIcon i1)
    {
        i1.idName = idName;
        i1.baseName = baseName;
        i1.registryIndex = registryIndex;

        if (colors != null)
        {
            i1.colors = new ArrayList<>();
            for (Color c: this.colors)
                i1.colors.add(new Color().set(c));
        }
        else
            i1.colors = null;

        if (alphas != null)
            i1.alphas = new ArrayList<>(alphas);

        return i1;
    }

    @Override
    public ItemIcon getCopy()
    {
        ItemIcon i1 = new ItemIcon();
        return clonePropertiesTo(i1);
    }

    public void resetColors()
    {
        if (this.colors != null)
        {
            ArrayList<Color> others = Game.registryItemIcon.getItemIcon(this.idName).colors;
            for (int i = 0; i < this.colors.size(); i++)
            {
                this.colors.get(i).set(others.get(i));
            }
        }
    }

    // Registers the item icon into the game's registry
    public ItemIcon register()
    {
        this.registryIndex = Game.registryItemIcon.itemIcons.size();
        Game.registryItemIcon.itemIcons.put(this.idName, this);
        return this;
    }
}
