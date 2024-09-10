package tanks.gui.screen;

import tanks.tank.Mine;
import tanks.tankson.FieldPointer;

public class ScreenEditorMine extends ScreenEditorTanksONable<Mine>
{
    public ScreenEditorMine(FieldPointer<Mine> mine, Screen screen)
    {
        super(mine, screen);

        this.title = "Edit %s";
        this.objName = "mine";
    }

    @Override
    public void setupTabs()
    {
        Tab tab = new Tab(this, "Mine properties", "");
        this.setTab(tab);
    }
}
