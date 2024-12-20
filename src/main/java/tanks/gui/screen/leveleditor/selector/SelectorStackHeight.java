package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.gui.screen.leveleditor.OverlayBlockHeight;
import tanks.obstacle.Obstacle;

public class SelectorStackHeight extends SelectorNumber<Obstacle>
{
    protected void init()
    {
        this.id = "stack_height";
        this.title = "Block height";
        this.objectProperty = "stackHeight";

        this.min = 0.5;
        this.max = Obstacle.default_max_height;
        this.defaultNum = 1;
        this.step = 0.5;
        this.image = "obstacle_height.png";
        this.buttonText = "Block height: %.1f";
        this.keybind = Game.game.input.editorHeight;
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlayBlockHeight(Game.screen, editor, this);
    }
}
