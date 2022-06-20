package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlaySelectTeam extends ScreenLevelEditorOverlay
{
    public ArrayList<Button> teamSelectButtons = new ArrayList<>();
    public ButtonList teamSelectList;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape
    );

    public OverlaySelectTeam(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        for (int i = 0; i < screenLevelEditor.teams.size(); i++)
        {
            Team t = screenLevelEditor.teams.get(i);
            int j = i;
            Button buttonToAdd = new Button(0, 0, 350, 40, t.name, () -> screenLevelEditor.setEditorTeam(j)
            );

            teamSelectButtons.add(buttonToAdd);
        }

        Button button = new Button(0, 0, 350, 40, "\u00A7127000000255none", () -> screenLevelEditor.setEditorTeam(screenLevelEditor.teams.size())
        );

        teamSelectButtons.add(button);

        this.teamSelectList = new ButtonList(teamSelectButtons, 0, 0, -30);
    }

    public void update()
    {
        for (Button b: teamSelectButtons)
        {
            b.enabled = true;
        }

        if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
            teamSelectButtons.get(screenLevelEditor.playerTeamNum).enabled = false;
        else if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
            teamSelectButtons.get(screenLevelEditor.teamNum).enabled = false;

        this.teamSelectList.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        String teamSelectTitle = null;

        if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
            teamSelectTitle = "Select tank team";
        else if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
            teamSelectTitle = "Select player team";

        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, teamSelectTitle);

        this.teamSelectList.draw();

        back.draw();
    }
}
