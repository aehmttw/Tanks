package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;

public class OverlayEditTeam extends ScreenLevelEditorOverlay
{
    public TextBox teamName;
    public Team team;

    public Runnable onEscape = () -> {};

    public OverlayEditTeam(Screen previous, ScreenLevelEditor screenLevelEditor, Team team)
    {
        super(previous, screenLevelEditor);

        this.team = team;

        teamName = new TextBox(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Team name", () ->
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
                , team.name);

        teamFriendlyFire.image = "shield.png";
        teamFriendlyFire.imageXOffset = -teamFriendlyFire.sizeX / 2 + teamFriendlyFire.sizeY / 2;
        teamFriendlyFire.imageYOffset = 1;
        teamFriendlyFire.imageSizeX = 30;
        teamFriendlyFire.imageSizeY = 30;

        teamName.lowerCase = true;

        if (team.friendlyFire)
            teamFriendlyFire.setText("Friendly fire: ", ScreenOptions.onText);
        else
            teamFriendlyFire.setText("Friendly fire: ", ScreenOptions.offText);

        if (screenLevelEditor.teams.size() <= 1)
            this.deleteTeam.enabled = false;
    }

    public Button back = new Button(this.centerX, this.centerY + 300, this.objWidth, this.objHeight, "Back", this::escape);

    public Button deleteTeam = new Button(this.centerX - this.objXSpace, this.centerY + 300, this.objWidth, this.objHeight, "Delete team", new Runnable()
    {
        @Override
        public void run()
        {
            editor.teams.remove(team);

            for (Movable m: Game.movables)
            {
                if (m.team == team)
                    m.team = null;
            }

            escape();
        }
    }
    );

    public Button teamFriendlyFire = new Button(this.centerX, this.centerY , this.objWidth, this.objHeight, "Friendly fire: on", new Runnable()
    {
        @Override
        public void run()
        {
            team.friendlyFire = !team.friendlyFire;
            if (team.friendlyFire)
                teamFriendlyFire.setText("Friendly fire: ", ScreenOptions.onText);
            else
                teamFriendlyFire.setText("Friendly fire: ", ScreenOptions.offText);
        }
    }, "If a team has friendly fire disabled---no tanks on that team will be able to---damage each other"
    );

    public Button teamColor = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Team color", () ->
        {
            Game.screen = new OverlayEditTeamColor(Game.screen, editor, team);
        }
    );

    @Override
    public void escape()
    {
        this.onEscape.run();
        super.escape();
    }

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

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 720, 20, 5);

        back.draw();
        teamName.draw();
        teamColor.draw();
        deleteTeam.draw();
        teamFriendlyFire.draw();

        if (team.enableColor)
        {
            Drawing.drawing.setColor(team.teamColorR, team.teamColorG, team.teamColorB);
            Drawing.drawing.fillInterfaceOval(teamColor.posX - teamColor.sizeX / 2 + teamColor.sizeY / 2, teamColor.posY, teamColor.sizeY * 0.8, teamColor.sizeY * 0.8);
        }

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Edit team");

        if (team.enableColor)
            Drawing.drawing.setColor(team.teamColorR, team.teamColorG, team.teamColorB);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, this.team.name);
    }
}
