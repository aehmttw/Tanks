package tanks.tank;

/**
 * Mimic of a tank player that is controlled by the logic of {@link TankPurple}.
 * @see TankPlayer
 */
public class TankPlayerMimic extends TankPurple
{
    public TankPlayerMimic(String name, double x, double y, double angle)
    {
        super(name, x, y, angle);
        this.colorR = 0;
        this.colorG = 150;
        this.colorB = 255;
    }
}
