package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenSelectorMusic;
import tanks.gui.screen.ScreenTankEditor;
import tanks.translation.Translation;

public class SelectorMusic extends Selector
{
    public boolean[] selectedOptions;
    public ScreenTankEditor screen;

    public SelectorMusic(double x, double y, double sX, double sY, String text, String[] o, Runnable f, ScreenTankEditor screen)
    {
        super(x, y, sX, sY, text, o, f);
        this.selectedOptions = new boolean[o.length];
        this.screen = screen;
    }


    @Override
    public void drawSelection()
    {
        String s = "\u00A7127000000255none";

        int numSelected = 0;
        String lastSelected = "";
        for (int i = 0; i < selectedOptions.length; i++)
        {
            if (selectedOptions[i])
            {
                numSelected++;
                lastSelected = options[i];
            }
        }

        if (numSelected == 1)
        {
            if (lastSelected.contains("tank/"))
                s = lastSelected.substring(lastSelected.indexOf("tank/") + "tank/".length(), lastSelected.indexOf(".ogg"));
            else if (lastSelected.contains("arcade/"))
                s = lastSelected.substring(lastSelected.indexOf("arcade/") + "arcade/".length(), lastSelected.indexOf(".ogg"));
            s = Game.formatString(s);
        }
        else if (numSelected > 1)
            s = numSelected + " tracks";


        if (translate)
            Drawing.drawing.drawInterfaceText(posX, posY, Translation.translate(s));
        else
            Drawing.drawing.drawInterfaceText(posX, posY, s);
    }

    @Override
    public void setScreen()
    {
        if (this.screen != null)
        {
            this.screen.tank.musicTracks.clear();
            this.screen.updateMusic();
        }

        ScreenSelectorMusic s = new ScreenSelectorMusic(this, Game.screen);
        s.drawBehindScreen = this.drawBehindScreen;
        Game.screen = s;
    }

}
