package tanks.network.event;

import tanks.Drawing;

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
    public void execute()
    {
        if (this.clientID == null && !this.sound.isEmpty())
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
