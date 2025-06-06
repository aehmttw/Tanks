package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.ButtonObject;
import tanks.gui.screen.*;
import tanks.tank.TankPlayable;
import tanks.tank.TankPlayer;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;

import java.util.ArrayList;

public class OverlayPlayerBuilds extends ScreenLevelEditorOverlay implements IRenamableScreen, ITankBuildScreen
{
    public ButtonList tankButtons;
    public int rows = 3;
    public int cols = 10;

    public Button addButton;
    
    public static int page = 0;

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + 60 * 3, 350, 40, "Next page", () -> page++);
    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + 60 * 3, 350, 40, "Previous page", () -> page--);
    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + 60 * 3, 40, 40, "", () -> page = 0);
    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + 60 * 3, 40, 40, "", () -> page = (tankButtons.buttons.size() - 1) / rows / cols);

    public String[] reorderFail = new String[]{"The default player tank build must be free!"};

    public Button reorder = new Button(this.centerX - this.objXSpace, this.centerY + 240, 350, 40, "Reorder", new Runnable()
    {
        @Override
        public void run()
        {
            tankButtons.reorder = !tankButtons.reorder;
            if (tankButtons.reorder)
                reorder.setText("Stop reordering");
            else
                reorder.setText("Reorder");
        }
    });

    public Button back = new Button(this.centerX, this.centerY + 240, 350, 40, "Back", this::escape);

    public OverlayPlayerBuilds(Screen previous, ScreenLevelEditor editor)
    {
        super(previous, editor);

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.previousTankPage.image = "icons/back.png";
        this.previousTankPage.imageSizeX = 25;
        this.previousTankPage.imageSizeY = 25;
        this.previousTankPage.imageXOffset = -145;

        this.lastTankPage.image = "icons/last.png";
        this.lastTankPage.imageSizeX = 20;
        this.lastTankPage.imageSizeY = 20;
        this.lastTankPage.imageXOffset = 0;

        this.firstTankPage.image = "icons/first.png";
        this.firstTankPage.imageSizeX = 20;
        this.firstTankPage.imageSizeY = 20;
        this.firstTankPage.imageXOffset = 0;

        this.tankButtons = new ButtonList(new ArrayList<>(), page, 0, 0);
        this.tankButtons.buttonWidth = 75;
        this.tankButtons.buttonHeight = 75;
        this.tankButtons.buttonXSpace = 100;
        this.tankButtons.buttonYSpace = 100;
        this.tankButtons.horizontalLayout = true;
        this.tankButtons.columns = 10;
        this.tankButtons.rows = 3;
        this.tankButtons.yOffset = -30;
        this.tankButtons.controlsYOffset = 20;

        this.tankButtons.fixedLastElements = 1;
        this.tankButtons.setupArrows();
        this.tankButtons.reorderBehavior = (i, j) ->
        {
            this.editor.level.playerBuilds.add(j, this.editor.level.playerBuilds.remove((int) i));
            this.refreshButtons();
        };

        this.refreshButtons();
    }

    public void refreshButtons()
    {
        this.tankButtons.buttons.clear();
        int count = Game.currentLevel.playerBuilds.size();
        for (int i = 0; i <= count; i++)
        {
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            TankPlayer.ShopTankBuild t;

            if (i < count)
            {
                t = Game.currentLevel.playerBuilds.get(i);
            }
            else
            {
                addButton = new Button(x, y, 50, 50, "+",  () ->
                {
                    ScreenAddSavedTankBuild s = new ScreenAddSavedTankBuild(this, this.editor.level.playerBuilds);
                    s.drawBehindScreen = true;
                    Game.screen = s;
                }, "Create a new player tank build!");
                this.tankButtons.buttons.add(addButton);
                addButton.fullInfo = true;

                continue;
            }

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                this.editTank(j);
            }, t.description);

            b.showText = true;

            int p = t.price;
            if (i == 0)
                b.setText("Default build");
            else if (p == 0)
                b.setText("Free!");
            else if (p == 1)
                b.setText("1 coin");
            else
                b.setText("%d coins", p);

            if (t.description.isEmpty())
                b.enableHover = false;

            this.tankButtons.buttons.add(b);
        }
        this.tankButtons.sortButtons();

        addButton.sizeX = 50;
        addButton.sizeY = 50;
    }

    public void editTank(int index)
    {
        Pointer<TankPlayer.ShopTankBuild> p = new ArrayListIndexPointer<>(editor.level.playerBuilds, index);
        ScreenEditorPlayerTankBuild s = new ScreenEditorPlayerTankBuild(p, this);

        if (editor.level.playerBuilds.size() == 1)
            s.delete.enabled = false;

        s.onComplete = this::refreshButtons;
        Game.screen = s;
        editor.modified = true;
    }

    @Override
    public void update()
    {
        tankButtons.fixedFirstElements = 1;
        if (editor.level.playerBuilds.size() > 1 && editor.level.playerBuilds.get(1).price <= 0)
            tankButtons.fixedFirstElements = 0;

        tankButtons.update();
        page = tankButtons.page;

        back.update();
        reorder.update();
        super.update();
    }
    
    @Override
    public void draw()
    {
        super.draw();

        if (Game.screen != this)
            return;

        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 600);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 240, "Player tank builds");

        back.draw();

        reorder.draw();
        tankButtons.draw();

        if (tankButtons.reorder && tankButtons.upButtons.size() > 0 && tankButtons.upButtons.get(1).selected && tankButtons.fixedFirstElements > 0)
            Drawing.drawing.drawTooltip(reorderFail);
    }

    @Override
    public boolean rename(String from, String to)
    {
        if (from.equals(to))
            return true;

        for (TankPlayable t: Game.currentLevel.playerBuilds)
        {
            if (to.equals(t.name))
                return false;
        }

        return true;
    }

    @Override
    public Pointer<TankPlayer.ShopTankBuild> addTank(TankPlayer.ShopTankBuild t, boolean select)
    {
        this.editor.level.playerBuilds.add(t);
        return new ArrayListIndexPointer<>(this.editor.level.playerBuilds, this.editor.level.playerBuilds.size() - 1).cast();
    }

    @Override
    public void removeTank(TankPlayer t)
    {
        this.editor.level.playerBuilds.remove(t);
    }

    @Override
    public void refreshTanks(TankPlayer t)
    {
        this.refreshButtons();
    }

}
