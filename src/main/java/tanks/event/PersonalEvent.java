package tanks.event;

import java.util.UUID;

public abstract class PersonalEvent implements INetworkEvent
{
	public UUID clientID;
}
