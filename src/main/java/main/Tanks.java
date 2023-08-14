package main;

import basewindow.ComputerFileManager;
import lwjglwindow.LWJGLWindow;
import tanks.*;
import tanks.Panel;
import tanks.extension.Extension;
import tanksonline.CommandExecutor;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;

public class Tanks
{
    public static void main(String[] args)
    {
        Game.framework = Game.Framework.lwjgl;
        int port = 8080;

        // Relaunches the .jar if Mac OS X is detected.
        boolean relaunch = System.getProperties().toString().contains("Mac OS X");

        // Goes through arguments and applies specified settings.
        for (String arg : args)
        {
            if (arg.equals("online_server"))
                Game.isOnlineServer = true;
            if (arg.matches("port=\\d+"))
                port = Integer.parseInt(arg.split("=")[1]);
            if (arg.equals("debug"))
                Game.debug = true;
            if (arg.equals("mac") || arg.equals("no_relaunch"))
                relaunch = false;
        }

        if (!Game.isOnlineServer)
        {
            try
            {
                if (relaunch && Game.framework == Game.Framework.lwjgl)
                {
                    // Attempts to relaunch from the .jar file.
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
                    // Creates and configures the LWJGL window.
                    Game.game.window = new LWJGLWindow(
                            "Tanks",
                            1400, 900 + Drawing.drawing.statsHeight,
                            Game.absoluteDepthBase,
                            new GameUpdater(), new GameDrawer(), new GameWindowHandler(),
                            Game.vsync, !Panel.showMouseTarget
                    );
                    Game.game.window.antialiasingEnabled = Game.antialiasing;
                }

                Game.postInitScript();

                Game.game.window.run();
            }
            catch (Throwable t)
            {
                fail(t);
            }
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

    public static void fail(Throwable e)
    {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        e.printStackTrace(p);

        StringBuilder props = new StringBuilder();
        Properties pr = System.getProperties();
        for (Object sr: pr.keySet())
            props.append(sr).append(": ").append(pr.get(sr)).append("\n");

        String errorMsg = "Oh noes!\n" +
                "Tanks ran into a problem and was unable to start :(\n\n" +
                "This may be caused by an error in the game, by launching the game incorrectly, or by missing drivers or unsupported hardware.\n\n" +
                "If you would like support regarding this issue, you may join the Tanks Discord via the following link:\n" +
                "https://discord.gg/aWPaJD3\n\n" +
                "Crash details:\n" +
                s.toString() + "\n";

        Game.logger.println(errorMsg + "System properties:\n" + props + "\n");
        System.err.println(errorMsg);

        if (props.toString().contains("Mac OS X"))
            System.exit(0);

        JFrame jFrame = new JFrame();
        jFrame.getRootPane().setWindowDecorationStyle(JRootPane.ERROR_DIALOG);
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JDialog jd = new JDialog(jFrame);

        jd.setLayout(new FlowLayout());

        jd.setBounds(500, 300, 400, 300);

        JTextArea jLabel = new JTextArea(errorMsg + "System properties:\n" + props + "\n", 40, 80);

        jLabel.setEditable(false);
        jLabel.setLineWrap(true);

        jd.addWindowListener(new WindowListener()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {

            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e)
            {

            }

            @Override
            public void windowIconified(WindowEvent e)
            {

            }

            @Override
            public void windowDeiconified(WindowEvent e)
            {

            }

            @Override
            public void windowActivated(WindowEvent e)
            {

            }

            @Override
            public void windowDeactivated(WindowEvent e)
            {

            }
        });


        JScrollPane scroll = new JScrollPane(jLabel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.createVerticalScrollBar();
        scroll.setBounds(0, 0, 100, 50);

        jd.add(scroll);
        jd.pack();
        jd.setVisible(true);
        jd.setTitle("Tanks");

        e.printStackTrace();
    }

    /**
     * Call this method to launch Tanks with extensions directly instead of loading them from a jar file!
     * This is useful if you want to test an extension without exporting it as a jar file.
     * The integer array passed determines the order in which these extensions will be added to the full list
     * (which includes extensions loaded from separate jar files traditionally)
     */
    public static void launchWithExtensions(String[] args, Extension[] extensions, int[] order)
    {
        Game.extraExtensions = extensions;
        Game.extraExtensionOrder = order;
        
        // Append "no_relaunch" to the arguments.
        String[] newArgs = Arrays.copyOf(args, args.length + 1);
        newArgs[args.length] = "no_relaunch";

        main(newArgs);
    }
}
