package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenOptions;

public class OverlayEditTeamColor extends ScreenLevelEditorOverlay
{
    public Team team;

    public TextBoxSlider teamRed;
    public TextBoxSlider teamGreen;
    public TextBoxSlider teamBlue;

    public Button back = new Button(this.centerX, this.centerY + 300, this.objWidth, this.objHeight, "Back", this::escape
    );

    public Button teamColorEnabled = new Button(this.centerX, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "Team color: off", new Runnable()
    {
        @Override
        public void run()
        {
            team.enableColor = !team.enableColor;
            if (team.enableColor)
                teamColorEnabled.setText("Team color: ", ScreenOptions.onText);
            else
                teamColorEnabled.setText("Team color: ", ScreenOptions.offText);
        }
    }
    );

    public OverlayEditTeamColor(Screen previous, ScreenLevelEditor screenLevelEditor, Team team)
    {
        super(previous, screenLevelEditor);
        this.team = team;

        if (team.enableColor)
            teamColorEnabled.setText("Team color: ", ScreenOptions.onText);
        else
            teamColorEnabled.setText("Team color: ", ScreenOptions.offText);

        teamRed = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Red", () ->
        {
            if (teamRed.inputText.length() <= 0)
                teamRed.inputText = "0";

            team.teamColorR = Integer.parseInt(teamRed.inputText);
        }
                , 0, 0, 255, 1);

        teamRed.allowLetters = false;
        teamRed.allowSpaces = false;
        teamRed.maxChars = 3;
        teamRed.maxValue = 255;
        teamRed.checkMaxValue = true;

        teamGreen = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Green", () ->
        {
            if (teamGreen.inputText.length() <= 0)
                teamGreen.inputText = "0";

            team.teamColorG = Integer.parseInt(teamGreen.inputText);
        }
                , 0, 0, 255, 1);

        teamGreen.allowLetters = false;
        teamGreen.allowSpaces = false;
        teamGreen.maxChars = 3;
        teamGreen.maxValue = 255;
        teamGreen.checkMaxValue = true;

        teamBlue = new TextBoxSlider(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Blue", () ->
        {
            if (teamBlue.inputText.length() <= 0)
                teamBlue.inputText = "0";

            team.teamColorB = Integer.parseInt(teamBlue.inputText);
        }
                , 0, 0, 255, 1);

        teamBlue.allowLetters = false;
        teamBlue.allowSpaces = false;
        teamBlue.maxChars = 3;
        teamBlue.maxValue = 255;
        teamBlue.checkMaxValue = true;

        teamRed.value = team.teamColorR;
        teamGreen.value = team.teamColorG;
        teamBlue.value = team.teamColorB;

        teamRed.inputText = (int) team.teamColorR + "";
        teamGreen.inputText = (int) team.teamColorG + "";
        teamBlue.inputText = (int) team.teamColorB + "";
    }

    public void update()
    {
        back.update();

        if (team.enableColor)
        {
            teamRed.update();
            teamGreen.update();
            teamBlue.update();
        }

        teamColorEnabled.update();

        super.update();
    }

    public void draw()
    {
        super.draw();
        back.draw();

        if (team.enableColor)
        {
            teamRed.r1 = 0;
            teamRed.r2 = 255;
            teamRed.g1 = teamGreen.value;
            teamRed.g2 = teamGreen.value;
            teamRed.b1 = teamBlue.value;
            teamRed.b2 = teamBlue.value;

            teamGreen.r1 = teamRed.value;
            teamGreen.r2 = teamRed.value;
            teamGreen.g1 = 0;
            teamGreen.g2 = 255;
            teamGreen.b1 = teamBlue.value;
            teamGreen.b2 = teamBlue.value;

            teamBlue.r1 = teamRed.value;
            teamBlue.r2 = teamRed.value;
            teamBlue.g1 = teamGreen.value;
            teamBlue.g2 = teamGreen.value;
            teamBlue.b1 = 0;
            teamBlue.b2 = 255;

            teamBlue.draw();
            teamGreen.draw();
            teamRed.draw();
        }

        teamColorEnabled.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Team color: %s", this.team.name);
    }
}
