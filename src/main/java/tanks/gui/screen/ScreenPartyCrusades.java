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

        ScreenPartyHost.chatbox.update();
    }

    @Override
    public void draw()
    {
        super.draw();

        ScreenPartyHost.chatbox.draw();

        Drawing.drawing.setColor(0, 0, 0);

        long time = System.currentTimeMillis();
        for (int i = 0; i < ScreenPartyHost.chat.size(); i++)
        {
            ChatMessage c = ScreenPartyHost.chat.get(i);
            if (time - c.time <= 30000 || ScreenPartyHost.chatbox.selected)
            {
                Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
            }
        }
    }
}
