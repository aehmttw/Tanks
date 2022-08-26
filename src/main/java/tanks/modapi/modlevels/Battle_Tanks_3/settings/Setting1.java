package tanks.modapi.modlevels.Battle_Tanks_3.settings;

import tanks.modapi.ModAPI;
import tanks.modapi.modlevels.Battle_Tanks_3.BattleTanks3;

public class Setting1
{
    public static int messageNum = 0;

    public static void setUp(BattleTanks3 game)
    {
        messageNum = 0;

        ModAPI.loadLevel("{120,30,0,220,0,50,35,50,0,100,50|3...5-18-normal-0.5,3...5-19-normal-0.5,3...5-20-normal-0.5,4-6,8-6,9...11-18-normal-0.5,9...11-19-normal-0.5,9...11-20-normal-0.5,2-6...21-hard-5.0,3-5-hard-5.0,3-22-hard-5.0,4-5-hard-5.5,4-22-hard-5.5,5-5-hard-6.0,5-22-hard-6.0,6-5-hard-6.5,6-22-hard-6.5,7-5-hard-7.0,7-22-hard-7.0,8-5-hard-6.5,8-22-hard-6.5,9-5-hard-6.0,9-22-hard-6.0,10-5-hard-5.5,10-22-hard-5.5,11-5-hard-5.0,11-22-hard-5.0,12-6...12-hard-5.0,12-16...21-hard-5.0,3...6-17-hole,6-18...21-hole,8-17...21-hole,9...10-17-hole,2-5-nobounce-5.0,2-22-nobounce-5.0,12-5-nobounce-5.0,12-22-nobounce-5.0,17...20-9-shrub,17...24-10-shrub,18...24-8-shrub,18...22-13-shrub,18-14-shrub,18...27-18-shrub,18...23-19-shrub,18...28-20-shrub,18...28-21-shrub,19...24-7-shrub,19...24-11-shrub,19-12-shrub,19...28-22-shrub,20...28-23-shrub,21...22-12-shrub,21...28-17-shrub,21...22-24-shrub,21-25-shrub,22...24-9-shrub,22...28-16-shrub,24...25-15-shrub,25...28-19-shrub,25...28-24-shrub,25...27-25-shrub,34...43-8-shrub,34...45-9-shrub,34...46-10-shrub,34...46-11-shrub,35...41-12-shrub,37...40-6-shrub,37...39-7-shrub,38...46-19-shrub,38...46-20-shrub,39...44-18-shrub,39...46-21-shrub,41...46-22-shrub,42...46-23-shrub,43...44-12-shrub,56...60-5-shrub,57...59-3-shrub,57...60-4-shrub,57...60-6-shrub,60...63-21-shrub,60...64-22-shrub,61...64-23-shrub,62...64-20-shrub,72...76-9-shrub,72...73-10-shrub,72...81-25-shrub,72...76-26-shrub,72...82-27-shrub,73...79-23-shrub,73...80-24-shrub,73...81-28-shrub,74...75-7-shrub,74...76-8-shrub,74...81-29-shrub,76-22-shrub,78...82-26-shrub,82...96-6-shrub,82...97-7-shrub,83...93-5-shrub,83...98-8-shrub,84...90-9-shrub,85...95-10-shrub,87...88-4-shrub,88...95-11-shrub,89...95-12-shrub,92...97-9-shrub,106...114-23-shrub,106...113-24-shrub,106...113-25-shrub,107...114-22-shrub,108...114-26-shrub,109...113-27-shrub,111...115-7-shrub,111...116-8-shrub,111...116-9-shrub,112...114-6-shrub,113-10-shrub,3...5-21-snow,9...11-21-snow,14-15...17-path,15...18-16-path,15...17-17-path,16-14...15-path,17...18-15-path,19...25-14-path,20...23-15-path,22...25-13-path,22-16-path,26...32-15-path,27-14-path,28...29-13-path,28...37-16-path,29...32-14-path,32...33-17-path,34...37-15-path,37...50-14-path,39...43-16-path,41...67-15-path,48...52-16-path,53...55-14-path,55...72-16-path,59...65-17-path,61...69-14-path,69...79-15-path,71...74-14-path,73...75-13-path,76...78-14-path,77...82-13-path,79...85-16-path,81...87-15-path,82...84-14-path,86...92-17-path,87...119-16-path,90...96-18-path,95...101-17-path,97...112-15-path,103...111-17-path,106...107-18-path,110...117-14-path,115...119-15-path,119-14-path|5-10-player-0-ally|ally-true,enemy-true}\\n\n");
        game.enableShooting = false;
        game.enableLayingMines = false;

        game.initGeneralTank(25, 15, 300);

        game.createTree(21, 9, 4);
        game.createTree(24, 19, 4);
        game.createTree(77, 26, 3);
        game.createTree(91, 9, 5);

        ModAPI.fillObstacle(3, 6, 9, 6, "normal", 0.5, 0.5);

        for (int i = 0; i <= 6; i++)
            ModAPI.fillObstacle(i, 4, i, 23, "normal", 0.5, i / 2.0 + 4);

        for (int i = 7; i <= 13; i++)
            ModAPI.fillObstacle(i, 4, i, 23, "normal", 0.5, 6.5 - i / 2.0 + 4);

        ModAPI.fillObstacle(12, 13, 12, 15, "breakable", 1, 3);

        ModAPI.setObstacle(5, 6, "light", 1, 1);
    }

    public static void draw(BattleTanks3 game)
    {
        if (game.timer > 2400 && game.generalTank.overrideDisplayState)
            game.generalTank.setOverrideState(false);

        if (game.timer > 2200 && messageNum == 8)
        {
            game.generalTank.setMessages("Good luck.");
            messageNum++;
        }
        else if (game.timer > 1900 && messageNum == 7)
        {
            game.generalTank.setMessages("Then, blow the factory up. Just find some explosives and set them alight.");
            messageNum++;
        }
        else if (game.timer > 1600 && messageNum == 6)
        {
            game.generalTank.setMessages("Go into the explosives factory and steal it, however you like.");
            messageNum++;
        }
        else if (game.timer > 1400 && messageNum == 5)
        {
            game.generalTank.setMessages("Your first mission is to acquire their journal.");
            messageNum++;
        }
        else if (game.timer > 1200 && messageNum == 4)
        {
            game.generalTank.setMessages("Let's go. Follow me.");
            messageNum++;
        }
        else if (game.timer > 1000 && messageNum == 3)
        {
            game.generalTank.setMessages("So we must act now, to avoid being conquered in the future.");
            messageNum++;
        }
        else if (game.timer > 700 && messageNum == 2)
        {
            game.generalTank.setMessages("But one of our enemies has already sent a warning saying that there will be war soon.");
            messageNum++;
        }
        else if (game.timer > 500 && messageNum == 1)
        {
            game.generalTank.setMessages("You managed to conquer many small cities, so good job on that.");
            messageNum++;
        }
        else if (game.timer > 300 && messageNum == 0)
        {
            game.generalTank.setMessages("Hey there, I'm the general.");
            game.generalTank.setOverrideState(true);
            messageNum++;
        }
    }

    public static void update(BattleTanks3 game)
    {
        if (game.timer < 300)
            game.generalTank.vX = -1.8;

        else if (game.timer < 1100)
            game.generalTank.vX = 0;

        else if (game.timer < 2500)
        {
            if (game.generalTank.angle < Math.PI * 2)
                game.generalTank.angle += 0.01;

            game.generalTank.vX = 1.8;
        }
        else
            game.levelEnd = true;
    }
}
