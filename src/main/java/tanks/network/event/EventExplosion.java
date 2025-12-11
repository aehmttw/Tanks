package tanks.network.event;

import tanks.Game;
import tanks.tank.*;

public class EventExplosion extends PersonalEvent
{
    public double posX, posY, radius, kbRadius, stunRadius, stunTime, damage;
    public boolean destroysObstacles;

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
        this.stunRadius = e.stunRadius;
        this.stunTime = e.stunTime;

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
            e.stunTime = this.stunTime;
            e.stunRadius = this.stunRadius;
            e.explode();
        }
    }
}
