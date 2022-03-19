package tanksonline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import tanks.event.EventKick;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.SynchronizedList;

public class TanksOnlineServer
{
    public int port;
    public EventLoopGroup bossGroup;
    public EventLoopGroup workerGroup;
    public SynchronizedList<TanksOnlineServerHandler> connections = new SynchronizedList<>();

    public static TanksOnlineServer instance;
    public ChannelFuture channel;

    public TanksOnlineServer(int port)
    {
        instance = this;
        this.port = port;
    }

    public void run()
    {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>()
                    {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new TanksOnlineServerHandler(instance));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            channel = b.bind(port).sync();

            channel.channel().closeFuture().sync();
        }
        catch (Exception e)
        {
            if (ScreenPartyHost.isServer)
            {
                e.printStackTrace();
            }
        }
        finally
        {
            close();
        }
    }

    public void close()
    {
        this.close("The server you were connected to has closed");
    }

    public void close(String reason)
    {
        synchronized (this.connections)
        {
            for (int i = 0; i < this.connections.size(); i++)
            {
                TanksOnlineServerHandler c = this.connections.get(i);
                c.sendEventAndClose(new EventKick(reason));
            }

            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            connections.clear();
        }
    }
}