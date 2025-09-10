package tanks.network;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import tanks.*;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.*;

import java.util.*;

public class ServerHandler extends NetworkHandler
{
	protected ArrayList<Integer> queuedEventIndices = new ArrayList<>();
	protected ArrayList<INetworkEvent> queuedEvents = new ArrayList<>();

	public Server server;

	public boolean initialized = false;
    public UUID clientID;

	public Player player;
	public String rawUsername;
	public String username;

	public long lastPingSent;
	public long lastLatency;
	public boolean pingReceived = true;

	public ServerHandler(Server s)
	{
		this.server = s;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		this.ctx = ctx;

		if (ctx != null && !Game.enableIPConnections)
			this.sendEventAndClose(new EventKick("This party is not accepting new players connecting by IP address"));

		if (ctx != null)
			this.reader.queue = ctx.channel().alloc().buffer();
		else
			this.reader.queue = Unpooled.buffer();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx)
	{
		if (steamID == null)
			ReferenceCountUtil.release(this.reader.queue);

		server.connections.remove(this);

		if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.includedPlayers.remove(this.clientID);
			ScreenPartyHost.disconnectedPlayers.add(this.clientID);

			if (this.clientID != null)
			{
				Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));
				Game.eventsOut.add(new EventAnnounceConnection(new ConnectedPlayer(this.clientID, this.rawUsername), false));
				Game.eventsOut.add(new EventChat("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));
				Game.eventsOut.add(new EventPlaySound("leave.ogg", 1.0f, 1.0f));

				Game.eventsIn.add(new EventPlaySound("leave.ogg", 1.0f, 1.0f));
				ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + this.username + " has left the party\u00A7000000000255"));
			}
		}
    }

	/**
	 * Queues an event to be added, to be sent exactly after all the events currently
	 * in Game.eventsOut
     */
	public void queueEvent(INetworkEvent e)
	{
		this.queuedEvents.add(e);
		this.queuedEventIndices.add(Game.eventsOut.size());
	}

	public void addEvents(ArrayList<INetworkEvent> events)
	{
		synchronized (this.events)
		{
			int j = 0;
			for (int i = 0; i < events.size(); i++)
			{
				while (j < this.queuedEventIndices.size() && this.queuedEventIndices.get(j) == i)
				{
					this.events.add(this.queuedEvents.get(j));
					j++;
				}

				this.events.add(events.get(i));
			}

			while (j < this.queuedEventIndices.size())
			{
				this.events.add(this.queuedEvents.get(j));
				j++;
			}

			this.queuedEvents.clear();
			this.queuedEventIndices.clear();
		}
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        if (closed)
            return;

        this.ctx = ctx;
        this.reader.queueMessage(this, ((ByteBuf) msg), this.clientID);

        if (steamID == null)
            ReferenceCountUtil.release(msg);
    }

    @Override
    public void reply()
    {
        super.reply();
        if (pingReceived && System.currentTimeMillis() - lastPingSent > 1000)
        {
            pingReceived = false;
            lastPingSent = System.currentTimeMillis();
            this.stackEvent(new EventPing(false));
        }
    }
}
