package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.item.Item;
import tanks.tank.Tank;

import java.util.ArrayList;

public class ScreenItemSavedInfo extends Screen implements IBlankBackgroundScreen
{
    public Screen previous;

    public int pageEntries = 10;

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Ok", () ->
    {
        Game.screen = this.previous;
    }
    );

    public ScreenItemSavedInfo(Screen s)
    {
        this.previous = s;
        this.music = this.previous.music;
        this.musicID = this.previous.musicID;
    }

    @Override
    public void update()
    {
        this.quit.update();
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        this.quit.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace, "Item saved to templates!");
    }
}
