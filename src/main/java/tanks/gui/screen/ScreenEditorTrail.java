package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.Trail;
import tanks.tank.Mine;
import tanks.tankson.Pointer;

import java.util.ArrayList;

public class ScreenEditorTrail extends ScreenEditorTanksONable<Trail>
{
    public ArrayList<Trail> allTrails;

    public ScreenEditorTrail(ArrayList<Trail> trails, Pointer<Trail> trail, Screen screen)
    {
        super(trail, screen);

        this.allTrails = trails;
        this.title = "Edit %s";
        this.objName = "trail";
    }

    @Override
    public void setupTabs()
    {
        Tab tab = new Tab(this, "Trail properties", "");
        this.setTab(tab);
    }

    @Override
    public void sortTopLevelTabs()
    {
        super.sortTopLevelTabs();
        this.topLevelButtons.get(0).image = "bullet_normal.png";
        this.topLevelButtons.get(0).imageSizeX *= 0.8;
        this.topLevelButtons.get(0).imageSizeY *= 0.8;
    }

    @Override
    public void setTarget(Trail value)
    {
        super.setTarget(value);
        this.quit.function.run();
    }

    @Override
    public void draw()
    {
        super.draw();

        double max = 0;
        for (Trail t: this.allTrails)
        {
            max = Math.max(max, t.maxLength + t.delay);
        }

        double length = Math.min(Drawing.drawing.interfaceSizeX * 0.6, max * Bullet.bullet_size);
        double start = Drawing.drawing.interfaceSizeX / 2 - length / 2;
        double end = Drawing.drawing.interfaceSizeX / 2 + length / 2;
        for (Trail t: this.allTrails)
        {
            t.drawForInterface(start, end, 90, Bullet.bullet_size, max);
        }

        this.target.get().drawForInterface(start, end, 150, Bullet.bullet_size, max);
        this.target.get().drawForInterface(start, end, 90, Bullet.bullet_size, max, true);
    }

    @Override
    public void drawTitle()
    {
        Drawing.drawing.displayInterfaceText(this.centerX, 30, "Edit trail");
    }
}
