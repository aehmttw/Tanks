package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.SelectorItemIcon;
import tanks.item.Item;
import tanks.item.ItemIcon;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ScreenSelector extends Screen implements IConditionalOverlayScreen, IDarkScreen, IBlankBackgroundScreen
{
    public Screen screen;
    public Selector selector;

    public boolean drawImages = false;
    public boolean drawModels = false;
    public boolean drawItemIcons = false;

    public boolean drawBehindScreen = false;
    public int oldOption;

    public int beforeItems = 0;

    public ScreenOverlayIconColors iconColors;

    public String title;

    public Item itemBeingEdited;
    public ItemIcon defaultIcon;

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            if (!selector.quick)
            {
                selector.function.run();
                Drawing.drawing.playSound("destroy.ogg", 2f);
                Drawing.drawing.playVibration("click");
            }

            selector.submitEffect();
            selector.lastFrame = Panel.panel.ageFrames;

            Game.screen = screen;
            Drawing.drawing.removeSyncedMusic(selector.options[selector.selectedOption], 500);
        }
    }
    );

    public ButtonList buttonList;

    public ScreenSelector(Selector s, Screen sc)
    {
        super(350, 40, 380, 60);

        this.selector = s;

        if (!selector.quick)
            this.quit.silent = true;

        this.oldOption = s.selectedOption;

        this.screen = sc;

        this.allowClose = sc.allowClose;

        ArrayList<Button> buttons = new ArrayList<>();

        if (s instanceof SelectorItemIcon)
            iconColors = new ScreenOverlayIconColors(this, (SelectorItemIcon) s, this.objYSpace * 2.75);

        for (int i = 0; i < selector.options.length; i++)
        {
            String n = selector.options[i];

            if (selector.music)
                n = n.substring(n.indexOf("tank/") + "tank/".length(), n.indexOf(".ogg"));

            if (selector.format)
                n = Game.formatString(n);

            int j = i;

            Button b = new Button(0, 0, this.objWidth, this.objHeight, n, () ->
            {
                if (selector.music)
                {
                    Drawing.drawing.removeSyncedMusic(selector.options[selector.selectedOption], 500);
                    Drawing.drawing.addSyncedMusic(selector.options[j], 1, true, 500);
                }

                selector.selectedOption = j;

                if (selector.sounds != null)
                    Drawing.drawing.playSound(selector.sounds[selector.selectedOption]);

                if (selector.quick)
                {
                    Game.screen = screen;
                    Drawing.drawing.removeSyncedMusic(selector.options[selector.selectedOption], 500);
                    selector.function.run();
                }

                if (selector instanceof SelectorItemIcon)
                {
                    ((SelectorItemIcon) selector).selectedIcon = selector.itemIcons[selector.selectedOption];

                    if (iconColors != null)
                        iconColors.refreshButtons();

                    if (itemBeingEdited != null)
                        itemBeingEdited.autoIcon = false;
                }
            }
            );

            if (selector.sounds != null)
                b.silent = true;

            buttons.add(b);
        }

        buttonList = new ButtonList(buttons, 0, 0, s instanceof SelectorItemIcon ? -90 : -30);
        buttonList.translate = selector.translate;

        if (selector.quick)
            quit.setText("Back");

        this.buttonList.sortButtons();

        this.music = sc.music;
        this.musicID = sc.musicID;

        this.title = Translation.translate("Select ") + s.translatedText.toLowerCase();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < buttonList.buttons.size(); i++)
        {
            Button b = buttonList.buttons.get(i);
            b.enabled = (i - beforeItems) != selector.selectedOption || selector.quick;

            if (drawImages || drawModels || drawItemIcons)
            {
                if (drawImages)
                {
                    b.image = selector.options[i];

                    if (selector.images != null)
                        b.image = selector.images[i];
                }

                if (drawModels && selector.models != null)
                    b.model = selector.models[i];

                b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
                b.imageYOffset = 0;
                b.imageSizeX = b.sizeY;
                b.imageSizeY = b.sizeY;

                if (drawItemIcons && selector.itemIcons != null)
                {
                    if (i >= beforeItems)
                        b.itemIcon = selector.itemIcons[i - beforeItems];
                    else
                    {
                        b.image = "icons/wand.png";
                        b.enabled = itemBeingEdited != null && !itemBeingEdited.autoIcon;
                    }
                }

                if (b.sizeX == b.sizeY)
                {
                    b.imageXOffset = 0;
                    b.imageSizeX *= 0.8;
                    b.imageSizeY *= 0.8;
                }
            }
        }

        if (!(iconColors != null && iconColors.colorSelector != null))
            buttonList.update();

        quit.update();

        if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE))
        {
            Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
            selector.selectedOption = oldOption;

            Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
            Drawing.drawing.playVibration("click");

            Game.screen = screen;
            Drawing.drawing.removeSyncedMusic(selector.options[selector.selectedOption], 500);
        }

        if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER))
        {
            Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ENTER);
            quit.function.run();
        }

        if (iconColors != null)
            iconColors.update();
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

        quit.draw();

        if (!(iconColors != null && iconColors.colorSelector != null))
        {
            buttonList.draw();

            Drawing.drawing.setInterfaceFontSize(this.titleSize);

            if (Level.isDark() || Panel.darkness > 64)
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, this.title);
        }

        if (iconColors != null)
            iconColors.draw();
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
