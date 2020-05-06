package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.network.Client;

public class ScreenPlayMultiplayer extends Screen
{
    public static Thread clientThread;

    Button party = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenParty();
        }
    },
            "Play with other people who are---connected to your local network---(or who are port forwarding)");

    Button online = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Online", new Runnable()
    {
        @Override
        public void run()
        {
            Game.lastOfflineScreen = Game.screen;

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT))
                Game.screen = new ScreenSelectOnlineServer();
            else
            {
                if (Game.lastOnlineServer.equals(""))
                    Game.screen = new ScreenOnlineWIP();
                else
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

                                e.printStackTrace(Game.logger);
                                e.printStackTrace();
                            }
                        }

                    });

                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            }
        }
    },
            "Access the online Tanks community!------(Shift + Click to change server)");



    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPlay();
        }
    }
    );

    @Override
    public void update()
    {
        party.update();
        online.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, "Select a multi-player game mode");
        back.draw();
        online.draw();
        party.draw();
    }

}
