package tanks.bullet;

import tanks.tank.Explosion;

public class DefaultBullets
{
    public static Bullet basic_bullet;
    public static Bullet mini_bullet;
    public static Bullet mega_bullet;
    public static Bullet rocket;
    public static Bullet sniper_rocket;
    public static Bullet void_rocket;
    public static Bullet homing_rocket;
    public static Bullet freezing_bullet;
    public static Bullet booster_bullet;
    public static Bullet explosive_bullet;

    public static BulletInstant laser;
    public static BulletInstant zap;
    public static BulletInstant healing_ray;

    public static BulletGas flamethrower;
    public static BulletGas air;

    public static BulletArc artillery_shell;

    public static void initialize()
    {
        basic_bullet = new Bullet();

        mini_bullet = new Bullet();
        mini_bullet.size /= 2;
        mini_bullet.speed *= 2;
        mini_bullet.bounces = 0;
        mini_bullet.damage /= 8;
        mini_bullet.maxLiveBullets = 8;

        mega_bullet = new Bullet();
        mega_bullet.size *= 2.5;
        mega_bullet.bounces = 3;
        mega_bullet.maxLiveBullets = 3;
        mega_bullet.heavy = true;

        rocket = new Bullet();
        rocket.speed *= 2;
        rocket.bounces = 0;
        rocket.maxLiveBullets = 3;
        rocket.effect = Bullet.BulletEffect.fire;

        sniper_rocket = new Bullet();
        sniper_rocket.speed *= 2;
        sniper_rocket.bounces = 2;
        sniper_rocket.maxLiveBullets = 4;
        sniper_rocket.effect = Bullet.BulletEffect.fire_trail;
        sniper_rocket.shotSound = "shoot_power.ogg";

        void_rocket = new Bullet();
        void_rocket.speed *= 2;
        void_rocket.bounces = 0;
        void_rocket.maxLiveBullets = 5;
        void_rocket.effect = Bullet.BulletEffect.dark_fire;

        homing_rocket = new Bullet();
        homing_rocket.speed *= 2;
        homing_rocket.bounces = 0;
        homing_rocket.maxLiveBullets = 3;
        homing_rocket.effect = Bullet.BulletEffect.fire;
        homing_rocket.homingSharpness = 1.0 / 5.5;

        freezing_bullet = new Bullet();
        freezing_bullet.maxLiveBullets = 2;
        freezing_bullet.bounces = 0;
        freezing_bullet.effect = Bullet.BulletEffect.ice;
        freezing_bullet.damage /= 4;
        freezing_bullet.freezing = true;
        freezing_bullet.overrideOutlineColor = true;
        freezing_bullet.outlineColorR = 255;
        freezing_bullet.outlineColorG = 255;
        freezing_bullet.outlineColorB = 255;

        booster_bullet = new Bullet();
        booster_bullet.maxLiveBullets = 5;
        booster_bullet.bounces = 0;
        booster_bullet.effect = Bullet.BulletEffect.ember;
        booster_bullet.damage = 0;
        booster_bullet.speed = 25 / 4.0;
        booster_bullet.boosting = true;
        booster_bullet.overrideOutlineColor = true;
        booster_bullet.outlineColorR = 255;
        booster_bullet.outlineColorG = 180;
        booster_bullet.outlineColorB = 0;
        
        explosive_bullet = new Bullet();
        explosive_bullet.maxLiveBullets = 2;
        explosive_bullet.bounces = 0;
        explosive_bullet.effect = Bullet.BulletEffect.trail;
        explosive_bullet.size = 20;
        explosive_bullet.hitExplosion = new Explosion();

        laser = new BulletInstant();
        laser.maxLiveBullets = 1;
        laser.bounces = 0;
        laser.overrideBaseColor = true;
        laser.baseColorR = 255;
        laser.baseColorG = 0;
        laser.baseColorB = 0;
        laser.overrideOutlineColor = true;
        laser.outlineColorR = 255;
        laser.outlineColorG = 200;
        laser.outlineColorB = 200;
        laser.shotSound = "laser.ogg";

        zap = new BulletInstant();
        zap.maxLiveBullets = 1;
        zap.bounces = 0;
        zap.hitStun = 100;
        zap.damage = 0.125;
        zap.rebounds = 3;
        zap.overrideBaseColor = true;
        zap.baseColorR = 0;
        zap.baseColorG = 255;
        zap.baseColorB = 255;
        zap.overrideOutlineColor = true;
        zap.outlineColorR = 200;
        zap.outlineColorG = 255;
        zap.outlineColorB = 255;
        zap.shotSound = "laser.ogg";

        healing_ray = new BulletInstant();
        healing_ray.maxLiveBullets = 1;
        healing_ray.bounces = 0;
        healing_ray.damage = -0.01;
        healing_ray.bulletCollision = false;
        healing_ray.overrideBaseColor = true;
        healing_ray.baseColorR = 0;
        healing_ray.baseColorG = 255;
        healing_ray.baseColorB = 0;
        healing_ray.overrideOutlineColor = true;
        healing_ray.outlineColorR = 200;
        healing_ray.outlineColorG = 255;
        healing_ray.outlineColorB = 200;
        healing_ray.shotSound = null;
        
        flamethrower = new BulletGas();
        flamethrower.bounces = 0;
        flamethrower.damage = 0.2;
        flamethrower.maxLiveBullets = 0;
        flamethrower.overrideBaseColor = true;
        flamethrower.overrideOutlineColor = true;
        flamethrower.baseColorR = 255;
        flamethrower.baseColorG = 255;
        flamethrower.baseColorB = 0;
        flamethrower.outlineColorR = 255;
        flamethrower.outlineColorG = 0;
        flamethrower.outlineColorB = 0;
        flamethrower.glowIntensity = 1;
        flamethrower.glowSize = 3;
        flamethrower.lowersBushes = false;
        flamethrower.burnsBushes = true;
        flamethrower.bulletCollision = false;
        flamethrower.shotSound = "flame.ogg";
        flamethrower.range = 312.5;
        flamethrower.endSize = Bullet.bullet_size * 10;

        air = new BulletGas();
        air.shotSound = "wind.ogg";
        air.pitchVariation = 1.0;
        air.overrideBaseColor = true;
        air.overrideOutlineColor = true;
        air.baseColorR = 160;
        air.baseColorG = 179;
        air.baseColorB = 191;
        air.noiseR = 95;
        air.noiseG = 76;
        air.noiseB = 64;
        air.outlineColorR = 100;
        air.outlineColorG = 131;
        air.outlineColorB = 151;
        air.glowIntensity = 0;
        air.glowSize = 0;
        air.opacity = 1.0 / 6;
        air.bulletHitKnockback = 0.04;
        air.tankHitKnockback = 0.1;
        air.lowersBushes = false;
        air.range = 1250;
        air.endSize = Bullet.bullet_size * 40;
        air.maxLiveBullets = 0;
        air.speed *= 2;
        air.accuracySpread = 20;
        air.bounces = 0;
        air.effect = Bullet.BulletEffect.none;
        air.damage = 0;
        air.heavy = true;

        artillery_shell = new BulletArc();
        artillery_shell.maxLiveBullets = 5;
        artillery_shell.size = 25;
        artillery_shell.bounces = 0;
        artillery_shell.lifespan = 1000 / 3.125;
        artillery_shell.shotSound = "arc.ogg";
    }
}
