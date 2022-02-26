import main.Tanks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

public class BotTest {
    Thread gameThread;

    @Before
    public void startGame() {
        // Starts game on separate to prevent it from blocking the other tests
        gameThread = new Thread(() -> Tanks.main(new String[]{}));
        gameThread.start();

        // Wait some time so the game can start
        Bot.r.delay(3000);

        Bot.fullScreen();
    }

    @Test
    public void startLevel() {
        // Moves in to the first "room"
        Bot.move(Direction.DOWN, 1000);
        Bot.move(Direction.DOWN_RIGHT, 1500);
        Bot.move(Direction.RIGHT, 2000);
        Bot.r.delay(500);

        // Gets the color of where the tank should be.
        assert checkColor(50, 53, 10, new Color(107, 74, 39), 15, 15, 10);

        // Shoot the first tank
        Bot.shootDirection(50, 60);
        // Wait for the bullet to hit the tank
        Bot.r.delay(1000);

        // Gets the color of where the tank shouldn't be anymore.
        assert checkColor(50, 53, 10, new Color(163, 163, 140), 20, 20, 10);
    }

    @After
    public void endGame() {
        // Some time so you can see what happens
        Bot.r.delay(1000);
        gameThread.stop();
    }

    public static boolean checkColor(double widthPercentage,
                                     double heightPercentage,
                                     int radius,
                                     Color compareColor,
                                     int rMax,
                                     int gMax,
                                     int bMax) {
        Color foundColor = Screen.takeColor(widthPercentage, heightPercentage, radius);
        System.out.println("Found color: " + foundColor);
        int[] difference = Screen.colorDifference(foundColor, compareColor);
        return difference[0] < rMax && difference[1] < gMax && difference[2] < bMax;
    }
}
