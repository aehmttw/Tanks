package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

import java.util.HashSet;

public class EventTankTransformCustom extends PersonalEvent
{
    public int tank;

    public double red;
    public double green;
    public double blue;

    public double red2;
    public double green2;
    public double blue2;

    public boolean enableCol3;
    public double red3;
    public double green3;
    public double blue3;

    public double size;
    public double turretSize;
    public double turretLength;

    public String baseModel;
    public String colorModel;
    public String turretBaseModel;
    public String turretModel;

    public String emblem;
    public double emblemRed;
    public double emblemGreen;
    public double emblemBlue;

    public double glowIntensity;
    public double glowSize;
    public double lightIntensity;
    public double lightSize;
    public double luminance;

    public boolean enableTracks;
    public double trackSpacing;

    public boolean requiredKill = false;

    public HashSet<String> tankMusic;

    public static final int no_effect = 0;
    public static final int exclamation = 1;
    public static final int poof = 2;

    public int effect;

    public int bulletCount;
    public double bulletSpread;

    public EventTankTransformCustom()
    {

    }

    public EventTankTransformCustom(Tank t, Tank newTank, int effect)
    {
        tank = t.networkID;

        this.red = newTank.colorR;
        this.green = newTank.colorG;
        this.blue = newTank.colorB;

        this.red2 = newTank.secondaryColorR;
        this.green2 = newTank.secondaryColorG;
        this.blue2 = newTank.secondaryColorB;

        this.enableCol3 = newTank.enableTertiaryColor;
        this.red3 = newTank.tertiaryColorR;
        this.green3 = newTank.tertiaryColorG;
        this.blue3 = newTank.tertiaryColorB;

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
        this.emblemRed = newTank.emblemR;
        this.emblemGreen = newTank.emblemG;
        this.emblemBlue = newTank.emblemB;

        this.bulletCount = newTank.bullet.shotCount;
        this.bulletSpread = newTank.bullet.shotSpread;

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

            t.baseModel = Drawing.drawing.createModel(baseModel);
            t.colorModel = Drawing.drawing.createModel(colorModel);
            t.turretBaseModel = Drawing.drawing.createModel(turretBaseModel);
            t.turretModel = Drawing.drawing.createModel(turretModel);

            t.emblem = emblem;
            t.emblemR = emblemRed;
            t.emblemG = emblemGreen;
            t.emblemB = emblemBlue;

            t.luminance = this.luminance;
            t.glowIntensity = this.glowIntensity;
            t.glowSize = this.glowSize;
            t.lightIntensity = this.lightIntensity;
            t.lightSize = this.lightSize;

            t.bullet.shotCount = bulletCount;
            t.bullet.shotSpread = bulletSpread;

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
                e1.colR = t.colorR;
                e1.colG = t.colorG;
                e1.colB = t.colorB;
                e1.glowR = this.red;
                e1.glowG = this.green;
                e1.glowB = this.blue;
                Game.effects.add(e1);
            }
            else if (effect == poof)
            {
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

            t.colorR = red;
            t.colorG = green;
            t.colorB = blue;

            t.secondaryColorR = red2;
            t.secondaryColorG = green2;
            t.secondaryColorB = blue2;

            t.enableTertiaryColor = enableCol3;
            t.tertiaryColorR = red3;
            t.tertiaryColorG = green3;
            t.tertiaryColorB = blue3;
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);

        b.writeDouble(this.red);
        b.writeDouble(this.green);
        b.writeDouble(this.blue);

        b.writeDouble(this.red2);
        b.writeDouble(this.green2);
        b.writeDouble(this.blue2);

        b.writeBoolean(this.enableCol3);
        b.writeDouble(this.red3);
        b.writeDouble(this.green3);
        b.writeDouble(this.blue3);

        b.writeDouble(this.glowIntensity);
        b.writeDouble(this.glowSize);
        b.writeDouble(this.lightIntensity);
        b.writeDouble(this.lightSize);
        b.writeDouble(this.luminance);

        b.writeDouble(this.size);
        b.writeDouble(this.turretSize);
        b.writeDouble(this.turretLength);

        NetworkUtils.writeString(b, this.baseModel);
        NetworkUtils.writeString(b, this.colorModel);
        NetworkUtils.writeString(b, this.turretBaseModel);
        NetworkUtils.writeString(b, this.turretModel);

        NetworkUtils.writeString(b, this.emblem);
        b.writeDouble(this.emblemRed);
        b.writeDouble(this.emblemGreen);
        b.writeDouble(this.emblemBlue);

        b.writeInt(this.bulletCount);
        b.writeDouble(this.bulletSpread);

        b.writeBoolean(this.enableTracks);
        b.writeDouble(this.trackSpacing);

        b.writeBoolean(this.requiredKill);

        b.writeInt(this.tankMusic.size());

        for (String s: this.tankMusic)
        {
            NetworkUtils.writeString(b, s);
        }

        b.writeInt(this.effect);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();

        this.red = b.readDouble();
        this.green = b.readDouble();
        this.blue = b.readDouble();

        this.red2 = b.readDouble();
        this.green2 = b.readDouble();
        this.blue2 = b.readDouble();

        this.enableCol3 = b.readBoolean();
        this.red3 = b.readDouble();
        this.green3 = b.readDouble();
        this.blue3 = b.readDouble();

        this.glowIntensity = b.readDouble();
        this.glowSize = b.readDouble();
        this.lightIntensity = b.readDouble();
        this.lightSize = b.readDouble();
        this.luminance = b.readDouble();

        this.size = b.readDouble();
        this.turretSize = b.readDouble();
        this.turretLength = b.readDouble();

        this.baseModel = NetworkUtils.readString(b);
        this.colorModel = NetworkUtils.readString(b);
        this.turretBaseModel = NetworkUtils.readString(b);
        this.turretModel = NetworkUtils.readString(b);

        this.emblem = NetworkUtils.readString(b);
        this.emblemRed = b.readDouble();
        this.emblemGreen = b.readDouble();
        this.emblemBlue = b.readDouble();

        this.bulletCount = b.readInt();
        this.bulletSpread = b.readDouble();

        this.enableTracks = b.readBoolean();
        this.trackSpacing = b.readDouble();

        this.requiredKill = b.readBoolean();

        int size = b.readInt();
        this.tankMusic = new HashSet<>();
        for (int i = 0; i < size; i++)
        {
            this.tankMusic.add(NetworkUtils.readString(b));
        }

        this.effect = b.readInt();
    }
}
