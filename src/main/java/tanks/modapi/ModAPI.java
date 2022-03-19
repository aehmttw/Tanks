package tanks.modapi;

import basewindow.BaseFile;
import basewindow.BaseFontRenderer;
import basewindow.BaseShapeRenderer;
import tanks.*;
import tanks.event.EventAddTransitionEffect;
import tanks.event.EventCreateCustomTank;
import tanks.event.EventSortNPCShopButtons;
import tanks.event.EventTankTeleport;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.modapi.events.*;
import tanks.modapi.menus.CustomShape;
import tanks.modapi.menus.FixedMenu;
import tanks.modapi.menus.FixedText;
import tanks.modapi.menus.TransitionEffect;
import tanks.modapi.modlevels.Team_Deathmatch;
import tanks.network.NetworkEventMap;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TeleporterOrb;
import tanks.tank.Turret;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ModAPI
{
    /** Stores registered mods */
    public static ArrayList<Class<? extends ModGame>> registeredCustomGames = new ArrayList<>();
    public static ArrayList<Class<? extends ModLevel>> registeredCustomLevels = new ArrayList<>();
    public static ArrayList<FixedMenu> menuGroup = new ArrayList<>();
    public static HashMap<Double, FixedMenu> ids = new HashMap<>();
    public static ArrayList<FixedMenu> removeMenus = new ArrayList<>();

    // Directions in radians
    public static final double up = Math.toRadians(-90);
    public static final double down = Math.toRadians(90);
    public static final double left = Math.toRadians(180);
    public static final double right = Math.toRadians(0);

    /**
     * To add a new mod, add {@code registerMod(yourMod.class)} to this function. Of course, type the name of your mod instead of "yourMod".<br><br>
     * You can also use other functions here, like the {@code ModAPI.printLevelString(levelPath)) function.<br><br>
     * However, keep in mind that this will only be called on the first frame of the game launch.
     * */
    public static void registerMods()
    {
        registerMod(Team_Deathmatch.class);
    }

    public static void setUp()
    {
        registerMods();

        NetworkEventMap.register(EventAddCustomMovable.class);
        NetworkEventMap.register(EventAddCustomShape.class);
        NetworkEventMap.register(EventAddNPC.class);
        NetworkEventMap.register(EventAddNPCShopItem.class);
        NetworkEventMap.register(EventAddObstacle.class);
        NetworkEventMap.register(EventAddObstacleText.class);
        NetworkEventMap.register(EventAddScoreboard.class);
        NetworkEventMap.register(EventAddTransitionEffect.class);
        NetworkEventMap.register(EventChangeBackgroundColor.class);
        NetworkEventMap.register(EventChangeNPCMessage.class);
        NetworkEventMap.register(EventChangeScoreboardAttribute.class);
        NetworkEventMap.register(EventClearMenuGroup.class);
        NetworkEventMap.register(EventClearNPCShop.class);
        NetworkEventMap.register(EventCustomLevelEndCondition.class);
        NetworkEventMap.register(EventDisableMinimap.class);
        NetworkEventMap.register(EventDisplayText.class);
        NetworkEventMap.register(EventDisplayTextGroup.class);
        NetworkEventMap.register(EventFillObstacle.class);
        NetworkEventMap.register(EventOverrideNPCState.class);
        NetworkEventMap.register(EventPurchaseNPCItem.class);
        NetworkEventMap.register(EventScoreboardUpdateScore.class);
        NetworkEventMap.register(EventSortNPCShopButtons.class);
        NetworkEventMap.register(EventSkipCountdown.class);

        fixedShapes = Game.game.window.shapeRenderer;
        fixedText = Game.game.window.fontRenderer;
    }

    public static void registerGame(Class<? extends ModGame> m) { registeredCustomGames.add(m); }
    public static void registerMod(Class<? extends ModLevel> m) { registeredCustomLevels.add(m); }

    public static void skipCountdown()
    {
        if (!(Game.screen instanceof ScreenGame))
            return;

        EventSkipCountdown e = new EventSkipCountdown();
        e.execute();
        Game.eventsOut.add(e);
    }

    public static String getLevelString(String levelName) {
        return getLevelString(levelName, false);
    }

    /**Prints the level or crusade string of a level file.*/
    public static String getLevelString(String levelName, boolean print)
    {
        StringBuilder levelString = new StringBuilder();

        BaseFile level = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.replace(' ','_') + ".tanks");
        try {
            level.startReading();

            while (level.hasNextLine()) {
                String line = level.nextLine();

                if (print)
                    System.out.print(line + "\\n");

                levelString.append(line).append("\n");
            }

            level.stopReading();

            return levelString.toString();
        }

        catch (FileNotFoundException e) {
            System.err.println("Invalid file name: " + levelName + "\n");
            Game.exitToCrash(new FileNotFoundException("Level \"" + levelName + "\" not found"));
        }

        return "";
    }

    public static void sendChatMessage(String message)
    {
        if (!ScreenPartyHost.isServer)
            return;

        EventServerChat e = new EventServerChat(message);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void sendChatMessage(String message, int colorR, int colorG, int colorB) {
        sendChatMessage(message, colorR, colorG, colorB, (int) Turret.calculateSecondaryColor(colorR), (int) Turret.calculateSecondaryColor(colorG), (int) Turret.calculateSecondaryColor(colorB));
    }

    public static void sendChatMessage(String message, int r1, int g1, int b1, int r2, int g2, int b2)
    {
        if (!ScreenPartyHost.isServer)
            return;

        EventServerChat e = new EventServerChat(message, r1, g1, b1, r2, g2, b2);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void addObject(Object o)
    {
        if (o instanceof Movable)
        {
            ((Movable) o).posX = ((Movable) o).posX * 50 + 25;
            Game.movables.add((Movable) o);

            if (o instanceof TankNPC t)
                Game.eventsOut.add(new EventAddNPC(t));

            else if (o instanceof Tank t)
                Game.eventsOut.add(new EventCreateCustomTank(t));

            else if (o instanceof CustomMovable m)
                Game.eventsOut.add(new EventAddCustomMovable(m));
        }
        else if (o instanceof Obstacle)
        {
            EventAddObstacle e = new EventAddObstacle((Obstacle) o);
            e.execute();
            Game.eventsOut.add(e);
        }
        else if (o instanceof FixedMenu)
            ModAPI.menuGroup.add((FixedMenu) o);

        else
            System.err.println("Invalid item given to ModAPI.addObject()");
    }

    public static void clearMenuGroup()
    {
        EventClearMenuGroup e = new EventClearMenuGroup();
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void drawTank(double x, double y, double size, double angle, double r1, double g1, double b1, double r2, double g2, double b2)
    {
        Drawing.drawing.setColor(r2, g2, b2);
        Tank.base_model.draw(x, y, size, size, angle);

        Drawing.drawing.setColor(r1, g1, b1);
        Tank.color_model.draw(x, y, size, size, angle);

        Drawing.drawing.setColor(r2, g2, b2);
        Turret.turret_model.draw(x, y, size, size, angle);

        Drawing.drawing.setColor((r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2);
        Turret.base_model.draw(x, y, size, size, angle);
    }

    public static void displayText(FixedText.types location, String text) {
        displayText(location, text, false, 0, 255, 255, 255);
    }

    public static void displayText(FixedText.types location, String text, double r, double g, double b) {
        displayText(location, text, false, 0, r, g, b);
    }

    public static void displayText(FixedText.types location, String text, int durationInMs, double r, double g, double b)
    {
        EventDisplayText e = new EventDisplayText(location, text, false, durationInMs, r, g, b);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void displayText(FixedText.types location, String text, boolean afterGameStarted, int durationInMs, double r, double g, double b)
    {
        EventDisplayText e = new EventDisplayText(location, text, afterGameStarted, durationInMs, r, g, b);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void editText(FixedText t, String text) {
        editText(t.id, text);
    }

    public static void editText(double id, String text)
    {
        EventChangeText e = new EventChangeText(id, text);
        e.execute();
        Game.eventsOut.add(e);
    }

    @Deprecated
    public static void displayTextGroup(String location, String[] texts, boolean afterGameStarted, Integer[] durationInMs, double fontSize, double r, double g, double b)
    {
        ArrayList<String> str = new ArrayList<>(Arrays.asList(texts));
        ArrayList<Integer> ints = new ArrayList<>(Arrays.asList(durationInMs));

        displayTextGroup(location, str, afterGameStarted, ints, fontSize, r, g, b);
    }

    @Deprecated
    public static void displayTextGroup(String location, String[] texts, boolean afterGameStarted, Integer[] durationInMs)
    {
        displayTextGroup(location, texts, afterGameStarted, durationInMs, 24,-1, -1, -1);
    }

    @Deprecated
    public static void displayTextGroup(String location, ArrayList<String> texts, boolean afterGameStarted, ArrayList<Integer> durationInMs, double fontSize, double r, double g, double b)
    {
        EventDisplayTextGroup e = new EventDisplayTextGroup(location, texts, afterGameStarted, durationInMs, fontSize, r, g, b);
        e.execute();
        Game.eventsOut.add(e);
    }

    /** If any Tank is within range of an area (in tiles)
     * @return an ArrayList of Tanks
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

    public static void respawnPlayer(Player p, Object... params) {
        respawnPlayer(p.tank, params);
    }

    public static void respawnPlayer(Tank t, Object... params)
    {
        if (!Tank.idMap.containsValue(t))
        {
            Game.movables.add(t);
            t.registerNetworkID();
        }

        for (int attemptNo = 0; attemptNo < 1000; attemptNo++)
        {
            int spawnIndex = (int) (Math.random() * Game.currentLevel.playerSpawnsX.size());

            if (!Game.currentLevel.playerSpawnsTeam.get(spawnIndex).equals(t.team))
                continue;

            double x = Game.currentLevel.playerSpawnsX.get(spawnIndex);
            double y = Game.currentLevel.playerSpawnsY.get(spawnIndex);

            if (params.length > 0)
            {
                TeleporterOrb o = new TeleporterOrb(t.posX, t.posY, t.posX, t.posY, x, y, t);
                EventTankTeleport e = new EventTankTeleport(o);
                e.execute();
                Game.eventsOut.add(e);
            }
            else
            {
                t.posX = x;
                t.posY = y;
            }

            break;
        }
    }

    public static String convertToString(double number)
    {
        if (number != (int) number)
            return "" + number;
        else
            return "" + (int) number;
    }

    public static String convertToString(double number, int maxZeroes)
    {
        return ("0".repeat(maxZeroes - ((int) number + "").length())) + (int) number;
    }

    public static String convertToString(double number, int placeValues, int decimalPlaceValues)
    {
        number = Math.floor(number * Math.pow(10, decimalPlaceValues)) / Math.pow(10, decimalPlaceValues);

        return ("0".repeat((placeValues + decimalPlaceValues) - (number + "").length())) + number;
    }

    // Drawing functions added in this mod api

    /** Abbreviations of renderers to draw fixed stuff */
    public static BaseShapeRenderer fixedShapes;
    public static BaseFontRenderer fixedText;

    public static void setObstacle(int x, int y, String registryName) {
        setObstacle(x, y, registryName, 1, 0);
    }

    public static void setObstacle(int x, int y, String registryName, double stackHeight) {
        setObstacle(x, y, registryName, stackHeight, 0);
    }

    public static void setObstacle(int x, int y, String registryName, double stackHeight, double startHeight)
    {
        try {
            Obstacle o = Game.registryObstacle.getEntry(registryName).obstacle
                    .getConstructor(String.class, double.class, double.class)
                    .newInstance(registryName, x, y);
            o.stackHeight = stackHeight;
            o.startHeight = startHeight;

            ModAPI.addObject(o);
        }
        catch (Exception e) {
            Game.exitToCrash(e);
        }
    }

    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName) {
        fillObstacle(startX, startY, endX, endY, registryName, 1, 0);
    }

    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight) {
        fillObstacle(startX, startY, endX, endY, registryName, stackHeight, 0);
    }

    public static void fillObstacle(int startX, int startY, int endX, int endY, String registryName, double stackHeight, double startHeight)
    {
        EventFillObstacle e = new EventFillObstacle(startX, startY, endX, endY, registryName, stackHeight, startHeight);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void addTextObstacle(double x, double y, String text) {
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

    public static void addTransitionEffect(TransitionEffect.types type) {
        addTransitionEffect(type, 1, 0, 0, 0);
    }

    public static void addTransitionEffect(TransitionEffect.types type, int colR, int colG, int colB) {
        addTransitionEffect(type, 1, colR, colG, colB);
    }

    public static void addTransitionEffect(TransitionEffect.types type, float speed, int colR, int colG, int colB)
    {
        EventAddTransitionEffect e = new EventAddTransitionEffect(type, speed, colR, colG, colB);
        e.execute();
        Game.eventsOut.add(e);
    }

    public static void addCustomShape(boolean all, CustomShape.types type, int x, int y, int sizeX, int sizeY, int r, int g, int b) {
        addCustomShape(all, type, x, y, sizeX, sizeY, 0, r, g, b, 255);
    }

    public static void addCustomShape(boolean all, CustomShape.types type, int x, int y, int sizeX, int sizeY, int r, int g, int b, int a) {
        addCustomShape(all, type, x, y, sizeX, sizeY, 0, r, g, b, a);
    }

    public static void addCustomShape(boolean all, CustomShape.types type, int x, int y, int sizeX, int sizeY, int duration, int r, int g, int b, int a)
    {
        EventAddCustomShape e = new EventAddCustomShape(type, x, y, sizeX, sizeY, duration, r, g, b, a);
        e.execute();

        if (all)
            Game.eventsOut.add(e);
    }

    public static void loadLevel(Level l)
    {
        l.loadLevel();
        Game.screen = new ScreenGame();
    }

    public static void loadLevel(String levelString)
    {
        loadLevel(new Level(levelString));
    }

    public static void changeBackgroundColor(int r, int g, int b) {
        changeBackgroundColor(r, g, b, -1, -1, -1);
    }

    public static void changeBackgroundColor(int r, int g, int b, int noiseR, int noiseG, int noiseB)
    {
        EventChangeBackgroundColor e = new EventChangeBackgroundColor(r, g, b, noiseR, noiseG, noiseB);
        e.execute();
        Game.eventsOut.add(e);
    }
}
