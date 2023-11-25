package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankRemote;

import java.util.HashSet;

public class EventTankTransformPreset extends PersonalEvent
{
    public int tank;
    public boolean sight;
    public boolean revert;

    public EventTankTransformPreset()
    {

    }

    public EventTankTransformPreset(Tank t, boolean sight, boolean revert)
    {
        tank = t.networkID;

        this.sight = sight;
        this.revert = revert;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (this.clientID == null && t instanceof TankRemote && ((TankRemote) t).tank instanceof TankAIControlled && (!revert || ((TankRemote) t).parentTransformations.size() > 0))
        {
            TankAIControlled target;

            if (revert)
                target = ((TankRemote) t).parentTransformations.remove(((TankRemote) t).parentTransformations.size() - 1);
            else
            {
                target = ((TankAIControlled) ((TankRemote) t).tank).healthTransformTank;
                if (sight)
                    target = ((TankAIControlled) ((TankRemote) t).tank).sightTransformTank;

                TankAIControlled p = new TankAIControlled(((TankRemote) t).tank.name, 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                ((TankAIControlled) ((TankRemote) t).tank).cloneProperties(p);
                ((TankRemote) t).parentTransformations.add(p);
            }

            t.size = target.size;
            t.turretSize = target.turretSize;
            t.turretLength = target.turretLength;

            t.baseModel = target.baseModel;
            t.colorModel = target.colorModel;
            t.turretBaseModel = target.turretBaseModel;
            t.turretModel = target.turretModel;
            t.multipleTurrets = target.multipleTurrets;

            t.emblem = target.emblem;
            t.emblemR = target.emblemR;
            t.emblemG = target.emblemG;
            t.emblemB = target.emblemB;

            t.luminance = target.luminance;
            t.glowIntensity = target.glowIntensity;
            t.glowSize = target.glowSize;
            t.lightIntensity = target.lightIntensity;
            t.lightSize = target.lightSize;

            t.bullet.shotCount = target.bullet.shotCount;
            t.bullet.shotSpread = target.bullet.shotSpread;

            t.mandatoryKill = target.mandatoryKill;
            t.musicTracks = target.musicTracks;

            t.enableTracks = target.enableTracks;
            t.trackSpacing = target.trackSpacing;

            ((TankAIControlled) ((TankRemote) t).tank).spawnedTankEntries = target.spawnedTankEntries;

            ((TankRemote) t).invisible = false;
            ((TankRemote) t).vanished = false;

            if (sight)
            {
                Effect e1 = Effect.createNewEffect(t.posX, t.posY, t.posZ + target.size * 0.75, Effect.EffectType.exclamation);
                e1.size = target.size;
                e1.colR = t.colorR;
                e1.colG = t.colorG;
                e1.colB = t.colorB;
                e1.glowR = target.colorR;
                e1.glowG = target.colorG;
                e1.glowB = target.colorB;
                Game.effects.add(e1);
            }

            t.colorR = target.colorR;
            t.colorG = target.colorG;
            t.colorB = target.colorB;

            t.secondaryColorR = target.secondaryColorR;
            t.secondaryColorG = target.secondaryColorG;
            t.secondaryColorB = target.secondaryColorB;

            t.enableTertiaryColor = target.enableTertiaryColor;
            t.tertiaryColorR = target.tertiaryColorR;
            t.tertiaryColorG = target.tertiaryColorG;
            t.tertiaryColorB = target.tertiaryColorB;
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeBoolean(this.sight);
        b.writeBoolean(this.revert);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.sight = b.readBoolean();
        this.revert = b.readBoolean();
    }
}
