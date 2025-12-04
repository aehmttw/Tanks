package tanks.tankson;

import basewindow.Color;
import tanks.BiConsumer;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletEffect;
import tanks.bullet.BulletGas;
import tanks.item.ItemBullet;
import tanks.item.ItemIcon;
import tanks.item.ItemMine;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankModels;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Compatibility
{
    /**
     * Add a static initializer to initialize compatibility table if necessary
     */
    public static final HashMap<String, BiFunction<Object, Object, Object>> compatibility_table = new HashMap<>();

    public static final HashMap<Class<?>, BiFunction<Field, Object, Object>> general_table = new HashMap<>();

    public static final HashMap<String, BiConsumer<Object, Object>> unused_table = new HashMap<>();

    public static final HashMap<String, String> field_table = new HashMap<>();

    static
    {
        addGeneralCase(Boolean.class, Compatibility::applyBoolean);
        compatibility_table.put("bullet", (owner, a) ->
        {
            if (a instanceof HashMap)
            {
                Object b = Serializer.parseObject((Map<String, Object>) a);
                if (b instanceof Bullet)
                {
                    if (owner instanceof TankAIControlled)
                    {
                        if (!((TankAIControlled) owner).enableMovement)
                            ((Bullet) b).recoil = 0;
                    }

                    return new ItemBullet.ItemStackBullet(null, new ItemBullet((Bullet) b), 0);
                }
            }

            throw new RuntimeException("Failed to convert bullet: " + a.getClass() + " " + a.toString());
        });

        compatibility_table.put("mine", (owner, a) ->
        {
            if (a instanceof HashMap)
            {
                Object b = Serializer.parseObject((Map<String, Object>) a);
                if (b instanceof Mine)
                    return new ItemMine.ItemStackMine(null, new ItemMine((Mine) b), 0);
            }

            throw new RuntimeException("Failed to convert mine: " + a.getClass() + " " + a.toString());
        });

        field_table.put("base_model", "baseSkin");
        field_table.put("color_model", "colorSkin");
        field_table.put("turret_base_model", "turretBaseSkin");
        field_table.put("turret_model", "turretSkin");

        compatibility_table.put("color_model", (owner, a) ->
               convertModelToSkin((String) a));

        compatibility_table.put("base_model", (owner, a) ->
                convertModelToSkin((String) a));

        compatibility_table.put("turret_base_model", (owner, a) ->
                convertModelToSkin((String) a));

        compatibility_table.put("turret_model", (owner, a) ->
                convertModelToSkin((String) a));

        compatibility_table.put("effect", (owner, a) ->
        {
            String s = (String) a;
            switch (s)
            {
                case "fire":
                    return BulletEffect.fire.getCopy();
                case "trail":
                    return BulletEffect.trail.getCopy();
                case "long_trail":
                    return BulletEffect.long_trail.getCopy();
                case "dark_fire":
                    return BulletEffect.dark_fire.getCopy();
                case "fire_trail":
                    return BulletEffect.fire_trail.getCopy();
                case "ice":
                    return BulletEffect.ice.getCopy();
                case "ember":
                    return BulletEffect.ember.getCopy();
                default:
                    return new BulletEffect();
            }
        });

        compatibility_table.put("icon", (owner, a) ->
        {
            String i = ((String) a).replace(".png", "");
            return Game.registryItemIcon.getItemIcon(i).getCopy();
        });

        unused_table.put("luminance", (owner, value) ->
        {
            if (owner instanceof Bullet)
                ((Bullet) owner).effect.luminance = (double) value;
        });

        unused_table.put("glow_intensity", (owner, value) ->
        {
            if (owner instanceof Bullet)
                ((Bullet) owner).effect.glowIntensity = (double) value;
        });

        unused_table.put("glow_size", (owner, value) ->
        {
            if (owner instanceof Bullet)
                ((Bullet) owner).effect.glowSize = (double) value;
        });

        for (String s: new String[]{"color_*", "color_*2", "color_*3", "emblem_*", "color_noise_*"})
        {
            for (String c: new String[]{"r", "g", "b"})
            {
                String name = s.replace("*", c);
                unused_table.put(name, (owner, value) ->
                {
                    Color col = null;

                    if (owner instanceof Tank)
                    {
                        Tank t = (Tank) owner;
                        if (s.equals("color_*"))
                            col = t.color;
                        else if (s.equals("color_*2"))
                            col = t.secondaryColor;
                        else if (s.equals("color_*3"))
                            col = t.tertiaryColor;
                        else if (s.equals("emblem_*"))
                            col = t.emblemColor;
                    }
                    else if (owner instanceof Bullet)
                    {
                        Bullet b = (Bullet) owner;
                        if (s.equals("color_*"))
                            col = b.baseColor;
                        else if (s.equals("color_*2"))
                            col = b.outlineColor;
                        else if (s.equals("color_noise_*") && b instanceof BulletGas)
                            col = ((BulletGas) b).noise;
                    }
                    else
                        throw new RuntimeException("Uh oh, could not convert " + owner + "'s field " + name);

                    if (col != null)
                    {
                        if (c.equals("r"))
                            col.red = (double) value;
                        else if (c.equals("g"))
                            col.green = (double) value;
                        else if (c.equals("b"))
                            col.blue = (double) value;
                    }
                });
            }
        }
    }

    public static <V> void addGeneralCase(Class<V> cls, BiFunction<Field, V, Object> func)
    {
        //noinspection unchecked
        general_table.put(cls, (BiFunction<Field, Object, Object>) func);
    }

    public static Object convert(Field f, Object owner, Object o)
    {
        if (general_table.containsKey(o.getClass()))
            return general_table.get(o.getClass()).apply(f, o);

        return compatibility_table.get(Serializer.getid(f)).apply(owner, o);
    }

    public static String convert(String f)
    {
        return field_table.get(f);
    }

    private static Object applyBoolean(Field f, Boolean b)
    {
        try
        {
            return b ? f.getType().getConstructor().newInstance() : null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static TankModels.TankSkin convertModelToSkin(String model)
    {
        return Game.registryModelTank.tankSkins.get(model.replace("/models/", "")
                .replace("/color/", "")
                .replace("/base/", "")
                .replace("/turret/", "")
                .replace("/turretbase/", "")
                .replace("tankarrow", "tank_arrow")
                .replace("tankcamoflauge", "tank_camoflauge")
                .replace("tankmimic", "tank_checkerboard")
                .replace("tankcross", "tank_cross")
                .replace("tankdiagonalstripes", "tank_diagonal_stripes")
                .replace("tankfixed", "tank_fixed")
                .replace("tankflames", "tank_flames")
                .replace("tankhorizontalstripes", "tank_horizontal_stripes")
                .replace("tankverticalstripes", "tank_vertical_stripes"));
    }
}
