package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;

public class EventSetMusic extends PersonalEvent
{
    public String music = "";
    public float volume;
    public boolean looped;
    public String id = "";
    public long fadeTime;

    public EventSetMusic()
    {

    }

    public EventSetMusic(String sound, float volume, boolean looped, String id, long fadeTime)
    {
        this.music = sound;
        this.volume = volume;
        this.looped = looped;
        this.id = id;
        this.fadeTime = fadeTime;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, music);
        b.writeFloat(volume);
        b.writeBoolean(looped);
        NetworkUtils.writeString(b, id);
        b.writeLong(fadeTime);
    }

    @Override
    public void read(ByteBuf b)
    {
        music = NetworkUtils.readString(b);
        volume = b.readFloat();
        looped = b.readBoolean();
        id = NetworkUtils.readString(b);
        fadeTime = b.readLong();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null)
        {
            if (music.equals(""))
                Drawing.drawing.stopMusic();
            else
                Drawing.drawing.playMusic(music, volume, looped, id, fadeTime);
        }
    }
}
