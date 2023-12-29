package tanks.network.event;

import tanks.network.ClientHandler;

public interface IClientThreadEvent
{
	void execute(ClientHandler s);
}
