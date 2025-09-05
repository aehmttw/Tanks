package tanks.network.event;

import tanks.*;
import tanks.minigames.Arcade;

public class EventArcadeHit extends PersonalEvent
{
    public int power;
    public double posX;
    public double posY;
    public double posZ;
    public int points;

    public EventArcadeHit(int power, double posX, double posY, double posZ, int points)
    {
        this.power = power;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.points = points;
    }

    public EventArcadeHit()
    {

    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.currentLevel instanceof Arcade)
        {
            ((Arcade) Game.currentLevel).chain = power;
            ((Arcade) Game.currentLevel).lastHit = ((Arcade) Game.currentLevel).age;

            Effect e = Effect.createNewEffect(posX, posY, posZ, Effect.EffectType.chain);
            e.radius = power;
            Game.effects.add(e);

            ((Arcade) Game.currentLevel).score += points;
            Drawing.drawing.playSound("hit_chain.ogg", (float) Math.pow(2, Math.min(Arcade.max_power * 3 - 1, power - 1) / 12.0), 0.5f);
        }
    }
}
