package tanks.gui.screen;

import tanks.*;
import tanks.event.EventPlayerChat;
import tanks.generator.LevelGeneratorVersus;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.network.Server;
import tanks.network.SynchronizedList;
import tanks.translation.Translation;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

public class ScreenPartyHost extends Screen
{
    Thread serverThread;
    public static Server server;
    public static boolean isServer = false;
    public static final SynchronizedList<UUID> includedPlayers = new SynchronizedList<>();
    public static final SynchronizedList<Player> readyPlayers = new SynchronizedList<>();
    public static final SynchronizedList<UUID> disconnectedPlayers = new SynchronizedList<>();
    public static ScreenPartyHost activeScreen;

    public String ip = "";

    public Button[] kickButtons = new Button[entries_per_page];

    public int usernamePage = 0;

    public static int entries_per_page = 10;
    public static int username_spacing = 30;
    public static int username_y_offset = -240;

    public static SynchronizedList<ChatMessage> chat = new SynchronizedList<>();

    public static ChatBox chatbox;

    public SynchronizedList<SharedLevel> sharedLevels = new SynchronizedList<>();
    public SynchronizedList<SharedCrusade> sharedCrusades = new SynchronizedList<>();

    Button newLevel = new Button(this.centerX + 190, this.centerY - 240, this.objWidth, this.objHeight, "Random level", () ->
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

    Button versus = new Button(this.centerX + 190, this.centerY - 180, this.objWidth, this.objHeight, "Versus", () ->
    {
        Game.cleanUp();
        String s = LevelGeneratorVersus.generateLevelString();
        Level l = new Level(s);
        l.loadLevel();
        ScreenGame.versus = true;

        Game.screen = new ScreenGame();
    }
            , "Fight other players in this party---in a randomly generated level");

    Button crusades = new Button(this.centerX + 190, this.centerY - 60, this.objWidth, this.objHeight, "Crusades", () ->
    {
        if (Crusade.currentCrusade == null)
            Game.screen = new ScreenPartyCrusades();
        else
            Game.screen = new ScreenPartyResumeCrusade();
    },
            "Fight battles in an order,---and see how long you can survive!");

    Button myLevels = new Button(this.centerX + 180 + this.objWidth / 4, this.centerY - 120, this.objWidth / 2 + 10, this.objHeight, "My levels", () -> Game.screen = new ScreenPlaySavedLevels(),
            "Play levels you have created");

    Button modLevels = new Button(this.centerX + 190 - this.objWidth / 4, this.centerY - 120, this.objWidth / 2, this.objHeight, "Mod levels", () -> Game.screen = new ScreenModdedLevels());

    Button share = new Button(this.centerX + 190, this.centerY + 40, this.objWidth, this.objHeight, "Upload", () -> Game.screen = new ScreenShareSelect());

    Button shared = new Button(this.centerX + 190, this.centerY + 100, this.objWidth, this.objHeight, "Download", () -> Game.screen = new ScreenSharedSummary(sharedLevels, sharedCrusades)
    );

    Button options = new Button(this.centerX + 190, this.centerY + 200, this.objWidth, this.objHeight, "Options", () -> Game.screen = new ScreenOptions()
    );

    Button partyOptions = new Button(this.centerX + 190, this.centerY + 260, this.objWidth, this.objHeight, "Party options", () -> Game.screen = new ScreenOptionsPartyHost()
    );

    Button quit = new Button(this.centerX - 170, this.centerY + 340, this.objWidth, this.objHeight, "End party", () -> Game.screen = new ScreenConfirmEndParty()
    );

    public ScreenPartyHost()
    {
        super(350, 40, 380, 60);

        this.music = "menu_3.ogg";
        this.musicID = "menu";

        chatbox = new ChatBox(this.centerX, Drawing.drawing.interfaceSizeY - 30, Drawing.drawing.interfaceSizeX - 20, 40, Game.game.input.chat, () ->
        {
            ScreenPartyHost.chat.add(0, new ChatMessage(Game.player, ScreenPartyHost.chatbox.inputText));
            Game.eventsOut.add(new EventPlayerChat(Game.player, ScreenPartyHost.chatbox.inputText));
        });

        if (Game.game.window.touchscreen)
            chatbox.defaultText = "Click here to send a chat message";

        for (int i = 0; i < this.kickButtons.length; i++)
        {
            final int j = i;
            kickButtons[i] = new Button(this.centerX - 20,
                    this.centerY + (1 + i) * username_spacing + username_y_offset, 25, 25, "x", () -> Game.screen = new ScreenPartyKick(server.connections.get(j + usernamePage * entries_per_page)));

            kickButtons[i].textOffsetY = -1;
            kickButtons[i].textOffsetX = 1;

            kickButtons[i].textColR = 255;
            kickButtons[i].textColG = 255;
            kickButtons[i].textColB = 255;

            kickButtons[i].unselectedColR = 255;
            kickButtons[i].unselectedColG = 0;
            kickButtons[i].unselectedColB = 0;

            kickButtons[i].selectedColR = 255;
            kickButtons[i].selectedColG = 127;
            kickButtons[i].selectedColB = 127;

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
            try {
                ip = Translation.translate("Your Local IP Address: %s (Port: %d)", Inet4Address.getLocalHost().getHostAddress(), Game.port);
            }
            catch (UnknownHostException e) {
                ip = Translation.translate("Connect to a non-cellular data network to play with others!");
            }

            if (ip.contains("%"))
                ip = Translation.translate("Connect to a network to play with others!");

            if (ip.contains("127.0.0.1"))
                ip = Translation.translate("Party host");

        }).start();
    }

    @Override
    public void update()
    {
        newLevel.update();
        crusades.update();
        myLevels.update();
        modLevels.update();
        versus.update();
        share.update();
        shared.update();
        partyOptions.update();
        options.update();
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
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        options.draw();
        partyOptions.draw();
        crusades.draw();
        myLevels.draw();
        modLevels.draw();
        versus.draw();
        newLevel.draw();
        share.draw();
        shared.draw();
        quit.draw();


        Drawing.drawing.setColor(0, 0, 0);

        if (Game.steamNetworkHandler.initialized)
        {
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 360, this.ip);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 320, "Also hosting on Steam peer-to-peer (Steam friends can join)");
        }
        else
        {
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 330, this.ip);
        }

        Drawing.drawing.displayInterfaceText(this.centerX + 190, this.centerY - 280, "Play:");

        Drawing.drawing.displayInterfaceText(this.centerX + 190, this.centerY + 0, "Level and crusade sharing:");

        Drawing.drawing.displayInterfaceText(this.centerX - 190, this.centerY - 280, "Players in this party:");

        Drawing.drawing.displayInterfaceText(this.centerX + 190, this.centerY + 160, "Options");

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

                n = Colors.blue + n;

                Drawing.drawing.drawInterfaceText(this.centerX - 190, this.centerY + username_y_offset, n);
            }

            if (server.connections != null)
            {
                for (int i = this.usernamePage * entries_per_page; i < Math.min(((this.usernamePage + 1) * entries_per_page), server.connections.size()); i++)
                {
                    if (server.connections.get(i).username != null)
                    {
                        try
                        {
                            Drawing.drawing.setInterfaceFontSize(this.textSize);
                            Drawing.drawing.setColor(0, 0, 0);
                            Drawing.drawing.drawInterfaceText(this.centerX - 190,
                                    this.centerY + (1 + i - this.usernamePage * entries_per_page) * username_spacing + username_y_offset,
                                    server.connections.get(i).username);

                            this.kickButtons[i - this.usernamePage * entries_per_page].draw();

                            Drawing.drawing.setInterfaceFontSize(this.textSize / 2);
                            Drawing.drawing.setColor(0, 0, 0);
                            Drawing.drawing.drawInterfaceText(this.centerX - 370,
                                    this.centerY + (1 + i - this.usernamePage * entries_per_page) * username_spacing + username_y_offset,
                                    server.connections.get(i).lastLatencyAverage + "ms");
                        }
                        catch (Exception ignored) {}
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
