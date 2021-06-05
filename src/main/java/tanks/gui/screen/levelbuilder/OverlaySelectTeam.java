package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlaySelectTeam extends ScreenLevelBuilderOverlay
{
    public ArrayList<Button> teamSelectButtons = new ArrayList<Button>();
    public ButtonList teamSelectList;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public OverlaySelectTeam(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

        for (int i = 0; i < screenLevelBuilder.teams.size(); i++)
        {
            Team t = screenLevelBuilder.teams.get(i);
            int j = i;
            Button buttonToAdd = new Button(0, 0, 350, 40, t.name, new Runnable()
            {
                @Override
                public void run()
                {
                    screenLevelBuilder.setEditorTeam(j);
                }
            }
            );

            teamSelectButtons.add(buttonToAdd);
        }

        Button button = new Button(0, 0, 350, 40, "\u00A7127000000255none", new Runnable()
        {
            @Override
            public void run()
            {
                screenLevelBuilder.setEditorTeam(screenLevelBuilder.teams.size());
            }
        }
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

        if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
            teamSelectButtons.get(screenLevelBuilder.playerTeamNum).enabled = false;
        else if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.enemyTank)
            teamSelectButtons.get(screenLevelBuilder.teamNum).enabled = false;

        this.teamSelectList.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        String teamSelectTitle = null;

        if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.enemyTank)
            teamSelectTitle = "Select tank team";
        else if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
            teamSelectTitle = "Select player team";

        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 270, teamSelectTitle);

        this.teamSelectList.draw();

        back.draw();
    }
}
