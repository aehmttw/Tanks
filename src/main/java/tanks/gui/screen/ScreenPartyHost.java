package tanks.gui.screen;

import tanks.*;
import tanks.network.ServerHandler;
import tanks.network.event.EventPlayerChat;
import tanks.generator.LevelGeneratorVersus;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Server;
import tanks.network.SynchronizedList;
import tanks.tank.Tank;
import tanks.tank.TankModels;
import tanks.translation.Translation;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

public class ScreenPartyHost extends Screen
{
    Thread serverThread;
    public static Server server;
    public static boolean isServer = false;
    public static SynchronizedList<UUID> includedPlayers = new SynchronizedList<>();
    public static SynchronizedList<Player> readyPlayers = new SynchronizedList<>();
    public static SynchronizedList<UUID> disconnectedPlayers = new SynchronizedList<>();
    public static ScreenPartyHost activeScreen;

    public String ip = "";

    public Button[] kickButtons = new Button[entries_per_page];

    public int usernamePage = 0;
    protected int lastConnectionCount = 0;

    public static int entries_per_page = 10;
    public static int username_spacing = 30;
    public static int username_y_offset = -240;

    public static SynchronizedList<ChatMessage> chat = new SynchronizedList<>();

    public static ChatBox chatbox;

    public SynchronizedList<SharedLevel> sharedLevels = new SynchronizedList<>();
    public SynchronizedList<SharedCrusade> sharedCrusades = new SynchronizedList<>();

    Button newLevel = new Button(this.centerX + 190, this.centerY - 250, this.objWidth, this.objHeight, "Random co-op", () ->
    {
        Game.cleanUp();
        Game.loadRandomLevel();
        Game.screen = new ScreenGame();
    }
            , "Generate a random level to play");

    Button nextUsernamePage = new Button(this.centerX - 190,
            this.centerY + username_y_offset + username_spacing * (1 + entries_per_page), 300, 30, "Next page", () -> usernamePage++
    );

    Button previousUsernamePage = new Button(this.centerX - 190, this.centerY + username_y_offset,
            300, 30, "Previous page", () -> usernamePage--
    );

    Button versus = new Button(this.centerX + 190, this.centerY - 190, this.objWidth, this.objHeight, "Random versus", () ->
    {
        Game.cleanUp();
        String s = LevelGeneratorVersus.generateLevelString();
        Level l = new Level(s);
        l.loadLevel();
        ScreenGame.versus = true;

        Game.screen = new ScreenGame();
    }
            , "Fight other players in this party---in a randomly generated level");

    Button crusades = new Button(this.centerX + 190, this.centerY - 130, this.objWidth, this.objHeight, "Crusades", () ->
    {
        if (Crusade.currentCrusade == null)
            Game.screen = new ScreenPartyCrusades();
        else
            Game.screen = new ScreenPartyResumeCrusade();
    },
            "Fight battles in an order,---and see how long you can survive!");

    Button minigames = new Button(this.centerX + 190, this.centerY - 70, this.objWidth, this.objHeight, "Minigames", () ->
    {
        Game.screen = new ScreenMinigames();
    },
            "Play Tanks in new ways!");

    Button myLevels = new Button(this.centerX + 190, this.centerY - 10, this.objWidth, this.objHeight, "My levels", () -> Game.screen = new ScreenPlaySavedLevels(),
            "Play levels you have created");

    Button share = new Button(this.centerX + 190, this.centerY + 80, this.objWidth, this.objHeight, "Upload", () -> Game.screen = new ScreenShareSelect());

    Button shared = new Button(this.centerX + 190, this.centerY + 140, this.objWidth, this.objHeight, "Download", () -> Game.screen = new ScreenSharedSummary(sharedLevels, sharedCrusades));

    Button options = new Button(this.centerX - 190, this.centerY + 210, this.objWidth, this.objHeight, "Options", () -> Game.screen = new ScreenOptions());

    Button partyOptions = new Button(this.centerX + 190, this.centerY + 210, this.objWidth, this.objHeight, "Party options", () ->
    {
        ScreenOptionsPartyHost s = new ScreenOptionsPartyHost();
        s.fromParty = true;
        Game.screen = s;
    });

    Button quit = new Button(this.centerX, this.centerY + 270, this.objWidth, this.objHeight, "End party", () -> Game.screen = new ScreenConfirmEndParty());

    Button toggleIP = new Button(-1000, -1000, this.objHeight, this.objHeight, "", () -> Game.showIP = !Game.showIP, "Toggle showing IP address");

    public ScreenPartyHost()
    {
        super(350, 40, 380, 60);

        if (ScreenPartyHost.server == null || ScreenPartyHost.server.connections.size() <= 0)
            this.music = "menu_3.ogg";
        else
            this.music = "menu_4.ogg";

        this.musicID = "menu";
        toggleIP.fullInfo = true;

        chatbox = new ChatBox(this.centerX, Drawing.drawing.getInterfaceEdgeY(true) - 30, Drawing.drawing.interfaceSizeX - 20, 40, Game.game.input.chat, () ->
        {
            ScreenPartyHost.chat.add(0, new ChatMessage(Game.player, ScreenPartyHost.chatbox.inputText));
            Game.eventsOut.add(new EventPlayerChat(Game.player, ScreenPartyHost.chatbox.inputText));
        });

        if (Game.game.window.touchscreen)
        {
            chatbox.defaultText = "Click here to send a chat message";
        }

        for (int i = 0; i < this.kickButtons.length; i++)
        {
            final int j = i;
            kickButtons[i] = new Button(this.centerX - 35,
                    this.centerY + (1 + i) * username_spacing + username_y_offset, 25, 25, "x", () -> Game.screen = new ScreenPartyKick(server.connections.get(j + usernamePage * entries_per_page)));

            kickButtons[i].textOffsetY = -2.5;

            kickButtons[i].textColR = 255;
            kickButtons[i].textColG = 255;
            kickButtons[i].textColB = 255;

            kickButtons[i].unselectedColR = 160;
            kickButtons[i].unselectedColG = 160;
            kickButtons[i].unselectedColB = 160;

            kickButtons[i].selectedColR = 255;
            kickButtons[i].selectedColG = 0;
            kickButtons[i].selectedColB = 0;

            kickButtons[i].fontSize = this.textSize;
        }

        activeScreen = this;
        isServer = true;
        serverThread = new Thread(() ->
        {
            try
            {
                server = new Server(Game.port);
                server.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();

        new Thread(() ->
        {
            ip = Translation.translate("Getting your IP Address...");
            try
            {
                ip = Translation.translate("Your Local IP Address: %s (Port: %d)", Inet4Address.getLocalHost().getHostAddress(), Game.port);
            }
            catch (UnknownHostException e)
            {
                ip = Translation.translate("Connect to a non-cellular data network to play with others!");
            }

            if (ip.contains("%"))
                ip = Translation.translate("Connect to a network to play with others!");

            if (ip.contains("127.0.0.1"))
                ip = Translation.translate("Party host");

        }
        ).start();
    }

    @Override
    public void update()
    {
        newLevel.update();
        crusades.update();
        myLevels.update();
        versus.update();
        minigames.update();
        share.update();
        shared.update();
        options.update();
        partyOptions.update();
        quit.update();

        if (server != null && server.connections != null)
        {
            if (this.usernamePage > 0)
                this.previousUsernamePage.update();

            if ((this.usernamePage + 1) * 10 < server.connections.size())
                this.nextUsernamePage.update();

            int entries = Math.min(10, server.connections.size() - this.usernamePage * entries_per_page);

            for (int i = 0; i < entries; i++)
            {
                this.kickButtons[i].update();
            }
        }

        if (!this.ip.equals(Translation.translate("Party host")))
            this.toggleIP.update();

        int c = server.connections.size();

        if (lastConnectionCount != c)
        {
            if (c <= 0)
                this.music = "menu_3.ogg";
            else
                this.music = "menu_4.ogg";
            Panel.forceRefreshMusic = true;
        }

        this.lastConnectionCount = c;
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        partyOptions.draw();
        options.draw();
        myLevels.draw();
        minigames.draw();
        crusades.draw();
        versus.draw();
        newLevel.draw();
        share.draw();
        shared.draw();
        quit.draw();

        Drawing.drawing.setColor(0, 0, 0);

        double ipY = 360;
        if (Game.steamNetworkHandler.initialized)
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 320, "Also hosting on Steam peer-to-peer (Steam friends can join)");
        else
            ipY = 330;

        String title = this.ip;

        if (!Game.showIP)
            title = Translation.translate("Party host");

        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - ipY, title);
        this.toggleIP.posX = this.centerX + Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, title) / Drawing.drawing.interfaceScale / 2 + 30;
        this.toggleIP.posY = this.centerY - ipY;

        if (Game.showIP)
            this.toggleIP.setText("-");
        else
            this.toggleIP.setText("+");

        if (!this.ip.equals(Translation.translate("Party host")))
            this.toggleIP.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.textSize);

        Drawing.drawing.displayInterfaceText(this.centerX + 190, this.centerY - 290, "Play:");

        Drawing.drawing.displayInterfaceText(this.centerX + 190, this.centerY + 40, "Level and crusade sharing:");

        Drawing.drawing.displayInterfaceText(this.centerX - 190, this.centerY - 280, "Players in this party:");

        if (server != null && server.connections != null)
        {
            if (this.usernamePage > 0)
                this.previousUsernamePage.draw();

            if ((this.usernamePage + 1) * entries_per_page <  server.connections.size())
                this.nextUsernamePage.draw();

            if (this.usernamePage <= 0)
            {
                String n = Game.player.username;
                if (Game.enableChatFilter)
                    n = Game.chatFilter.filterChat(n);

                n = "\u00A7000127255255" + n;

                Drawing.drawing.setBoundedInterfaceFontSize(this.textSize, 250, Game.player.username);
                Drawing.drawing.drawInterfaceText(this.centerX - 190, this.centerY + username_y_offset, n);
                Tank.drawTank(this.centerX - Drawing.drawing.getStringWidth(n) / 2 - 230, this.centerY + username_y_offset, Game.player.colorR, Game.player.colorG, Game.player.colorB, Game.player.turretColorR, Game.player.turretColorG, Game.player.turretColorB);
            }

            if (server.connections != null)
            {
                for (int i = this.usernamePage * entries_per_page; i < Math.min(((this.usernamePage + 1) * entries_per_page), server.connections.size()); i++)
                {
                    ServerHandler h = server.connections.get(i);
                    if (h.username != null)
                    {
                        try
                        {
                            double y = this.centerY + (1 + i - this.usernamePage * entries_per_page) * username_spacing + username_y_offset;
                            Drawing.drawing.setBoundedInterfaceFontSize(this.textSize, 250, server.connections.get(i).username);
                            double w = Drawing.drawing.getStringWidth(h.username) / 2;
                            Drawing.drawing.setColor(0, 0, 0);
                            Drawing.drawing.drawInterfaceText(this.centerX - 190, y, server.connections.get(i).username);

                            Tank.drawTank(this.centerX - w - 230, y, h.player.colorR, h.player.colorG, h.player.colorB, h.player.turretColorR, h.player.turretColorG, h.player.turretColorB);

                            this.kickButtons[i - this.usernamePage * entries_per_page].draw();

                            Drawing.drawing.setInterfaceFontSize(this.textSize / 2);
                            Drawing.drawing.setColor(0, 0, 0);
                            Drawing.drawing.drawInterfaceText(this.centerX - w - 255, y, server.connections.get(i).lastLatency + "ms", true);
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }
            }
        }
    }

    public static class SharedLevel
    {
        public String level;
        public String name;
        public String creator;

        public SharedLevel(String level, String name, String creator)
        {
            this.level = level;
            this.name = name;
            this.creator = creator;
        }
    }

    public static class SharedCrusade
    {
        public String crusade;
        public String name;
        public String creator;

        public SharedCrusade(String crusade, String name, String creator)
        {
            this.crusade = crusade;
            this.name = name;
            this.creator = creator;
        }
    }

}
