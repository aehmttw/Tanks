package tanks.modapi.modlevels.Battle_Tanks_3.settings;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.event.EventMineExplode;
import tanks.modapi.ModAPI;
import tanks.modapi.menus.FixedText;
import tanks.modapi.modlevels.Battle_Tanks_3.BattleTanks3;
import tanks.modapi.modlevels.Battle_Tanks_3.movables.Journal;
import tanks.tank.Mine;

public class Setting2
{
    public static int messageNum = 0;
    public static Journal journal;
    public static boolean added = false;
    public static boolean collected = false;

    public static void setUp(BattleTanks3 game)
    {
        messageNum = 0;
        added = false;
        collected = false;
        Drawing.drawing.movingCamera = false;

        game.listeningForEvents = true;

        ModAPI.loadLevel("{83,30,0,220,0,50,35,50,0,100,50|42...45-18-normal-5.0,48...50-14-normal-5.0,51-20...22-normal-5.0,56...59-18-normal-5.0,65-6...7-normal-5.0,68...78-7-normal-0.5,68...78-8-normal-0.5,68...78-11-normal-0.5,68...78-12-normal-0.5,70...72-17-normal-0.5,70...72-18-normal-0.5,70...72-19-normal-0.5,35...82-3-hard-5.0,35-4...13-hard-5.0,35-18...25-hard-5.0,36...37-4-hard-5.0,36...80-5-hard-5.0,36...41-18-hard-5.0,36...82-23-hard-5.0,36-24...25-hard-5.0,37...82-25-hard-5.0,38...40-24-hard-5.0,39...44-4-hard-5.0,41-10...17-hard-5.0,42...46-24-hard-5.0,46...50-4-hard-5.0,46-6...18-hard-5.0,47-14-hard-5.0,47...55-18-hard-5.0,48...51-24-hard-5.0,51...59-14-hard-5.0,51-19-hard-5.0,52...55-4-hard-5.0,53...56-24-hard-5.0,57...60-4-hard-5.0,58...60-24-hard-5.0,60-13-hard-5.0,60-15-hard-5.0,60...63-18-hard-5.0,61...64-14-hard-5.0,62...65-4-hard-5.0,62...64-24-hard-5.0,64-15...17-hard-5.0,64-19...22-hard-5.0,65-8...13-hard-5.0,66...69-24-hard-5.0,67...70-4-hard-5.0,71...74-24-hard-5.0,72...77-4-hard-5.0,76...79-24-hard-5.0,79...82-4-hard-5.0,79-6...22-hard-5.0,80-6...22-hard-5.0,81-6...11-hard-5.0,81-13...19-hard-5.0,81...82-21-hard-5.0,81...82-22-hard-5.0,81...82-24-hard-5.0,82-5...20-hard-5.0,65-14...22-nobounce-5.0,66...72-14-nobounce-5.0,66...78-22-nobounce-5.0,75...78-14-nobounce-5.0,78-15...21-nobounce-5.0,37-24-explosive-5.0,38-4-explosive-5.0,41-24-explosive-5.0,45-4-explosive-5.0,47-24-explosive-5.0,51-4-explosive-5.0,52-24-explosive-5.0,56-4-explosive-5.0,57-24-explosive-5.0,60-14-explosive-5.0,61-4-explosive-5.0,61-24-explosive-5.0,64-18-explosive-5.0,65-24-explosive-5.0,66-4-explosive-5.0,66-15-explosive-5.0,66-21-explosive-5.0,70-24-explosive-5.0,71-4-explosive-5.0,75-24-explosive-5.0,77-15-explosive-5.0,78-4-explosive-5.0,80-24-explosive-5.0,81-5-explosive-5.0,81-12-explosive-5.0,81-20-explosive-5.0,0-2...6-shrub,0-8...13-shrub,1-3...13-shrub,2-4...11-shrub,2...8-18-shrub,2...7-19-shrub,3-4...11-shrub,3...9-17-shrub,3...7-20-shrub,4...6-15-shrub,4...8-16-shrub,5-1-shrub,5-14-shrub,6...8-0-shrub,8...20-1-shrub,8...20-2-shrub,8...13-3-shrub,8...20-4-shrub,8...20-5-shrub,9...14-6-shrub,10...18-0-shrub,14...15-7-shrub,15...20-3-shrub,16...20-6-shrub,17...19-7-shrub,19...28-11-shrub,20...26-10-shrub,20...28-12-shrub,21...25-9-shrub,22...24-8-shrub,23...27-13-shrub,24...25-7-shrub,27-9-shrub,28-10-shrub,0...3-0-path,0...4-1-path,0-2-path,2...5-2-path,2...6-3-path,2...6-4-path,4...7-5-path,5...8-6-path,5...9-7-path,6...9-8-path,7...10-9-path,8...11-10-path,8...12-11-path,10...14-12-path,11...16-13-path,12...19-14-path,12...20-15-path,15...21-16-path,17...22-17-path,20...34-14-path-5.0,21...34-15-path-5.0,22...34-16-path-5.0,23...34-17-path-5.0,77-21-explosive-5.0|34-18-brown-2-enemy,34-13-brown-2-enemy,37-7-gray-1-enemy,38-7-gray-1-enemy,38-20-mint-0-enemy,38-21-mint-0-enemy,62-20-magenta-2-enemy,62-21-magenta-2-enemy,63-16-brown-2-enemy,49-16-mint-0-enemy,59-8-magenta-2-enemy,62-12-magenta-2-enemy,56-10-magenta-2-enemy,53-8-magenta-2-enemy,69-6-dummy-1-enemy,71-6-dummy-1-enemy,73-6-dummy-1-enemy,75-6-dummy-1-enemy,77-6-dummy-1-enemy,77-13-dummy-3-enemy,75-13-dummy-3-enemy,73-13-dummy-3-enemy,71-13-dummy-3-enemy,69-13-dummy-3-enemy,24-10-player-0-ally,15-4-player-0-ally,1-8-player-0-ally,6-17-player-0-ally|ally-true,enemy-true}\nitems\nMine 45s timer,mine.png,1,0,3,100,mine,4500.0,5000.0,200.0,2.0,1,100.0,35.0,true\n");

        game.createTree(15, 4, 2);
        game.createTree(26, 9, 3);
        game.createTree(22, 22, 4);

        game.initGeneralTank(11, 4, 90);
        game.generalTank.angle = game.generalTank.getAngleInDirection(0, 0);

        ModAPI.fillObstacle(35, 3, 82, 25, "hard", 0.5, 4.5);

        journal = new Journal();
    }

    public static void draw(BattleTanks3 game)
    {
        if (game.timer > 1200 && game.generalTank.overrideDisplayState)
            game.generalTank.setOverrideState(false);

        if (game.timer > 900 && messageNum == 3)
        {
            game.generalTank.setMessages("Good luck.");
            messageNum++;
        }
        else if (game.timer > 600 && messageNum == 2)
        {
            game.generalTank.setMessages("Steal their journal, lay the mine, and GET OUT OF THERE.");
            messageNum++;
        }
        else if (game.timer > 300 && messageNum == 1)
        {
            game.generalTank.setMessages("I've given you some mines with a 45 second fuse to blow the factory up.");
            messageNum++;
        }
        else if (game.timer > 5 && messageNum == 0)
        {
            game.generalTank.setMessages("Here is the explosives factory you need to sabotage.");
            game.generalTank.setOverrideState(true);
            messageNum++;
        }
    }

    public static void update(BattleTanks3 game)
    {
        if (game.fadeIsDone() && !Drawing.drawing.movingCamera)
            Drawing.drawing.movingCamera = true;

        if (game.waitTimer > -50 && game.waitTimer < 0)
        {
            if (game.playersAlive() && collected)
                game.levelWon = true;
            else
                game.levelLost = true;
        }

        if (game.timer > 1200)
        {
            Game.removeMovables.add(game.generalTank);

            if (!added)
            {
                ModAPI.displayText(FixedText.types.actionbar, "Mission: Find and steal their journal!");
                added = true;
            }
        }

        if (game.currentEventClasses.contains(EventMineExplode.class))
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof Mine && ((Mine) m).radius > Game.tile_size * 6)
                {
                    game.waitTimer = 300;

                    if (journal.collected)
                        game.levelWon = true;
                    else
                        game.levelLost = true;
                    break;
                }
            }
        }
    }
}
