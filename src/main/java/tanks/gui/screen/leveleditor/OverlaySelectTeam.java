package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.leveleditor.selector.SelectorChoice;

public class OverlaySelectTeam extends OverlaySelectChoice<Team>
{
    public Button newTeam = new Button(this.centerX + 380, this.centerY + 300, 350, 40, "New team", () ->
    {
        Team t = new Team(System.currentTimeMillis() + "");
        editor.teams.add(t);

        OverlaySelectTeam s = (OverlaySelectTeam) Game.screen;
        OverlayEditTeam o = new OverlayEditTeam(Game.screen, editor, t);
        Game.screen = o;
        o.onEscape = () ->
        {
            SelectorChoice<Team> sel = (SelectorChoice<Team>) this.selector;
            o.previous = new OverlaySelectTeam(s.previous, s.editor, sel);
        };
    });

    public Button reorder = new Button(this.centerX - 380, this.centerY + 300, this.objWidth, this.objHeight, "Reorder teams", new Runnable()
    {
        @Override
        public void run()
        {
            selector.buttonList.reorder = !selector.buttonList.reorder;
            editor.modified = true;

            if (selector.buttonList.reorder)
                reorder.setText("Stop reordering");
            else
                reorder.setText("Reorder teams");
        }
    }
    );

    public OverlaySelectTeam(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorChoice<Team> selector)
    {
        super(previous, screenLevelEditor, selector);
        this.selector.buttonList.setupArrows();

        this.selector.buttonList.reorderBehavior = (i, j) ->
        {
            this.editor.teams.add(j, this.editor.teams.remove((int)i));
            this.selector.buttonList.buttons.add(j, this.selector.buttonList.buttons.remove((int) i));
            this.selector.buttonList.sortButtons();

            if (this.selector.selectedIndex == i)
                this.selector.selectedIndex = j;
            else if (this.selector.selectedIndex == j)
                this.selector.selectedIndex = i;
        };
    }

    @Override
    public void draw()
    {
        super.draw();

        int pageCount = this.selector.buttonList.rows * this.selector.buttonList.columns;
        for (int i = this.selector.buttonList.page * pageCount; i < (this.selector.buttonList.page + 1) * pageCount; i++)
        {
            if (i >= this.selector.choices.size())
                continue;

            Button b = this.choiceButtons.get(i);

            Team t = (Team) this.selector.choices.get(i);
            if (t.enableColor)
            {
                Drawing.drawing.setColor(t.teamColor);
                Drawing.drawing.fillInterfaceOval(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY, b.sizeY * 0.8, b.sizeY * 0.8);
            }

            Drawing.drawing.setColor(255, 255, 255, 200);
            if (!t.friendlyFire)
                Drawing.drawing.drawInterfaceImage( "shield.png", b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY + 2, 25, 25);
        }

        this.reorder.draw();
        this.newTeam.draw();
    }

    @Override
    public void update()
    {
        super.update();
        this.reorder.update();
        this.newTeam.update();
    }

    @Override
    public void escape()
    {
        this.selector.setChoice(editor, editor.mousePlaceable, this.selector.selectedIndex);
        super.escape();
    }
}
