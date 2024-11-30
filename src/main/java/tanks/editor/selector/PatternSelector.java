package tanks.editor.selector;

import tanks.Game;
import tanks.gui.screen.leveleditor.OverlayBeatBlockPattern;

public class PatternSelector extends GroupIdSelector
{
    public void init()
    {
        super.init();
        this.id = "pattern";
        this.title = "Beat pattern";
        this.buttonText = "Beat pattern: ";
        this.image = "obstacle_beat.png";
        this.max = 8;
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlayBeatBlockPattern(Game.screen, editor, this);
    }

    @Override
    public void load()
    {
        this.button.setText(this.buttonText, getMetadata());
    }

    @Override
    public void setMetadata(String d)
    {
        this.number = Math.log(Integer.parseInt(String.valueOf(d.charAt(0)))) / Math.log(2) * 2 + (d.charAt(1) == 'a' ? 0 : 1);
    }

    @Override
    public String getMetadata()
    {
        return (int) Math.pow(2, (int) (number / 2)) + (number % 2 == 0 ? "a" : "b");
    }
}
