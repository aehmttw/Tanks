package main;

import basewindow.ComputerFileManager;
import lwjglwindow.LWJGLWindow;
import swingwindow.SwingWindow;
import tanks.*;
import tanksonline.CommandExecutor;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServer;

import java.io.File;

public class Tanks
{
    public static void main(String[] args)
    {
        Game.framework = Game.Framework.lwjgl;
        int port = 8080;

        boolean relaunch = System.getProperties().toString().contains("Mac OS X");

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("online_server"))
                Game.isOnlineServer = true;

            if (args[i].startsWith("port="))
                port = Integer.parseInt(args[i].split("=")[1]);

            if (args[i].equals("swing"))
                Game.framework = Game.Framework.swing;

            if (args[i].equals("debug"))
                Game.debug = true;

            if (args[i].equals("mac"))
                relaunch = false;
        }

        if (!Game.isOnlineServer)
        {
            if (relaunch && Game.framework == Game.Framework.lwjgl)
            {
                try
                {
                    String path = new File(Tanks.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();

                    if (path.endsWith(".jar"))
                    {
                        String[] command = new String[]{"java", "-XstartOnFirstThread", "-jar", path, "mac"};
                        Runtime.getRuntime().exec(command);
                        Runtime.getRuntime().exit(0);
                        return;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (Game.framework == Game.Framework.lwjgl || Game.framework == Game.Framework.swing)
                Game.game.fileManager = new ComputerFileManager();

            Game.initScript();

            if (Game.framework == Game.Framework.lwjgl)
            {
                Game.game.window = new LWJGLWindow("Tanks", 1400, 900 + Drawing.drawing.statsHeight, Game.absoluteDepthBase, new GameUpdater(), new GameDrawer(), new GameWindowHandler(), Game.vsync, !Panel.showMouseTarget);
                ((LWJGLWindow)Game.game.window).antialiasingEnabled = Game.antialiasing;
            }
            else if (Game.framework == Game.Framework.swing)
                Game.game.window = new SwingWindow("Tanks", 1400, 900 + Drawing.drawing.statsHeight, Game.absoluteDepthBase, new GameUpdater(), new GameDrawer(), new GameWindowHandler(), Game.vsync, !Panel.showMouseTarget);

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
