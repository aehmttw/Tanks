package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlayLevelOptionsTeams extends ScreenLevelEditorOverlay
{
    public ArrayList<Button> teamEditButtons = new ArrayList<>();
    public ButtonList teamEditList;

    public Button back = new Button(this.centerX - 190, this.centerY + 300, 350, 40, "Back", this::escape);

    public Button newTeam = new Button(this.centerX + 190, this.centerY + 300, 350, 40, "New team", () ->
    {
        Team t = new Team(System.currentTimeMillis() + "");
        screenLevelEditor.teams.add(t);
        Game.screen = new OverlayEditTeam(Game.screen, screenLevelEditor, t);
    }
    );

    public OverlayLevelOptionsTeams(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
        this.load();
    }

    public void load()
    {
        this.teamEditButtons.clear();
        for (int i = 0; i < screenLevelEditor.teams.size(); i++)
        {
            Team t = screenLevelEditor.teams.get(i);
            Button buttonToAdd = new Button(0, 0, 350, 40, t.name, () -> Game.screen = new OverlayEditTeam((OverlayLevelOptionsTeams) Game.screen, screenLevelEditor, t)
            );

            teamEditButtons.add(buttonToAdd);
        }

        this.teamEditList = new ButtonList(teamEditButtons, 0, 0, -30);
    }

    public void update()
    {
        this.teamEditList.update();

        back.update();
        newTeam.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        this.teamEditList.draw();

        back.draw();
        newTeam.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Teams");
    }
}
