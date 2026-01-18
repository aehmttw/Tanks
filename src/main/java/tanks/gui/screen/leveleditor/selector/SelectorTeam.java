package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.Team;
import tanks.gui.screen.leveleditor.OverlayEditTeam;
import tanks.gui.screen.leveleditor.OverlaySelectTeam;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class SelectorTeam extends SelectorChoice<Team>
{
    public static final String selector_name = "team";
    public static final String player_selector_name = "player_team";

    public SelectorTeam(Field f)
    {
        super(f);

        this.description = team ->
        {
            if (team == null)
                return "Tanks without a team will be---hostile towards all other tanks";
            return null;
        };
        this.addNoneChoice = true;
        this.onEdit = (editor, t) ->
        {
            OverlaySelectTeam s = (OverlaySelectTeam) Game.screen;
            OverlayEditTeam o = new OverlayEditTeam(Game.screen, editor, t);
            Game.screen = o;
            o.onEscape = () ->
            {
                SelectorChoice<Team> sel = this;
                o.previous = new OverlaySelectTeam(s.previous, s.editor, sel);
            };
        };
    }

    @Override
    public String choiceToString(Team choice)
    {
        if (choice == null)
            return "null";

        return choice.name;
    }

    @Override
    public void changeMetadata(ScreenLevelEditor editor, GameObject o, int add)
    {
        updateDefaultChoices(editor, o);

        selectedIndex += add;
        if (Game.currentLevel.enableTeams && editor == null)
            setChoice(editor, o, -1);
        else
            setChoice(editor, o, selectedIndex);
    }

    public void updateDefaultChoices(ScreenLevelEditor editor, GameObject o)
    {
        if (editor != null)
            this.choices = editor.teams;
        else if (Game.currentLevel.enableTeams)
            this.choices = Game.currentLevel.teamsList;
        else if (!Game.currentLevel.disableFriendlyFire)
            this.choices = new ArrayList<>(Arrays.asList(Game.playerTeam, Game.enemyTeam));
        else
            this.choices = new ArrayList<>(Arrays.asList(Game.playerTeamNoFF, Game.enemyTeamNoFF));

        this.selectedIndex = this.choices.indexOf(this.getMetadata(o));
        this.selectedChoice = (Team) this.getMetadata(o);
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor editor)
    {
        updateDefaultChoices(editor, editor.mousePlaceable);
        Game.screen = new OverlaySelectTeam(Game.screen, editor, this);
    }

    @Override
    public String getMetadataDisplayString(GameObject o)
    {
        Object t = this.getMetadata(o);
        if (t == null)
            return "\u00A7127000000255none";
        else
            return t.toString();
    }
}
