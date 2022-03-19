package tanks.challenges;

import tanks.modapi.ModAPI;
import tanks.modapi.ModLevel;
import tanks.modapi.menus.FixedText;
import tanks.modapi.menus.Scoreboard;
import tanks.obstacle.ObstacleIndestructible;
import tanks.modapi.TankNPC;

public class TeamDeathmatch extends ModLevel
{
    public Scoreboard scoreboard;

    public TeamDeathmatch()
    {
        super("{85,30,0,175,0,0,40,0,0,100,50|31-3...9-normal-0.5,31-19...26-normal-0.5,36...49-11,36...49-15,54-3...9-normal-0.5,54-19...26-normal-0.5,2-3-hard-5.5,2-4-hard-6.0,2-25-hard-5.5,2-26-hard-6.0,3-2-hard-5.5,3-5-hard-6.0,3-24-hard-5.5,3-27-hard-6.0,4-1-hard-5.5,4-6-hard-6.0,4-23-hard-5.5,4-28-hard-6.0,5-1-hard-6.0,5-6-hard-5.5,5-7-hard-3.0,5-8-hard-2.5,5-9-hard-3.0,5-10-hard-2.5,5-11-hard-3.0,5-12-hard-2.5,5-13-hard-3.0,5-14-hard-2.5,5-15-hard-3.0,5-16-hard-2.5,5-17-hard-3.0,5-18-hard-2.5,5-19-hard-3.0,5-20-hard-2.5,5-21-hard-3.0,5-22-hard-3.5,5-23-hard-6.0,5-28-hard-5.5,6-2-hard-6.0,6-27-hard-5.5,7-3-hard-3.0,7-11-hard-3.0,7-18-hard-2.5,7-26-hard-2.5,8-3-hard-2.5,8-11-hard-2.5,8-18-hard-3.0,8-26-hard-3.0,9-3-hard-3.0,9-11-hard-3.0,9-18-hard-2.5,9-26-hard-2.5,10-3-hard-2.5,10-11-hard-2.5,10-18-hard-3.0,10-26-hard-3.0,11-3-hard-3.0,11-11-hard-3.0,11-18-hard-2.5,11-26-hard-2.5,12-3-hard-2.5,12-11-hard-2.5,12-18-hard-3.0,12-26-hard-3.0,13-3-hard-3.0,13-12-hard-2.5,13-17-hard-3.0,13-26-hard-2.5,14-3-hard-2.5,14-13-hard-2.5,14-16-hard-3.0,14-26-hard-3.0,15-3-hard-3.0,15-26-hard-2.5,16-3-hard-2.5,16-26-hard-3.0,17-3-hard-3.0,17-26-hard-2.5,18-3-hard-2.5,18-25-hard-5.5,18-26-hard-6.0,19-3-hard-3.0,19-27-hard-6.0,20-2-hard-6.0,20-28-hard-6.0,21-1-hard-6.0,21-6-hard-5.5,21-23-hard-6.0,21-28-hard-5.5,22-1-hard-5.5,22-6-hard-6.0,22-7-hard-3.0,22-8-hard-2.5,22-9-hard-3.0,22-10-hard-2.5,22-11-hard-3.0,22-12-hard-2.5,22-13-hard-3.0,22-16-hard-3.0,22-17-hard-2.5,22-18-hard-3.0,22-19-hard-2.5,22-20-hard-3.0,22-21-hard-2.5,22-22-hard-3.0,22-23-hard-5.5,22-28-hard-6.0,23-2-hard-5.5,23-5-hard-6.0,23-24-hard-5.5,23-27-hard-6.0,25-14...15-hard-0.5,59-14...15-hard-0.5,59-27-hard-6.0,60-2-hard-6.0,60-5-hard-5.5,60-24-hard-6.0,60-28-hard-6.0,61-1-hard-6.0,61-6-hard-5.5,61-23-hard-6.0,61-28-hard-5.5,62-1-hard-5.5,62-6-hard-6.0,62-7-hard-3.0,62-8-hard-2.5,62-9-hard-3.0,62-10-hard-2.5,62-11-hard-3.0,62-12-hard-2.5,62-13-hard-3.0,62-16-hard-3.0,62-17-hard-2.5,62-18-hard-3.0,62-19-hard-2.5,62-20-hard-3.0,62-21-hard-2.5,62-22-hard-3.0,62-23-hard-5.5,62-28-hard-6.0,63-2-hard-5.5,63-27-hard-6.0,64-3-hard-3.0,64-26-hard-2.5,65-3-hard-2.5,65-26-hard-3.0,66-3-hard-3.0,66-26-hard-2.5,67-3-hard-2.5,67-26-hard-3.0,68-3-hard-3.0,68-26-hard-2.5,69-3-hard-2.5,69-26-hard-3.0,70-3-hard-3.0,70-26-hard-2.5,71-3-hard-2.5,71-12-hard-2.5,71-16-hard-2.5,71-26-hard-3.0,72-3-hard-3.0,72-11-hard-2.5,72-17-hard-2.5,72-26-hard-2.5,73-3-hard-2.5,73-10-hard-2.5,73-18-hard-2.5,73-26-hard-3.0,74-3-hard-3.0,74-10-hard-3.0,74-18-hard-3.0,74-26-hard-2.5,75-3-hard-2.5,75-10-hard-2.5,75-18-hard-2.5,75-25-hard-6.0,75-26-hard-5.5,76-3-hard-5.5,76-10-hard-3.0,76-18-hard-3.0,76-27-hard-5.5,77-2-hard-5.5,77-10-hard-2.5,77-18-hard-2.5,77-28-hard-5.5,78-1-hard-5.5,78-6-hard-6.0,78-23-hard-5.5,78-28-hard-6.0,79-1-hard-6.0,79-6-hard-5.5,79-7-hard-2.5,79-8-hard-3.0,79-9-hard-2.5,79-10-hard-3.0,79-11-hard-2.5,79-12-hard-3.0,79-13-hard-2.5,79-14-hard-3.0,79-15-hard-2.5,79-16-hard-3.0,79-17-hard-2.5,79-18-hard-3.0,79-19-hard-2.5,79-20-hard-3.0,79-21-hard-2.5,79-22-hard-3.0,79-23-hard-6.0,79-28-hard-5.5,80-2-hard-6.0,80-5-hard-5.5,80-24-hard-6.0,80-25-hard-5.5,80-26-hard-6.0,80-27-hard-5.5,81-3-hard-6.0,81-4-hard-5.5,6-13-hole,6-16-hole,7-14...15-hole,77-14...15-hole,78-13-hole,78-16-hole,38-12...14-breakable-0.5,47-12...14-breakable-0.5,36-7-sand,36-16...17-sand,37-19-sand,37-22...24-sand,37-25-sand,37-26-sand,37-28-sand,38...42-1-sand,38...39-2-sand,38...39-3-sand,38...40-4-sand,38...40-5-sand,38-9-sand,38...40-18-sand,38...40-20-sand,38-23-sand,38-28-sand,39-0-sand,39...40-6-sand,39-7-sand,39-8-sand,39...40-9-sand,39...40-10-sand,39...40-16-sand,39...40-17-sand,39...40-19-sand,39...40-21-sand,39...40-22-sand,39-23-sand,39...40-24-sand,39...40-25-sand,39-26-sand,39-27...29-sand,40...43-0-sand,40-2-sand,40-3-sand,40-7-sand,40-8-sand,40-23-sand,40-26...27-sand,40-28-sand,40-29-sand,41-2-sand,46-7...10-sand,46-16...22-sand,46-23-sand,46-24...29-sand,47-4...8-sand,47-9-sand,47-10-sand,47-16...18-sand,47-19-sand,47-20...29-sand,48...50-3-sand,48...49-4-sand,48-5-sand,48-18-sand,48...49-23-sand,48-29-sand,49-0...1-sand,49-2-sand,49-5-sand,49-8...9-sand,49...50-16-sand,49-24-sand,50-0-sand,50...51-1-sand,50...51-2-sand,50-4-sand,50-7-sand,50-17...18-sand,50-19-sand,50-20...22-sand,51-0-sand,52...53-5-sand,53-1-sand,54-0-sand,55-0...1-sand,41-3...10-water,41-16...29-water,42...43-2-water,42-3-water,42-4...10-water-2.5,42-16...29-water-2.5,43...44-1-water,43-3...10-water-2.5,43-16...29-water-2.5,44...45-0-water,44-2...10-water-2.5,44-16...29-water-2.5,45...47-1-water-2.5,45...46-2-water-2.5,45-3-water-2.5,45-4...10-water,45-16...29-water,46...47-0-water-2.5,46-3...6-water,47...48-2-water,47-3-water,48-0...1-water,4-3-teleporter,4-26-teleporter,21-3-teleporter,21-26-teleporter,61-3-teleporter,61-26-teleporter,78-3-teleporter,78-26-teleporter|6-14-medic-0-team 1,6-15-medic-0-team 1,78-14-medic-2-team 2,78-15-medic-2-team 2,37-13-black-0-team 1,48-13-black-2-team 2,67-8-darkgreen-2-team 2,67-20-darkgreen-2-team 2,16-8-darkgreen-0-team 1,16-20-darkgreen-0-team 1,7-8-player-0-team 1,11-8-player-0-team 1,7-20-player-0-team 1,11-20-player-0-team 1,16-11-player-0-team 1,16-17-player-0-team 1,67-12-player-2-team 2,67-16-player-2-team 2,76-20-player-2-team 2,71-20-player-2-team 2,71-8-player-2-team 2,76-8-player-2-team 2|team 1-false-255.0-0.0-0.0,team 2-false-0.0-0.0-255.0}\ncoins\n15\nshop\nRocket,bullet_fire.png,3,0,1,100,bullet,normal,fire,8.0,0,1.0,5,100.0,10.0,5.0,false\nShield,shield.png,15,0,1,100,shield,1.5,10.0,50.0\nMachine Gun,bullet_mini.png,3,0,50,1000,bullet,normal,none,8.0,0,0.1,10,3.0,5.0,0.1,false\n");

        this.enableKillMessages = true;
        this.playerKillCoins = 10;
    }

    @Override
    public void setUp()
    {
        scoreboard = new Scoreboard("Team Deathmatch", Scoreboard.objectiveTypes.kills, this.teamsList).changeAttribute("titleFontSize", 20);

        ModAPI.addObject(scoreboard);
        ModAPI.addObject(new TankNPC("npc", 42, 13, Math.PI / 2, "Use this shop at your own risk!\n/shop", "Shop", 255, 0, 0));

        ObstacleIndestructible o = new ObstacleIndestructible("hard", 22, 14);
        o.startHeight = 2;
        ModAPI.addObject(o);
        o = new ObstacleIndestructible("hard", 22, 15);
        o.startHeight = 2;
        ModAPI.addObject(o);
        o = new ObstacleIndestructible("hard", 62, 14);
        o.startHeight = 2;
        ModAPI.addObject(o);
        o = new ObstacleIndestructible("hard", 62, 15);
        o.startHeight = 2;
        ModAPI.addObject(o);

        ModAPI.displayText(FixedText.types.title, "Team Deathmatch", false, 5000, 255, 0, 0);
        ModAPI.displayText(FixedText.types.subtitle, "Kill all your enemies", false, 5000, 255, 255, 255);

        // ModAPI.displayTextGroup(FixedText.types.title, new String[] {"3", "2", "1", "Fight!"}, true, new Integer[] {1000, 1000, 1000, 1000});
        // ModAPI.displayText(FixedText.types.actionbar, "Use the teleporters!", true, 3000, 255, 255, 255);
    }
}