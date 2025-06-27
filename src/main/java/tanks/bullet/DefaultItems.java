package tanks.bullet;

import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.item.ItemShield;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.translation.Translation;

public class DefaultItems
{
    public static ItemBullet basic_bullet = new ItemBullet();
    public static ItemBullet mini_bullet = new ItemBullet();
    public static ItemBullet mega_bullet = new ItemBullet();
    public static ItemBullet rocket = new ItemBullet();
    public static ItemBullet sniper_rocket = new ItemBullet();
    public static ItemBullet void_rocket = new ItemBullet();
    public static ItemBullet homing_rocket = new ItemBullet();
    public static ItemBullet freezing_bullet = new ItemBullet();
    public static ItemBullet booster_bullet = new ItemBullet();
    public static ItemBullet explosive_bullet = new ItemBullet();

    public static ItemBullet laser = new ItemBullet();
    public static ItemBullet zap = new ItemBullet();
    public static ItemBullet healing_ray = new ItemBullet();

    public static ItemBullet flamethrower = new ItemBullet();
    public static ItemBullet air = new ItemBullet();

    public static ItemBullet artillery_shell = new ItemBullet();

    public static ItemMine basic_mine = new ItemMine();

    public static ItemShield shield = new ItemShield();

    public static void initialize()
    {
        basic_bullet.bullet = new Bullet();
        basic_bullet.name = Translation.translate("Basic bullet");
        basic_bullet.icon = "bullet_normal.png";

        mini_bullet.bullet = new Bullet();
        mini_bullet.bullet.size /= 2;
        mini_bullet.bullet.pitch *= 2;
        mini_bullet.bullet.speed *= 2;
        mini_bullet.bullet.bounces = 0;
        mini_bullet.bullet.damage /= 8;
        mini_bullet.bullet.maxLiveBullets = 8;
        mini_bullet.name = Translation.translate("Mini bullet");
        mini_bullet.icon = "bullet_mini.png";
        mini_bullet.cooldownBase = 5;

        mega_bullet.bullet = new Bullet();
        mega_bullet.bullet.size *= 2.5;
        mega_bullet.bullet.pitch /= 2.5;
        mega_bullet.bullet.bounces = 3;
        mega_bullet.bullet.maxLiveBullets = 3;
        mega_bullet.bullet.heavy = true;
        mega_bullet.name = Translation.translate("Mega bullet");
        mega_bullet.icon = "bullet_large.png";

        rocket.bullet = new Bullet();
        rocket.bullet.speed *= 2;
        rocket.bullet.bounces = 0;
        rocket.bullet.maxLiveBullets = 3;
        rocket.bullet.effect = BulletEffect.fire.getCopy();
        rocket.name = Translation.translate("Rocket");
        rocket.icon = "bullet_fire.png";

        sniper_rocket.bullet = new Bullet();
        sniper_rocket.bullet.speed *= 2;
        sniper_rocket.bullet.bounces = 2;
        sniper_rocket.bullet.maxLiveBullets = 4;
        sniper_rocket.bullet.effect =  BulletEffect.fire_trail.getCopy();
        sniper_rocket.bullet.shotSound = "shoot_power.ogg";
        sniper_rocket.name = Translation.translate("Sniper rocket");
        sniper_rocket.icon = "bullet_fire.png";

        void_rocket.bullet = new Bullet();
        void_rocket.bullet.speed *= 2;
        void_rocket.bullet.bounces = 0;
        void_rocket.bullet.maxLiveBullets = 5;
        void_rocket.bullet.effect =  BulletEffect.dark_fire.getCopy();
        void_rocket.name = Translation.translate("Void rocket");
        void_rocket.icon = "bullet_dark_fire.png";

        homing_rocket.bullet = new Bullet();
        homing_rocket.bullet.speed *= 2;
        homing_rocket.bullet.bounces = 0;
        homing_rocket.bullet.maxLiveBullets = 3;
        homing_rocket.bullet.effect =  BulletEffect.fire.getCopy();
        homing_rocket.bullet.homingSharpness = 1.0 / 5.5;
        homing_rocket.name = Translation.translate("Homing rocket");
        homing_rocket.icon = "bullet_homing.png";

        freezing_bullet.bullet = new Bullet();
        freezing_bullet.bullet.maxLiveBullets = 2;
        freezing_bullet.bullet.bounces = 0;
        freezing_bullet.bullet.effect = BulletEffect.ice.getCopy();
        freezing_bullet.bullet.damage /= 4;
        freezing_bullet.bullet.freezing = true;
        freezing_bullet.bullet.overrideOutlineColor = true;
        freezing_bullet.bullet.outlineColorR = 255;
        freezing_bullet.bullet.outlineColorG = 255;
        freezing_bullet.bullet.outlineColorB = 255;
        freezing_bullet.name = Translation.translate("Freezing bullet");
        freezing_bullet.icon = "bullet_freeze.png";

        booster_bullet.bullet = new Bullet();
        booster_bullet.bullet.maxLiveBullets = 5;
        booster_bullet.bullet.bounces = 0;
        booster_bullet.bullet.effect = BulletEffect.ember.getCopy();
        booster_bullet.bullet.damage = 0;
        booster_bullet.bullet.speed = 25 / 4.0;
        booster_bullet.bullet.boosting = true;
        booster_bullet.bullet.overrideOutlineColor = true;
        booster_bullet.bullet.outlineColorR = 255;
        booster_bullet.bullet.outlineColorG = 180;
        booster_bullet.bullet.outlineColorB = 0;
        booster_bullet.name = Translation.translate("Booster");
        booster_bullet.icon = "bullet_boost.png";
        
        explosive_bullet.bullet = new Bullet();
        explosive_bullet.bullet.maxLiveBullets = 2;
        explosive_bullet.bullet.bounces = 0;
        explosive_bullet.bullet.effect = BulletEffect.trail.getCopy();
        explosive_bullet.bullet.size = 20;
        explosive_bullet.bullet.pitch /= 2;
        explosive_bullet.bullet.hitExplosion = new Explosion();
        explosive_bullet.name = Translation.translate("Explosive bullet");
        explosive_bullet.icon = "bullet_explosive.png";

        laser.bullet = new BulletInstant();
        laser.bullet.maxLiveBullets = 1;
        laser.bullet.bounces = 0;
        laser.bullet.overrideBaseColor = true;
        laser.bullet.baseColorR = 255;
        laser.bullet.baseColorG = 0;
        laser.bullet.baseColorB = 0;
        laser.bullet.overrideOutlineColor = true;
        laser.bullet.outlineColorR = 255;
        laser.bullet.outlineColorG = 200;
        laser.bullet.outlineColorB = 200;
        laser.bullet.shotSound = "laser.ogg";
        laser.name = Translation.translate("Laser");
        laser.icon = "bullet_laser.png";

        zap.bullet = new BulletInstant();
        zap.bullet.maxLiveBullets = 1;
        zap.bullet.bounces = 0;
        zap.bullet.hitStun = 100;
        zap.bullet.damage = 0.125;
        zap.bullet.rebounds = 3;
        zap.bullet.overrideBaseColor = true;
        zap.bullet.baseColorR = 0;
        zap.bullet.baseColorG = 255;
        zap.bullet.baseColorB = 255;
        zap.bullet.overrideOutlineColor = true;
        zap.bullet.outlineColorR = 200;
        zap.bullet.outlineColorG = 255;
        zap.bullet.outlineColorB = 255;
        zap.bullet.shotSound = "laser.ogg";
        zap.name = Translation.translate("Zap");
        zap.icon = "bullet_electric.png";

        healing_ray.bullet = new BulletInstant();
        healing_ray.bullet.maxLiveBullets = 1;
        healing_ray.bullet.bounces = 0;
        healing_ray.bullet.damage = -0.01;
        healing_ray.bullet.bulletCollision = false;
        healing_ray.bullet.overrideBaseColor = true;
        healing_ray.bullet.baseColorR = 0;
        healing_ray.bullet.baseColorG = 255;
        healing_ray.bullet.baseColorB = 0;
        healing_ray.bullet.overrideOutlineColor = true;
        healing_ray.bullet.outlineColorR = 200;
        healing_ray.bullet.outlineColorG = 255;
        healing_ray.bullet.outlineColorB = 200;
        healing_ray.bullet.soundVolume = 0;
        healing_ray.name = Translation.translate("Healing ray");
        healing_ray.icon = "bullet_healing.png";
        healing_ray.cooldownBase = 0;

        BulletGas f = new BulletGas();
        flamethrower.bullet = f;
        flamethrower.bullet.bounces = 0;
        flamethrower.bullet.damage = 0.2;
        flamethrower.bullet.maxLiveBullets = 0;
        flamethrower.bullet.overrideBaseColor = true;
        flamethrower.bullet.overrideOutlineColor = true;
        flamethrower.bullet.baseColorR = 255;
        flamethrower.bullet.baseColorG = 255;
        flamethrower.bullet.baseColorB = 0;
        flamethrower.bullet.outlineColorR = 255;
        flamethrower.bullet.outlineColorG = 0;
        flamethrower.bullet.outlineColorB = 0;
        f.effect.glowIntensity = 1;
        f.effect.glowSize = 3;
        flamethrower.bullet.lowersBushes = false;
        flamethrower.bullet.burnsBushes = true;
        flamethrower.bullet.bulletCollision = false;
        flamethrower.bullet.shotSound = "flame.ogg";
        flamethrower.bullet.lifespan = 100;
        f.endSize = Bullet.bullet_size * 10;
        flamethrower.name = Translation.translate("Flamethrower");
        flamethrower.icon = "bullet_flame.png";
        flamethrower.cooldownBase = 0;

        BulletGas a = new BulletGas();
        air.bullet = a;
        a.shotSound = "wind.ogg";
        a.pitchVariation = 1.0;
        a.overrideBaseColor = true;
        a.overrideOutlineColor = true;
        a.baseColorR = 160;
        a.baseColorG = 179;
        a.baseColorB = 191;
        a.noiseR = 95;
        a.noiseG = 76;
        a.noiseB = 64;
        a.outlineColorR = 100;
        a.outlineColorG = 131;
        a.outlineColorB = 151;
        a.effect.glowIntensity = 0;
        a.effect.glowSize = 0;
        a.opacity = 1.0 / 6;
        a.mineCollision = false;
        a.bulletHitKnockback = 0.04;
        a.tankHitKnockback = 0.1;
        a.lowersBushes = false;
        a.lifespan = 200;
        a.endSize = Bullet.bullet_size * 40;
        a.maxLiveBullets = 0;
        a.speed *= 2;
        a.accuracySpread = 20;
        a.bounces = 0;
        a.damage = 0;
        a.heavy = true;
        air.name = Translation.translate("Air");
        air.icon = "bullet_air.png";
        air.cooldownBase = 0;

        BulletArc c = new BulletArc();
        artillery_shell.bullet = c;
        c.maxLiveBullets = 5;
        c.size = 25;
        c.pitch /= 2.5;
        c.bounces = 0;
        c.maxRange = 1000;
        c.effect = BulletEffect.long_trail.getCopy();
        c.shotSound = "arc.ogg";
        artillery_shell.name = Translation.translate("Artillery shell");
        artillery_shell.icon = "bullet_arc.png";

        basic_mine.mine = new Mine();
        basic_mine.name = Translation.translate("Basic mine");
        basic_mine.cooldownBase = 50;
        basic_mine.icon = "mine.png";

        shield.name = Translation.translate("Shield");
        shield.icon = "shield.png";
    }
}
