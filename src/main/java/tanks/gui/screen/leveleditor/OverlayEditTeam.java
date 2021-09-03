package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.ScreenOptions;

public class OverlayEditTeam extends ScreenLevelBuilderOverlay
{
    public TextBox teamName;
    public Team team;

    public OverlayEditTeam(OverlayLevelOptionsTeams previous, ScreenLevelEditor screenLevelEditor, Team team)
    {
        super(previous, screenLevelEditor);

        this.team = team;

        teamName = new TextBox(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Team name", new Runnable()
        {
            @Override
            public void run()
            {
                boolean duplicate = false;

                for (int i = 0; i < screenLevelEditor.teams.size(); i++)
                {
                    if (teamName.inputText.equals(screenLevelEditor.teams.get(i).name))
                    {
                        duplicate = true;
                        break;
                    }
                }

                if (teamName.inputText.length() <= 0 || duplicate)
                    teamName.inputText = team.name;
                else
                {
                    team.name = teamName.inputText;
                }
            }

        }
                , team.name);

        teamName.lowerCase = true;

        if (team.friendlyFire)
            teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.onText;
        else
            teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.offText;

        if (screenLevelEditor.teams.size() <= 1)
            this.deleteTeam.enabled = false;
    }

    public Button back = new Button(this.centerX, this.centerY + 300, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public Button deleteTeam = new Button(this.centerX, this.centerY + 300 - this.objYSpace, this.objWidth, this.objHeight, "Delete team", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.teams.remove(team);

            for (Movable m: Game.movables)
            {
                if (m.team == team)
                    m.team = null;
            }

            escape();
        }
    }
    );

    public Button teamFriendlyFire = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Friendly fire: on", new Runnable()
    {
        @Override
        public void run()
        {
            team.friendlyFire = !team.friendlyFire;
            if (team.friendlyFire)
                teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.onText;
            else
                teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.offText;
        }
    }
    );

    public Button teamColor = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Team color", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenEditTeamColor(Game.screen, screenLevelEditor, team);
        }
    }
    );

    public void update()
    {
        deleteTeam.update();
        teamName.update();
        teamFriendlyFire.update();
        teamColor.update();
        back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        back.draw();
        teamName.draw();
        teamColor.draw();
        deleteTeam.draw();
        teamFriendlyFire.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, this.team.name);
    }
}
