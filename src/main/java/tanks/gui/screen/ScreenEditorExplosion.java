package tanks.gui.screen;

import tanks.tank.Explosion;
import tanks.tankson.FieldPointer;

public class ScreenEditorExplosion extends ScreenEditorTanksONable<Explosion>
{
    public ScreenEditorExplosion(FieldPointer<Explosion> explosion, Screen screen)
    {
        super(explosion, screen);

        this.title = "Edit %s";
        this.objName = "explosion";
    }

    @Override
    public void setupTabs()
    {
        Tab tab = new Tab(this, "Explosion properties", "");
        this.setTab(tab);
    }
}
