package tanks.tankson;

import basewindow.Color;
import basewindow.IModel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

public class ObjectBuffer
{
    public enum Type
    {
        // Primatives
        NULL((byte) 0x00),
        FALSE((byte) 0x10),
        TRUE((byte) 0x11),
        INF((byte) 0xFF),
        NINF((byte) 0xFE),

        // Integers
        CHAR((byte) 0xA1),
        SHORT((byte) 0xA2),
        INT((byte) 0xA3),
        LONG((byte) 0xA4),

        // Floats
//        FP8((byte) 0xB0),    Pulling these out for now because they are not used in Java
//        FP16((byte) 0xB1),   Pulling these out for now because they are not used in Java
        FLOAT((byte) 0xB2),
        DOUBLE((byte) 0xB3),

        //Variable Length
        ENUM((byte) 0xC0),
        STRING((byte) 0xC1),
        LIST((byte) 0xC2),
        OBJECT((byte) 0x0B),

        //Tanks Specific
        COLOR((byte) 0x20),
        IModel((byte) 0x21);

        private final byte code;

        Type(byte code)
        {
            this.code = code;
        }

        public byte getCode()
        {
            return this.code;
        }

        public static Type fromByte(byte code)
        {
            for (Type command: Type.values())
            {
                if (command.code == code)
                    return command;
            }
            throw new IllegalArgumentException("Unknown command code: " + code);
        }
    }

    public static Type getType(long tag)
    {
        return Type.fromByte((byte) (tag & 0xFF));
    }

    public static long getId(long tag)
    {
        return tag >> 8;
    }

    public static long getTag(Type type, long id)
    {
        return (id << 8) | (type.getCode() & 0xFFL);
    }

    /** Key for {@link #hash(String)}. Must stay fixed, as hashes are written into serialized files. */
    private static final long hashKey0 = 0x0706050403020100L;
    private static final long hashKey1 = 0x0f0e0d0c0b0a0908L;

    /** Reads up to 8 bytes of m, from start (inclusive) to end (exclusive), as a little endian long. */
    private static long word(byte[] m, int start, int end)
    {
        long w = 0;
        for (int i = end - 1; i >= start; i--)
            w = (w << 8) | (m[i] & 0xFFL);

        return w;
    }

    /** SipHash-2-4 of the UTF-8 bytes of the given id. */
    public static long hash(String id)
    {
        byte[] m = id.getBytes(StandardCharsets.UTF_8);

        long v0 = 0x736f6d6570736575L ^ hashKey0;
        long v1 = 0x646f72616e646f6dL ^ hashKey1;
        long v2 = 0x6c7967656e657261L ^ hashKey0;
        long v3 = 0x7465646279746573L ^ hashKey1;

        int blocks = m.length - (m.length % 8);

        for (int i = 0; i < blocks; i += 8)
        {
            long w = word(m, i, i + 8);

            v3 ^= w;
            for (int r = 0; r < 2; r++)
            {
                // inlined round, as Java has no way to pass these back out of a call
                v0 += v1;
                v1 = Long.rotateLeft(v1, 13);
                v1 ^= v0;
                v0 = Long.rotateLeft(v0, 32);
                v2 += v3;
                v3 = Long.rotateLeft(v3, 16);
                v3 ^= v2;
                v0 += v3;
                v3 = Long.rotateLeft(v3, 21);
                v3 ^= v0;
                v2 += v1;
                v1 = Long.rotateLeft(v1, 17);
                v1 ^= v2;
                v2 = Long.rotateLeft(v2, 32);
            }
            v0 ^= w;
        }

        // the last block is the remaining bytes, padded with zeroes, with the length mod 256 in the top byte
        long last = word(m, blocks, m.length) | (((long) m.length & 0xFF) << 56);

        v3 ^= last;
        for (int r = 0; r < 2; r++)
        {
            v0 += v1;
            v1 = Long.rotateLeft(v1, 13);
            v1 ^= v0;
            v0 = Long.rotateLeft(v0, 32);
            v2 += v3;
            v3 = Long.rotateLeft(v3, 16);
            v3 ^= v2;
            v0 += v3;
            v3 = Long.rotateLeft(v3, 21);
            v3 ^= v0;
            v2 += v1;
            v1 = Long.rotateLeft(v1, 17);
            v1 ^= v2;
            v2 = Long.rotateLeft(v2, 32);
        }
        v0 ^= last;

        v2 ^= 0xFF;
        for (int r = 0; r < 4; r++)
        {
            v0 += v1;
            v1 = Long.rotateLeft(v1, 13);
            v1 ^= v0;
            v0 = Long.rotateLeft(v0, 32);
            v2 += v3;
            v3 = Long.rotateLeft(v3, 16);
            v3 ^= v2;
            v0 += v3;
            v3 = Long.rotateLeft(v3, 21);
            v3 ^= v0;
            v2 += v1;
            v1 = Long.rotateLeft(v1, 17);
            v1 ^= v2;
            v2 = Long.rotateLeft(v2, 32);
        }

        return (v0 ^ v1 ^ v2 ^ v3) << 8 >> 8;
    }

    public static byte[] toBytes(Object o)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream b = new DataOutputStream(bytes);

        try
        {
            writeTo(b, o);
        }
        catch (IOException e)
        {
            // a ByteArrayOutputStream never actually throws, but DataOutputStream declares it
            throw new RuntimeException(e);
        }

        return bytes.toByteArray();
    }

    private static void writeTo(DataOutputStream b, Object o) throws IOException
    {
        if (o instanceof HashMap)
        {
            HashMap<?, ?> h = (HashMap<?, ?>) o;
            ArrayList<Long> keys = new ArrayList<>((Collection<? extends Long>) h.keySet());
            if (keys.remove(hash("name")))
                keys.add(0, hash("name"));

            if (keys.remove(hash("obj_type")))
                keys.add(0, hash("obj_type"));

            for (long el: keys)
            {
                Object v = h.get(el);

                if (v == null)
                {
                    b.writeLong(getTag(Type.NULL, el));
                }
                else if (v instanceof Boolean)
                {
                    b.writeLong(getTag(((boolean) v) ? Type.TRUE : Type.FALSE, el));
                }
                else if (v instanceof Character)
                {
                    b.writeLong(getTag(Type.CHAR, el));
                    b.writeChar((char) v);
                }
                else if (v instanceof Short)
                {
                    b.writeLong(getTag(Type.SHORT, el));
                    b.writeShort((short) v);
                }
                else if (v instanceof Integer)
                {
                    b.writeLong(getTag(Type.INT, el));
                    b.writeInt((int) v);
                }
                else if (v instanceof Long)
                {
                    b.writeLong(getTag(Type.LONG, el));
                    b.writeLong((long) v);
                }
                else if (v instanceof Float)
                {
                    b.writeLong(getTag(Type.FLOAT, el));
                    b.writeFloat((float) v);
                }
                else if (v instanceof Double)
                {
                    if (((Double) v).isInfinite())
                        b.writeLong(getTag(Type.INF, el));
                    else
                    {
                        b.writeLong(getTag(Type.DOUBLE, el));
                        b.writeDouble((double) v);
                    }
                }
                else if (v instanceof String)
                {
                    b.writeLong(getTag(Type.STRING, el));
                    b.write(((String) v).getBytes(StandardCharsets.UTF_16));
                    b.writeByte(0);
                    b.writeByte(0);
                }
                else if (v instanceof Enum)
                {
                    b.writeLong(getTag(Type.ENUM, el));
                    b.write(v.toString().getBytes(StandardCharsets.UTF_16));
                    b.writeByte(0);
                    b.writeByte(0);
                }
                else if (v instanceof IModel)
                {
                    b.writeLong(getTag(Type.IModel, el));
                    b.write(v.toString().getBytes(StandardCharsets.UTF_16));
                    b.writeLong(0L);
                }
                else if (v instanceof AbstractCollection)
                {
                    b.writeLong(getTag(Type.LIST, el));
                    writeTo(b, v);
                }
                else if (v instanceof Color)
                {
                    Color c = ((Color) v);
                    b.writeLong(getTag(Type.COLOR, el));
                    b.writeDouble(c.red);
                    b.writeDouble(c.green);
                    b.writeDouble(c.blue);
                    b.writeDouble(c.alpha);
                }
                else if (v instanceof HashMap)
                {
                    b.writeLong(getTag(Type.OBJECT, el));
                    writeTo(b, v);
                }
            }
            b.writeLong(0L);
        }
        else if (o instanceof AbstractCollection)
        {
            AbstractCollection<?> c = (AbstractCollection<?>) o;
            long counter = 0;
            for (Object el: c)
            {
                if (el == null)
                {
                    b.writeLong(getTag(Type.NULL, counter));
                }
                else if (el instanceof Boolean)
                {
                    b.writeLong(getTag(((boolean) el) ? Type.TRUE : Type.FALSE, counter));
                }
                else if (el instanceof Character)
                {
                    b.writeLong(getTag(Type.CHAR, counter));
                    b.writeChar((char) el);
                }
                else if (el instanceof Short)
                {
                    b.writeLong(getTag(Type.SHORT, counter));
                    b.writeShort((short) el);
                }
                else if (el instanceof Integer)
                {
                    b.writeLong(getTag(Type.INT, counter));
                    b.writeInt((int) el);
                }
                else if (el instanceof Long)
                {
                    b.writeLong(getTag(Type.LONG, counter));
                    b.writeLong((long) el);
                }
                else if (el instanceof Float)
                {
                    b.writeLong(getTag(Type.FLOAT, counter));
                    b.writeFloat((float) el);
                }
                else if (el instanceof Double)
                {
                    if (((Double) el).isInfinite())
                        b.writeLong(getTag(Type.INF, counter));
                    else
                    {
                        b.writeLong(getTag(Type.DOUBLE, counter));
                        b.writeDouble((double) el);
                    }
                }
                else if (el instanceof String)
                {
                    b.writeLong(getTag(Type.STRING, counter));
                    b.write(((String) el).getBytes(StandardCharsets.UTF_16));
                    b.writeShort(0);
                }
                else if (el instanceof Enum)
                {
                    b.writeLong(getTag(Type.ENUM, counter));
                    b.write(el.toString().getBytes(StandardCharsets.UTF_16));
                    b.writeShort(0);
                }
                else if (el instanceof IModel)
                {
                    b.writeLong(getTag(Type.IModel, counter));
                    b.write(el.toString().getBytes(StandardCharsets.UTF_16));
                    b.writeShort(0);
                }
                else if (el instanceof AbstractCollection)
                {
                    b.writeLong(getTag(Type.LIST, counter));
                    writeTo(b, el);
                }
                else if (el instanceof Color)
                {
                    Color col = ((Color) el);
                    b.writeLong(getTag(Type.COLOR, counter));
                    b.writeDouble(col.red);
                    b.writeDouble(col.green);
                    b.writeDouble(col.blue);
                    b.writeDouble(col.alpha);
                }
                else if (el instanceof HashMap)
                {
                    b.writeLong(getTag(Type.OBJECT, counter));
                    writeTo(b, el);
                }
                counter++;
            }
            b.writeLong(0L);
        }
        else
        {
            throw new RuntimeException("Unable to convert Object of type: " + o.getClass().getName());
        }
    }

    protected static class ParserState
    {
        ByteBuffer buffer;
        int depth;

        public ParserState(byte[] buffer)
        {
            depth = 0;
            this.buffer = ByteBuffer.wrap(buffer);
        }

        public static HashMap<Long, Object> parse(ParserState state)
        {
            HashMap<Long, Object> result = new HashMap<>();
            while (state.buffer.hasRemaining())
            {
                Map.Entry<Long, Object> p = state.parse();
                if (p.getKey() == 0 && p.getValue() == null)
                    break;
                result.put(p.getKey(), p.getValue());
            }
            return result;
        }

        public Map.Entry<Long, Object> parse()
        {
            long tag = buffer.getLong();
            long id = getId(tag);
            switch (getType(tag))
            {
                //Primatives
                case NULL:
                {
                    if (getId(tag) == 0)
                        depth--;
                    return new SimpleImmutableEntry<>(id, null);
                }
                case TRUE:
                    return new SimpleImmutableEntry<>(id, true);
                case FALSE:
                    return new SimpleImmutableEntry<>(id, false);
                case INF:
                    return new SimpleImmutableEntry<>(id, Double.POSITIVE_INFINITY);

                //Integers
                case CHAR:
                    return new SimpleImmutableEntry<>(id, buffer.getChar());
                case SHORT:
                    return new SimpleImmutableEntry<>(id, buffer.getShort());
                case INT:
                    return new SimpleImmutableEntry<>(id, buffer.getInt());
                case LONG:
                    return new SimpleImmutableEntry<>(id, buffer.getLong());

                //Float
                case FLOAT:
                    return new SimpleImmutableEntry<>(id, buffer.getFloat());
                case DOUBLE:
                    return new SimpleImmutableEntry<>(id, buffer.getDouble());

                //Variable Length
                case ENUM:
                {
                    StringBuilder sb = new StringBuilder();
                    while (buffer.hasRemaining())
                    {
                        byte b1 = buffer.get();
                        byte b2 = buffer.get();
                        if (b1 == 0 && b2 == 0)
                            break;
                        sb.append(new String(new byte[]{b1, b2}, StandardCharsets.UTF_16));
                    }
                    return new SimpleImmutableEntry<>(id, sb.toString());
                }
                case STRING:
                {
                    StringBuilder sb = new StringBuilder();
                    while (buffer.hasRemaining())
                    {
                        byte b1 = buffer.get();
                        byte b2 = buffer.get();
                        if (b1 == 0 && b2 == 0)
                            break;
                        sb.append(new String(new byte[]{b1, b2}, StandardCharsets.UTF_16));
                    }
                    return new SimpleImmutableEntry<>(id, sb.toString());
                }
                case OBJECT:
                {
                    HashMap<Long, Object> h = new HashMap<>();
                    int target = depth++;
                    while (depth > target)
                    {
                        Map.Entry<Long, Object> kv = parse();
                        long k = kv.getKey();
                        Object v = kv.getValue();
                        if (k == 0 && v == null && depth == target)
                            break;
                        h.put(k, v);
                    }
                    return new SimpleImmutableEntry<>(id, h);
                }
                case LIST:
                {
                    ArrayList<Object> a = new ArrayList<>();
                    int target = depth++;
                    while (depth > target)
                    {
                        Map.Entry<Long, Object> kv = parse();
                        long k = kv.getKey();
                        Object v = kv.getValue();
                        if (k == 0 && v == null && depth == target)
                            break;
                        a.add(kv.getValue());
                    }
                    return new SimpleImmutableEntry<>(id, a);
                }

                //Tanks Specific Objects
                case COLOR:
                {
                    Color c = new Color(buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
                    return new SimpleImmutableEntry<>(id, c);
                }
                case IModel:
                {
                    StringBuilder sb = new StringBuilder();
                    while (buffer.hasRemaining())
                    {
                        byte b1 = buffer.get();
                        byte b2 = buffer.get();
                        if (b1 == 0 && b2 == 0)
                            break;
                        sb.append(new String(new byte[]{b1, b2}, StandardCharsets.UTF_16));
                    }
                    return new SimpleImmutableEntry<>(id, sb.toString());
                }

                default:
                    throw new RuntimeException("Unknown type: " + getType(tag));
            }

        }
    }

    public static HashMap<Long, Object> parse(byte[] buffer)
    {
        return ParserState.parse(new ParserState(buffer));
    }
}
