package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.obstacle.Obstacle;

public class SelectorGroupID extends SelectorNumber<Obstacle>
{
    public void init()
    {
        this.id = "group_id";
        this.title = "Group ID";
        this.objectProperty = "groupID";

        this.keybind = Game.game.input.editorGroupID;
        this.format = "%.0f";
        this.buttonText = "Group ID: %.0f";
        this.image = "id.png";
        this.min = 0;
    }
}