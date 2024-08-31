package tanks.tankson;

import basewindow.IModel;
import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.item.Item;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.tank.TankAIControlled;

import org.json.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class TanksJson {

    protected static TankAIControlled defaultTank = new TankAIControlled("", 0, 0, 50, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
    protected static HashMap<Class<?>, Object> defaults = new HashMap<>();

    protected static Object getDefault(Class<?> c) throws InstantiationException, IllegalAccessException
    {
        if (!defaults.containsKey(c))
            defaults.put(c, c.newInstance());

        return defaults.get(c);
    }

    public static JSONObject toJson(Object o) {
        if (getAnnotation(o.getClass(), TanksONable.class) != null)
        {
            try
            {
                HashMap<String, Object> h = new HashMap<>();

                h.put("obj_type", getAnnotation(o.getClass(), TanksONable.class).value());
                if (o instanceof Bullet) {
                    h.put("bullet_type", ((Bullet) o).typeName);
                } else if (o instanceof Item) {
                    h.put("item_type", ((Item) o).getClass().getField("item_class_name").get(null));
                }

                for (Field f : o.getClass().getFields())
                {
                    String id = null;

                    Property i = f.getAnnotation(Property.class);
                    if (i != null)
                        id = i.id();

                    if (o instanceof TankAIControlled && Objects.equals(f.get(o), f.get(defaultTank)))
                        id = null;
                    else if ((o instanceof Bullet || o instanceof Mine) && Objects.equals(f.get(o), f.get(getDefault(o.getClass()))))
                        id = null;

                    if (id != null) {
                        Object obj = f.get(o);
                        if (getAnnotation(obj.getClass(), TanksONable.class) != null) {
                            h.put(id, toJson(obj));
                        } else {
                            if (obj instanceof IModel) {
                                h.put(id, obj.toString());
                            } else {
                                h.put(id, obj);
                            }
                        }
                    }
                }
                return new JSONObject((Map) h);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Failed to turn object to Map: " + o);
        }
    }

    public static <A extends Annotation> A getAnnotation(Class<?> target, Class<A> a)
    {
        while (target != null)
        {
            A r = target.getAnnotation(a);

            if (r != null)
                return r;

            target = target.getSuperclass();
        }

        return null;
    }

    public static Object parseObject(String s) {
        JSONObject json = new JSONObject(s);
        return toTanksONable(json.toMap());
    }

    public static Object toTanksONable(Map<String, Object> map)
    {
        Object o;

        try
        {
            if (!map.containsKey("obj_type"))
                return map;

            String name = (String) map.get("obj_type");

            switch (name)
            {
                case "tank":
                    o = new TankAIControlled("", 0, 0, 50, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                    break;
                case "bullet":
                {
                    String type = (String) map.get("bullet_type");
                    o = Game.registryBullet.getEntry(type).bullet.newInstance();
                    break;
                }
                case "mine":
                    o = new Mine();
                    break;
                case "item":
                {
                    String type = (String) map.get("item_type");
                    o = Game.registryItem.getEntry(type).item.newInstance();
                    break;
                }
                case "item_stack":
                {
                    o = ((Item) map.get("item")).getStack(null);
                    break;
                }
                case "shop_item":
                {
                    o = new Item.ShopItem();
                    break;
                }
                case "crusade_shop_item":
                {
                    o = new Item.CrusadeShopItem();
                    break;
                }
                case "explosion":
                {
                    o = new Explosion();
                    break;
                }
                default:
                    throw new RuntimeException("Bad object type: " + name);
            }

            for (Field f: o.getClass().getFields())
            {
                String id = null;

                Property i = f.getAnnotation(Property.class);
                if (i != null)
                    id = i.id();

                if (id != null && map.containsKey(id))
                {
                    if (HashSet.class.isAssignableFrom(f.getType())) {
                        f.set(o, new HashSet<>((ArrayList<?>) map.get(id)));
                    }else if (IModel.class.isAssignableFrom(f.getType()) && map.get(id) != null) {
                        f.set(o, Drawing.drawing.createModel((String) map.get(id)));
                    }else if (f.getType().isEnum()) {
                        f.set(o, Enum.valueOf((Class<? extends Enum>) f.getType(), (String) map.get(id)));
                    } else if (f.getType() == int.class) {
                        f.set(o, (int) Math.max(i.minValue(), Math.min(i.maxValue(), ((Number) map.get(id)).intValue())));
                    } else if (f.getType() == double.class) {
                        f.set(o, Math.max(i.minValue(), Math.min(i.maxValue(), ((Number) map.get(id)).doubleValue())));
                    } else {
                        Object obj = map.get(id);
                        if (obj instanceof Map) {
                            f.set(o, toTanksONable((Map) obj));
                        } else {
                            f.set(o, obj);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return o;
    }

}
