package tanks.gui.screen.leveleditor;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;

import java.util.ArrayList;

public class OverlayLevelOptionsTeams extends ScreenLevelEditorOverlay
{
    public ArrayList<Button> teamEditButtons = new ArrayList<>();
    public ButtonList teamEditList;

    public Button back = new Button(this.centerX - 190, this.centerY + 300, 350, 40, "Back", this::escape
    );

    public Button newTeam = new Button(this.centerX + 190, this.centerY + 300, 350, 40, "New team", () -> {
        Game.screen = new OverlayGenerateTeams(Game.screen, this.screenLevelEditor);

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT))
            ((OverlayGenerateTeams) Game.screen).generate.function.run();
    });

    public Button evenSplit = new Button(this.centerX, this.centerY + 250, 350, 40, "", new Runnable() {
        @Override
        public void run() {
            screenLevelEditor.level.evenSplit = !screenLevelEditor.level.evenSplit;

            evenSplit.setText("Even Split: ", (screenLevelEditor.level.evenSplit ? ScreenOptions.onText : ScreenOptions.offText));
        }
    }, "Split players evenly---into teams");

    public OverlayLevelOptionsTeams(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
        this.load();

        evenSplit.setText("Even Split: ", (screenLevelEditor.level.evenSplit ? ScreenOptions.onText : ScreenOptions.offText));
    }

    public void load()
    {
        this.teamEditButtons.clear();
        for (int i = 0; i < screenLevelEditor.teams.size(); i++)
        {
            Team t = screenLevelEditor.teams.get(i);
            Button buttonToAdd = new Button(0, 0, 350, 40, t.name, () -> Game.screen = new OverlayEditTeam(Game.screen, screenLevelEditor, t)
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
        evenSplit.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        this.teamEditList.draw();

        back.draw();
        newTeam.draw();
        evenSplit.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, "Teams");
    }
}
