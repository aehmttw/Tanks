package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.Client;

public class ScreenJoinOnlineServer extends Screen
{
    public static Thread clientThread;

    public ScreenJoinOnlineServer()
    {
        this.music = "tomato_feast_2.ogg";
        this.musicID = "menu";

        ip.allowDots = true;
        ip.maxChars = 43;
        ip.allowColons = true;
        ip.lowerCase = true;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPlayMultiplayer();
        }
    }
    );

    TextBox ip = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 800, 40, "Online server URL or IP Address", new Runnable()
    {

        @Override
        public void run()
        {
            Game.lastOnlineServer = ip.inputText;
            ScreenOptions.saveOptions(Game.homedir);
        }
    },
            Game.lastOnlineServer);

    Button join = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Join", new Runnable()
    {
        @Override
        public void run()
        {
            Game.lastOfflineScreen = Game.screen;

            {
                Game.eventsOut.clear();
                clientThread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ScreenConnecting s = new ScreenConnecting(clientThread);
                        Game.screen = s;

                        try
                        {
                            String ipaddress = Game.lastOnlineServer;
                            int port = Game.port;

                            if (ipaddress.contains(":"))
                            {
                                port = Integer.parseInt(ipaddress.split(":")[1]);
                                ipaddress = ipaddress.split(":")[0];
                            }

                            if (ipaddress.equals(""))
                                Client.connect("localhost", Game.port, true); //TODO
                            else
                                Client.connect(ipaddress, port, true);
                        }
                        catch (Exception e)
                        {
                            s.text = "Failed to connect";
                            s.exception = e.getLocalizedMessage();
                            s.finished = true;

                            s.music = "tomato_feast_1.ogg";
                            Panel.forceRefreshMusic = true;

                            e.printStackTrace(Game.logger);
                            e.printStackTrace();
                        }
                    }

                });

                clientThread.setDaemon(true);
                clientThread.start();
            }
        }
    });



    @Override
    public void update()
    {
        ip.update();
        join.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        ip.draw();
        join.draw();
        back.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Select online server");

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "The official online server is not yet available.");
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, "However, you can join a 3rd party online server.");

    }
}
