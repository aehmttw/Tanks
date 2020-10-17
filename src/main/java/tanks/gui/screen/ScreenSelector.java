package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.Selector;
import tanks.gui.input.InputBindings;

import java.util.ArrayList;

public class ScreenSelector extends Screen implements IOverlayScreen
{
    public Screen screen;
    public Selector selector;

    public boolean drawImages = false;
    public boolean drawBehindScreen = false;

    public String title;

    Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = screen;

            if (!selector.quick)
                selector.function.run();
        }
    }
    );

    public ButtonList buttonList;

    public ScreenSelector(Selector s, Screen sc)
    {
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
                b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
                b.imageSizeX = b.sizeY;
                b.imageSizeY = b.sizeY;
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
            this.screen.draw();
        else
            this.drawDefaultBackground();

        buttonList.draw();

        quit.draw();

        Drawing.drawing.setInterfaceFontSize(24);

        if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, this.title);
    }

    @Override
    public boolean showOverlay()
    {
        return this.drawBehindScreen;
    }
}
