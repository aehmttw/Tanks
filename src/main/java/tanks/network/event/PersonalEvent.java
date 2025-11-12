package tanks.network.event;

import java.util.UUID;

public abstract class PersonalEvent implements INetworkEvent
{
    /** Is set to null when client receives event from server, and to clientID when server receives event from client */
    @NetworkIgnored
	public UUID clientID;
}
