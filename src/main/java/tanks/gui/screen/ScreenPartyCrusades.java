package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenPartyCrusades extends ScreenCrusades implements IPartyMenuScreen
{
    public ScreenPartyCrusades()
    {
        this.quit2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
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
}
