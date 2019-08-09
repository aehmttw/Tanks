package tanks.event;

public interface INetworkEvent extends IEvent
{
	public String getNetworkString();

	public void execute();
}
