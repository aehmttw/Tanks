package tanks.tank;

public interface IPlayerTank
{
    double getTouchCircleSize();

    boolean showTouchCircle();

    double getDrawRange();

    void setDrawRange(double range);
}
