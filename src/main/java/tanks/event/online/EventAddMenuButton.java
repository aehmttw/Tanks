package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnlineWaiting;
import tanks.gui.screen.ScreenOverlayOnline;

public class EventAddMenuButton extends EventAddButton
{
    public boolean unpause;

    public EventAddMenuButton()
    {
        super();
    }

    public EventAddMenuButton(int id, Button b, boolean unpause)
    {
        super(id, b);
        this.unpause = unpause;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && id >= 0 && id < ScreenOverlayOnline.max_button_count)
        {
            ScreenOverlayOnline s = Panel.panel.onlineOverlay;
            Button b;

            final int buttonID = this.id;
            b = new Button(this.posX, this.posY, this.sizeX, this.sizeY, this.text, new Runnable()
            {
                @Override
                public void run()
                {
                    Game.eventsOut.add(new EventPressedButton(-1 - buttonID));

                    if (wait)
                        Game.screen = new ScreenOnlineWaiting();

                    if (unpause)
                        Panel.onlinePaused = false;
                }
            }, hover);

            b.enabled = this.enabled;
            b.enableHover = !this.hover.equals("");

            s.buttons.put(this.id, b);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeBoolean(this.unpause);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        this.unpause = b.readBoolean();
    }
}
