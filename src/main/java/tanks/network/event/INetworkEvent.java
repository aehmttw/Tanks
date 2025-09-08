package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.network.NetworkFieldHandle;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public interface INetworkEvent extends IEvent
{
    @Retention(RetentionPolicy.RUNTIME)
    @interface NetworkIgnored
    {
    }

    HashMap<Class<? extends INetworkEvent>, ArrayList<NetworkFieldHandle>> classToFields = new HashMap<>();

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
        ArrayList<NetworkFieldHandle> fields = classToFields.computeIfAbsent(getClass(), c ->
        {
            ArrayList<NetworkFieldHandle> handles = new ArrayList<>();
            for (Field f : c.getFields())
            {
                if (NetworkFieldHandle.shouldCheckField(f))
                    handles.add(new NetworkFieldHandle(f));
            }
            return handles;
        });

        for (NetworkFieldHandle f : fields)
            f.currentObject = this;

        NetworkFieldHandle.testObject = this;

        return fields;
    }
}
