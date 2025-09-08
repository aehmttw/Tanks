package tanks.network;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.*;
import tanks.network.event.INetworkEvent;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class NetworkFieldHandle
{
    protected static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    // Used for network field validation upon game only. Do not use for anything else, will cause race conditions
    public static Object testObject = null;

    public static class FieldHandle<T>
    {
        protected final Class<T> castType;
        protected Function<ByteBuf, ?> read;
        protected BiConsumer<ByteBuf, T> write;

        protected FieldHandle(Class<T> castType, Function<ByteBuf, ?> readFunc, BiConsumer<ByteBuf, T> writeFunc)
        {
            this.castType = castType;
            this.read = readFunc;
            this.write = writeFunc;
        }

        public T read(ByteBuf b)
        {
            return castType.cast(read.apply(b));
        }

        public void write(ByteBuf b, Object t)
        {
            write.accept(b, (T) t);
        }

        @SuppressWarnings("UnusedReturnValue")
        public static <T> FieldHandle<T> register(Class<T> castType, Function<ByteBuf, ?> read, BiConsumer<ByteBuf, T> write)
        {
            return register(castType, castType, read, write);
        }

        public static <O, T> FieldHandle<T> register(Class<O> originalType, Class<T> castType, Function<ByteBuf, ?> read, BiConsumer<ByteBuf, T> write)
        {
            FieldHandle<T> f = new FieldHandle<>(castType, read, write);
            sigs.put(originalType, f);
            return f;
        }
    }

    static Map<Class<?>, FieldHandle<?>> sigs = new HashMap<>();

    public static boolean shouldCheckField(Field f)
    {
        return !Modifier.isStatic(f.getModifiers()) && !f.isAnnotationPresent(INetworkEvent.NetworkIgnored.class);
    }

    private static double readDouble(ByteBuf b)
    {
        return b.readFloat();
    }

    private static void writeDouble(ByteBuf b, double d)
    {
        b.writeFloat((float) d);
    }

    public static void initialize()
    {
        if (!sigs.isEmpty())
            return;

        FieldHandle.register(int.class, Integer.class, ByteBuf::readInt, ByteBuf::writeInt);
        FieldHandle.register(long.class, Long.class, ByteBuf::readLong, ByteBuf::writeLong);
        FieldHandle.register(float.class, Float.class, ByteBuf::readFloat, ByteBuf::writeFloat);
        FieldHandle.register(double.class, Double.class, NetworkFieldHandle::readDouble, NetworkFieldHandle::writeDouble);  // treat doubles as floats
        FieldHandle.register(boolean.class, Boolean.class, ByteBuf::readBoolean, ByteBuf::writeBoolean);
        FieldHandle.register(String.class, NetworkUtils::readString, NetworkUtils::writeString);
        FieldHandle.register(Color.class, NetworkUtils::readColor, NetworkUtils::writeColor);
        FieldHandle.register(UUID.class, NetworkUtils::readUUID, NetworkUtils::writeUUID);

        // bytebuf takes int for these
        FieldHandle.register(byte.class, int.class, ByteBuf::readByte, ByteBuf::writeByte);
        FieldHandle.register(short.class, int.class, ByteBuf::readShort, ByteBuf::writeShort);
        FieldHandle.register(char.class, int.class, ByteBuf::readChar, ByteBuf::writeChar);
    }

    public Object currentObject;

    protected final MethodHandle read, write;
    protected final FieldHandle<?> fieldHandle;
    protected final Field field;

    public NetworkFieldHandle(Field field)
    {
        try
        {
            this.read = lookup.unreflectGetter(field);
            this.write = lookup.unreflectSetter(field);
            this.fieldHandle = Objects.requireNonNull(sigs.get(field.getType()), "Failed to find field handle for '" + field.getName() + "' of type '" + field.getType() + "' from class '" + field.getDeclaringClass() + "'");
            this.field = field;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(this + ": " + e);
        }
    }

    public void read(ByteBuf b)
    {
        try
        {
            this.write.invoke(currentObject, this.fieldHandle.read(b));
        }
        catch (Throwable e)
        {
            throw new RuntimeException(this + ", " + this.fieldHandle.read(b) + ": " + e);
        }
    }

    public void write(ByteBuf b)
    {
        try
        {
            this.fieldHandle.write(b, this.read.invoke(currentObject));
        }
        catch (Throwable e)
        {
            throw new RuntimeException(this + ", " + e);
        }
    }

    public String toString()
    {
        return currentObject.getClass().getSimpleName() + ": " + this.field.getName();
    }
}
