package tanks.editor.selector;

import tanks.Game;
import tanks.obstacle.Obstacle;

public class GroupIdSelector extends NumberSelector<Obstacle>
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