import java.awt.*;
import java.util.ArrayList;

public class Screen {
    public static final Dimension screenSize  = Toolkit.getDefaultToolkit().getScreenSize();
    public static final Point screenCenter    = new Point(screenSize.width / 2, screenSize.height / 2);

    public static Point getPointByPercentages(double widthPercentage, double heightPercentage) {
        double fractionX = 100.0 / widthPercentage;
        double fractionY = 100.0 / heightPercentage;
        int adjustedX = (int) (screenSize.width / fractionX);
        int adjustedY = (int) (screenSize.height / fractionY);
        return new Point(adjustedX, adjustedY);
    }

    /**
     * @param percentageWidth range: 0..100
     * @param percentageHeight range: 0..100
     * @param radius Is used for the size of the square of pixels of which the average color is taken.
     * @return
     */
    public static Color takeColor(double percentageWidth, double percentageHeight, int radius) {
        Point adjustedPos = getPointByPercentages(percentageWidth, percentageHeight);

        // Move the cursor out of the way
        Bot.r.mouseMove(0, 0);

        Bot.r.mouseMove(adjustedPos.x, adjustedPos.y);
        Bot.r.delay(500);

        System.out.println("Taking color from: " + adjustedPos.x + " " + adjustedPos.y);
        ArrayList<Color> pickedColors = new ArrayList<>();
        for (int xOffset = -(radius / 2); xOffset < radius / 2; xOffset++) {
            for (int yOffset = -(radius / 2); yOffset < radius / 2; yOffset++) {
                int x = adjustedPos.x + xOffset;
                int y = adjustedPos.y + yOffset;
                Color color = Bot.r.getPixelColor(x, y);
                pickedColors.add(color);
                // System.out.printf("%d, %d: %s,%s,%s\n", x, y, color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        int totalRed    = 0;
        int totalGreen  = 0;
        int totalBlue   = 0;

        for (Color color : pickedColors) {
            totalRed    += color.getRed();
            totalGreen  += color.getGreen();
            totalBlue   += color.getBlue();
        }

        Bot.r.mouseMove(screenCenter.x, screenCenter.y);
        Color averageColor = new Color(totalRed / pickedColors.size(), totalGreen / pickedColors.size(), totalBlue / pickedColors.size());
        // System.out.printf("Average color: %s,%s,%s\n", averageColor.getRed(), averageColor.getGreen(), averageColor.getBlue());

        return averageColor;
    }

    public static int[] colorDifference(Color color1, Color color2) {
        int redDifference = Math.abs(color1.getRed() - color2.getRed());
        int greenDifference = Math.abs(color1.getGreen() - color2.getGreen());
        int blueDifference = Math.abs(color1.getBlue() - color2.getBlue());
        return new int[]{redDifference, greenDifference, blueDifference, redDifference + greenDifference + blueDifference};
    }
}
