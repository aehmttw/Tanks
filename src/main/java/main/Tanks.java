package main;

import basewindow.ComputerFileManager;
import lwjglwindow.LWJGLWindow;
import tanks.*;
import tanks.extension.Extension;
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

            if (args[i].equals("debug"))
                Game.debug = true;

            if (args[i].equals("mac") || args[i].equals("no_relaunch"))
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

            if (Game.framework == Game.Framework.lwjgl)
                Game.game.fileManager = new ComputerFileManager();

            Game.initScript();

            if (Game.framework == Game.Framework.lwjgl)
            {
                Game.game.window = new LWJGLWindow("Tanks", 1400, 900 + Drawing.drawing.statsHeight, Game.absoluteDepthBase, new GameUpdater(), new GameDrawer(), new GameWindowHandler(), Game.vsync, !Panel.showMouseTarget);
                ((LWJGLWindow)Game.game.window).antialiasingEnabled = Game.antialiasing;
            }

            Game.postInitScript();

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

    /*
        Call this method to launch Tanks with extensions directly instead of loading them from a jar file!
        This is useful if you want to test an extension without exporting it as a jar file.
        The integer array passed determines the order in which these extensions will be added to the full list
        (which includes extensions loaded from separate jar files traditionally)
     */
    public static void launchWithExtensions(String[] args, Extension[] extensions, int[] order)
    {
        Game.extraExtensions = extensions;
        Game.extraExtensionOrder = order;

        String[] args2 = new String[args.length + 1];
        System.arraycopy(args, 0, args2, 0, args.length);
        args2[args.length] = "no_relaunch";

        main(args2);
    }
}
