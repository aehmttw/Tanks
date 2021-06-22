package tanks.rpc;

import club.minnced.discord.rpc.*;
import tanks.Game;

public class RichPresence {
    public static String APP_ID = "761613328509567066";
    public static String STEAM_ID = "1660910";

    DiscordRPC lib;
    DiscordRichPresence presence;
    DiscordEventHandlers handlers;
    Thread callbacks;

    public RichPresence() {
        if (!Game.game.presenceEnabled) return;

        lib = DiscordRPC.INSTANCE;
        presence = new DiscordRichPresence();

        handlers = new DiscordEventHandlers();
        //handlers.ready = (DiscordUser user) -> {System.out.println("Rich Presence Ready!");};

        lib.Discord_Initialize(APP_ID, handlers, true, STEAM_ID);

        presence.startTimestamp = Game.game.gameStartTime;
        presence.details = "Tanks %s".format(Game.version);
        presence.state = "Starting Up...";
        presence.largeImageKey = "tanks";
        presence.largeImageText = "Tanks %s".format(Game.version);
        lib.Discord_UpdatePresence(presence);

        callbacks = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    lib.Discord_Shutdown();
                    break;
                }
            }
        }, "RPC-Callback-Handler");
        callbacks.start();
    }

    public void exit(){
        if (!Game.game.presenceEnabled) return;
        callbacks.interrupt();
    }

    public void update(int gamemode) { this.update(gamemode, RichPresenceEvent.IDLE); }

    public void update(int gamemode, int screen) {
        if (!Game.game.presenceEnabled) return;

        if (gamemode == RichPresenceEvent.TITLE_SCREEN) {
            presence.details = "Title Screen";
        } else if (gamemode == RichPresenceEvent.OPTIONS_MENU) {
            presence.details = "Options Menu";
        } else if (gamemode == RichPresenceEvent.SINGLEPLAYER) {
            presence.details = "Playing Alone";
        } else if (gamemode == RichPresenceEvent.MULTIPLAYER) {
            presence.details = "In a Party";
        } else if (gamemode == RichPresenceEvent.BSOD) {
            presence.details = "Blue Screen of Death";
        } else {
            presence.details = "Tanks %s".format(Game.version);
        }

        if (screen == RichPresenceEvent.RANDOM_LEVEL) {
            presence.state = "Fighting a Random Level";
        } else if (screen == RichPresenceEvent.CRUSADE_SELECT) {
            presence.state = "Selecting a Crusade";
        } else if (screen == RichPresenceEvent.CRUSADE) {
            presence.state = "Fighting a Crusade";
        } else if (screen == RichPresenceEvent.LEVEL_SELECT) {
            presence.state = "Selecting a Custom Level";
        } else if (screen == RichPresenceEvent.CUSTOM_LEVEL) {
            presence.state = "Fighting a Custom Level";
        } else if (screen == RichPresenceEvent.LEVEL_EDITOR) {
            presence.state = "Editing a Custom Level";
        } else if (screen == RichPresenceEvent.TUTORIAL) {
            presence.state = "In the Tutorial"; // Using the word "in" only because there is no Play Tutorial option in parties
        } else if (screen == RichPresenceEvent.IDLE) {
            presence.state = "Idle";
        } else if (screen == RichPresenceEvent.VERSUS) {
            presence.state = "Fighting Others";
        } else if (screen == RichPresenceEvent.CRUSADE_EDITOR) {
            presence.state = "Editing a Crusade";
        } else {
            presence.state = "Idle";
        }

        lib.Discord_UpdatePresence(presence);
    }
}
