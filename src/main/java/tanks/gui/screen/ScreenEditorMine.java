package tanks.gui.screen;

import tanks.tank.Mine;
import tanks.tankson.FieldPointer;
import tanks.tankson.Pointer;

public class ScreenEditorMine extends ScreenEditorTanksONable<Mine>
{
    public ScreenEditorMine(Pointer<Mine> mine, Screen screen)
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

    @Override
    public void sortTopLevelTabs()
    {
        super.sortTopLevelTabs();
        this.topLevelButtons.get(0).image = "mine.png";
        this.topLevelButtons.get(0).imageSizeX *= 0.8;
        this.topLevelButtons.get(0).imageSizeY *= 0.8;
    }
}
