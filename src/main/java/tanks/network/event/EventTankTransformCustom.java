package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.*;
import tanks.network.NetworkUtils;
import tanks.tank.*;

import java.util.HashSet;

public class EventTankTransformCustom extends PersonalEvent
{
    public int tank;

    public Color color = new Color();
    public Color color2 = new Color();
    public Color color3 = new Color();

    public boolean enableCol3;

    public double size;
    public double turretSize;
    public double turretLength;

    public String baseModel;
    public String colorModel;
    public String turretBaseModel;
    public String turretModel;

    public String emblem;
    public Color emblemColor = new Color();

    public double glowIntensity;
    public double glowSize;
    public double lightIntensity;
    public double lightSize;
    public double luminance;

    public boolean enableTracks;
    public double trackSpacing;

    public boolean requiredKill = false;

    @NetworkIgnored
    public HashSet<String> tankMusic;

    public static final int no_effect = 0;
    public static final int exclamation = 1;
    public static final int poof = 2;

    public int effect;

    public int bulletCount = 1;
    public double bulletSpread = 0;

    public EventTankTransformCustom()
    {
        this.tankMusic = new HashSet<>();
    }

    public EventTankTransformCustom(Tank t, Tank newTank, int effect)
    {
        tank = t.networkID;

        this.color.set(newTank.color);
        this.color2.set(newTank.secondaryColor);
        this.color3.set(newTank.tertiaryColor);

        this.enableCol3 = newTank.enableTertiaryColor;

        this.luminance = newTank.luminance;
        this.glowIntensity = newTank.glowIntensity;
        this.glowSize = newTank.glowSize;
        this.lightIntensity = newTank.lightIntensity;
        this.lightSize = newTank.lightSize;

        this.size = newTank.size;
        this.turretSize = newTank.turretSize;
        this.turretLength = newTank.turretLength;

        this.baseModel = newTank.baseModel.file;
        this.colorModel = newTank.colorModel.file;
        this.turretBaseModel = newTank.turretBaseModel.file;
        this.turretModel = newTank.turretModel.file;

        this.effect = effect;

        this.emblem = newTank.emblem;
        this.emblemColor.set(newTank.emblemColor);

        if (newTank instanceof TankAIControlled)
        {
            this.bulletCount = ((TankAIControlled) newTank).getBullet().shotCount;
            this.bulletSpread = ((TankAIControlled) newTank).getBullet().multishotSpread;
        }

        this.enableTracks = newTank.enableTracks;
        this.trackSpacing = newTank.trackSpacing;

        this.requiredKill = newTank.mandatoryKill;
        this.tankMusic = newTank.musicTracks;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (this.clientID == null && t instanceof TankRemote)
        {
            t.size = size;
            t.turretSize = turretSize;
            t.turretLength = turretLength;

            t.baseModel = Drawing.drawing.getModel(baseModel);
            t.colorModel = Drawing.drawing.getModel(colorModel);
            t.turretBaseModel = Drawing.drawing.getModel(turretBaseModel);
            t.turretModel = Drawing.drawing.getModel(turretModel);

            t.emblem = emblem;
            t.emblemColor.set(emblemColor);

            t.luminance = this.luminance;
            t.glowIntensity = this.glowIntensity;
            t.glowSize = this.glowSize;
            t.lightIntensity = this.lightIntensity;
            t.lightSize = this.lightSize;

            if (((TankRemote) t).tank instanceof TankAIControlled)
            {
                ((TankAIControlled) ((TankRemote) t).tank).getBullet().shotCount = bulletCount;
                ((TankAIControlled) ((TankRemote) t).tank).getBullet().multishotSpread = bulletSpread;
            }

            t.mandatoryKill = requiredKill;
            t.musicTracks = tankMusic;

            t.enableTracks = enableTracks;
            t.trackSpacing = trackSpacing;

            ((TankRemote) t).invisible = false;
            ((TankRemote) t).vanished = false;

            if (effect == exclamation)
            {
                Effect e1 = Effect.createNewEffect(t.posX, t.posY, t.posZ + this.size * 0.75, Effect.EffectType.exclamation);
                e1.size = this.size;
                e1.setColorsFromTank(t);
                Game.effects.add(e1);
            }
            else if (effect == poof)
            {
                if (Game.effectsEnabled)
                {
                    for (int i = 0; i < 50 * Game.effectMultiplier; i++)
                    {
                        Effect e = Effect.createNewEffect(t.posX, t.posY, t.size / 4, Effect.EffectType.piece);
                        e.setColorsFromTank(t);

                        if (Game.enable3d)
                            e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, 1 + Math.random() * t.size / 50.0);
                        else
                            e.setPolarMotion(Math.random() * 2 * Math.PI, 1 + Math.random() * t.size / 50.0);

                        Game.effects.add(e);
                    }
                }
            }

            t.color.set(this.color);
            t.secondaryColor.set(this.color2);
            t.tertiaryColor.set(this.color3);

            t.enableTertiaryColor = enableCol3;
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        NetworkUtils.writeCollection(b, tankMusic, NetworkUtils::writeString);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        NetworkUtils.readCollection(b, tankMusic, NetworkUtils::readString);
    }
}
