package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectRotation;
import tanks.tank.Tank;

public class SelectorRotation<T extends GameObject> extends SelectorNumber<T>
{
    @Override
    protected void init()
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

    public Double getObject()
    {
        return super.getObject() / (Math.PI / 2);
    }

    public void setObject(Double o)
    {
        super.setObject(o * (Math.PI / 2));
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectRotation(Game.screen, editor, this);
    }
}
