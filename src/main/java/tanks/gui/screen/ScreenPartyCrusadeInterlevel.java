package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.gui.Firework;
import tanks.gui.SpeedrunTimer;

import java.util.ArrayList;

public class ScreenPartyCrusadeInterlevel extends Screen implements IDarkScreen
{
    public String msg1;
    public String msg2;

    boolean odd = false;

    ArrayList<Firework> fireworks1 = new ArrayList<Firework>();
    ArrayList<Firework> fireworks2 = new ArrayList<Firework>();

    Button replayCrusade = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Try again", new Runnable()
    {
        @Override
        public void run()
        {
            if (checkCrusadeEnd())
            {
                Crusade.currentCrusade.loadLevel();
                Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
            }
        }
    }
    );

    Button replayCrusadeWin = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Replay the level", new Runnable()
    {
        @Override
        public void run()
        {
            if (checkCrusadeEnd())
            {
                Crusade.currentCrusade.loadLevel();
                Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
                Crusade.currentCrusade.replay = true;
            }
        }
    }
            , "You will not gain extra lives---"
            + "from replaying a level you've already beaten.---"
            + "However, you can still earn coins!---"
            + "You will still lose a life if you die.");

    Button nextLevel = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Next level", new Runnable()
    {
        @Override
        public void run()
        {
            if (checkCrusadeEnd())
            {
                Crusade.currentCrusade.currentLevel++;
                Crusade.currentCrusade.replay = false;
                Crusade.currentCrusade.loadLevel();
                Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
            }
        }
    }
    );

    Button quitCrusadeEnd = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            if (checkCrusadeEnd())
            {
                Game.resetTiles();
                Crusade.crusadeMode = false;
                Crusade.currentCrusade = null;
                Game.screen = ScreenPartyHost.activeScreen;
            }
        }
    }
    );

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            if (checkCrusadeEnd())
            {
                Game.resetTiles();
                Crusade.crusadeMode = false;
                Game.screen = ScreenPartyHost.activeScreen;
                Crusade.currentCrusade.currentLevel++;
            }
        }
    }
    );

    Button quitLose = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.resetTiles();
            Crusade.crusadeMode = false;
            Game.screen = ScreenPartyHost.activeScreen;
        }
    }
    );

    Button next = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Continue", new Runnable()
    {
        @Override
        public void run()
        {
            if (checkCrusadeEnd())
            {
                Game.resetTiles();
                Game.screen = new ScreenPartyLobby();
                ScreenGame.versus = false;
            }
        }
    }
    );

    public ScreenPartyCrusadeInterlevel(boolean win)
    {
        Game.player.hotbar.percentHidden = 100;

        if (ScreenPartyHost.isServer)
        {
            if (Panel.win)
                Drawing.drawing.playSound("win.ogg");
            else
                Drawing.drawing.playSound("lose.ogg");
        }

        if (Panel.win)
            this.music = "win_music.ogg";
        else
            this.music = "lose_music.ogg";

        if (win)
            this.music = "win_crusade.ogg";

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

        if (Panel.win && Game.fancyGraphics && !Game.game.window.drawingShadow)
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

        if (Game.showSpeedrunTimer)
            SpeedrunTimer.draw();

        if ((Panel.win && Game.fancyGraphics) || (Level.currentColorR + Level.currentColorG + Level.currentColorB) / 3.0 < 127)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, msg1);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Lives remaining: " + Game.player.remainingLives);
        Drawing.drawing.setInterfaceFontSize(this.textSize);

        if (Drawing.drawing.interfaceScaleZoom > 1)
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 23 / 6, msg2);
        else
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 25 / 6, msg2);
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

    public boolean checkCrusadeEnd()
    {
        for (Player p: Game.players)
        {
            if (p.remainingLives > 0)
                return true;
        }

        Game.resetTiles();
        Crusade.crusadeMode = false;
        Crusade.currentCrusade = null;
        Game.screen = ScreenPartyHost.activeScreen;

        return false;
    }
}
