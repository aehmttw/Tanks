package tanks.network;

import java.util.ArrayList;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tanks.Game;
import tanks.ScreenHostingEnded;

/**
 * Discards any incoming data.
 */
public class Server
{
	public int port = Game.port;
	public EventLoopGroup bossGroup;
	public EventLoopGroup workerGroup;
	public ArrayList<ServerHandler> connections = new ArrayList<ServerHandler>();

	public Server instance = this;
	public ChannelFuture channel;

	public Server(int port)
	{
		this.port = port;
	}

	public void run() throws Exception 
	{
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
				public void initChannel(SocketChannel ch) throws Exception 
				{
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
		//synchronized(this.connections)
		{
			for (int i = 0; i < this.connections.size(); i++)
			{
				ServerHandler c = this.connections.get(i);
				c.kick(c.ctx, "The host has ended the party");
			}

			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			connections.clear();
		}
	}    
}