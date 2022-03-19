package tanks.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.UUID;

public class Client 
{
	public static ClientHandler handler;

	public static String currentHost;
    public static int currentPort;

    public static UUID connectionID = null;

    public static void connect(String host, int port, boolean online, UUID connectionID) throws Exception
    {
        currentHost = host;
        currentPort = port;

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try 
        {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() 
            {
                @Override
                public void initChannel(SocketChannel ch)
                {
                	handler = new ClientHandler(online, connectionID);
                    ch.pipeline().addLast(handler);
                }
            });
            
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } 
        finally 
        {
            workerGroup.shutdownGracefully();
        }
    }
}