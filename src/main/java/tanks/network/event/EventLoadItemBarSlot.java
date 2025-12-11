package tanks.network.event;

import tanks.Game;

import java.util.UUID;

public class EventLoadItemBarSlot extends PersonalEvent
{
    public int slot;
    public UUID targetID;

    public EventLoadItemBarSlot()
    {

    }

    public EventLoadItemBarSlot(UUID clientID, int slot)
    {
        this.slot = slot;
        this.targetID = clientID;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.targetID.equals(Game.clientID))
        {
            Game.player.hotbar.itemBar.selected = slot;
        }
    }
}
