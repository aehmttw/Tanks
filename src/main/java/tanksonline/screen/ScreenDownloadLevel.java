package tanksonline.screen;

import tanks.Game;
import tanks.event.online.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.ScreenOnline;
import tanksonline.TanksOnlineServerHandler;
import tanksonline.UploadedLevel;

public class ScreenDownloadLevel extends ScreenLayout
{
    public UploadedLevel level;
    public String name;

    public ScreenLayout screen;
    public ScreenLayout instance = this;

    Button quit = new Button(sizeX - 560, sizeY - 50, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            screen.setScreen();
            player.sendEvent(new EventCleanUp());
        }
    }
    );

    Button delete = new Button(200, sizeY - 50, 350, 40, "Delete", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenLayout s = new ScreenConfirmRemoveLevel(player, level, instance);
            s.setScreen();

            player.sendEvent(new EventCleanUp());
        }
    }
    );

    public ScreenDownloadLevel(TanksOnlineServerHandler player, UploadedLevel level)
    {
        super(player);
        this.screen = player.screen;

        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        quit.wait = true;
        quit.xAlignment = 1;
        quit.yAlignment = 1;
        delete.xAlignment = -1;
        delete.yAlignment = 1;
        this.buttons.add(quit);

        this.name = level.name;
        this.level = level;

        this.delete.wait = true;
        if (player.computerID.equals(level.creator))
            this.buttons.add(delete);
    }

    @Override
    public void setScreen()
    {
        this.player.screen = this;

        this.player.sendEvent(new EventSetMusic(this.music, Game.musicVolume, true, musicID, 500));

        this.player.sendEvent(new EventSendLevelToDownload(this.name, level.level));

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
