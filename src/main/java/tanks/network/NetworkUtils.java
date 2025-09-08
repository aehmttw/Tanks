package tanks.network;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.*;

import java.nio.charset.*;
import java.util.*;

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

    public static void writeUUID(ByteBuf b, UUID u)
    {
        NetworkUtils.writeString(b, u == null ? null : u.toString());
    }

    public static UUID readUUID(ByteBuf b)
    {
        String s = NetworkUtils.readString(b);
        return s == null ? null : UUID.fromString(s);
    }

    public static <T> void writeCollection(ByteBuf b, Collection<T> l, BiConsumer<ByteBuf, T> write)
    {
        b.writeInt(l.size());
        for (T s: l)
            write.accept(b, s);
    }

    public static <T, V extends Collection<T>> void readCollection(ByteBuf b, V list, Function<ByteBuf, T> read)
    {
        int size = b.readInt();
        for (int i = 0; i < size; i++)
            list.add(read.apply(b));
    }

	public static Color readColor(ByteBuf b)
	{
        Color c = new Color();
		c.red = b.readDouble();
		c.green = b.readDouble();
		c.blue = b.readDouble();
        return c;
	}

	public static void writeColor(ByteBuf b, Color c)
	{
        b.writeDouble(c.red);
		b.writeDouble(c.green);
		b.writeDouble(c.blue);
	}
}
