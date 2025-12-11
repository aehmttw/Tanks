package tanks.network.event;

import tanks.*;

import java.util.UUID;

public class EventUpdateRemainingLives extends PersonalEvent
{
    public UUID playerID;
    public int lives;

    public EventUpdateRemainingLives(Player p)
    {
        this.playerID = p.clientID;
        this.lives = p.remainingLives;
    }

    public EventUpdateRemainingLives()
    {

    }

    @Override
    public void execute()
    {
        if (clientID == null && playerID.equals(Game.clientID))
        {
            Game.player.remainingLives = lives;
        }
    }
}
