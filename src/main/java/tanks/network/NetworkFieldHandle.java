package tanks.network;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.*;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.util.*;

public class NetworkFieldHandle
{
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public static class FieldHandle<T>
    {
        private final Class<T> castType;
        private final Function<ByteBuf, ?> read;
        private final BiConsumer<ByteBuf, T> write;

        private FieldHandle(Class<T> castType, Function<ByteBuf, ?> read, BiConsumer<ByteBuf, T> write)
        {
            this.castType = castType;
            this.read = read;
            this.write = write;
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

    public static void initialize()
    {
        if (!sigs.isEmpty())
            return;

        FieldHandle.register(int.class, ByteBuf::readInt, ByteBuf::writeInt);
        FieldHandle.register(long.class, ByteBuf::readLong, ByteBuf::writeLong);
        FieldHandle.register(float.class, float.class, ByteBuf::readFloat, ByteBuf::writeFloat);
        FieldHandle.register(double.class, float.class, ByteBuf::readFloat, ByteBuf::writeFloat);   // treat doubles as floats
        FieldHandle.register(boolean.class, ByteBuf::readBoolean, ByteBuf::writeBoolean);
        FieldHandle.register(String.class, NetworkUtils::readString, NetworkUtils::writeString);
        FieldHandle.register(Color.class, NetworkUtils::readColor, NetworkUtils::writeColor);
        FieldHandle.register(UUID.class, NetworkUtils::readUUID, NetworkUtils::writeUUID);

        // bytebuf takes int for these
        FieldHandle.register(byte.class, int.class, ByteBuf::readByte, ByteBuf::writeByte);
        FieldHandle.register(short.class, int.class, ByteBuf::readShort, ByteBuf::writeShort);
        FieldHandle.register(char.class, int.class, ByteBuf::readChar, ByteBuf::writeChar);
    }

    public Object object;
    private final MethodHandle read, write;
    private final FieldHandle<?> fieldHandle;

    public NetworkFieldHandle(Field field, Object o)
    {
        initialize();
        this.object = o;
        try
        {
            this.read = lookup.unreflectGetter(field);
            this.write = lookup.unreflectSetter(field);
            this.fieldHandle = sigs.get(field.getType());
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void read(ByteBuf b)
    {
        try
        {
            this.write.invoke(this.object, this.fieldHandle.read(b));
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public void write(ByteBuf b)
    {
        try
        {
            this.fieldHandle.write(b, this.read.invoke(this.object));
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }
}
