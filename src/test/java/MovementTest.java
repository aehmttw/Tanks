import org.junit.Test;
import tanks.tank.Tank;
import tanks.tank.TankBlack;

public class MovementTest {
    @Test public void movement() {
        int starting = 75;
        int movement = 100;

        Tank tank = new TankBlack("tank", starting, starting, 90);
        tank.moveInDirection(starting, starting + movement, movement);
        assert tank.posX == starting + movement * starting;
    }
}
