package tanks;

public class Colors
{
    public static String orange = "\u00A7255127000255";
    public static String blue = "\u00A7000150255255";

    public static String green = "\u00a7000200000255";
    public static String red = "\u00A7200000000255";

    public static String black = "\u00a7000000000255";
    public static String white = "\u00a7255255255255";

    public static String getTextColor()
    {
        return (Game.currentLevel != null && Level.isDark()) ? white : black;
    }
}
