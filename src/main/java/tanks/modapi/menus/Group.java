package tanks.modapi.menus;

import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.modapi.ModAPI;

public class Group extends FixedMenu
{
    public FixedMenu[] menus;
    public boolean async;
    public boolean isInterface;

    int menuNum = 0;
    long defineTime = System.currentTimeMillis();

    public Group(FixedMenu... menus)
    {
        this(true, false, menus);
    }

    public Group(boolean displayAsync, FixedMenu... menus)
    {
        this(true, false, menus);
    }

    public Group(boolean displayAsync, boolean isInterface, FixedMenu... menus)
    {
        this.menus = menus;
        this.isInterface = isInterface;
        this.async = displayAsync;

        ModAPI.menuGroup.add(this);
    }

    @Override
    public void draw()
    {
        if (this.isInterface && Game.screen instanceof ScreenGame)
            for (FixedMenu m : menus)
                ((ScreenGame) Game.screen).drawables[m.drawLevel].add(m);

        if (this.async)
            for (FixedMenu m : menus)
                m.draw();

        else
        {
            if (menus[menuNum].afterGameStarted && !(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing))
                return;

            menus[menuNum].draw();
        }
    }

    @Override
    public void update()
    {
        if (this.async)
        {
            for (FixedMenu m : menus)
                m.update();
            return;
        }

        if (menus[menuNum].afterGameStarted && !(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing))
            defineTime = System.currentTimeMillis();

        if (System.currentTimeMillis() - defineTime > menus[menuNum].duration)
        {
            if (menuNum < menus.length - 1)
                menuNum++;
            else
                ModAPI.removeMenus.add(this);
        }
    }
}
