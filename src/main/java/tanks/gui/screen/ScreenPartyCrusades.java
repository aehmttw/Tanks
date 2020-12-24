package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenPartyCrusades extends ScreenCrusades
{
    public ScreenPartyCrusades()
    {
        this.quit2 = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
        {
            @Override
            public void run()
            {
                Game.screen = ScreenPartyHost.activeScreen;
            }
        }
        );
    }

    @Override
    public void update()
    {
        super.update();
    }

    @Override
    public void draw()
    {
        super.draw();
    }

    @Override
    public void setupLayoutParameters()
    {
        if (Drawing.drawing.interfaceScaleZoom > 1)
            this.centerY -= this.objYSpace / 2;
    }
}
