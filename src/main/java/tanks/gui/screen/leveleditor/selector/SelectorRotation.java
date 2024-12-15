package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectRotation;
import tanks.tank.Tank;

import java.util.Objects;

public class SelectorRotation<T extends GameObject> extends SelectorNumber<T>
{
    @Override
    public void init()
    {
        this.id = "rotation";
        this.keybind = Game.game.input.editorRotate;
        this.loop = true;
        this.format = "%.0f";
        this.min = 0;
        this.max = 4;

        if (gameObject instanceof Tank)
        {
            this.objectProperty = "angle";
            this.image = "rotate_tank.png";
            this.title = "Select tank orientation";
            this.buttonText = "Tank orientation";
        }
        else
        {
            this.objectProperty = "rotation";
            this.image = "rotate_obstacle.png";
            this.title = "Select obstacle orientation";
            this.buttonText = "Obstacle rotation";
        }
    }

    public void update()
    {
        if (!this.init)
            return;

        try
        {
            Object sel = getPropertyBase();
            Object obj = getObjectProp();

            if (!Objects.equals(sel, prevObject))
            {
                if (gameObject instanceof Tank)
                    ((Tank) gameObject).orientation = this.number * Math.PI / 2;

                objPropField.set(gameObject, sel);
                gameObject.onPropertySet(this);
                prevObject = sel;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object getProperty()
    {
        return this.number * Math.PI / 2;
    }

    public void setProperty(Object o)
    {
        super.setProperty(((double) o) / (Math.PI / 2));
    }

    @Override
    public String getMetadata()
    {
        return super.getMetadata();
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectRotation(Game.screen, editor, this);
    }
}
