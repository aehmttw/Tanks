package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenEditorPlayerTankBuild;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.util.ArrayList;

public class OverlayPlayerBuilds extends ScreenLevelEditorOverlay
{
    public ArrayList<Button> tankButtons = new ArrayList<>();
    public int rows = 3;
    public int cols = 10;
    
    public static int page = 0;

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + 60 * 3, 350, 40, "Next page", () -> page++);
    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + 60 * 3, 350, 40, "Previous page", () -> page--);
    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + 60 * 3, 40, 40, "", () -> page = 0);
    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + 60 * 3, 40, 40, "", () -> page = (tankButtons.size() - 1) / rows / cols);

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

        int count = Game.currentLevel.playerBuilds.size();
        for (int i = 0; i <= count; i++)
        {
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            Tank t;

            if (i < count)
            {
                t = Game.currentLevel.playerBuilds.get(i);
                t.emblemR = t.secondaryColorR;
                t.emblemG = t.secondaryColorG;
                t.emblemB = t.secondaryColorB;
            }
            else
            {
                Button b = new Button(x, y, 50, 50, "+", "Create a new player tank build! (not yet implemented)");
                this.tankButtons.add(b);
                b.fullInfo = true;

                continue;
            }

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                Pointer<TankPlayer> p = new ArrayListIndexPointer<>(editor.level.playerBuilds, j);
                ScreenEditorPlayerTankBuild s = new ScreenEditorPlayerTankBuild(p, this);
                s.onComplete = () ->
                {
                    if (p.get() == null)
                        this.editor.level.playerBuilds.remove(j);
                };
                Game.screen = s;
                editor.modified = true;
            }
                    , t.description);

            if (t.description.isEmpty())
                b.enableHover = false;

            this.tankButtons.add(b);
        }
    }

    @Override
    public void update()
    {
        for (int i = 0; i < this.tankButtons.size(); i++)
        {
            Button b = this.tankButtons.get(i);

            if (i / (this.cols * this.rows) == page)
                b.update();
        }

        nextTankPage.enabled = (this.tankButtons.size() - 1) / (this.rows * this.cols) > page;
        previousTankPage.enabled = page > 0;

        if (this.tankButtons.size() > this.rows * this.cols)
        {
            nextTankPage.update();
            previousTankPage.update();

            if ((tankButtons.size() - 1) / rows / cols >= 2)
            {
                lastTankPage.update();
                firstTankPage.update();
            }
        }

        back.update();
        super.update();
    }
    
    @Override
    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 600);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 240, "Player tank builds");

        back.draw();

        if (this.tankButtons.size() > this.rows * this.cols)
        {
            nextTankPage.draw();
            previousTankPage.draw();

            if ((tankButtons.size() - 1) / rows / cols >= 2)
            {
                lastTankPage.draw();
                firstTankPage.draw();
            }

            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, nextTankPage.posY,
                    Translation.translate("Page %d of %d", (page + 1), (tankButtons.size() / (cols * rows) + Math.min(1, tankButtons.size() % (cols * rows)))));

        }

        for (int i = tankButtons.size() - 1; i >= 0; i--)
        {
            if (i / (cols * rows) == page)
                tankButtons.get(i).draw();
        }
    }
}
