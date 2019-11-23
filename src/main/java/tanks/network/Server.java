package tanks.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tanks.Game;
import tanks.event.EventKick;
import tanks.gui.screen.ScreenHostingEnded;

import java.util.ArrayList;

public class Server
{
	public int port;
	public EventLoopGroup bossGroup;
	public EventLoopGroup workerGroup;
	public SynchronizedList<ServerHandler> connections = new SynchronizedList<ServerHandler>();

	public Server instance = this;
	public ChannelFuture channel;

	public Server(int port)
	{
		this.port = port;
	}

	public void run() {
		bossGroup = new NioEventLoopGroup(); // (1)
		workerGroup = new NioEventLoopGroup();

		try
		{
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class) // (3)
			.childHandler(new ChannelInitializer<SocketChannel>()
			{ // (4)
				@Override
				public void initChannel(SocketChannel ch) {
					ch.pipeline().addLast(new ServerHandler(instance));
				}
			})
			.option(ChannelOption.SO_BACKLOG, 128)          // (5)
			.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

			// Bind and start to accept incoming connections.
			channel = b.bind(port).sync(); // (7)

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to gracefully
			// shut down your server.
			channel.channel().closeFuture().sync();
		}
		catch (Exception e)
		{
			e.printStackTrace(Game.logger);
			e.printStackTrace();
			Game.screen = new ScreenHostingEnded(e.getLocalizedMessage());
		}
		finally 
		{
			close();
		}
	}

	public void close()
	{  
		this.close("The host has ended the party");
	}
	
	public void close(String reason)
	{    	
		synchronized(this.connections)
		{
			for (int i = 0; i < this.connections.size(); i++)
			{
				ServerHandler c = this.connections.get(i);
				c.sendEventAndClose(new EventKick(reason));
			}

			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			connections.clear();
		}
	}    
}