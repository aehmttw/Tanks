package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankMimic;
import tanks.tank.TankRemote;

public class EventTankMimicTransform extends PersonalEvent
{
    public int tank;
    public String type;

    public boolean isPlayer;

    public EventTankMimicTransform()
    {

    }

    public EventTankMimicTransform(Tank t, boolean isPlayer)
    {
        tank = t.networkID;
        type = t.name;
        this.isPlayer = isPlayer;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (this.clientID == null && t instanceof TankRemote)
        {
            Tank t1 = Game.registryTank.getEntry(type).getTank(t.posX, t.posY, t.angle);

            double r = t.turret.colorR;
            double g = t.turret.colorG;
            double b = t.turret.colorB;

            ((TankRemote) t).copyTank(t1);

            if (this.isPlayer)
            {
                t.colorR = 0;
                t.colorG = 150;
                t.colorB = 255;
                t.colorModel = Tank.color_model;
            }

            t.turret.colorR = r;
            t.turret.colorG = g;
            t.turret.colorB = b;

            if (!(t1 instanceof TankMimic))
            {
                t.baseModel = TankMimic.base_model;
                t.turretBaseModel = TankMimic.turret_base_model;
                t.turretModel = TankMimic.turret_model;

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
        NetworkUtils.writeString(b, this.type);
        b.writeBoolean(this.isPlayer);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.type = NetworkUtils.readString(b);
        this.isPlayer = b.readBoolean();
    }
}
