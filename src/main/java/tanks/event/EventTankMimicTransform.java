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
    public int mimic;
    public int tank;
    public String type;

    public int colorR;
    public int colorG;
    public int colorB;

    public int turretR;
    public int turretG;
    public int turretB;

    public boolean isPlayer;

    public EventTankMimicTransform()
    {

    }

    public EventTankMimicTransform(Tank mimic, Tank t, boolean isPlayer)
    {
        tank = mimic.networkID;
        type = mimic.name;

        this.colorR = (int) t.colorR;
        this.colorG = (int) t.colorG;
        this.colorB = (int) t.colorB;

        this.turretR = (int) t.turret.colorR;
        this.turretG = (int) t.turret.colorG;
        this.turretB = (int) t.turret.colorB;

        this.isPlayer = isPlayer;
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
                t.colorR = this.colorR;
                t.colorG = this.colorG;
                t.colorB = this.colorB;

                t.turret.colorR = this.turretR;
                t.turret.colorG = this.turretG;
                t.turret.colorB = this.turretB;

                t.colorModel = Tank.color_model;
            }

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

        if (this.isPlayer)
        {
            b.writeInt(this.colorR);
            b.writeInt(this.colorG);
            b.writeInt(this.colorB);

            b.writeInt(this.turretR);
            b.writeInt(this.turretG);
            b.writeInt(this.turretB);
        }
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.type = NetworkUtils.readString(b);

        this.isPlayer = b.readBoolean();

        if (this.isPlayer)
        {
            this.colorR = b.readInt();
            this.colorG = b.readInt();
            this.colorB = b.readInt();

            this.turretR = b.readInt();
            this.turretG = b.readInt();
            this.turretB = b.readInt();
        }
    }
}
