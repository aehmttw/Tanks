package tanks.modapi.modlevels.Battle_Tanks_3.settings;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.Player;
import tanks.gui.screen.ScreenPartyHost;
import tanks.modapi.ModAPI;
import tanks.modapi.TankNPC;
import tanks.modapi.menus.FixedMenu;
import tanks.modapi.menus.FixedText;
import tanks.modapi.modlevels.Battle_Tanks_3.Battle_Tanks_3;

public class Setting3
{
    public static int messageNum = 0;
    static double textUpdateCooldown = 50;
    static double id;

    public static void setUp(Battle_Tanks_3 game)
    {
        messageNum = 0;
        ModAPI.loadLevel(ModAPI.getLevelString("general hub"));
        game.initGeneralTank(26, 16, -90);
        Drawing.drawing.setColor(255, 255, 255);
        ModAPI.addTextObstacle(8, 15, "Check with the shopkeeper!---<--");
        game.enableShooting = false;
        game.enableLayingMines = false;
        ModAPI.addObject(new TankNPC("shop", 1, 16, 0, "/shop", "Shop", 100, 100, 100));
        ModAPI.addTextObstacle(14, 10, "Exit door to go to---next level");
        ModAPI.displayText(FixedText.types.actionbar, "");

        for (FixedMenu m : ModAPI.menuGroup)
        {
            if (m instanceof FixedText && ((FixedText) m).text.length() == 0) {
                id = m.id;
                break;
            }
        }
    }

    public static void draw(Battle_Tanks_3 game)
    {
        if (game.timer > 3000 && game.generalTank.overrideDisplayState)
            game.generalTank.setOverrideState(false);

        if (game.timer > 2800 && messageNum == 9) {
            game.generalTank.setMessages("And also, good luck.");
            messageNum++;
        }
        else if (game.timer > 2400 && messageNum == 7) {
            game.generalTank.setMessages("Set up a hideout in a location that is as close as possible to the location, but also very hidden.");
            messageNum++;
        }
        else if (game.timer > 2100 && messageNum == 6) {
            game.generalTank.setMessages("I would like you to gather some information about this place.");
            messageNum++;
        }
        else if (game.timer > 1700 && messageNum == 5) {
            game.generalTank.setMessages("Besides, since there was little storage space at the explosives factory, it's the only lead we've got to their storage system.");
            messageNum++;
        }
        else if (game.timer > 1500 && messageNum == 4) {
            game.generalTank.setMessages("Destroying this location could potentially weaken them a lot.");
            messageNum++;
        }
        else if (game.timer > 1000 && messageNum == 3) {
            game.generalTank.setMessages("Supply location: 49.86095640282017, -117.18315571462577");
            messageNum++;
        }
        else if (game.timer > 800 && messageNum == 2) {
            game.generalTank.setMessages("The third-to-last page says:");
            messageNum++;
        }
        else if (game.timer > 300 && messageNum == 1) {
            game.generalTank.setMessages("Your next mission would have been to invade some enemy military stations, but we have found some crucial information on the journal.");
            messageNum++;
        }
        else if (messageNum == 0) {
            game.generalTank.setMessages("Good job raiding the explosives factory.");
            messageNum++;
        }
    }

    public static void update(Battle_Tanks_3 game)
    {
        textUpdateCooldown -= Panel.frameFrequency;

        int playersNeeded = 0;
        if (ScreenPartyHost.isServer)
            for (Player p : Game.players)
            {
                if (p.tank.posY > Game.tile_size * 6)
                {
                    playersNeeded++;
                    break;
                }
            }

        else
            playersNeeded = Game.playerTank.posY < Game.tile_size * 6 ? 0 : 1;

        if (textUpdateCooldown <= 0)
        {
            ModAPI.editText(id, playersNeeded + (playersNeeded == 1 ? " player " : " players ") + "needed to go to next level");
            textUpdateCooldown = 50;
        }

        game.levelEnd = playersNeeded == 0;
    }
}
