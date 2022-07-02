package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScreenCrusadeDetails extends Screen
{
    public Crusade crusade;
    public ScreenCrusadeLevels background;

    public Button begin = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Play", new Runnable()
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

    public Button resume = new Button(this.centerX, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Resume", new Runnable()
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

    public Button startOver = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Start over", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade c;

            if (crusade.internal)
            {
                String[] l = crusade.contents.split("\n");
                c = new Crusade(new ArrayList<>(Arrays.asList(l)), crusade.name, crusade.fileName);
            }
            else
                c = new Crusade(Game.game.fileManager.getFile(crusade.fileName), crusade.name);

            Crusade.currentCrusade = c;
            Crusade.crusadeMode = true;
            Crusade.currentCrusade.begin();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    });

    public Button edit = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Edit", new Runnable()
    {
        @Override
        public void run()
        {
            if (crusade.started)
                Game.screen = new ScreenCrusadeEditWarning(Game.screen, crusade);
            else
                Game.screen = new ScreenCrusadeEditor(crusade);
        }
    });

    public Button delete = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Delete crusade", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenConfirmDeleteCrusade(Game.screen, crusade);
        }
    });

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer)
            Game.screen = new ScreenPartyCrusades();
        else
            Game.screen = new ScreenCrusades();
    });

    public Button back2 = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer)
            Game.screen = new ScreenPartyCrusades();
        else
            Game.screen = new ScreenCrusades();
    });

    public ScreenCrusadeDetails(Crusade c)
    {
        this.crusade = c;

        this.music = "menu_5.ogg";
        this.musicID = "menu";

        if (Game.previewCrusades)
            this.forceInBounds = true;

        if (c.levels.size() <= 0)
        {
            begin.enabled = false;
            begin.enableHover = true;
            begin.setHoverText("This crusade has no levels.---Add some to play it!");
        }

        if (Game.previewCrusades)
            this.background = new ScreenCrusadeLevels(this.crusade);
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
        if (Game.previewCrusades)
            this.background.draw();
        else
            this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, Drawing.drawing.interfaceSizeX * 0.7, this.objYSpace * 9);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, Drawing.drawing.interfaceSizeX * 0.7 - 20, this.objYSpace * 9 - 20);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize * 2);

        if (this.crusade.internal)
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, Translation.translate(crusade.name.replace("_", " ")));
        else
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, crusade.name.replace("_", " "));

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Levels: %d", crusade.levels.size());

        if (crusade.started && !ScreenPartyHost.isServer)
        {
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "Current battle: %d", (crusade.currentLevel + 1));
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Remaining lives: %d", Game.player.remainingLives);
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
