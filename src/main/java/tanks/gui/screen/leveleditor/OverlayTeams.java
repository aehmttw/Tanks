package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlayTeams extends ScreenLevelEditorOverlay
{
    public ArrayList<Button> teamSelectButtons = new ArrayList<>();
    public ButtonList teamSelectList;

    /** If set, will refresh team buttons the next time this screen is updated */
    public boolean refreshOnUpdate = false;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape);

    public Button editTeam = new Button(0, 0, back.sizeY * 0.8, back.sizeY * 0.8, "", () ->
    {
        Game.screen = new OverlayEditTeam(this, this.screenLevelEditor, this.screenLevelEditor.getEditorTeam());
        this.refreshOnUpdate = true;

    }, "Edit team");

    public Button newTeam = new Button(this.centerX + 380, this.centerY + 300, 350, 40, "New team", () ->
    {
        Team t = new Team(System.currentTimeMillis() + "");
        screenLevelEditor.teams.add(t);
        Game.screen = new OverlayEditTeam(Game.screen, screenLevelEditor, t);
        this.refreshOnUpdate = true;
    }
    );

    public Button reorderLevels = new Button(this.centerX - 380, this.centerY + 300, this.objWidth, this.objHeight, "Reorder teams", new Runnable()
    {
        @Override
        public void run()
        {
            teamSelectList.reorder = !teamSelectList.reorder;

            if (teamSelectList.reorder)
                reorderLevels.setText("Stop reordering");
            else
                reorderLevels.setText("Reorder teams");
        }
    }
    );

    public OverlayTeams(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
        this.setup();
    }

    public void setup()
    {
        this.teamSelectButtons.clear();
        for (int i = 0; i < screenLevelEditor.teams.size(); i++)
        {
            Team t = screenLevelEditor.teams.get(i);
            int j = i;
            Button buttonToAdd = new Button(0, 0, 350, 40, t.name, () -> screenLevelEditor.setEditorTeam(j)
            );

            teamSelectButtons.add(buttonToAdd);
        }

        Button button = new Button(0, 0, 350, 40, "\u00A7127000000255none", () -> screenLevelEditor.setEditorTeam(screenLevelEditor.teams.size()), "Tanks without a team will be---hostile towards all other tanks");

        teamSelectButtons.add(button);

        this.teamSelectList = new ButtonList(teamSelectButtons, 0, 0, -30);
        this.teamSelectList.manualDarkMode = true;
        this.teamSelectList.page = screenLevelEditor.getEditorTeamNum() / this.teamSelectList.rows / this.teamSelectList.columns;
        this.teamSelectList.fixedLastElements = 1;
        this.teamSelectList.setupArrows();

        this.teamSelectList.reorderBehavior = (i, j) ->
        {
            int page = this.teamSelectList.page;
            this.screenLevelEditor.teams.add(j, this.screenLevelEditor.teams.remove((int)i));
            this.setup();
            this.teamSelectList.page = page;
            this.teamSelectList.reorder = true;
        };

        this.musicInstruments = true;

        this.editTeam.image = "icons/pencil.png";
        this.editTeam.imageSizeX = 20;
        this.editTeam.imageSizeY = 20;
        this.editTeam.fullInfo = true;
    }

    public void update()
    {
        if (this.refreshOnUpdate)
            this.setup();

        this.refreshOnUpdate = false;

        for (Button b: teamSelectButtons)
        {
            b.enabled = true;
        }

        teamSelectButtons.get(screenLevelEditor.getEditorTeamNum()).enabled = false;

        this.teamSelectList.update();
        this.back.update();

        int selectedTeam = screenLevelEditor.getEditorTeamNum();
        int pageCount = this.teamSelectList.rows * this.teamSelectList.columns;
        if (!teamSelectList.reorder && selectedTeam < screenLevelEditor.teams.size() && selectedTeam >= this.teamSelectList.page * pageCount && selectedTeam < (teamSelectList.page + 1) * pageCount)
        {
            Button b = this.teamSelectButtons.get(selectedTeam);
            this.editTeam.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
            this.editTeam.posY = b.posY;
            this.editTeam.update();
        }

        reorderLevels.update();
        newTeam.update();
        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1200, 720);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1180, 700);
//        Drawing.drawing.drawPopup(centerX, centerY + 15,1200, 750, 10, 5);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        String teamSelectTitle = null;

        if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
            teamSelectTitle = "Select tank team";
        else if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
            teamSelectTitle = "Select player team";

        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, teamSelectTitle);

        this.teamSelectList.draw();

        int selectedTeam = screenLevelEditor.getEditorTeamNum();
        int pageCount = this.teamSelectList.rows * this.teamSelectList.columns;
        if (!teamSelectList.reorder && selectedTeam < screenLevelEditor.teams.size() && selectedTeam >= this.teamSelectList.page * pageCount && selectedTeam < (teamSelectList.page + 1) * pageCount)
        {
            Button b = this.teamSelectButtons.get(selectedTeam);
            this.editTeam.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
            this.editTeam.posY = b.posY;
            this.editTeam.draw();
        }

        for (int i = this.teamSelectList.page * pageCount; i < (teamSelectList.page + 1) * pageCount; i++)
        {
            if (i >= screenLevelEditor.teams.size())
                continue;

            Button b = this.teamSelectButtons.get(i);

            Team t = screenLevelEditor.teams.get(i);
            if (t.enableColor)
            {
                Drawing.drawing.setColor(t.teamColorR, t.teamColorG, t.teamColorB);
                Drawing.drawing.fillInterfaceOval(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY, b.sizeY * 0.8, b.sizeY * 0.8);
            }

            Drawing.drawing.setColor(255, 255, 255, 200);
            if (!t.friendlyFire)
                Drawing.drawing.drawInterfaceImage( "shield.png", b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY + 2, 25, 25);
        }

        reorderLevels.draw();
        back.draw();
        newTeam.draw();
    }
}
