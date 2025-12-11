package tanks.network.event;

import tanks.*;
import tanks.gui.screen.ScreenGame;

public class EventLevelFinishedQuick extends PersonalEvent
{
    public String winningTeam;

    public EventLevelFinishedQuick()
    {

    }

    public EventLevelFinishedQuick(String winner)
    {
        this.winningTeam = winner;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        if (Game.clientID.toString().equals(winningTeam) || (Game.playerTank != null && Game.playerTank.team != null && Game.playerTank.team.name.equals(this.winningTeam)))
        {
            Panel.win = true;
            Panel.winlose = "Victory!";
            Drawing.drawing.playSound("win.ogg", 1.0f, true);
        }
        else
        {
            Panel.win = false;
            Panel.winlose = "You were destroyed!";
            if (!(Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).isVersus))
                Drawing.drawing.playSound("lose.ogg", 1.0f, true);
        }

        ScreenGame.finishedQuick = true;
    }
}
