package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankMimic;
import tanks.tank.TankRemote;
import tanks.tank.Turret;

public class EventTankMimicTransform extends PersonalEvent
{
    public int tank;
    public String type;

    public boolean isPlayer;

    public double red;
    public double green;
    public double blue;

    public double red2;
    public double green2;
    public double blue2;

    public EventTankMimicTransform()
    {

    }

    public EventTankMimicTransform(Tank t, boolean isPlayer)
    {
        tank = t.networkID;
        type = t.name;
        this.isPlayer = isPlayer;

        this.red = t.colorR;
        this.green = t.colorG;
        this.blue = t.colorB;

        this.red2 = t.turret.colorR;
        this.green2 = t.turret.colorG;
        this.blue2 = t.turret.colorB;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (this.clientID == null && t instanceof TankRemote)
        {
            Tank t1 = Game.registryTank.getEntry(type).getTank(t.posX, t.posY, t.angle);
            Tank.freeIDs.add(t1.networkID);

            ((TankRemote) t).copyTank(t1);
            ((TankRemote) t).invisible = false;

            if (this.isPlayer)
            {
                t.colorR = red;
                t.colorG = green;
                t.colorB = blue;

                t.turret.colorR = red2;
                t.turret.colorG = green2;
                t.turret.colorB = blue2;

                t.colorModel = Tank.color_model;
            }

            if (!(t1 instanceof TankMimic) || isPlayer)
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

        b.writeDouble(this.red);
        b.writeDouble(this.green);
        b.writeDouble(this.blue);

        b.writeDouble(this.red2);
        b.writeDouble(this.green2);
        b.writeDouble(this.blue2);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.type = NetworkUtils.readString(b);
        this.isPlayer = b.readBoolean();

        this.red = b.readDouble();
        this.green = b.readDouble();
        this.blue = b.readDouble();

        this.red2 = b.readDouble();
        this.green2 = b.readDouble();
        this.blue2 = b.readDouble();
    }
}
