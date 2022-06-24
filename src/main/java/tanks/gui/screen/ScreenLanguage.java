package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ScreenLanguage extends Screen
{
    public static int page = 0;

    public SavedFilesList languages;

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle()
    );

    public ScreenLanguage()
    {
        super(350, 40, 380, 60);

        this.music = "menu_options.ogg";
        this.musicID = "menu";

        languages = new SavedFilesList(Game.homedir + Game.languagesPath, page, 0, -30,
                (name, file) ->
                {
                    changeLanguage(new Translation(file));
                }, (file) -> null, (file, button) ->
                {
                    try
                    {
                        file.startReading();
                        button.text = file.nextLine();
                        file.stopReading();
                    }
                    catch (Exception e)
                    {

                    }
                }, ".lang");

        languages.buttons.add(0, new Button(0, 0, 350, 40, "English", () ->
        {
            changeLanguage(null);
        }
        ));

        languages.buttons.add(1, new Button(0, 0, 350, 40, "Français", () ->
        {
            changeLanguage(new Translation("fr.lang"));
        }
        ));

        /*languages.buttons.add(2, new Button(0, 0, 350, 40, "Español", () ->
        {
            changeLanguage(new Translation("es.lang"));
        }
        ));*/

        /*languages.buttons.add(2, new Button(0, 0, 350, 40, "Svenska", () ->
        {
            changeLanguage(new Translation("se.lang"));
        }
        ));

        languages.buttons.add(3, new Button(0, 0, 350, 40, "Română", () ->
        {
            changeLanguage(new Translation("ro.lang"));
        }
        ));*/

        languages.sortButtons();
    }

    public void changeLanguage(Translation t)
    {
        Translation.currentTranslation = t;
        Game.screen = new ScreenLanguage();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < languages.buttons.size(); i++)
        {
            Button b = languages.buttons.get(i);

            if (Translation.currentTranslation == null)
                b.enabled = i != 0;
            else
                b.enabled = !b.text.equals(Translation.currentTranslation.name);
        }

        languages.update();
        page = languages.page;

        quit.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        languages.draw();

        quit.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Languages");
    }
}
