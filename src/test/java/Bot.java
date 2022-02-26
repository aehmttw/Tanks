import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Bot {
    public static Robot r;
    static {
        try {
            r = new Robot();
            r.setAutoDelay(50);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void move(Direction direction, int time) {
        int[] keyCodes = direction.getKeyCodes();
        for (int keyCode : keyCodes) {
            r.keyPress(keyCode);
        }
        r.delay(time);
        for (int keyCode : keyCodes) {
            r.keyRelease(keyCode);
        }
    }

    public static void shoot() {
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void shootDirection(double widthPercentage, double heightPercentage) {
        Point adjustedPoint = Screen.getPointByPercentages(widthPercentage, heightPercentage);
        r.mouseMove(adjustedPoint.x, adjustedPoint.y);
        r.delay(100);
        shoot();
        r.delay(100);
        r.mouseMove(Screen.screenCenter.x, Screen.screenCenter.y);
    }

    public static void fullScreen() {
        r.keyPress(KeyEvent.VK_F11);
        r.delay(10);
        r.keyRelease(KeyEvent.VK_F11);
    }
}

enum Direction {
    UP(new int[]{KeyEvent.VK_W}),
    UP_RIGHT(new int[]{KeyEvent.VK_W, KeyEvent.VK_D}),
    RIGHT(new int[]{KeyEvent.VK_D}),
    DOWN_RIGHT(new int[]{KeyEvent.VK_D, KeyEvent.VK_S}),
    DOWN(new int[]{KeyEvent.VK_S}),
    DOWN_LEFT(new int[]{KeyEvent.VK_S, KeyEvent.VK_A}),
    LEFT(new int[]{KeyEvent.VK_A}),
    UP_LEFT(new int[]{KeyEvent.VK_A, KeyEvent.VK_W});

    private final int[] keyCodes;
    public int[] getKeyCodes() {
        return keyCodes;
    }

    Direction(int[] keyCodes) {
        this.keyCodes = keyCodes;
    }
}