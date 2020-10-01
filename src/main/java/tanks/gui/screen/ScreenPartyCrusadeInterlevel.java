package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.gui.Firework;

import java.util.ArrayList;

public class ScreenPartyCrusadeInterlevel extends Screen implements IPartyMenuScreen, IDarkScreen
{
    public String msg1;
    public String msg2;

    boolean odd = false;

    ArrayList<Firework> fireworks1 = new ArrayList<Firework>();
    ArrayList<Firework> fireworks2 = new ArrayList<Firework>();

    Button replayCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Try again", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    }
    );

    Button replayCrusadeWin = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Replay the level", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
            Crusade.currentCrusade.replay = true;
        }
    }
            , "You will not gain extra lives---"
            + "from replaying a level you've already beaten.---"
            + "However, you can still earn coins!---"
            + "You will still lose a life if you die.");

    Button nextLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Next level", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.currentCrusade.currentLevel++;
            Crusade.currentCrusade.replay = false;
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    }
    );

    Button quitCrusadeEnd = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.crusadeMode = false;
            Crusade.currentCrusade = null;
            Game.screen = ScreenPartyHost.activeScreen;
        }
    }
    );

    Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.crusadeMode = false;
            Game.screen = ScreenPartyHost.activeScreen;
            Crusade.currentCrusade.currentLevel++;
        }
    }
    );

    Button quitLose = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Crusade.crusadeMode = false;
            Game.screen = ScreenPartyHost.activeScreen;
        }
    }
    );

    Button next = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Continue", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPartyLobby();
            ScreenGame.versus = false;
        }
    }
    );

    public ScreenPartyCrusadeInterlevel()
    {
        Game.player.hotbar.percentHidden = 100;

        if (ScreenPartyHost.isServer)
        {
            if (Panel.win)
                Drawing.drawing.playSound("win.ogg");
            else
                Drawing.drawing.playSound("lose.ogg");
        }

        if (Panel.win && Game.fancyGraphics)
        {
            for (int i = 0; i < 5; i++)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, getFireworkArray());
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }
        }
    }

    @Override
    public void update()
    {
        if (Panel.win && Game.fancyGraphics)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.update();
        }
        else
        {
            if (Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
                quitCrusadeEnd.update();
            else
            {
                if (Panel.levelPassed || Crusade.currentCrusade.replay)
                {
                    nextLevel.update();
                    replayCrusadeWin.update();
                    quit.update();
                }
                else
                {
                    replayCrusade.update();
                    quitLose.update();
                }
            }
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (Panel.win && Game.fancyGraphics)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.draw();
        }
        else
        {
            if (Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
                quitCrusadeEnd.draw();
            else
            {
                if (Panel.levelPassed || Crusade.currentCrusade.replay)
                {
                    quit.draw();
                    replayCrusadeWin.draw();
                    nextLevel.draw();
                }
                else
                {
                    quitLose.draw();
                    replayCrusade.draw();
                }
            }
        }

        if (Panel.win && Game.fancyGraphics)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 190, msg1);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Lives remaining: " + Game.player.remainingLives);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 250, msg2);

        if (Panel.win && Game.fancyGraphics)
        {
            ArrayList<Firework> fireworks = getFireworkArray();
            if (Math.random() < ScreenInterlevel.firework_frequency * Panel.frameFrequency)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, fireworks);
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }

            for (int i = 0; i < getFireworkArray().size(); i++)
            {
                fireworks.get(i).drawUpdate(fireworks, getOtherFireworkArray());
            }

            if (Game.superGraphics)
            {
                for (int i = 0; i < getFireworkArray().size(); i++)
                {
                    fireworks.get(i).drawGlow();
                }
            }

            //A fix to some glitchiness on ios
            Drawing.drawing.setColor(0, 0, 0, 0);
            Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

            fireworks.clear();
            odd = !odd;
        }
    }


    public ArrayList<Firework> getFireworkArray()
    {
        if (odd)
            return fireworks2;
        else
            return fireworks1;
    }

    public ArrayList<Firework> getOtherFireworkArray()
    {
        if (odd)
            return fireworks1;
        else
            return fireworks2;
    }
}
