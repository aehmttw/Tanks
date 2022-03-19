package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenCrusadeEditWarning extends Screen
{
    public Screen previous;
    public Crusade crusade;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    public Button confirm = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Edit crusade", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.fileManager.getFile(Game.homedir + Game.savedCrusadePath + crusade.name).delete();
            Game.screen = new ScreenCrusadeEditor(crusade);
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
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "Are you sure you want to edit the crusade?");
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4 / 3, "Editing the crusade will reset progress.");

        confirm.draw();
        back.draw();
    }
}
