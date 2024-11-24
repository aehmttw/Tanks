package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.SpeedrunTimer;
import tanks.tank.TankAIControlled;

public class ScreenPartyCrusadeInterlevel extends Screen implements IDarkScreen
{
    public String msg1;
    public String msg2;

    public DisplayFireworks fireworksDisplay;

    Button replayCrusade = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Try again", () ->
    {
        if (checkCrusadeEnd())
        {
            Crusade.currentCrusade.retry = true;
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    }
    );

    Button replayCrusadeWin = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Replay level", () ->
    {
        if (checkCrusadeEnd())
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

    Button nextLevel = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Next level", () ->
    {
        if (checkCrusadeEnd())
        {
            Crusade.currentCrusade.retry = false;
            Crusade.currentCrusade.currentLevel++;
            Crusade.currentCrusade.replay = false;
            Crusade.currentCrusade.loadLevel();
            Game.screen = new ScreenGame(Crusade.currentCrusade.getShop());
        }
    }
    );

    Button quitCrusadeEnd = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Continue", () ->
    {
        if (checkCrusadeEnd())
        {
            if (Panel.win)
                Crusade.currentCrusade.currentLevel++;

            Game.cleanUp();
            Game.screen = new ScreenCrusadeStats(Crusade.currentCrusade, Crusade.currentCrusade.crusadePlayers.get(Game.player), true);
        }
    }
    );

    Button quit = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back to party", () ->
    {
        if (checkCrusadeEnd())
        {
            Crusade.currentCrusade.retry = false;
            Game.resetTiles();
            Crusade.crusadeMode = false;
            Game.screen = ScreenPartyHost.activeScreen;
            Crusade.currentCrusade.currentLevel++;
        }
    }
    );

    Button quitLose = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Back to party", () ->
    {
        Crusade.currentCrusade.retry = true;
        Game.resetTiles();
        Crusade.crusadeMode = false;
        Game.screen = ScreenPartyHost.activeScreen;
    }
    );

    Button next = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Continue", () ->
    {
        Game.resetTiles();
        ScreenGame.versus = false;

        if (Crusade.currentCrusade != null)
        {
            CrusadePlayer us = null;
            for (Player p: Crusade.currentCrusade.crusadePlayers.keySet())
            {
                if (p.clientID.equals(Game.clientID))
                    us = Crusade.currentCrusade.getCrusadePlayer(p);
            }

            Game.screen = new ScreenCrusadeStats(Crusade.currentCrusade, us, true);
        }
        else
            Game.screen = new ScreenPartyLobby();
    }
    );

    Button save = new Button(0, 0, this.objHeight * 1.5, this.objHeight * 1.5, "", () ->
    {
        String ls = Game.currentLevelString;

        StringBuilder tanks = new StringBuilder("\ntanks\n");
        if (Crusade.crusadeMode && Crusade.currentCrusade.customTanks.size() > 0)
        {
            for (TankAIControlled t: Crusade.currentCrusade.customTanks)
                tanks.append(t.toString()).append("\n");

            ls = ls + tanks;
        }

        ScreenSaveLevel sc = new ScreenSaveLevel(System.currentTimeMillis() + "", ls, Game.screen);
        Level lev = new Level(ls);

        lev.preview = true;
        lev.loadLevel(sc);
        Game.screen = sc;

        sc.fromInterlevel = true;
        sc.music = music;
        sc.musicID = musicID;
        sc.updateDownloadButton();
    }
    );

    public ScreenPartyCrusadeInterlevel(boolean win, boolean lose)
    {
        Game.player.hotbar.percentHidden = 100;

        /*if (ScreenPartyHost.isServer)
        {
            if (Panel.win)
                Drawing.drawing.playSound("win.ogg");
            else
                Drawing.drawing.playSound("lose.ogg");
        }*/

        if (Panel.levelPassed)
        {
            if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks)
            {
                this.nextLevel.posY += this.objYSpace / 2;
                this.quit.posY -= this.objYSpace / 2;
            }
        }

        if (Panel.win)
            this.music = "win_music.ogg";
        else
            this.music = "lose_music.ogg";

        if (win)
            this.music = "win_crusade.ogg";

        this.fireworksDisplay = new DisplayFireworks(true, win ? 2 : 1);

        //if (lose)
         //   this.music = "lose_crusade.ogg";

        save.posX = Drawing.drawing.interfaceSizeX - Drawing.drawing.interfaceScaleZoom * 40;
        save.posY = Drawing.drawing.interfaceSizeY - 50 - Drawing.drawing.interfaceScaleZoom * 40;
        save.image = "icons/save.png";

        save.imageSizeX = this.objHeight;
        save.imageSizeY = this.objHeight;
    }

    @Override
    public void update()
    {
        if (Panel.win && Game.effectsEnabled)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.update();
        }
        else
        {
            if (Crusade.currentCrusade == null || Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
                quitCrusadeEnd.update();
            else
            {
                if (Panel.levelPassed || Crusade.currentCrusade.replay)
                {
                    nextLevel.update();

                    if (Crusade.currentCrusade.respawnTanks)
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

        save.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (Panel.win && Game.effectsEnabled && !Game.game.window.drawingShadow)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (Panel.win && Game.effectsEnabled && !Game.game.window.drawingShadow)
            this.fireworksDisplay.draw();

        if (ScreenPartyLobby.isClient)
        {
            next.draw();
        }
        else
        {
            if (Crusade.currentCrusade == null || Crusade.currentCrusade.win || Crusade.currentCrusade.lose)
                quitCrusadeEnd.draw();
            else
            {
                if (Panel.levelPassed || Crusade.currentCrusade.replay)
                {
                    quit.draw();

                    if (Crusade.currentCrusade.respawnTanks)
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

        if ((Panel.win && Game.effectsEnabled) || Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 19 / 6, msg1);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Lives remaining: %d", Game.player.remainingLives);
        Drawing.drawing.setInterfaceFontSize(this.textSize);

        if (Drawing.drawing.interfaceScaleZoom > 1)
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 23 / 6, msg2);
        else
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 25 / 6, msg2);

        save.draw();
    }

    public boolean checkCrusadeEnd()
    {
        for (Player p: Game.players)
        {
            if (p.remainingLives > 0)
                return true;
        }

        Game.resetTiles();

        Game.cleanUp();
        Game.screen = new ScreenCrusadeStats(Crusade.currentCrusade, Crusade.currentCrusade.getCrusadePlayer(Game.player), true);

        return false;
    }
}
