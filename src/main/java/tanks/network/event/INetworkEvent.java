package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.network.*;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.*;

public interface INetworkEvent extends IEvent
{
    @Retention(RetentionPolicy.RUNTIME)
    @interface NetworkIgnored
    {
    }

    HashMap<Class<? extends INetworkEvent>, ArrayList<NetworkFieldHandle>> fields = new HashMap<>();

    default void write(ByteBuf b)
    {
        for (NetworkFieldHandle f : getFieldHandles())
            f.write(b);
    }

    default void read(ByteBuf b)
    {
        for (NetworkFieldHandle f : getFieldHandles())
            f.read(b);
    }

    void execute();

    default ArrayList<NetworkFieldHandle> getFieldHandles()
    {
        return fields.computeIfAbsent(getClass(), c ->
        {
            ArrayList<NetworkFieldHandle> handles = new ArrayList<>();
            for (Field f : c.getFields())
                handles.add(new NetworkFieldHandle(f, this));
            return handles;
        });
    }
}
