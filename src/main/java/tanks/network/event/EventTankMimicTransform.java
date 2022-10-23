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

            ((TankRemote) t).copyTank(t1);
            ((TankRemote) t).invisible = false;
            t.fromRegistry = false;

            if (!(this.target == this.tank))
            {
                t.baseModel = TankModels.checkerboard.base;
                t.turretBaseModel = TankModels.checkerboard.turretBase;
                t.turretModel = TankModels.checkerboard.turret;

                if (Game.effectsEnabled)
                {
                    for (int i = 0; i < 50 * Game.effectMultiplier; i++)
                    {
                        Effect e = Effect.createNewEffect(t.posX, t.posY, t.size / 4, Effect.EffectType.piece);
                        double var = 50;
                        e.colR = Math.min(255, Math.max(0, t.colorR + Math.random() * var - var / 2));
                        e.colG = Math.min(255, Math.max(0, t.colorG + Math.random() * var - var / 2));
                        e.colB = Math.min(255, Math.max(0, t.colorB + Math.random() * var - var / 2));

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

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeInt(this.target);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.target = b.readInt();
    }
}
