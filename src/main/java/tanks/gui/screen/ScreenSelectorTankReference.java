package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.tank.ITankField;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankReference;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Objects;

public class ScreenSelectorTankReference extends ScreenEditorTanksONable<ITankField>
{
    public static int tankPage;

    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public ArrayList<String> tankNames = new ArrayList<>();

    public Screen screen;
    public String title;
    public String objName;

    Pointer<ITankField> pointer;

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Next page", () -> tankPage++);

    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Previous page", () -> tankPage--);

    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + this.objYSpace * 3.5, 40, 40, "", () -> tankPage = 0);

    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + this.objYSpace * 3.5, 40, 40, "", () -> tankPage = (tankButtons.size() - 1) / objectButtonRows / objectButtonCols);

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 4.5, this.objWidth, this.objHeight, "Done", new Runnable()
    {
        @Override
        public void run()
        {
            onComplete.run();
            Game.screen = screen;
        }
    }
    );

    public Button copy = new Button(this.centerX, this.centerY - this.objYSpace * 2.5, this.objWidth * 1.25, this.objHeight, "Create an editable copy", new Runnable()
    {
        @Override
        public void run()
        {
            Tank t = pointer.get().resolve();
            if (t instanceof TankAIControlled)
            {
                TankAIControlled t1 = new TankAIControlled();
                ((TankAIControlled) t).cloneProperties(t1);
                t = t1;
            }
            pointer.set((ITankField) t);
            ScreenEditorTank s = new ScreenEditorTank(pointer.cast(), screen);
            s.onComplete = onComplete;
            s.objName = objName;

            if (screen instanceof ScreenEditorTanksONable)
                ((ScreenEditorTanksONable<?>)screen).clearMusicTracks();
            Game.screen = s;
        }
    }, "");

    public ScreenSelectorTankReference(String title, Pointer<ITankField> pointer, Screen tankScreen)
    {
        super(pointer, tankScreen);

        this.allowClose = false;
        this.title = "Select %s";
        this.objName = title;
        this.pointer = pointer;

        this.music = tankScreen.music;
        this.musicID = tankScreen.musicID;
        this.screen = tankScreen;

        int rows = objectButtonRows;
        int cols = objectButtonCols;

        this.nextTankPage.enabled = false;
        this.previousTankPage.enabled = false;

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

        int start = pointer.nullable ? 0 : 1;
        int count = Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size() + 1;
        for (int i = start; i < count; i++)
        {
            int index = (i - start) % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 70 + 100 * ((index / cols) % rows);

            TankAIControlled t;

            if (i <= 0)
                t = null;
            else if (i <= Game.registryTank.tankEntries.size())
            {
                Tank tt = Game.registryTank.tankEntries.get(i - 1).getTank(x, y, 0);
                if (tt instanceof TankAIControlled)
                    t = (TankAIControlled) tt;
                else
                    continue;
            }
            else
            {
                t = new TankAIControlled("", 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                Game.currentLevel.customTanks.get(i - 1 - Game.registryTank.tankEntries.size()).cloneProperties(t);
            }

            final TankAIControlled tt = t;

            String desc = "";

            if (t != null)
                desc = t.description;

            Button b;

            if (t != null)
            {
                b = new ButtonObject(t, x, y, 75, 75, () -> pointer.set(new TankReference(t.name)), desc);

                if (desc.isEmpty())
                    b.enableHover = false;
            }
            else
            {
                b = new Button(x, y, 50, 50, "x", () -> pointer.set(null), "None");

                b.fullInfo = true;
                b.textOffsetY = -2.5;

                b.bgColR = 160;
                b.bgColG = 160;
                b.bgColB = 160;

                b.selectedColR = 255;
                b.selectedColG = 0;
                b.selectedColB = 0;

                b.disabledColR = 127;
                b.disabledColG = 0;
                b.disabledColB = 0;

                b.textColR = 255;
                b.textColG = 255;
                b.textColB = 255;
            }

            this.tankNames.add(t == null ? null : t.name);
            this.tankButtons.add(b);
        }
    }

    @Override
    public void setupTabs()
    {

    }

    @Override
    public void update()
    {
        if (copy.hoverTextRaw.isEmpty())
            copy.setHoverText("Creating an editable copy will unlink the tank used---for the %s from the original---tank in the level. This allows you to modify the---%s independently of the tank---in the level.", this.objName, this.objName);

        int pageCount = (this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols);
        if (tankPage > pageCount)
            tankPage = pageCount;

        for (int i = 0; i < this.tankButtons.size(); i++)
        {
            if (i / (this.objectButtonCols * this.objectButtonRows) == tankPage)
            {
                this.tankButtons.get(i).enabled = !Objects.equals(pointer.get() == null ? null : pointer.get().getName(), this.tankNames.get(i));
                this.tankButtons.get(i).update();
            }
        }

        if (nextTankPage.enabled || previousTankPage.enabled)
        {
            nextTankPage.update();
            previousTankPage.update();

            if ((tankButtons.size() - 1) / objectButtonRows / objectButtonCols >= 2)
            {
                lastTankPage.update();
                firstTankPage.update();
            }
        }

        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            this.quit.function.run();
        }

        quit.update();
        copy.update();
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 660);

        quit.draw();

        this.nextTankPage.enabled = ((this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > tankPage);
        this.previousTankPage.enabled = (tankPage > 0);
        this.lastTankPage.enabled = this.nextTankPage.enabled;
        this.firstTankPage.enabled = this.previousTankPage.enabled;
        if (nextTankPage.enabled || previousTankPage.enabled)
        {
            nextTankPage.draw();
            previousTankPage.draw();

            if ((tankButtons.size() - 1) / objectButtonRows / objectButtonCols >= 2)
            {
                lastTankPage.draw();
                firstTankPage.draw();
            }

            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, nextTankPage.posY,
                    Translation.translate("Page %d of %d", (tankPage + 1), (tankButtons.size() / (objectButtonCols * objectButtonRows) + Math.min(1, tankButtons.size() % (objectButtonCols * objectButtonRows)))));

        }

        for (int i = tankButtons.size() - 1; i >= 0; i--)
        {
            if (i / (objectButtonCols * objectButtonRows) == tankPage)
                tankButtons.get(i).draw();
        }

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        if (this.pointer.get() != null)
        {
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.75, "Note: the selected tank will be linked to its original copy, and edits will be shared.");
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.25, "To instead use an unlinked copy, click 'Create an editable copy'.");
            copy.draw();
        }
        else
        {
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.25, "No %s is currently selected.", this.objName);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.75, "Click on a tank to use it as %s.", this.objName);
        }

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, this.title, this.objName);
    }

    @Override
    public void onAttemptClose()
    {
        this.screen.onAttemptClose();
    }
}
