package tanks.tank;

/**
 * Represents the tank of the player running this instance of Tanks - the TankPlayer (if hosting or singleplayer) or TankPlayerController (if connected as a client) that the user controls.
 */
public interface ILocalPlayerTank
{
    double getTouchCircleSize();

    boolean showTouchCircle();

    double getDrawRange();

    void setDrawRange(double range);
}
