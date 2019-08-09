package tanks.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client 
{
	public static ClientHandler handler;
	
    public static void connect(String host, int port) throws Exception 
    {
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
                public void initChannel(SocketChannel ch) throws Exception 
                {
                	handler = new ClientHandler();
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