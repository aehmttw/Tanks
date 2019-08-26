package tanks.network;

import java.nio.charset.Charset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import tanks.Game;
import tanks.ScreenPartyLobby;
import tanks.event.IEvent;
import tanks.event.INetworkEvent;

public class ClientHandler extends ChannelInboundHandlerAdapter 
{	
	public String message = "";
	public boolean reading = false;
	public static MessageExecutor executor = new MessageExecutor();
	
	public ChannelHandlerContext ctx;
	
	@Override
    public void channelActive(ChannelHandlerContext ctx)
    {
		String s = "<$" + Game.network_protocol + "." + Game.clientID + "." + Game.username + ">";
		final ByteBuf buffy = Unpooled.wrappedBuffer(s.getBytes());
		ctx.channel().writeAndFlush(buffy);
		this.ctx = ctx;
		ScreenPartyLobby.isClient = true;
    }
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
		ScreenPartyLobby.isClient = false;
    }
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
		ByteBuf buffy = (ByteBuf) msg;

		String s = buffy.toString(Charset.defaultCharset());
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);

			if (c == '<')
				reading = true;
			else if (c == '>')
			{
				reading = false;	
				
				executor.executeMessage(message, null);
				message = "";
			}
			else if (reading)
				message += c;
		}

		ReferenceCountUtil.release(msg);
		
		String reply = "<>";
		for (int i = 0; i < Game.events.size(); i++)
		{
			IEvent e = Game.events.get(i);
			if (e instanceof INetworkEvent)
			{
				INetworkEvent event = (INetworkEvent) e;
				reply += "<" + NetworkEventMap.get(event.getClass()) + "#" + event.getNetworkString() + ">";
			}
		}
		Game.events.clear();
		
		final ByteBuf buffy2 = Unpooled.wrappedBuffer((reply).getBytes());
		ctx.writeAndFlush(buffy2);
		
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}