package tanks.tankson;

import basewindow.Color;
import basewindow.IModel;
import tanks.Game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A compact binary form of the maps {@link Serializer#toMap} builds, for network transfers and saved files.
 * <br><br>
 * A buffer opens with a header: the ObjectBuffer {@link #VERSION}, the TanksON version, the size of the
 * {@link PropertyIds} table it was written with, and an addendum of any property ids that weren't in that
 * table (such as ones added by extensions), numbered on from the end of it. Then comes a single value.
 * <br><br>
 * Every value is a {@link Type} byte followed by its payload. An object is a map with an obj_type key,
 * and writes its keys as their numbers in the table. Any other map writes its keys as values of their own.
 * Objects, maps and lists are prefixed with their number of entries, and UTF-8 strings with their length in bytes.
 */
public class ObjectBuffer
{
    /** Bumped by hand whenever the encoding changes. The property id table versions itself by its size. */
    public static final int VERSION = 1;

    /** Deeper nesting than this is treated as corrupt rather than risking a stack overflow on network data. */
    public static final int MAX_DEPTH = 256;

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
        MAP((byte) 0xC3),
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
            throw new IllegalArgumentException("Unknown type code: 0x" + Integer.toHexString(code & 0xFF));
        }
    }

    /** Whether a map is written as an object, with its keys numbered by the property id table. */
    public static boolean isObject(Map<?, ?> m)
    {
        if (!(m.get("obj_type") instanceof String))
            return false;

        for (Object k: m.keySet())
        {
            if (!(k instanceof String))
                return false;
        }

        return true;
    }

    public static byte[] toBytes(Object o)
    {
        Writer body = new Writer();
        body.writeValue(o);

        Writer header = new Writer();
        header.writeVarInt(VERSION);

        String[] tanksONVersion = Serializer.TANKSON_VERSION.split("\\.");
        header.writeVarInt(Integer.parseInt(tanksONVersion[0]));
        header.writeVarInt(Integer.parseInt(tanksONVersion[1]));

        header.writeVarInt(body.tableSize);
        header.writeVarInt(body.addendum.size());
        for (String id: body.addendum)
            header.writeString(id);

        byte[] h = header.toByteArray();
        byte[] b = body.toByteArray();
        byte[] out = Arrays.copyOf(h, h.length + b.length);
        System.arraycopy(b, 0, out, h.length, b.length);
        return out;
    }

    public static Object parse(byte[] bytes)
    {
        try
        {
            Reader r = new Reader(bytes);

            int version = r.readVarInt();
            if (version > VERSION)
                throw new RuntimeException("Unknown ObjectBuffer version " + version + ". You may be running an older version of Tanks with a newer game file.");

            String tanksONVersion = r.readVarInt() + "." + r.readVarInt();
            if (Game.compareVersions(tanksONVersion, Serializer.TANKSON_VERSION) > 0)
                throw new RuntimeException("Unknown TanksON Version " + tanksONVersion + ". You may be running an older version of Tanks with a newer game file.");

            r.tableSize = r.readVarInt();
            if (r.tableSize > PropertyIds.size())
                throw new RuntimeException("ObjectBuffer has " + r.tableSize + " property ids, but this version of Tanks only knows " + PropertyIds.size() +
                    ". You may be running an older version of Tanks with a newer game file.");

            r.addendum = new String[r.readCount()];
            for (int i = 0; i < r.addendum.length; i++)
                r.addendum[i] = r.readString();

            Object o = r.readValue(0);

            if (r.buffer.hasRemaining())
                throw new RuntimeException("ObjectBuffer has " + r.buffer.remaining() + " unread bytes at the end");

            return o;
        }
        catch (BufferUnderflowException e)
        {
            throw new RuntimeException("ObjectBuffer ended unexpectedly, it may be truncated", e);
        }
    }

    protected static class Writer
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream b = new DataOutputStream(bytes);

        /** Property ids missing from the table, in the order they were given numbers. */
        ArrayList<String> addendum = new ArrayList<>();
        HashMap<String, Integer> addendumNumbers = new HashMap<>();

        int tableSize = PropertyIds.size();

        public byte[] toByteArray()
        {
            return bytes.toByteArray();
        }

        public int getNumber(String id)
        {
            int num = PropertyIds.toNumber(id);
            if (num >= 0)
                return num;

            Integer added = addendumNumbers.get(id);
            if (added == null)
            {
                added = tableSize + addendum.size();
                addendum.add(id);
                addendumNumbers.put(id, added);
            }

            return added;
        }

        public void writeValue(Object v)
        {
            try
            {
                if (v == null)
                    writeType(Type.NULL);
                else if (v instanceof Boolean)
                    writeType((boolean) v ? Type.TRUE : Type.FALSE);
                else if (v instanceof Character)
                {
                    writeType(Type.CHAR);
                    b.writeChar((char) v);
                }
                else if (v instanceof Short)
                {
                    writeType(Type.SHORT);
                    b.writeShort((short) v);
                }
                else if (v instanceof Integer)
                {
                    writeType(Type.INT);
                    writeVarLong(zigzag((int) v));
                }
                else if (v instanceof Long)
                {
                    writeType(Type.LONG);
                    writeVarLong(zigzag((long) v));
                }
                else if (v instanceof Float)
                {
                    writeType(Type.FLOAT);
                    b.writeFloat((float) v);
                }
                else if (v instanceof Double)
                {
                    double d = (double) v;
                    if (d == Double.POSITIVE_INFINITY)
                        writeType(Type.INF);
                    else if (d == Double.NEGATIVE_INFINITY)
                        writeType(Type.NINF);
                    else
                    {
                        writeType(Type.DOUBLE);
                        b.writeDouble(d);
                    }
                }
                else if (v instanceof String)
                {
                    writeType(Type.STRING);
                    writeString((String) v);
                }
                else if (v instanceof Enum)
                {
                    writeType(Type.ENUM);
                    writeString(((Enum<?>) v).name());
                }
                else if (v instanceof IModel)
                {
                    writeType(Type.IModel);
                    writeString(v.toString());
                }
                else if (v instanceof Color)
                {
                    Color c = (Color) v;
                    writeType(Type.COLOR);
                    b.writeDouble(c.red);
                    b.writeDouble(c.green);
                    b.writeDouble(c.blue);
                    b.writeDouble(c.alpha);
                }
                else if (v instanceof Map)
                {
                    Map<?, ?> m = (Map<?, ?>) v;
                    if (isObject(m))
                    {
                        writeType(Type.OBJECT);
                        writeVarInt(m.size());
                        for (Map.Entry<?, ?> e: m.entrySet())
                        {
                            writeVarInt(getNumber((String) e.getKey()));
                            writeValue(e.getValue());
                        }
                    }
                    else
                    {
                        writeType(Type.MAP);
                        writeVarInt(m.size());
                        for (Map.Entry<?, ?> e: m.entrySet())
                        {
                            writeValue(e.getKey());
                            writeValue(e.getValue());
                        }
                    }
                }
                else if (v instanceof Collection)
                {
                    Collection<?> c = (Collection<?>) v;
                    writeType(Type.LIST);
                    writeVarInt(c.size());
                    for (Object el: c)
                        writeValue(el);
                }
                else
                    throw new RuntimeException("Unable to write an object of type " + v.getClass().getName() + " to an ObjectBuffer: " + v);
            }
            catch (IOException e)
            {
                // a ByteArrayOutputStream never actually throws, but DataOutputStream declares it
                throw new RuntimeException(e);
            }
        }

        public void writeType(Type t) throws IOException
        {
            b.writeByte(t.getCode());
        }

        /** UTF-8, as most of what's written (ids, names) is ASCII, which takes a byte per character. */
        public void writeString(String s)
        {
            byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
            writeVarInt(utf8.length);
            bytes.write(utf8, 0, utf8.length);
        }

        public void writeVarInt(int i)
        {
            writeVarLong(i & 0xFFFFFFFFL);
        }

        /** Unsigned LEB128: seven bits per byte, low bits first, with the top bit set on all but the last byte. */
        public void writeVarLong(long l)
        {
            while ((l & ~0x7FL) != 0)
            {
                bytes.write((int) ((l & 0x7F) | 0x80));
                l >>>= 7;
            }

            bytes.write((int) l);
        }

        /** Maps signed numbers to unsigned ones so small negative numbers stay short as varints. */
        public static long zigzag(long l)
        {
            return (l << 1) ^ (l >> 63);
        }
    }

    protected static class Reader
    {
        ByteBuffer buffer;

        /** Size of the property id table the buffer was written with, which is where its addendum starts. */
        int tableSize;
        String[] addendum;

        public Reader(byte[] bytes)
        {
            this.buffer = ByteBuffer.wrap(bytes);
        }

        public String getId(int num)
        {
            if (num < tableSize)
                return PropertyIds.toId(num);

            if (num - tableSize < addendum.length)
                return addendum[num - tableSize];

            throw new RuntimeException("Property number " + num + " is in neither the property id table nor the ObjectBuffer's addendum");
        }

        public Object readValue(int depth)
        {
            if (depth > MAX_DEPTH)
                throw new RuntimeException("ObjectBuffer is nested more than " + MAX_DEPTH + " levels deep");

            Type type = Type.fromByte(buffer.get());
            switch (type)
            {
                //Primatives
                case NULL:
                    return null;
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                case INF:
                    return Double.POSITIVE_INFINITY;
                case NINF:
                    return Double.NEGATIVE_INFINITY;

                //Integers
                case CHAR:
                    return buffer.getChar();
                case SHORT:
                    return buffer.getShort();
                case INT:
                    return (int) unzigzag(readVarLong());
                case LONG:
                    return unzigzag(readVarLong());

                //Floats
                case FLOAT:
                    return buffer.getFloat();
                case DOUBLE:
                    return buffer.getDouble();

                //Variable Length
                case ENUM:
                case STRING:
                case IModel:
                    return readString();
                case LIST:
                {
                    int count = readCount();
                    ArrayList<Object> a = new ArrayList<>(count);
                    for (int i = 0; i < count; i++)
                        a.add(readValue(depth + 1));

                    return a;
                }
                case MAP:
                {
                    int count = readCount();
                    LinkedHashMap<Object, Object> m = new LinkedHashMap<>();
                    for (int i = 0; i < count; i++)
                    {
                        Object k = readValue(depth + 1);
                        m.put(k, readValue(depth + 1));
                    }

                    return m;
                }
                case OBJECT:
                {
                    int count = readCount();
                    HashMap<String, Object> m = new HashMap<>();
                    for (int i = 0; i < count; i++)
                    {
                        String k = getId(readVarInt());
                        m.put(k, readValue(depth + 1));
                    }

                    return m;
                }

                //Tanks Specific
                case COLOR:
                    return new Color(buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), buffer.getDouble());

                default:
                    throw new RuntimeException("Unknown type: " + type);
            }
        }

        public String readString()
        {
            int length = readCount();

            // Decoded whole rather than piece by piece, so multi-byte characters such as emoji stay together
            byte[] utf8 = new byte[length];
            buffer.get(utf8);
            return new String(utf8, StandardCharsets.UTF_8);
        }

        /**
         * A length or number of entries. Every entry takes at least a byte, so a count larger than what's left
         * of the buffer can only come from corrupt data, and is refused before anything is allocated for it.
         */
        public int readCount()
        {
            int count = readVarInt();
            if (count < 0 || count > buffer.remaining())
                throw new RuntimeException("ObjectBuffer is corrupt: a count of " + count + " with only " + buffer.remaining() + " bytes left");

            return count;
        }

        public int readVarInt()
        {
            long l = readVarLong();
            if (l > 0xFFFFFFFFL)
                throw new RuntimeException("ObjectBuffer is corrupt: varint too large for an int");

            return (int) l;
        }

        public long readVarLong()
        {
            long l = 0;
            for (int shift = 0; shift < 64; shift += 7)
            {
                byte b = buffer.get();
                l |= (long) (b & 0x7F) << shift;

                if ((b & 0x80) == 0)
                    return l;
            }

            throw new RuntimeException("ObjectBuffer is corrupt: varint longer than 10 bytes");
        }

        public static long unzigzag(long l)
        {
            return (l >>> 1) ^ -(l & 1);
        }
    }
}
