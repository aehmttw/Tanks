package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenOptionsSound extends Screen
{
    public static final String soundEffectsText = "Sound effects: ";
    public static final String musicText = "Music: ";

    public ScreenOptionsSound()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";

        if (Game.musicEnabled && Game.game.window.soundsEnabled)
            musicToggle.text = musicText + ScreenOptions.onText;
        else
            musicToggle.text = musicText + ScreenOptions.offText;

        if (Game.soundsEnabled && Game.game.window.soundsEnabled)
            soundEffects.text = soundEffectsText + ScreenOptions.onText;
        else
            soundEffects.text = soundEffectsText + ScreenOptions.offText;
    }

    Button soundEffects = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.soundsEnabled = !Game.soundsEnabled;

            if (Game.soundsEnabled)
                soundEffects.text = soundEffectsText + ScreenOptions.onText;
            else
                soundEffects.text = soundEffectsText + ScreenOptions.offText;
        }
    });

    Button musicToggle = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.musicEnabled = !Game.musicEnabled;

            if (Game.musicEnabled)
            {
                musicToggle.text = musicText + ScreenOptions.onText;
                Panel.panel.playScreenMusic(0);
            }
            else
            {
                musicToggle.text = musicText + ScreenOptions.offText;
                Drawing.drawing.stopMusic();
            }
        }
    });

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    }
    );

    @Override
    public void update()
    {
        soundEffects.update();
        musicToggle.update();

        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        soundEffects.draw();
        musicToggle.draw();

        back.draw();
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Sound options");
    }
}
