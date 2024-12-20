package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.gui.screen.leveleditor.OverlayBeatBlockPattern;

public class SelectorBeatPattern extends SelectorGroupID
{
    protected void init()
    {
        super.init();
        this.id = "pattern";
        this.title = "Beat pattern";
        this.buttonText = "Beat pattern: ";
        this.image = "obstacle_beat.png";
        this.max = 7;
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
        setNumber(Math.log(Integer.parseInt(String.valueOf(d.charAt(0)))) / Math.log(2) * 2 + (d.charAt(1) == 'a' ? 0 : 1));
    }

    @Override
    public String getMetadata()
    {
        return (int) Math.pow(2, (int) (number() / 2)) + (number() % 2 == 0 ? "a" : "b");
    }
}
