package tanksonline.screen;

import tanks.Game;
import tanks.event.online.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.ScreenOnline;
import tanksonline.TanksOnlineServerHandler;

public abstract class ScreenSpecial extends ScreenLayout
{
    public String id;

    public ScreenSpecial(TanksOnlineServerHandler player, String id)
    {
        super(player);
        this.id = id;
    }

    @Override
    public void setScreen()
    {
        this.player.sendEvent(new EventSetScreen(this.id));
        this.player.sendEvent(new EventSetMusic(this.music, Game.musicVolume, true, musicID, 500));

        this.player.screen = this;

        for (int i = 0; i < this.shapes.size(); i++)
        {
            ScreenOnline.Shape s = this.shapes.get(i);
            this.player.sendEvent(new EventAddShape(i, s));
        }

        for (int i = 0; i < this.texts.size(); i++)
        {
            ScreenOnline.Text t = this.texts.get(i);
            this.player.sendEvent(new EventAddText(i, t));
        }

        for (int i = 0; i < this.buttons.size(); i++)
        {
            Button b = this.buttons.get(i);
            this.player.sendEvent(new EventAddButton(i, b));
        }

        for (int i = 0; i < this.textBoxes.size(); i++)
        {
            TextBox t = this.textBoxes.get(i);
            this.player.sendEvent(new EventAddTextBox(i, t));
        }
    }
}
