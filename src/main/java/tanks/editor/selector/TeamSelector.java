package tanks.editor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.Team;
import tanks.gui.screen.leveleditor.OverlayEditTeam;
import tanks.tank.Tank;

import java.util.ArrayList;
import java.util.Arrays;

public class TeamSelector<T extends GameObject> extends ChoiceSelector<T, Team>
{
    /**
     * The default selected team, by default the index of the enemy team.<br>
     * It is changed when placing player spawns, in which it defaults to the index of the ally team.
     * <br><br>
     * If <code>choices.size()</code> <= <code>defaultTeamIndex</code>,
     * the chosen team is the last one in the choice list.
     */
    public int defaultTeamIndex = 1;

    @Override
    public void init()
    {
        this.id = "team";
        this.title = "Select " + (gameObject instanceof Tank ? "tank" : "obstacle") + " team";
        this.objectProperty = "team";

        this.keybind = Game.game.input.editorTeam;
        this.image = "team.png";
        this.description = team ->
        {
            if (team == null)
                return "Tanks without a team will be---hostile towards all other tanks";
            return null;
        };
        this.addNoneChoice = true;
        this.onEdit = t -> Game.screen = new OverlayEditTeam(Game.screen, editor, t);

        updateDefaultChoices();

        if (Game.currentLevel.enableTeams && editor == null)
            setChoice(-1, false);
        else
            setChoice(Math.min(this.choices.size() - 1, defaultTeamIndex), false);
    }

    @Override
    public void load()
    {
        updateDefaultChoices();

        if (this.selectedChoice != null)
            this.button.setText("Team: ", this.selectedChoice.name);
        else
            this.button.setText("No team");
    }

    @Override
    public String choiceToString(Team choice)
    {
        if (choice == null)
            return "null";

        return choice.name;
    }

    public void updateDefaultChoices()
    {
        if (editor != null)
            this.choices = editor.teams;
        else if (Game.currentLevel.enableTeams)
            this.choices = Game.currentLevel.teamsList;
        else if (!Game.currentLevel.disableFriendlyFire)
            this.choices = new ArrayList<>(Arrays.asList(Game.playerTeam, Game.enemyTeam));
        else
            this.choices = new ArrayList<>(Arrays.asList(Game.playerTeamNoFF, Game.enemyTeamNoFF));
    }
}
