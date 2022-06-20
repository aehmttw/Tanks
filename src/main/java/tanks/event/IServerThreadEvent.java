package tanks.event;

import tanks.network.ServerHandler;

public interface IServerThreadEvent 
{
	void execute(ServerHandler s);
}
