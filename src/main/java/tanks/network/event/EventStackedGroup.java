package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.network.NetworkEventMap;

import java.util.ArrayList;

public class EventStackedGroup extends PersonalEvent
{
    public Class<? extends INetworkEvent> eventCls;
    public ArrayList<INetworkEvent> events = new ArrayList<>();

    public EventStackedGroup() {}
    public EventStackedGroup(Class<? extends INetworkEvent> c)
    {
        this.eventCls = c;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(NetworkEventMap.get(this.eventCls));
        b.writeInt(this.events.size());
        for (INetworkEvent e : this.events)
            e.write(b);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.eventCls = NetworkEventMap.get(b.readInt());
        int size = b.readInt();
        for (int i = 0; i < size; i++)
        {
            try
            {
                INetworkEvent e = this.eventCls.getConstructor().newInstance();
                e.read(b);
                this.events.add(e);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void execute()
    {
        for (INetworkEvent e : this.events)
            e.execute();
    }
}
