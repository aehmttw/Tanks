package tanks.gui.screen;

import basewindow.IModel;
import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.SelectorMusic;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ScreenSelectorMusic extends Screen implements IConditionalOverlayScreen, IDarkScreen
{
    public Screen screen;
    public SelectorMusic selector;

    public String[] images;
    public boolean drawBehindScreen = false;

    public String title;

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            Drawing.drawing.playSound("destroy.ogg", 2f);
            Drawing.drawing.playVibration("click");
            selector.submitEffect();
            selector.lastFrame = Panel.panel.ageFrames;
            Game.screen = screen;

            for (int i = 0; i < selector.options.length; i++)
            {
                if (selector.selectedOptions[i])
                    Drawing.drawing.removeSyncedMusic(selector.options[i], 500);
            }

            selector.function.run();
        }
    }
    );

    public ButtonList buttonList;

    public ScreenSelectorMusic(SelectorMusic s, Screen sc)
    {
        super(350, 40, 380, 60);

        this.quit.silent = true;

        this.screen = sc;
        this.selector = s;

        this.allowClose = sc.allowClose;

        ArrayList<Button> buttons = new ArrayList<>();

        for (int i = 0; i < selector.options.length; i++)
        {
            String n = selector.options[i];
            if (n.contains("tank/"))
                n = n.substring(n.indexOf("tank/") + "tank/".length(), n.indexOf(".ogg"));
            else if (n.contains("arcade/"))
                n = n.substring(n.indexOf("arcade/") + "arcade/".length(), n.indexOf(".ogg"));

            if (selector.format)
                n = Game.formatString(n);

            int j = i;

            Button b = new Button(0, 0, this.objWidth, this.objHeight, n, () ->
            {
                selector.selectedOptions[j] = !selector.selectedOptions[j];

                if (!selector.selectedOptions[j])
                    Drawing.drawing.removeSyncedMusic(selector.options[j], 500);
                else
                    Drawing.drawing.addSyncedMusic(selector.options[j], Game.musicVolume, true, 500);
            }
            );

            buttons.add(b);
        }

        buttonList = new ButtonList(buttons, 0, 0, -30);
        buttonList.translate = selector.translate;

        if (selector.quick)
            quit.setText("Back");

        this.buttonList.sortButtons();

        this.music = sc.music;
        this.musicID = sc.musicID;

        this.title = Translation.translate("Select ") + s.translatedText.toLowerCase();

        for (int i = 0; i < selector.options.length; i++)
        {
            if (selector.selectedOptions[i])
                Drawing.drawing.addSyncedMusic(selector.options[i], Game.musicVolume, true, 500);
        }
    }

    @Override
    public void update()
    {
        for (int i = 0; i < buttonList.buttons.size(); i++)
        {
            Button b = buttonList.buttons.get(i);
            if (selector.selectedOptions[i])
            {
                b.image = "icons/play.png";
                b.imageSizeX = 30;
                b.imageSizeY = 30;
                b.imageXOffset = -135;
            }
            else
                b.image = null;
        }

        buttonList.update();

        quit.update();

        if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER))
        {
            Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ENTER);
            quit.function.run();
        }
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
        {
            this.enableMargins = this.screen.enableMargins;
            this.screen.draw();
        }
        else
        {
            Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
            this.drawDefaultBackground();
        }

        buttonList.draw();

        quit.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        if (Level.isDark() || Panel.darkness > 64)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, this.title);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.8, "You may select multiple options");
    }

    @Override
    public double getOffsetX()
    {
        if (drawBehindScreen)
            return screen.getOffsetX();
        else
            return super.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        if (drawBehindScreen)
            return screen.getOffsetY();
        else
            return super.getOffsetY();
    }

    @Override
    public double getScale()
    {
        if (drawBehindScreen)
            return screen.getScale();
        else
            return super.getScale();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        if (screen instanceof IConditionalOverlayScreen)
            return ((IConditionalOverlayScreen) screen).isOverlayEnabled();

        return screen instanceof ScreenGame || screen instanceof ILevelPreviewScreen || screen instanceof IOverlayScreen;
    }

    @Override
    public void onAttemptClose()
    {
        this.screen.onAttemptClose();
    }
}
