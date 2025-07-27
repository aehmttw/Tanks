package tanks.network;

import basewindow.Color;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetworkUtils 
{
	public static final Charset charset = StandardCharsets.UTF_8;
	
	public static String readString(ByteBuf b)
	{
		int l = b.readInt();

		if (l < 0)
			return null;

		return b.readCharSequence(l, charset).toString();
	}
	
	public static void writeString(ByteBuf b, String s)
	{
		int extra = 0;

		if (s == null)
		{
			b.writeInt(-1);
			return;
		}

		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == '\u00A7')
				extra++;
		
		b.writeInt(s.length() + extra);
		b.writeCharSequence(s, charset);
	}

	public static void readColor(ByteBuf b, Color c)
	{
		c.red = b.readDouble();
		c.green = b.readDouble();
		c.blue = b.readDouble();
	}

	public static void writeColor(ByteBuf b, Color c)
	{
		b.writeDouble(c.red);
		b.writeDouble(c.green);
		b.writeDouble(c.blue);
	}
}
