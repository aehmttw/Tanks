package tanks.modapi.modlevels.Battle_Tanks_3.settings;

import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenPartyHost;
import tanks.modapi.ModAPI;
import tanks.modapi.modlevels.Battle_Tanks_3.Battle_Tanks_3;

public class Setting4
{
    public static int messageNum = 0;
    public static boolean removed = false;

    public static void setUp(Battle_Tanks_3 game)
    {
        messageNum = 0;
        removed = false;

        ModAPI.loadLevel(ModAPI.getLevelString("setting 3"));

        game.initGeneralTank(2, 18, 90);

        game.createTree(6, 7, 4);
        game.createTree(17, 10, 3);
        game.createTree(5, 20, 4);
        game.createTree(16, 21, 3);
    }

    public static void draw(Battle_Tanks_3 game)
    {
        if (game.timer > 700 && game.generalTank.overrideDisplayState)
            game.generalTank.setOverrideState(false);

        if (game.timer > 500 && messageNum == 2) {
            game.generalTank.setMessages("Good luck.");
            messageNum++;
        }
        else if (game.timer > 300 && messageNum == 1) {
            game.generalTank.setMessages("Cross them, and be careful.");
            messageNum++;
        }
        else if (messageNum == 0) {
            game.generalTank.setMessages("The coordinates on the journal point to a location inside this mountain range.");
            messageNum++;
        }
    }

    public static void update(Battle_Tanks_3 game)
    {
        if (game.timer > 700 && !removed) {
            Game.removeMovables.add(game.generalTank);
            removed = true;
        }

        if (ScreenPartyHost.isServer)
            for (Player p : Game.players)
            {
                if (p.tank.posX > 100 * Game.tile_size) {
                    game.levelEnd = true;
                    break;
                }
            }

        else if (Game.playerTank.posX > 100 * Game.tile_size)
            game.levelEnd = true;
    }
}
