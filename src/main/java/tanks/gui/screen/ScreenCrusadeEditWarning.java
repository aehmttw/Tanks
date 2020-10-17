package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenCrusadeEditWarning extends Screen
{
    public Screen previous;
    public Crusade crusade;

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 60), this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    public Button confirm = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2), this.objWidth, this.objHeight, "Edit crusade", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.fileManager.getFile(Game.homedir + Game.savedCrusadePath + crusade.name).delete();
            Game.screen = new ScreenCrusadeBuilder(crusade);
        }
    }
    );

    public ScreenCrusadeEditWarning(Screen previous, Crusade crusade)
    {
        this.previous = previous;
        this.crusade = crusade;

        this.music = previous.music;
        this.musicID = previous.musicID;
    }

    @Override
    public void update()
    {
        confirm.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, "Are you sure you want to edit the crusade?");
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 80, "Editing the crusade will reset progress.");

        confirm.draw();
        back.draw();
    }
}
