package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenConfirmDeleteCrusade extends Screen
{
    public Screen previous;
    public Crusade crusade;

    public Button cancelDelete = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "No", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    public Button confirmDelete = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Yes", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.fileManager.getFile(crusade.fileName).delete();
            Game.screen = new ScreenCrusades();
        }
    }
    );

    public ScreenConfirmDeleteCrusade(Screen previous, Crusade crusade)
    {
        this.previous = previous;
        this.crusade = crusade;

        this.music = previous.music;
        this.musicID = previous.musicID;
    }

    @Override
    public void update()
    {
        confirmDelete.update();
        cancelDelete.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to delete the crusade?");

        confirmDelete.draw();
        cancelDelete.draw();
    }
}
