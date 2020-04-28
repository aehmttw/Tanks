package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.gui.Firework;

import java.util.ArrayList;

public class ScreenPartyInterlevel extends Screen implements IPartyMenuScreen, IDarkScreen
{
    ArrayList<Firework> fireworks = new ArrayList<Firework>();
    ArrayList<Firework> removeFireworks = new ArrayList<Firework>();

    Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Generate a new level", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenGame.versus)
            {
                Game.cleanUp();
                new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
                Game.screen = new ScreenGame();
            }
            else
            {
                Game.reset();
                Game.screen = new ScreenGame();
            }
        }
    }
        );

    Button replay = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 0, 350, 40, "Replay the level", new Runnable()
    {
        @Override
        public void run()
        {
            Level level = new Level(Game.currentLevelString);
            level.loadLevel();
            Game.screen = new ScreenGame();
        }
    }
        );

    Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenPartyHost.activeScreen;
            ScreenGame.versus = false;
            ScreenInterlevel.fromSavedLevels = false;
        }
    }
        );

    Button replayHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Replay the level", new Runnable()
    {
        @Override
        public void run()
        {
            Level level = new Level(Game.currentLevelString);
            level.loadLevel();
            Game.screen = new ScreenGame();
        }
    }
    );

    Button quitHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Back to party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenPartyHost.activeScreen;
            ScreenGame.versus = false;
            ScreenInterlevel.fromSavedLevels = false;
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

    public ScreenPartyInterlevel()
    {
        Panel.panel.hotbar.percentHidden = 100;

        if (Panel.win)
            Drawing.drawing.playSound("win.ogg");
        else
            Drawing.drawing.playSound("lose.ogg");

        if (Panel.win && Game.fancyGraphics)
        {
            for (int i = 0; i < 5; i++)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.sizeX, Drawing.drawing.sizeY, fireworks, removeFireworks);
                f.setRandomColor();
                f.vY = - Math.random() * 1.5 * Game.currentSizeY / 18 - 6;
                f.vX = Math.random() * 5 - 2.5;
                fireworks.add(f);
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
        else if (ScreenInterlevel.fromSavedLevels)
        {
            quitHigherPos.update();
            replayHigherPos.update();
        }
        else
        {
            quit.update();
            replay.update();
            newLevel.update();
        }

        if (ScreenPartyHost.isServer)
            ScreenPartyHost.chatbox.update();
        else if (ScreenPartyLobby.isClient)
            ScreenPartyLobby.chatbox.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (Panel.win && Game.fancyGraphics)
        {
            if (ScreenPartyHost.isServer)
                ScreenPartyHost.chatbox.defaultTextColor = "\u00A7255255255255";
            else if (ScreenPartyLobby.isClient)
                ScreenPartyLobby.chatbox.defaultTextColor = "\u00A7255255255255";
        }

        if (Panel.win && Game.fancyGraphics)
            Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);

        if (ScreenPartyLobby.isClient)
        {
            next.draw();
        }
        else if (ScreenInterlevel.fromSavedLevels)
        {
            quitHigherPos.draw();
            replayHigherPos.draw();
        }
        else
        {
            quit.draw();
            replay.draw();
            newLevel.draw();
        }

        if (Panel.win && Game.fancyGraphics)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, Panel.winlose);

        if (Panel.win && Game.fancyGraphics)
        {
            if (Math.random() < 0.02)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.sizeX, Drawing.drawing.sizeY, fireworks, removeFireworks);
                f.setRandomColor();
                f.vY = - Math.random() * 1.5 * Game.currentSizeY / 18 - 6;
                f.vX = Math.random() * 5 - 2.5;
                fireworks.add(f);
            }

            for (int i = 0; i < fireworks.size(); i++)
            {
                fireworks.get(i).drawUpdate();
            }

            for (int i = 0; i < removeFireworks.size(); i++)
            {
                fireworks.remove(removeFireworks.get(i));
            }
        }

        if (ScreenPartyLobby.isClient)
        {
            ScreenPartyLobby.chatbox.draw();
            long time = System.currentTimeMillis();
            for (int i = 0; i < ScreenPartyLobby.chat.size(); i++)
            {
                ChatMessage c = ScreenPartyLobby.chat.get(i);
                if (time - c.time <= 30000 || ScreenPartyLobby.chatbox.selected)
                {
                    if (Panel.win && Game.fancyGraphics)
                        Drawing.drawing.setColor(255, 255, 255);
                    else
                        Drawing.drawing.setColor(0, 0, 0);

                    Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
                }
            }
        }
        else if (ScreenPartyHost.isServer)
        {
            ScreenPartyHost.chatbox.draw();
            long time = System.currentTimeMillis();
            for (int i = 0; i < ScreenPartyHost.chat.size(); i++)
            {
                ChatMessage c = ScreenPartyHost.chat.get(i);
                if (time - c.time <= 30000 || ScreenPartyHost.chatbox.selected)
                {
                    if (Panel.win && Game.fancyGraphics)
                        Drawing.drawing.setColor(255, 255, 255);
                    else
                        Drawing.drawing.setColor(0, 0, 0);

                    Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
                }
            }
        }

        if (Panel.win && Game.fancyGraphics)
        {
            if (ScreenPartyHost.isServer)
                ScreenPartyHost.chatbox.defaultTextColor = "\u00A7127127127255";
            else if (ScreenPartyLobby.isClient)
                ScreenPartyLobby.chatbox.defaultTextColor = "\u00A7127127127255";
        }
    }
}
