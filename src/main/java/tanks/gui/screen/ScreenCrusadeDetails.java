package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenCrusadeDetails extends Screen implements IPartyMenuScreen
{
    public Crusade crusade;

    public Button begin = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "Play", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade = crusade;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.begin();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    });

    public Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 0, this.objWidth, this.objHeight, "Resume", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade = crusade;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    });

    public Button startOver = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "Start over", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade = crusade;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.begin();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    });

    public Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Edit", new Runnable()
    {
        @Override
        public void run()
        {
            if (crusade.started)
                Game.screen = new ScreenCrusadeEditWarning(Game.screen, crusade);
            else
                Game.screen = new ScreenCrusadeBuilder(crusade);
        }
    });

    public Button delete = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Delete crusade", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenConfirmDeleteCrusade(Game.screen, crusade);
        }
    });

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
                Game.screen = new ScreenPartyCrusades();
            else
                Game.screen = new ScreenCrusades();
        }
    });

    public Button back2 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
                Game.screen = new ScreenPartyCrusades();
            else
                Game.screen = new ScreenCrusades();
        }
    });

    public ScreenCrusadeDetails(Crusade c)
    {
        this.crusade = c;

        this.music = "tomato_feast_5.ogg";
        this.musicID = "menu";

        if (c.levels.size() <= 0)
        {
            begin.enabled = false;
            begin.enableHover = true;
            begin.hoverText = new String[]{"This crusade has no levels.", "Add some to play it!"};
        }
    }

    @Override
    public void update()
    {
        if (crusade.started && !ScreenPartyHost.isServer)
        {
            resume.update();
            startOver.update();
        }
        else
            begin.update();

        if (!(crusade.readOnly || crusade.internal || ScreenPartyHost.isServer))
        {
            edit.update();
            delete.update();
            back.update();
        }
        else
            back2.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(48);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, crusade.name.replace("_", " "));
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, "Levels: " + crusade.levels.size());

        if (crusade.started && !ScreenPartyHost.isServer)
        {
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Current battle: " + (crusade.currentLevel + 1));
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "Remaining lives: " + Game.player.remainingLives);
        }

        if (!(crusade.readOnly || crusade.internal || ScreenPartyHost.isServer))
        {
            edit.draw();
            delete.draw();
            back.draw();
        }
        else
            back2.draw();

        if (crusade.started && !ScreenPartyHost.isServer)
        {
            resume.draw();
            startOver.draw();
        }
        else
            begin.draw();
    }
}
