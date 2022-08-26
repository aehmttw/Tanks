package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.InputSelector;
import tanks.gui.input.InputBinding;

public class ScreenBindInput extends Screen
{
    public InputBinding input;
    public String name;
    public Screen previous;
    public InputSelector selector;
    public boolean number;

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    Button unbind = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "Unbind", new Runnable()
    {
        @Override
        public void run()
        {
            input.input = 0;
            input.inputType = null;
            Game.screen = previous;
        }
    }
    );

    Button reset = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Reset", new Runnable()
    {
        @Override
        public void run()
        {
            input.reset();
            Game.screen = previous;
        }
    }
    );

    public ScreenBindInput(Screen prev, InputBinding input, String name, InputSelector selector, boolean number)
    {
        this.music = prev.music;
        this.musicID = prev.musicID;
        this.input = input;
        this.name = name;
        this.previous = prev;
        Game.game.window.validPressedButtons.clear();
        Game.game.window.validPressedKeys.clear();
        this.selector = selector;
        this.number = number;
    }

    @Override
    public void update()
    {
        back.update();
        unbind.update();
        reset.update();

        if (Game.screen == this)
        {
            if (!Game.game.window.validPressedKeys.isEmpty())
            {
                input.input = Game.game.window.validPressedKeys.get(0);
                input.inputType = InputBinding.InputType.keyboard;
                Game.screen = previous;
                Drawing.drawing.playSound("destroy.ogg", 2f);
                this.selector.glowEffects.clear();
                this.selector.lastFrame = Panel.panel.ageFrames;
                this.selector.submitEffect(this.number);
            }
            else if (!Game.game.window.validPressedButtons.isEmpty())
            {
                input.input = Game.game.window.validPressedButtons.get(0);
                input.inputType = InputBinding.InputType.mouse;
                Game.screen = previous;
                Drawing.drawing.playSound("destroy.ogg", 2f);
                this.selector.glowEffects.clear();
                this.selector.lastFrame = Panel.panel.ageFrames;
                this.selector.submitEffect(this.number);
            }

            Game.game.window.validPressedButtons.clear();
            Game.game.window.validPressedKeys.clear();
        }
    }

    @Override
    public void draw()
    {
        //this.drawDefaultBackground();

        previous.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 800, 400);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 780 * this.objWidth / 350, 380 * this.objHeight / 40);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Press a key or mouse button to use");
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, "that key or button for \"%s\"", this.name);

        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "Currently using %s", input.getInputName());

        reset.draw();
        unbind.draw();
        back.draw();
    }
}
