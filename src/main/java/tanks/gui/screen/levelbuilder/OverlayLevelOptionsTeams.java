package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;

import java.util.ArrayList;

public class OverlayLevelOptionsTeams extends ScreenLevelBuilderOverlay
{
    public ArrayList<Button> teamEditButtons = new ArrayList<Button>();
    public ButtonList teamEditList;

    public Button back = new Button(this.centerX - 190, this.centerY + 300, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public Button newTeam = new Button(this.centerX + 190, this.centerY + 300, 350, 40, "New team", new Runnable()
    {
        @Override
        public void run()
        {
            Team t = new Team(System.currentTimeMillis() + "");
            screenLevelBuilder.teams.add(t);
            Game.screen = new OverlayEditTeam((OverlayLevelOptionsTeams) Game.screen, screenLevelBuilder, t);
        }
    }
    );

    public OverlayLevelOptionsTeams(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);
        this.load();
    }

    public void load()
    {
        this.teamEditButtons.clear();
        for (int i = 0; i < screenLevelBuilder.teams.size(); i++)
        {
            Team t = screenLevelBuilder.teams.get(i);
            Button buttonToAdd = new Button(0, 0, 350, 40, t.name, new Runnable()
            {
                @Override
                public void run()
                {
                    Game.screen = new OverlayEditTeam((OverlayLevelOptionsTeams) Game.screen, screenLevelBuilder, t);
                }
            }
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
        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 270, "Teams");
    }
}
