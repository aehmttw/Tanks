package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.Client;

import java.util.UUID;

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

    Button back = new Button(this.centerX, this.centerY + objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPlayMultiplayer();
        }
    }
    );

    TextBox ip = new TextBox(this.centerX, this.centerY - objYSpace / 2, this.objWidth * 16 / 7, this.objHeight, "Online server URL or IP Address", new Runnable()
    {

        @Override
        public void run()
        {
            Game.lastOnlineServer = ip.inputText;
            ScreenOptions.saveOptions(Game.homedir);
        }
    },
            Game.lastOnlineServer);

    Button join = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Join", new Runnable()
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

                        UUID connectionID = UUID.randomUUID();
                        Client.connectionID = connectionID;

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
                                Client.connect("localhost", Game.port, true, connectionID); //TODO
                            else
                                Client.connect(ipaddress, port, true, connectionID);
                        }
                        catch (Exception e)
                        {
                            if (Game.screen == s && Client.connectionID == connectionID)
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
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Select online server");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "The official online server is not yet available.");
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "However, you can join a 3rd party online server.");

    }
}
