package tanks.bullet;

import basewindow.Color;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.tank.Turret;
import tanks.tankson.*;

import java.util.ArrayList;

@TanksONable("bullet_effect")
public class BulletEffect implements ICopyable<BulletEffect>, ITanksONEditable
{
    @Property(id = "trails", name = "Trail", category = BulletEffectPropertyCategory.trail, miscType = Property.MiscType.trails)
    public ArrayList<Trail> trailEffects = new ArrayList<>();

    @Property(id = "particles", name = "Enable particles", category = BulletEffectPropertyCategory.particle)
    public boolean enableParticles = false;
    @Property(id = "particle_color", name = "Particle color", category = BulletEffectPropertyCategory.particle, miscType = Property.MiscType.colorRGB)
    public Color particleColor = new Color(0, 0, 0, 0);
    @Property(id = "particle_glow", name = "Particle glow", category = BulletEffectPropertyCategory.particle, minValue = 0, maxValue = 1)
    public double particleGlow = 1;
    @Property(id = "particle_lifespan", name = "Particle lifespan", category = BulletEffectPropertyCategory.particle, minValue = 0)
    public double particleLifespan = 0.5;
    @Property(id = "particle_speed", name = "Particle speed", category = BulletEffectPropertyCategory.particle, minValue = 0)
    public double particleSpeed = 4;

    @Property(id = "luminance", minValue = 0.0, maxValue = 1.0, name = "Luminance", category = BulletEffectPropertyCategory.glow, desc = "How bright the bullet will be in dark lighting. At 0, the bullet will be shaded like terrain by lighting. At 1, the bullet will always be fully bright.")
    public double luminance = 0.5;
    @Property(id = "glow_intensity", minValue = 0.0, name = "Aura intensity", category = BulletEffectPropertyCategory.glow)
    public double glowIntensity = 1;
    @Property(id = "glow_size", minValue = 0.0, name = "Aura size", category = BulletEffectPropertyCategory.glow)
    public double glowSize = 4;
    @Property(id = "glow_glowy", name = "Aura glowy", category = BulletEffectPropertyCategory.glow)
    public boolean glowGlowy = true;
    @Property(id = "glow_color_override", name = "Custom aura color", category = BulletEffectPropertyCategory.glow)
    public boolean overrideGlowColor = false;
    @Property(id = "glow_color", name = "Aura color", category = BulletEffectPropertyCategory.glow, miscType = Property.MiscType.colorRGB)
    public Color glowColor = new Color(0, 0, 0, 0);

    public static BulletEffect none = new BulletEffect();
    public static BulletEffect trail = new BulletEffect();
    public static BulletEffect long_trail = new BulletEffect();
    public static BulletEffect fire = new BulletEffect();
    public static BulletEffect fire_trail = new BulletEffect();
    public static BulletEffect dark_fire = new BulletEffect();
    public static BulletEffect ember = new BulletEffect();
    public static BulletEffect ice = new BulletEffect();

    static
    {
        trail.trailEffects.add(new Trail(0, 1, 1, 15, 127, 127, 127, 100, 127, 127, 127, 0, false, 0.5, true, true));

        long_trail.trailEffects.add(new Trail(0, 1, 1, 15 * 1.5, 127, 127, 127, 100, 127, 127, 127, 0, false, 0.5, true, true));

        fire.trailEffects.add(new Trail(0, 1, 1, 15, 127, 127, 127, 100, 127, 127, 127, 0, false, 0.5, true, true));
        fire.trailEffects.add(new Trail(0, 5, 1, 5, 255, 255, 0, 255, 255, 0, 0, 0, false, 1, true, true));

        fire_trail.trailEffects.add(new Trail(7,2, 2, 50, 80, 80, 80, 100, 80, 80, 80, 0, false, 0.5, false, true));
        fire_trail.trailEffects.add(new Trail(3,2, 2, 4, 80, 80, 80, 0, 80, 80, 80, 100, false, 0.5, true, false));
        fire_trail.trailEffects.add(new Trail(0, 5, 1, 5, 255, 255, 0, 255, 255, 0, 0, 0, false, 1, true, true));

        dark_fire.trailEffects.add(new Trail(0, 1, 1, 15, 127, 127, 127, 100, 127, 127, 127, 0, false, 0.5, true, true));
        dark_fire.trailEffects.add(new Trail(0, 5, 1, 5, 64, 0, 128, 255, 0, 0, 0, 0, false, 1, true, true));

        ice.enableParticles = true;
        ice.particleColor.red = 128;
        ice.particleColor.green = 255;
        ice.particleColor.blue = 255;
        ice.particleGlow = 0.25;

        ember.enableParticles = true;
        ember.particleColor.red = 255;
        ember.particleColor.green = 180;
        ember.particleColor.blue = 0;

        fire.enableParticles = true;
        fire.particleColor.red = 255;
        fire.particleColor.green = 180;
        fire.particleColor.blue = 64;
        fire.particleLifespan = 0.25;
        fire.particleSpeed = 12;

        fire_trail.enableParticles = true;
        fire_trail.particleColor.red = 255;
        fire_trail.particleColor.green = 180;
        fire_trail.particleColor.blue = 64;
        fire_trail.particleLifespan = 0.25;
        fire_trail.particleSpeed = 12;

        dark_fire.enableParticles = true;
        dark_fire.particleColor.red = 0;
        dark_fire.particleColor.green = 0;
        dark_fire.particleColor.blue = 0;
        dark_fire.particleGlow = 0;
        dark_fire.particleLifespan = 0.25;
        dark_fire.particleSpeed = 12;

        fire.overrideGlowColor = true;
        fire.glowColor.red = 255;
        fire.glowColor.green = 180;
        fire.glowColor.blue = 0;
        fire.glowIntensity = 0.8;
        fire.glowSize = 16;

        fire_trail.overrideGlowColor = true;
        fire_trail.glowColor.red = 255;
        fire_trail.glowColor.green = 180;
        fire_trail.glowColor.blue = 0;
        fire_trail.glowIntensity = 0.8;
        fire_trail.glowSize = 16;

        dark_fire.overrideGlowColor = true;
        dark_fire.glowColor.red = 0;
        dark_fire.glowColor.green = 0;
        dark_fire.glowColor.blue = 0;
        dark_fire.glowIntensity = 0.5;
        dark_fire.glowGlowy = false;
        dark_fire.glowSize = 6;
    }

    public double drawForInterface(double x, double width, double y, double size, ArrayList<Effect> effects)
    {
        return drawForInterface(x, width, y, size, effects, 1, true);
    }

    public double drawForInterface(double x, double width, double y, double size, ArrayList<Effect> effects, double stretch, boolean bullet)
    {
        double max = 0;

        if (this.enableParticles)
            max = this.particleLifespan * 31.25;

        for (Trail t: this.trailEffects)
        {
            max = Math.max(max, t.maxLength + t.delay);
        }

        double fullLength = max * size * stretch;
        double l = Math.min(width, fullLength);
        double start = x - l / 2;
        double end = x + l / 2;

        for (Trail t : this.trailEffects)
        {
            t.drawForInterface(start, end, y, size, max);
        }

        if (bullet)
        {
            if (!this.overrideGlowColor)
                Drawing.drawing.setColor(Turret.calculateSecondaryColor(0) * this.glowIntensity, Turret.calculateSecondaryColor(150) * this.glowIntensity, Turret.calculateSecondaryColor(255) * this.glowIntensity, 255, this.glowGlowy ? 1 : 0);
            else
                Drawing.drawing.setColor(this.glowColor.red * this.glowIntensity, this.glowColor.green * this.glowIntensity, this.glowColor.blue * this.glowIntensity, 255, this.glowGlowy ? 1 : 0);

            Drawing.drawing.fillInterfaceGlow(start, y, size * this.glowSize, size * this.glowSize, !this.glowGlowy);

            Drawing.drawing.setColor(Turret.calculateSecondaryColor(0) * this.glowIntensity, Turret.calculateSecondaryColor(150) * this.glowIntensity, Turret.calculateSecondaryColor(255) * this.glowIntensity, 255, this.glowGlowy ? 1 : 0);
            Drawing.drawing.fillInterfaceOval(start, y, size, size);
            Drawing.drawing.setColor(0, 150, 255, 255, this.luminance);
            Drawing.drawing.fillInterfaceOval(start, y, size * 0.6, size * 0.6);
        }

        if (this.enableParticles && Game.bulletTrails && Math.random() < Panel.frameFrequency * Game.effectMultiplier && Game.effectsEnabled)
        {
            Effect e = Effect.createNewEffect(start, y, Effect.EffectType.interfacePiece);
            double var = 50;
            e.maxAge *= this.particleLifespan;

            double r1 = this.particleColor.red;
            double g1 = this.particleColor.green;
            double b1 = this.particleColor.blue;

            e.colR = Math.min(255, Math.max(0, r1 + Math.random() * var - var / 2));
            e.colG = Math.min(255, Math.max(0, g1 + Math.random() * var - var / 2));
            e.colB = Math.min(255, Math.max(0, b1 + Math.random() * var - var / 2));

            if (this.particleGlow <= 0)
                e.enableGlow = false;

            e.glowR = e.colR * (1 - this.particleGlow);
            e.glowG = e.colG * (1 - this.particleGlow);
            e.glowB = e.colB * (1 - this.particleGlow);

            e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Bullet.bullet_size / 50.0 * this.particleSpeed);
            e.vX += 3.125 * l / fullLength;
            effects.add(e);
        }

        return l;
    }

    @Override
    public String getName()
    {
        return "bullet_effect";
    }

    @Override
    public String toString()
    {
        return Serializer.toTanksON(this);
    }

    public static BulletEffect fromString(String s)
    {
        return (BulletEffect) Serializer.fromTanksON(s);
    }

}
