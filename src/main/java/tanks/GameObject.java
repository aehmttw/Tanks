package tanks;

import tanks.gui.screen.leveleditor.selector.MetadataSelector;
import tanks.tankson.MetadataProperty;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class GameObject
{
    public double posX;
    public double posY;
    public double posZ;

    public boolean draggable = false;

    public static HashMap<Class<? extends GameObject>, LinkedHashMap<String, MetadataSelector>> metadataPropertiesByClass = new HashMap<>();

    /**
     * If you want a subclass to use a different id for a selector (for example, have the player and ai tank teams use separate selectors in the editor),
     * you can add to the map here to replace a selector id with another one for that subclass specifically.
     */
    public HashMap<String, String> overrideMetadataPropertyIDs = new HashMap<>();

    public String primaryMetadataID = null;
    public String secondaryMetadataID = null;

    public static HashMap<Class<? extends GameObject>, MetadataSelector> primaryMetadataField = new HashMap<>();
    public static HashMap<Class<? extends GameObject>, MetadataSelector> secondaryMetadataField = new HashMap<>();

    public String getMetadata()
    {
        return "";
    }

    public void setMetadata(String meta)
    {

    }

    public void refreshMetadata()
    {

    }

    protected void setupMetadataFields()
    {
        try
        {
            if (!metadataPropertiesByClass.containsKey(this.getClass()))
            {
                LinkedHashMap<String, MetadataSelector> props = new LinkedHashMap<>();
                metadataPropertiesByClass.put(this.getClass(), props);

                for (Field f : this.getClass().getFields())
                {
                    MetadataProperty a = f.getAnnotation(MetadataProperty.class);
                    if (a != null)
                    {
                        MetadataSelector s = Game.registryMetadataSelectors.getEntry(a.selector()).getConstructor(Field.class).newInstance(f);
                        String id = a.id();
                        if (overrideMetadataPropertyIDs.get(id) != null)
                            id = overrideMetadataPropertyIDs.get(id);

                        s.id = id;
                        props.put(id, s);

                        if (id.equals(this.primaryMetadataID))
                            primaryMetadataField.put(this.getClass(), s);

                        if (id.equals(this.secondaryMetadataID))
                            secondaryMetadataField.put(this.getClass(), s);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public HashMap<String, MetadataSelector> getMetadataProperties()
    {
        this.setupMetadataFields();
        return metadataPropertiesByClass.get(this.getClass());
    }

    public MetadataSelector getMetadataProperty(String name)
    {
        this.setupMetadataFields();
        return metadataPropertiesByClass.get(this.getClass()).get(name);
    }

    public MetadataSelector getPrimaryMetadataProperty()
    {
        this.setupMetadataFields();
        return primaryMetadataField.get(this.getClass());
    }

    public MetadataSelector getSecondaryMetadataProperty()
    {
        this.setupMetadataFields();
        return secondaryMetadataField.get(this.getClass());
    }

    public static double distanceBetween(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(sqDistBetw(x1, y1, x2, y2));
    }

    public static double sqDistBetw(double x1, double y1, double x2, double y2)
    {
        return (x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2);
    }

    public static double sqDistBetw(final GameObject a, final GameObject b)
    {
        return sqDistBetw(a.posX, a.posY, b.posX, b.posY);
    }

    public static boolean withinRange(final GameObject a, final GameObject b, double range)
    {
        return sqDistBetw(a, b) < range * range;
    }

    public static double distanceBetween(final GameObject a, final GameObject b)
    {
        return distanceBetween(a.posX, a.posY, b.posX, b.posY);
    }

    public static double angleBetween(double a, double b)
    {
        return (a - b + Math.PI * 3) % (Math.PI*2) - Math.PI;
    }

    public static double absoluteAngleBetween(double a, double b)
    {
        return Math.abs((a - b + Math.PI * 3) % (Math.PI * 2) - Math.PI);
    }
}