package tanks.tank;

/**
 * Represents the tank of the player running this instance of Tanks - the TankPlayer (if hosting or singleplayer) or TankPlayerController (if connected as a client) that the user controls.
 */
public interface ILocalPlayerTank
{
    double getTouchCircleSize();

    boolean showTouchCircle();

    double getDrawRangeMin();

    double getDrawRangeMax();

    double getDrawLifespan();

    boolean getShowTrace();

    void setDrawRanges(double lifespan, double rangeMin, double rangeMax, boolean trace);
}
