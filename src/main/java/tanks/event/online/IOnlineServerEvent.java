package tanks.event.online;

import tanks.event.INetworkEvent;
import tanksonline.TanksOnlineServerHandler;

public interface IOnlineServerEvent extends INetworkEvent
{
    void execute(TanksOnlineServerHandler s);
}
