package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankRemote;

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
                if (sight)
                    target = (TankAIControlled) ((TankAIControlled) ((TankRemote) t).tank).sightTransformTankField.resolve();
                else
                    target = (TankAIControlled) ((TankAIControlled) ((TankRemote) t).tank).healthTransformTankField.resolve();

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

            t.baseSkin = target.baseSkin;
            t.colorSkin = target.colorSkin;
            t.turretBaseSkin = target.turretBaseSkin;
            t.turretSkin = target.turretSkin;

            t.multipleTurrets = target.multipleTurrets;

            t.emblem = target.emblem;
            t.emblemColor.set(target.emblemColor);

            t.luminance = target.luminance;
            t.glowIntensity = target.glowIntensity;
            t.glowSize = target.glowSize;
            t.lightIntensity = target.lightIntensity;
            t.lightSize = target.lightSize;

            if (((TankRemote) t).tank instanceof TankAIControlled)
            {
                ((TankAIControlled) ((TankRemote) t).tank).getBullet().shotCount = target.getBullet().shotCount;
                ((TankAIControlled) ((TankRemote) t).tank).getBullet().multishotSpread = target.getBullet().multishotSpread;
                ((TankAIControlled) ((TankRemote) t).tank).bulletItem = target.bulletItem;
                ((TankAIControlled) ((TankRemote) t).tank).mineItem = target.mineItem;

                ((TankAIControlled) ((TankRemote) t).tank).spawnedTankEntries = target.spawnedTankEntries;
                ((TankAIControlled) ((TankRemote) t).tank).sightTransformTankField = target.sightTransformTankField;
                ((TankAIControlled) ((TankRemote) t).tank).healthTransformTankField = target.healthTransformTankField;
            }

            t.mandatoryKill = target.mandatoryKill;
            t.musicTracks = target.musicTracks;

            t.enableTracks = target.enableTracks;
            t.trackSpacing = target.trackSpacing;


            ((TankRemote) t).invisible = false;
            ((TankRemote) t).vanished = false;

            if (sight)
            {
                Effect e1 = Effect.createNewEffect(t.posX, t.posY, t.posZ + target.size * 0.75, Effect.EffectType.exclamation);
                e1.size = target.size;
                e1.colR = t.color.red;
                e1.colG = t.color.green;
                e1.colB = t.color.blue;
                e1.glowR = target.color.red;
                e1.glowG = target.color.green;
                e1.glowB = target.color.blue;
                Game.effects.add(e1);
            }

            t.color.set(target.color);
            t.secondaryColor.set(target.secondaryColor);

            t.enableTertiaryColor = target.enableTertiaryColor;
            t.tertiaryColor.set(target.tertiaryColor);
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
