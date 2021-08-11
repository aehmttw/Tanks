package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;

import java.util.ArrayList;

public class ScreenSelector extends Screen implements IConditionalOverlayScreen
{
    public Screen screen;
    public Selector selector;

    public String[] images;
    public boolean drawImages = false;
    public boolean drawBehindScreen = false;

    public String title;

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            if (!selector.quick)
                selector.function.run();

            Game.screen = screen;
        }
    }
    );

    public ButtonList buttonList;

    public ScreenSelector(Selector s, Screen sc)
    {
        super(350, 40, 380, 60);

        this.screen = sc;
        this.selector = s;

        ArrayList<Button> buttons = new ArrayList<>();

        for (int i = 0; i < selector.options.length; i++)
        {
            String n = selector.options[i];

            if (selector.format)
                n = Game.formatString(n);

            int j = i;

            Button b = new Button(0, 0, this.objWidth, this.objHeight, n, new Runnable()
            {
                @Override
                public void run()
                {
                    selector.selectedOption = j;

                    if (selector.quick)
                    {
                        Game.screen = screen;
                        selector.function.run();
                    }
                }
            }
            );

            buttons.add(b);
        }

        buttonList = new ButtonList(buttons, 0, 0, -30);

        if (selector.quick)
            quit.text = "Back";

        this.buttonList.sortButtons();

        this.music = sc.music;
        this.musicID = sc.musicID;

        this.title = "Select " + s.text.toLowerCase();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < buttonList.buttons.size(); i++)
        {
            Button b = buttonList.buttons.get(i);
            b.enabled = i != selector.selectedOption || selector.quick;

            if (drawImages)
            {
                b.image = selector.options[i];

                if (images != null)
                    b.image = selector.images[i];

                b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
                b.imageSizeX = b.sizeY;
                b.imageSizeY = b.sizeY;

                if (b.sizeX == b.sizeY)
                {
                    b.imageXOffset = 0;
                    b.imageSizeX *= 0.8;
                    b.imageSizeY *= 0.8;
                }
            }
        }

        buttonList.update();

        quit.update();

        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
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
            this.drawDefaultBackground();

        buttonList.draw();

        quit.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, this.title);
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
}
