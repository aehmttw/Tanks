package tanks.network;

import basewindow.Color;
import tanks.gui.screen.ScreenArcadeBonuses;
import tanks.network.event.INetworkEvent;
import tanks.network.event.PersonalEvent;
import tanks.tankson.ObjectBuffer;
import tanks.tankson.TanksONDocument;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Serializes every registered network event as a compact typed ObjectBuffer list. */
public final class EventObjectBuffer
{
    private static final Map<Class<?>, Plan> plans = new ConcurrentHashMap<>();

    private EventObjectBuffer()
    {
    }

    public static byte[] encode(INetworkEvent event)
    {
        ByteBuffer b = encodeToBuffer(event);
        return Arrays.copyOfRange(b.array(), b.arrayOffset() + b.position(), b.arrayOffset() + b.limit());
    }

    /**
     * The same bytes as {@link #encode}, as a view of the encoder's array to avoid copying it.
     * The buffer is not shared, so the caller may keep it.
     */
    public static ByteBuffer encodeToBuffer(INetworkEvent event)
    {
        // Looked up on every send rather than cached, as registration can renumber events
        int id = NetworkEventMap.get(event.getClass());
        if (id < 0)
            throw new IllegalArgumentException("Unregistered network event: " + event.getClass());
        Plan plan = plan(event.getClass());

        ObjectBuffer.Writer w = ObjectBuffer.Writer.network();
        w.writeListHeader(plan.listSize);
        w.writeInt(id);
        try
        {
            if (plan.packedFloats > 0)
            {
                w.writeFloatArrayHeader(plan.packedFloats);
                for (int i = 0; i < plan.packedFloats; i++)
                    w.writeFloatRaw((float) plan.fields[i].getDouble(event));
            }

            for (int i = plan.packedFloats; i < plan.fields.length; i++)
            {
                Field field = plan.fields[i];
                switch (plan.kinds[i])
                {
                    case INT:
                        w.writeInt(field.getInt(event));
                        break;
                    case LONG:
                        w.writeLong(field.getLong(event));
                        break;
                    case BOOLEAN:
                        w.writeBoolean(field.getBoolean(event));
                        break;
                    case FLOAT:
                        w.writeFloat(field.getFloat(event));
                        break;
                    case DOUBLE:
                        w.writeDouble(field.getDouble(event));
                        break;
                    case STRING:
                        w.writeStringValue((String) field.get(event));
                        break;
                    default:
                        Object value = field.get(event);
                        if (value != null && plan.tanksON[i])
                        {
                            Object document = TanksONDocument.toValue((String) value);
                            if (document != null)
                                value = document;
                        }
                        if (value instanceof Double && !plan.customSerializer)
                            value = ((Double) value).floatValue();
                        w.writeValue(prepare(value));
                }
            }
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        return w.toNetworkBuffer();
    }

    /** Consumes body. */
    public static INetworkEvent decode(ByteBuf body)
    {
        ByteBuffer bytes = body.nioBuffer();
        body.skipBytes(body.readableBytes());
        try
        {
            ObjectBuffer.Reader reader = ObjectBuffer.networkReader(bytes);
            if (reader.readType() != ObjectBuffer.Type.LIST)
                throw new IllegalArgumentException("Network event must be an ObjectBuffer list");

            int count = reader.readCount();
            if (count == 0)
                throw new IllegalArgumentException("Invalid ObjectBuffer network event header");
            Object header = reader.readValue(1);
            if (!(header instanceof Integer))
                throw new IllegalArgumentException("Invalid ObjectBuffer network event header");
            int id = (Integer) header;
            Class<? extends INetworkEvent> type = NetworkEventMap.get(id);
            if (type == null)
                throw new IllegalArgumentException("Unknown network event: " + id);

            Plan plan = plan(type);
            if (count != plan.listSize)
                throw new IllegalArgumentException("Incorrect network field count for " + type.getName());

            if (plan.constructor == null)
                throw new IllegalArgumentException("Unable to construct network event " + type.getName() + ": no public no-argument constructor");
            INetworkEvent event = plan.constructor.newInstance();
            Field[] fields = plan.fields;
            if (plan.packedFloats > 0)
            {
                Object packed = reader.readValue(1);
                if (!(packed instanceof float[]) || ((float[]) packed).length != plan.packedFloats)
                    throw new IllegalArgumentException("Incorrect network float array for " + type.getName());
                float[] numbers = (float[]) packed;
                for (int i = 0; i < plan.packedFloats; i++)
                    fields[i].setDouble(event, numbers[i]);
            }

            for (int i = plan.packedFloats; i < fields.length; i++)
            {
                Object value = reader.readValue(1);
                if (value instanceof List && plan.tanksON[i])
                    value = TanksONDocument.fromValue(value);
                fields[i].set(event, restore(value, fields[i].getType()));
            }

            if (reader.hasRemaining())
                throw new IllegalArgumentException("Trailing bytes in ObjectBuffer network message");
            return event;
        }
        catch (BufferUnderflowException e)
        {
            throw new IllegalArgumentException("Truncated ObjectBuffer network message", e);
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalArgumentException("Unable to decode network event", e);
        }
    }

    private enum Kind { INT, LONG, BOOLEAN, FLOAT, DOUBLE, STRING, OTHER }

    /** Everything about an event class that encoding and decoding need, worked out once. */
    private static final class Plan
    {
        final boolean customSerializer;
        final Field[] fields;
        final Kind[] kinds;
        final boolean[] tanksON;
        final int packedFloats;
        /** Entries in the encoded list: the id, the packed float array if any, then the other fields. */
        final int listSize;
        final Constructor<? extends INetworkEvent> constructor;

        Plan(Class<? extends INetworkEvent> type)
        {
            this.customSerializer = hasCustomSerializer(type);
            this.fields = discoverFields(type, customSerializer).toArray(new Field[0]);
            this.kinds = new Kind[fields.length];
            this.tanksON = new boolean[fields.length];
            int packed = 0;
            while (packed < fields.length && isPackedFloat(fields[packed], customSerializer))
                packed++;
            this.packedFloats = packed;
            for (int i = 0; i < fields.length; i++)
            {
                tanksON[i] = fields[i].isAnnotationPresent(INetworkEvent.NetworkTanksON.class);
                kinds[i] = kind(fields[i].getType(), tanksON[i]);
            }
            this.listSize = fields.length + 1 - packed + (packed > 0 ? 1 : 0);

            Constructor<? extends INetworkEvent> c = null;
            try
            {
                c = type.getConstructor();
            }
            catch (NoSuchMethodException ignored)
            {
                // Only decoding needs it, and reports it there
            }
            this.constructor = c;
        }

        private static Kind kind(Class<?> t, boolean tanksON)
        {
            if (t == int.class)
                return Kind.INT;
            if (t == long.class)
                return Kind.LONG;
            if (t == boolean.class)
                return Kind.BOOLEAN;
            if (t == float.class)
                return Kind.FLOAT;
            // Unpacked primitive doubles only exist with custom serializers, which keep full precision
            if (t == double.class)
                return Kind.DOUBLE;
            if (t == String.class && !tanksON)
                return Kind.STRING;
            return Kind.OTHER;
        }
    }

    @SuppressWarnings("unchecked")
    private static Plan plan(Class<?> type)
    {
        Plan p = plans.get(type);
        if (p == null)
            p = plans.computeIfAbsent(type, t -> new Plan((Class<? extends INetworkEvent>) t));
        return p;
    }

    private static boolean hasCustomSerializer(Class<?> type)
    {
        try
        {
            return type.getMethod("write", ByteBuf.class).getDeclaringClass() != INetworkEvent.class;
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Event has no serializer: " + type, e);
        }
    }

    private static List<Field> discoverFields(Class<?> type, boolean custom)
    {
        ArrayList<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass())
        {
            if (current == PersonalEvent.class)
                continue; // Sender identity comes from the connection, not the event payload.
            for (Field field: current.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic() ||
                    (field.isAnnotationPresent(INetworkEvent.NetworkIgnored.class) && !field.getName().equals("tankMusic")))
                    continue;
                field.setAccessible(true);
                fields.add(field);
            }
        }
        fields.sort((a, b) ->
        {
            int order = Boolean.compare(!isPackedFloat(a, custom), !isPackedFloat(b, custom));
            return order != 0 ? order : a.getName().compareTo(b.getName());
        });
        return Collections.unmodifiableList(fields);
    }

    private static boolean isPackedFloat(Field field, boolean customSerializer)
    {
        return !customSerializer && field.getType() == double.class;
    }

    private static Object prepare(Object value)
    {
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Character ||
            value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float ||
            value instanceof Double || value instanceof Color || value instanceof byte[])
            return value;
        if (value instanceof UUID)
            return Arrays.asList(((UUID) value).getMostSignificantBits(), ((UUID) value).getLeastSignificantBits());
        if (value instanceof ScreenArcadeBonuses.Bonus)
        {
            ScreenArcadeBonuses.Bonus bonus = (ScreenArcadeBonuses.Bonus) value;
            HashMap<String, Object> values = new HashMap<>();
            values.put("name", bonus.name);
            values.put("value", bonus.value);
            values.put("red", bonus.red);
            values.put("green", bonus.green);
            values.put("blue", bonus.blue);
            return values;
        }
        if (value instanceof Enum)
            return ((Enum<?>) value).name();
        if (value instanceof Collection)
        {
            ArrayList<Object> list = new ArrayList<>();
            for (Object item: (Collection<?>) value)
                list.add(prepare(item));
            return list;
        }
        if (value instanceof Map)
        {
            HashMap<Object, Object> map = new HashMap<>();
            for (Map.Entry<?, ?> entry: ((Map<?, ?>) value).entrySet())
                map.put(prepare(entry.getKey()), prepare(entry.getValue()));
            return map;
        }
        throw new IllegalArgumentException("Unsupported event field type " + value.getClass().getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object restore(Object value, Class<?> type)
    {
        if (value == null)
            return null;
        if (type == UUID.class)
        {
            List<?> parts = (List<?>) value;
            if (parts.size() != 2)
                throw new IllegalArgumentException("Invalid network UUID");
            return new UUID(((Number) parts.get(0)).longValue(), ((Number) parts.get(1)).longValue());
        }
        if (type == HashSet.class)
            return new HashSet((Collection<?>) value);
        if (type == ScreenArcadeBonuses.Bonus.class)
        {
            Map<?, ?> fields = (Map<?, ?>) value;
            return new ScreenArcadeBonuses.Bonus((String) fields.get("name"), ((Number) fields.get("value")).intValue(),
                ((Number) fields.get("red")).doubleValue(), ((Number) fields.get("green")).doubleValue(),
                ((Number) fields.get("blue")).doubleValue());
        }
        if (type.isEnum())
            return Enum.valueOf((Class<? extends Enum>) type, (String) value);
        if (type == double.class || type == Double.class)
            return ((Number) value).doubleValue();
        if (type == float.class || type == Float.class)
            return ((Number) value).floatValue();
        if (type == int.class || type == Integer.class)
            return ((Number) value).intValue();
        if (type == long.class || type == Long.class)
            return ((Number) value).longValue();
        return value;
    }
}
