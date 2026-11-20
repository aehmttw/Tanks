package tanks.network.event;

import tanks.Game;
import tanks.Panel;

public class EventSyncMusic extends PersonalEvent
{
    public long offset = 0;

    public EventSyncMusic()
    {
    }

    public EventSyncMusic(long offset)
    {
        this.offset = offset;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            if (Panel.menuMusicStartTime < 0 && Game.syncPartyMusic)
            {
                Panel.menuMusicStartTime = System.currentTimeMillis() - offset;
                Game.eventsOut.add(new EventSyncMusic(offset));
            }
        }
        else
            Panel.menuMusicStartTime = System.currentTimeMillis() - offset;
    }
}
