package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.registry.RegistryTank;
import tanks.tank.*;

public class EventTankMimicTransform extends PersonalEvent
{
    public int tank;
    public int target;


    public EventTankMimicTransform()
    {

    }

    public EventTankMimicTransform(Tank t, Tank target)
    {
        this.tank = t.networkID;
        this.target = target.networkID;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (this.clientID == null && t instanceof TankRemote)
        {
            Tank t1 = null;

            if (this.target == this.tank)
            {
                for (TankAIControlled t2 : Game.currentLevel.customTanks)
                {
                    if (t2.name.equals(t.name))
                    {
                        t1 = new TankAIControlled("", 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                        t2.cloneProperties((TankAIControlled) t1);
                        break;
                    }
                }

                if (t1 == null)
                {
                    RegistryTank.TankEntry e = Game.registryTank.getEntry(t.name);
                    t1 = e.getTank(0, 0, 0);
                }
            }
            else
                t1 = Tank.idMap.get(target);

            if (t1 == null)
            {
                t1 = new TankDummy(t.name, t.posX, t.posY, t.angle);
            }

            TankModels.TankSkin baseSkin = t.baseSkin;
            TankModels.TankSkin turretBaseSkin = t.turretBaseSkin;
            TankModels.TankSkin turretSkin = t.turretSkin;

            ((TankRemote) t).copyTank(t1);
            ((TankRemote) t).invisible = false;
            t.fromRegistry = false;

            if (!(this.target == this.tank))
            {
                t.baseSkin = baseSkin;
                t.turretBaseSkin = turretBaseSkin;
                t.turretSkin = turretSkin;

                if (Game.effectsEnabled)
                {
                    for (int i = 0; i < 50 * Game.effectMultiplier; i++)
                    {
                        Effect e = Effect.createNewEffect(t.posX, t.posY, t.size / 4, Effect.EffectType.piece);
                        double var = 50;
                        e.setColorsFromTank(t);

                        if (Game.enable3d)
                            e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, 1 + Math.random() * t.size / 50.0);
                        else
                            e.setPolarMotion(Math.random() * 2 * Math.PI, 1 + Math.random() * t.size / 50.0);

                        Game.effects.add(e);
                    }
                }
            }
        }
    }
}
