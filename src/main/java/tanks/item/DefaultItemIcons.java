package tanks.item;

import basewindow.Color;
import tanks.bullet.BulletEffect;
import tanks.bullet.DefaultItems;
import tanks.tank.TankPlayer;

public class DefaultItemIcons
{
    public static ItemIcon bullet_normal = new ItemIcon("bullet_normal", "bullet",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    TankPlayer.default_secondary_color,
                    TankPlayer.default_primary_color},
            new Boolean[]{true, false, false}).register();

    public static ItemIcon bullet_mini = new ItemIcon("bullet_mini", "bullet_mini",
            new Color[]{
                    new Color(0, 0, 0),
                    new Color(0, 0, 0)},
            new Boolean[]{false, false}).register();

    public static ItemIcon bullet_large = new ItemIcon("bullet_large", "bullet_large",
            new Color[]{
                    TankPlayer.default_secondary_color,
                    TankPlayer.default_primary_color},
            new Boolean[]{false, false}).register();

    public static ItemIcon bullet_fire = new ItemIcon("bullet_fire",  "bullet_fire",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(255, 0, 0),
                    new Color(255, 255, 0),
                    new Color(255, 0, 0),
                    new Color(255, 220, 0)},
                new Boolean[]{true, true, false, false, true, false, false, false}).register();

    public static ItemIcon bullet_homing = new ItemIcon("bullet_homing",  "bullet_fire",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    new Color(0, 0, 0, 0),
                    new Color(255, 100, 0),
                    new Color(255, 160, 0),
                    new Color(255, 0, 0),
                    new Color(255, 255, 0),
                    new Color(255, 0, 0),
                    new Color(255, 220, 0)},
            new Boolean[]{true, true, true, true, true, false, false, false}).register();

    public static ItemIcon bullet_fire_trail = new ItemIcon("bullet_fire_trail", "bullet_fire",
            new Color[]{
                    new Color(0, 0, 0, 0),
                    BulletEffect.fire_trail.trailEffects.get(0).frontColor,
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(255, 0, 0),
                    new Color(255, 255, 0),
                    new Color(255, 0, 0),
                    new Color(255, 220, 0)},
            new Boolean[]{true, true, false, false, true, false, false, false}).register();

    public static ItemIcon bullet_dark_fire = new ItemIcon("bullet_dark_fire", "bullet_fire",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(0, 0, 0, 0),
                    new Color(64, 0, 128),
                    new Color(0, 0, 0),
                    new Color(0, 0, 0),
                    new Color(32, 0, 64)},
            new Boolean[]{true, true, false, false, true, false, false, false}).register();

    public static ItemIcon bullet_flame = new ItemIcon("bullet_flame", "bullet_flame",
            new Color[]{
                    new Color(255, 0, 0),
                    new Color(255, 255, 0),
                    new Color(255, 255, 255)},
            new Boolean[]{true, false, false}).register();

    public static ItemIcon bullet_laser = new ItemIcon("bullet_laser", "bullet_laser",
            new Color[]{
                    new Color(255, 0, 0),
                    new Color(255, 40, 40),
                    new Color(255, 80, 80),
                    new Color(255, 0, 0, 0)},
            new Boolean[]{false, true, true, true}).register();

    public static ItemIcon bullet_healing = new ItemIcon("bullet_healing", "bullet_laser",
            new Color[]{
                    new Color(0, 255, 0),
                    new Color(120, 255, 120),
                    new Color(160, 255, 160),
                    new Color(0, 255, 0)},
            new Boolean[]{false, true, true, true}).register();

    public static ItemIcon bullet_electric = new ItemIcon("bullet_electric", "bullet_electric",
            new Color[]{
                    new Color(0, 255, 255),
                    new Color(127, 255, 255)},
            new Boolean[]{false, false}).register();

    public static ItemIcon bullet_freeze = new ItemIcon("bullet_freeze", "bullet_freeze",
            new Color[]{
                    new Color(0, 255, 255),
                    new Color(255, 255, 255),
                    new Color(0, 255, 255)},
            new Boolean[]{true, true, false}).register();

    public static ItemIcon bullet_arc = new ItemIcon("bullet_arc", "bullet_arc",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    TankPlayer.default_secondary_color,
                    TankPlayer.default_primary_color},
            new Boolean[]{true, false, false}).register();

    public static ItemIcon bullet_block = new ItemIcon("bullet_block", "bullet_block",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    TankPlayer.default_secondary_color,
                    TankPlayer.default_primary_color},
            new Boolean[]{true, false, false}).register();

    public static ItemIcon bullet_explosive = new ItemIcon("bullet_explosive", "bullet",
            new Color[]{
                    BulletEffect.trail.trailEffects.get(0).frontColor,
                    new Color(255, 0, 0),
                    new Color(255, 255, 0)},
            new Boolean[]{true, false, false}).register();

    public static ItemIcon bullet_boost = new ItemIcon("bullet_boost", "bullet_boost",
            new Color[]{
                    new Color(255, 235, 18),
                    new Color(255, 182, 7)},
            new Boolean[]{false, false}).register();

    public static ItemIcon bullet_air = new ItemIcon("bullet_air", "bullet_air",
            new Color[]{
                    new Color(200, 200, 200),
                    new Color(200, 200, 255)},
            new Boolean[]{true, true}).register();

    public static ItemIcon mine = new ItemIcon("mine", "mine",
            new Color[]{
                    TankPlayer.default_secondary_color,
                    TankPlayer.default_primary_color,
                    new Color(255, 220, 0)},
            new Boolean[]{true, true, true}).register();

    public static ItemIcon shield = new ItemIcon("shield", "shield",
            new Color[]{
                    new Color(170, 170, 170),
                    new Color(230, 230, 230),
                    new Color(170, 110, 50)},
            new Boolean[]{false, true, false}).register();

    public static ItemIcon shield_gold = new ItemIcon("shield_gold", "shield",
            new Color[]{
                    new Color(255, 160, 0),
                    new Color(255, 220, 0),
                    new Color(170, 110, 50)},
            new Boolean[]{false, true, false}).register();

    public static ItemIcon item = new ItemIcon("item", "item.png").register();

    public static ItemIcon no_item = new ItemIcon("no_item", "noitem.png");

}
