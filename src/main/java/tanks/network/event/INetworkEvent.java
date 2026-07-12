package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.network.NetworkUtils;
import tanks.network.ReflectionHandle;

import java.lang.annotation.*;
import java.util.UUID;

public interface INetworkEvent extends IEvent
{
    ReflectionHandle<ByteBuf> handle = new ReflectionHandle<ByteBuf>()
        .setFieldFilter(f -> !f.isAnnotationPresent(NetworkIgnored.class))
        .registerTypeHandle(int.class, Integer.class, ByteBuf::readInt, ByteBuf::writeInt)
        .registerTypeHandle(long.class, Long.class, ByteBuf::readLong, ByteBuf::writeLong)
        .registerTypeHandle(float.class, Float.class, ByteBuf::readFloat, ByteBuf::writeFloat)
        .registerTypeHandle(double.class, Double.class, INetworkEvent::readDouble, INetworkEvent::writeDouble)
        .registerTypeHandle(boolean.class, Boolean.class, ByteBuf::readBoolean, ByteBuf::writeBoolean)
        .registerTypeHandle(String.class, NetworkUtils::readString, NetworkUtils::writeString)
        .registerTypeHandle(Color.class, INetworkEvent::readColor, NetworkUtils::writeColor)
        .registerTypeHandle(UUID.class, INetworkEvent::readUUID, INetworkEvent::writeUUID);

    static double readDouble(ByteBuf b)
    {
        return b.readFloat();
    }

    static void writeDouble(ByteBuf b, double d)
    {
        b.writeFloat((float) d);
    }

    static UUID readUUID(ByteBuf b)
    {
        String s = NetworkUtils.readString(b);
        if (s == null)
            return null;
        return UUID.fromString(s);
    }

    static void writeUUID(ByteBuf b, UUID uuid)
    {
        NetworkUtils.writeString(b, uuid != null ? uuid.toString() : null);
    }

    static Color readColor(ByteBuf b)
    {
        Color c = new Color();
        NetworkUtils.readColor(b, c);
        return c;
    }

    default void write(ByteBuf b)
    {
        handle.writeObject(b, this);
    }

    default void read(ByteBuf b)
    {
        handle.readObject(b, this);
    }

    void execute();

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NetworkIgnored
    {
    }
}
