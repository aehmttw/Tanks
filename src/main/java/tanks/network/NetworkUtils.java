package tanks.network;

import basewindow.Color;
import tanks.tankson.TanksONDocument;

import io.netty.buffer.ByteBuf;

import java.nio.charset.*;

public class NetworkUtils
{
    public static final Charset charset = StandardCharsets.UTF_8;

    public static String readString(ByteBuf b)
    {
        return readString(b, b.readInt());
    }

    private static String readString(ByteBuf b, int length)
    {
        if (length == -1)
            return null;

        checkLength(b, length);
        return b.readCharSequence(length, charset).toString();
    }

    public static void writeString(ByteBuf b, String s)
    {
        if (s == null)
        {
            b.writeInt(-1);
            return;
        }

        byte[] bytes = s.getBytes(charset);
        b.writeInt(bytes.length);
        b.writeBytes(bytes);
    }

    /** Only structured game data uses this codec; chat, names and other strings stay literal. */
    public static void writeTanksON(ByteBuf b, String s)
    {
        if (s != null)
        {
            byte[] compact = TanksONDocument.encode(s);
            if (compact != null && compact.length + 4 < s.getBytes(charset).length)
            {
                b.writeInt(-2);
                b.writeInt(compact.length);
                b.writeBytes(compact);
                return;
            }
        }
        writeString(b, s);
    }

    public static String readTanksON(ByteBuf b)
    {
        int length = b.readInt();
        if (length != -2)
            return readString(b, length);

        length = b.readInt();
        checkLength(b, length);
        byte[] bytes = new byte[length];
        b.readBytes(bytes);
        return TanksONDocument.decode(bytes);
    }

    private static void checkLength(ByteBuf b, int length)
    {
        if (length < 0 || length > MessageReader.max_event_size || length > b.readableBytes())
            throw new IllegalArgumentException("Invalid network string length: " + length);
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
