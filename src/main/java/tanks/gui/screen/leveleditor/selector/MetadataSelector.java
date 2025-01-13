package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.tankson.MetadataProperty;

import java.lang.reflect.Field;

public abstract class MetadataSelector
{
    public Field metadataField;
    public MetadataProperty metadataProperty;
    public String id;

    public MetadataSelector(Field f)
    {
        this.metadataField = f;
        this.metadataProperty = f.getAnnotation(MetadataProperty.class);
    }

    public void setMetadata(ScreenLevelEditor ed, GameObject o, Object value)
    {
        try
        {
            if (ed != null)
            {
                ed.currentMetadata.put(this.id, value);
                ed.setMousePlaceable();
            }

            this.metadataField.set(o, value);
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public Object getMetadata(GameObject o)
    {
        try
        {
            return this.metadataField.get(o);
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
        return null;
    }

    public abstract void changeMetadata(ScreenLevelEditor e, GameObject o, int add);

    public abstract void openEditorOverlay(ScreenLevelEditor editor);

    public String getMetadataDisplayString(GameObject o)
    {
        return this.getMetadata(o) + "";
    }

}
