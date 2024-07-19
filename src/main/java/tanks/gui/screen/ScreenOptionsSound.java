package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenOptionsSound extends Screen
{
    public static final String layeredMusicText = "Layered music: ";

    public ScreenOptionsSound()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        musicVolume.allowLetters = false;
        musicVolume.allowSpaces = false;
        musicVolume.maxChars = 3;
        musicVolume.maxValue = 100;
        musicVolume.checkMaxValue = true;
        musicVolume.integer = true;

        musicVolume.r1 = 210;
        musicVolume.g1 = 210;
        musicVolume.b1 = 210;

        soundVolume.allowLetters = false;
        soundVolume.allowSpaces = false;
        soundVolume.maxChars = 3;
        soundVolume.maxValue = 100;
        soundVolume.checkMaxValue = true;
        soundVolume.integer = true;

        soundVolume.r1 = 210;
        soundVolume.g1 = 210;
        soundVolume.b1 = 210;

        if (Game.enableLayeredMusic)
            layeredMusic.setText(layeredMusicText, ScreenOptions.onText);
        else
            layeredMusic.setText(layeredMusicText, ScreenOptions.offText);
    }

    TextBoxSlider musicVolume = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 0.25, this.objWidth, this.objHeight, "Music volume", new Runnable()
    {
        @Override
        public void run()
        {
            if (musicVolume.inputText.length() <= 0)
                musicVolume.inputText = musicVolume.previousInputText;

            Game.musicVolume = Integer.parseInt(musicVolume.inputText) / 100f;

            Game.musicEnabled = Game.musicVolume > 0;

            if (Game.musicEnabled)
                Panel.panel.playScreenMusic(0);
            else
                Drawing.drawing.stopMusic();
        }
    }
            , Math.round(Game.musicVolume * 100f), 0, 100, 1);


    TextBoxSlider soundVolume = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace * 1.25, this.objWidth, this.objHeight, "Sound volume", new Runnable()
    {
        @Override
        public void run()
        {
            if (soundVolume.inputText.length() <= 0)
                soundVolume.inputText = soundVolume.previousInputText;

            Game.soundVolume = Integer.parseInt(soundVolume.inputText) / 100f;

            Game.soundsEnabled = Game.soundVolume > 0;
        }
    }
            , Math.round(Game.soundVolume * 100f), 0, 100, 1);

    Button layeredMusic = new Button(this.centerX, this.centerY + this.objYSpace * 1.25, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.enableLayeredMusic = !Game.enableLayeredMusic;

            if (Game.enableLayeredMusic)
                layeredMusic.setText(layeredMusicText, ScreenOptions.onText);
            else
                layeredMusic.setText(layeredMusicText, ScreenOptions.offText);
        }
    },
            "When layered music is enabled, different---" +
                    "instruments will be added to the soundtrack---" +
                    "based on criteria such as remaining tank---" +
                    "types or arcade rampage level.--- ---" +
                    "This may cause lag and desynchronization---" +
                    "of music on some devices.");


    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
    );

    @Override
    public void update()
    {
        soundVolume.update();
        musicVolume.update();
        layeredMusic.update();

        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        back.draw();

        layeredMusic.draw();
        musicVolume.draw();
        soundVolume.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Sound options");
    }
}
