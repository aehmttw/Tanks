package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Explosion;
import tanks.tank.TankAIControlled;

public class EventExplosion extends PersonalEvent
{
    public double posX;
    public double posY;
    public double radius;
    public double kbRadius;
    public boolean destroysObstacles;
    public double damage;

    public EventExplosion()
    {

    }

    public EventExplosion(Explosion e)
    {
        this.posX = e.posX;
        this.posY = e.posY;
        this.radius = e.radius;
        this.kbRadius = e.knockbackRadius;
        this.damage = e.damage;

        if (e.tankKnockback == 0 && e.bulletKnockback == 0)
            this.kbRadius = 0;

        this.destroysObstacles = e.destroysObstacles;
    }

    @Override
    public void execute()
    {
        if (clientID == null)
        {
            Explosion e = new Explosion(this.posX, this.posY, this.radius, damage, destroysObstacles, (TankAIControlled) Game.dummyTank);

            if (this.kbRadius != 0)
                e.tankKnockback = 1;

            e.knockbackRadius = this.kbRadius;
            e.explode();
        }
    }
}
