package tanks.network.event;

import tanks.Game;
import tanks.tank.TankPlayerController;

import java.util.UUID;

public class EventTankControllerUpdateAmmunition extends PersonalEvent implements IStackableEvent
{
    public UUID clientIdTarget;
    public int action1Live;
    public int action1Max;
    public int action2Live;
    public int action2Max;
    public double cooldown;
    public double cooldownBase;
    public double cooldown2;
    public double cooldownBase2;

    public EventTankControllerUpdateAmmunition()
    {

    }

    public EventTankControllerUpdateAmmunition(UUID clientID, int a1, int a1max, int a2, int a2max, double cooldown, double cooldownBase, double cooldown2, double cooldownBase2)
    {
        this.clientIdTarget = clientID;
        this.action1Live = a1;
        this.action1Max = a1max;
        this.action2Live = a2;
        this.action2Max = a2max;
        this.cooldown = cooldown;
        this.cooldownBase = cooldownBase;
        this.cooldown2 = cooldown2;
        this.cooldownBase2 = cooldownBase2;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && clientIdTarget.equals(Game.clientID) && Game.playerTank instanceof TankPlayerController)
        {
            TankPlayerController c = (TankPlayerController) Game.playerTank;
            c.liveBullets = action1Live;
            c.maxLiveBullets = action1Max;
            c.liveMines = action2Live;
            c.maxLiveMines = action2Max;
            c.bulletCooldownBase = cooldownBase;
            c.bulletCooldown = cooldown;
            c.mineCooldownBase = cooldownBase2;
            c.mineCooldown = cooldown2;
        }
    }

    @Override
    public int getIdentifier()
    {
        return clientIdTarget.hashCode();
    }
}
