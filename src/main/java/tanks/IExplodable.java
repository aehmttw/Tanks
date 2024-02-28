package tanks;

import tanks.tank.Explosion;

public interface IExplodable
{
    void onExploded(Explosion explosion);
    void applyExplosionKnockback(double angle, double power, Explosion explosion);
    double getSize();
}