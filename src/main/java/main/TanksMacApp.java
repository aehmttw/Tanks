package main;

import basewindow.ComputerFileManager;
import lwjglwindow.LWJGLWindow;
import tanks.*;
import tanksonline.CommandExecutor;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServer;

import java.io.File;

public class TanksMacApp
{
    public static void main(String[] args)
    {
        Game.framework = Game.Framework.lwjgl;
        int port = 8080;

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("online_server"))
                Game.isOnlineServer = true;
            if (args[i].matches("port=\\d+"))
                port = Integer.parseInt(args[i].split("=")[1]);
            if (args[i].equals("debug"))
                Game.debug = true;
        }

        if (!Game.isOnlineServer)
        {
            if (Game.framework == Game.Framework.lwjgl)
                Game.game.fileManager = new ComputerFileManager();

            Game.initScript();

            if (Game.framework == Game.Framework.lwjgl)
            {
                Game.game.window = new LWJGLWindow("Tanks", 1400, 900 + Drawing.drawing.statsHeight, Game.absoluteDepthBase, new GameUpdater(), new GameDrawer(), new GameWindowHandler(), Game.vsync, !Panel.showMouseTarget);
                Game.game.window.antialiasingEnabled = Game.antialiasing;
            }

            Game.game.window.run();
        }
        else
        {
            System.out.println("TanksOnline has started!");
            Game.registerEvents();
            PlayerMap.instance.load();
            new CommandExecutor().run();
            new TanksOnlineServer(port).run();
        }
    }
}
