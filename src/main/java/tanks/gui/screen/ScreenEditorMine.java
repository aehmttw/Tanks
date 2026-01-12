package tanks.gui.screen;

import tanks.tank.Mine;
import tanks.tank.MinePropertyCategory;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

public class ScreenEditorMine extends ScreenEditorTanksONable<Mine>
{
    public ScreenEditorItem screenEditorItem;

    public ScreenEditorMine(Pointer<Mine> mine, Screen screen)
    {
        super(mine, screen);

        this.title = "Edit %s";
        this.objName = "mine";
    }

    @Override
    public void setupTabs()
    {
        this.iconPrefix = "mineeditor";

        Tab tab = new Tab(this, "Mine properties", MinePropertyCategory.mine);
        new Tab(this, "Colors", MinePropertyCategory.colors).rows = 3;

        this.setTab(tab);
    }

    @Override
    public void sortTopLevelTabs()
    {
        super.sortTopLevelTabs();
        this.topLevelButtons.get(0).image = "mine.png";
        this.topLevelButtons.get(0).imageSizeX *= 0.8;
        this.topLevelButtons.get(0).imageSizeY *= 0.8;

        this.topLevelButtons.get(1).imageSizeX *= 0.8;
        this.topLevelButtons.get(1).imageSizeY *= 0.8;
    }

    @Override
    public void validateChangedProperty(Pointer<?> f, Property p, Object oldValue)
    {
        if (this.screenEditorItem != null)
            this.screenEditorItem.validateChangedProperty(f, p, oldValue);
        else
            super.validateChangedProperty(f, p, oldValue);
    }
}
