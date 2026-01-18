package tanks;

import tanks.gui.IFixedMenu;
import tanks.network.NetworkEventMap;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankNPC;

import java.util.ArrayList;
import java.util.Arrays;

public class ModAPI
{
    /** Stores registered mods */
    public static ArrayList<IFixedMenu> menuGroup = new ArrayList<>();
    public static ArrayList<IFixedMenu> removeMenus = new ArrayList<>();


    /**
     * To add a new mod, add {@code registerMod(yourMod.class)} to this function. Of course, type the name of your mod instead of "yourMod".<br><br>
     * You can also use other functions here, like the {@code printLevelString(levelPath)) function defined below.<br><br>
     * However, keep in mind that this will only be called on the first frame of the game launch.
     * */
    public static void registerMods()
    {

    }

    public static void setUp()
    {
        registerMods();
        NetworkEventMap.register(EventAddColoredTile.class);
        NetworkEventMap.register(EventAddFixedShape.class);
        NetworkEventMap.register(EventAddNPC.class);
        NetworkEventMap.register(EventAddObstacle.class);
        NetworkEventMap.register(EventAddObstacleText.class);
        NetworkEventMap.register(EventAddScoreboard.class);
        NetworkEventMap.register(EventChangeScoreboardAttribute.class);
        NetworkEventMap.register(EventClearNPCShop.class);
        NetworkEventMap.register(EventSortNPCShopButtons.class);
        NetworkEventMap.register(EventDisplayText.class);
        NetworkEventMap.register(EventDisplayTextGroup.class);
        NetworkEventMap.register(EventPurchaseNPCItem.class);
        NetworkEventMap.register(EventScoreboardUpdateScore.class);
    }

    public static void addObject(Object o)
    {
        if (o instanceof Movable)
        {
            ((Movable) o).posX = ((Movable) o).posX * 50 + 25;
            Game.movables.add((Movable) o);

            if (o instanceof TankNPC)
                Game.eventsOut.add(new EventAddNPC((TankNPC) o));
            else if (o instanceof Tank)
                Game.eventsOut.add(new EventTankCustomCreate(((Tank) o)));
        }
        else if (o instanceof Obstacle)
        {
            EventAddObstacle e = new EventAddObstacle((Obstacle) o);
            e.execute();
            Game.eventsOut.add(e);
        }
        else if (o instanceof IFixedMenu)
            ModAPI.menuGroup.add((IFixedMenu) o);

        else
            System.err.println("Invalid item given to ModAPI.addObject()");
    }

//    public static void drawTank(double x, double y, double size, double angle, double r1, double g1, double b1, double r2, double g2, double b2)
//    {
//        Drawing.drawing.setColor(r2, g2, b2);
//        TankModels.tank.base.draw(x, y, size, size, angle);
//
//        Drawing.drawing.setColor(r1, g1, b1);
//        TankModels.tank.color.draw(x, y, size, size, angle);
//
//        Drawing.drawing.setColor(r2, g2, b2);
//        TankModels.tank.turret.draw(x, y, size, size, angle);
//
//        Drawing.drawing.setColor((r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2);
//        TankModels.tank.turretBase.draw(x, y, size, size, angle);
//    }

    public static void displayText(String location, String text) {
        displayText(location, text, false, 0, 0, 0, 0);
    }

    public static void displayText(String location, String text, boolean afterGameStarted, int durationInMs, double r, double g, double b)
    {
        EventDisplayText e = new EventDisplayText(location, text, afterGameStarted, durationInMs, r, g, b);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void displayTextGroup(String location, String[] texts, boolean afterGameStarted, Integer[] durationInMs, double fontSize, double r, double g, double b)
    {
        ArrayList<String> str = new ArrayList<>(Arrays.asList(texts));
        ArrayList<Integer> ints = new ArrayList<>(Arrays.asList(durationInMs));

        displayTextGroup(location, str, afterGameStarted, ints, fontSize, r, g, b);
    }

    public static void displayTextGroup(String location, String[] texts, boolean afterGameStarted, Integer[] durationInMs)
    {
        displayTextGroup(location, texts, afterGameStarted, durationInMs, 24,-1, -1, -1);
    }

    public static void displayTextGroup(String location, ArrayList<String> texts, boolean afterGameStarted, ArrayList<Integer> durationInMs, double fontSize, double r, double g, double b)
    {
        EventDisplayTextGroup e = new EventDisplayTextGroup(location, texts, afterGameStarted, durationInMs, fontSize, r, g, b);
        e.execute();
        Game.eventsOut.add(e);
    }

    /** If any Tank is within range of an area
     * @return Tank, null
     * */
    public static ArrayList<Tank> withinRange(double x, double y, double size)
    {
        ArrayList<Tank> output = new ArrayList<>();

        for (Movable m : Game.movables)
        {
            if (m instanceof Tank)
                if (Math.pow(m.posX - (x * 50 + 25), 2) + Math.pow(m.posY - (y * 50 + 25), 2) <= (size * 50) * (size * 50))
                    output.add((Tank) m);
        }

        return output;
    }

    public static class RespawnPoint
    {
        public double posX;
        public double posY;
        public double angle;

        public RespawnPoint(Tank t)
        {
            this(t, Game.currentLevel);
        }

        public RespawnPoint(Tank t, Level l)
        {
            for (int attemptNo = 0; attemptNo < 10; attemptNo++)
            {
                int spawnIndex = (int) (Math.random() * l.playerSpawnsX.size());

                if (!l.playerSpawnsTeam.get(spawnIndex).equals(t.team))
                    continue;

                this.posX = l.playerSpawnsX.get(spawnIndex);
                this.posY = l.playerSpawnsY.get(spawnIndex);
                this.angle = l.playerSpawnsAngle.get(spawnIndex);

                break;
            }
        }
    }

    // Drawing functions added in this mod api
    public static void addTextObstacle(double x, double y, String text)
    {
        addTextObstacle(x, y, text, 0);
    }

    /**
     * {@code duration} is how long you want to wait before the obstacle disappears.
     * Set it to 0 if you want it to be permanent
     * */
    public static void addTextObstacle(double x, double y, String text, long duration)
    {
        EventAddObstacleText e = new EventAddObstacleText((int) (Math.random() * Integer.MAX_VALUE), text, x, y, Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, duration);
        Game.eventsOut.add(e);
        e.execute();
    }

    public static void changeBackgroundColor(int r, int g, int b)
    {
        changeBackgroundColor(r, g, b, -1, -1, -1);
    }

    public static void changeBackgroundColor(int r, int g, int b, int noiseR, int noiseG, int noiseB)
    {
        EventChangeBackgroundColor e = new EventChangeBackgroundColor(r, g, b, noiseR, noiseG, noiseB);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void colorTile(double x, double y, double length, double width, double r, double g, double b)
    {
        colorTile(x, y, length, width, r, g, b, 255, false, 1);
    }

    public static void colorTile(double x, double y, double length, double width, double r, double g, double b, boolean flashing)
    {
        colorTile(x, y, length, width, r, g, b, 255, flashing, 1);
    }

    public static void colorTile(double x, double y, double length, double width, double r, double g, double b, double a, boolean flashing, double flashSpeedMultiplier)
    {
        EventAddColoredTile c = new EventAddColoredTile(x, y, length, width, r, g, b, a, flashing, flashSpeedMultiplier);
        Game.eventsOut.add(c);
        c.execute();
    }
}
