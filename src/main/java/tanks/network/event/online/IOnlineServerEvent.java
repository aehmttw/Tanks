package tanks.network.event.online;

import tanks.network.event.INetworkEvent;
import tanksonline.TanksOnlineServerHandler;

public interface IOnlineServerEvent extends INetworkEvent
{
    void execute(TanksOnlineServerHandler s);
}
