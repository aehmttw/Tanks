package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.network.NetworkUtils;

public class EventPlaySound extends PersonalEvent
{
    public String sound;
    public float pitch;
    public float volume;

    public EventPlaySound()
    {

    }

    public EventPlaySound(String s, float pitch, float volume)
    {
        this.sound = s;
        this.pitch = pitch;
        this.volume = volume;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.sound);
        b.writeFloat(this.pitch);
        b.writeFloat(this.volume);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.sound = NetworkUtils.readString(b);
        this.pitch = b.readFloat();
        this.volume = b.readFloat();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && !this.sound.equals(""))
        {
            try
            {
                Drawing.drawing.playSound(this.sound, this.pitch, this.volume);
            }
            catch (Exception e)
            {
                System.out.println("Invalid sound: " + this.sound);
            }
        }
    }
}
