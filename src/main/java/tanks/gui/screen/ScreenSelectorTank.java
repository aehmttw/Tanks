package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Consumer;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.hotbar.item.ItemBullet;
import tanks.registry.RegistryBullet;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.translation.Translation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ScreenSelectorTank extends Screen implements IConditionalOverlayScreen
{
    public static int tankPage;

    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public boolean drawBehindScreen;

    public Screen screen;

    public String title;
    public Tank currentTank;

    public Button nextTankPage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> tankPage++);

    public Button previousTankPage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> tankPage--);

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = screen;
        }
    }
    );

    public ScreenSelectorTank(String title, Tank tank, Screen tankScreen, ArrayList<TankAIControlled> customTanks, Consumer<TankAIControlled> function, boolean nullTank)
    {
        super(350, 40, 380, 60);

        this.title = title;
        this.allowClose = false;
        this.currentTank = tank;

        this.music = tankScreen.music;
        this.musicID = tankScreen.musicID;
        this.screen = tankScreen;

        int rows = objectButtonRows;
        int cols = objectButtonCols;

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.previousTankPage.image = "icons/back.png";
        this.previousTankPage.imageSizeX = 25;
        this.previousTankPage.imageSizeY = 25;
        this.previousTankPage.imageXOffset = -145;

        int nt = 1;
        if (nullTank)
            nt = 0;

        int count = Game.registryTank.tankEntries.size() + customTanks.size() + 1;
        for (int i = nt; i < count; i++)
        {
            int index = (i - nt) % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            TankAIControlled t;

            if (i <= 0)
                t = null;
            else if (i <= Game.registryTank.tankEntries.size())
            {
                Tank tt = Game.registryTank.tankEntries.get(i - 1).getTank(x, y, 0);
                if (tt instanceof TankAIControlled)
                {
                    t = (TankAIControlled) tt;

                    for (RegistryBullet.BulletEntry e: Game.registryBullet.bulletEntries)
                    {
                        if (e.bullet.equals(t.bullet.bulletClass))
                        {
                            t.bullet.icon = e.image;
                            t.bullet.className = ItemBullet.classMap2.get(t.bullet.bulletClass);
                        }
                    }
                }
                else
                    continue;
            }
            else
            {
                t = new TankAIControlled("", 0, 0, 0, 0, 0, 0, 0, TankAIControlled.ShootAI.none);
                customTanks.get(i - 1 - Game.registryTank.tankEntries.size()).cloneProperties(t);
            }

            final TankAIControlled tt = t;

            String desc = "";

            if (t != null)
                desc = t.description;

            if (i >= Game.registryTank.tankEntries.size() + 1)
            {
                if (t != null && !t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7000255255255Custom tank from this level";
            }

            Button b;

            if (t != null)
            {
                b = new ButtonObject(t, x, y, 75, 75, () ->
                {
                    quit.function.run();
                    function.accept(tt);
                }
                        , desc);

                if (desc.equals(""))
                    b.enableHover = false;
            }
            else
            {
                b = new Button(x, y, 50, 50, "x", () ->
                {
                    quit.function.run();
                    function.accept(tt);
                }, "None");

                b.fullInfo = true;
                b.textOffsetY = -2.5;

                b.unselectedColR = 160;
                b.unselectedColG = 160;
                b.unselectedColB = 160;

                b.selectedColR = 255;
                b.selectedColG = 0;
                b.selectedColB = 0;

                b.textColR = 255;
                b.textColG = 255;
                b.textColB = 255;
            }

            this.tankButtons.add(b);
        }

        BaseFile directory = Game.game.fileManager.getFile(Game.homedir + Game.tankDir);
        if (!directory.exists())
        {
            directory.mkdirs();
        }

        ArrayList<String> files = new ArrayList<>();

        try
        {
            ArrayList<String> ds = directory.getSubfiles();

            for (String p : ds)
            {
                if (p.endsWith(".tanks"))
                    files.add(p);
            }
        }
        catch (IOException e)
        {
            Game.exitToCrash(e);
        }

        Collections.sort(files);

        for (String l: files)
        {
            BaseFile file = Game.game.fileManager.getFile(l);

            int index = (count - nt) % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);
            count++;

            try
            {
                file.startReading();
                String tankStr = file.nextLine();
                file.stopReading();
                TankAIControlled t = TankAIControlled.fromString(tankStr);
                final TankAIControlled tt = t;

                String desc = t.description;
                if (!t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7000255000255Saved custom tank template";

                ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
                {
                    quit.function.run();
                    function.accept(tt);
                }
                        , desc);
                b.text = l;

                if (desc.equals(""))
                    b.enableHover = false;

                this.tankButtons.add(b);
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }
    }

    @Override
    public void update()
    {
        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            Game.screen = screen;
        }

        for (int i = 0; i < this.tankButtons.size(); i++)
        {
            if (i / (this.objectButtonCols * this.objectButtonRows) == tankPage)
                this.tankButtons.get(i).update();
        }

        if ((this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > tankPage)
            nextTankPage.update();

        if (tankPage > 0)
            previousTankPage.update();

        quit.update();
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
        {
            this.enableMargins = screen.enableMargins;
            screen.draw();
        }
        else
            this.drawDefaultBackground();

        if (Game.screen != this)
            return;

        if ((tankButtons.size() - 1) / (objectButtonRows * objectButtonCols) > tankPage)
            nextTankPage.draw();

        if (tankPage > 0)
            previousTankPage.draw();

        for (int i = tankButtons.size() - 1; i >= 0; i--)
        {
            if (i / (objectButtonCols * objectButtonRows) == tankPage)
                tankButtons.get(i).draw();
        }

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, title);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.25, "Note: this will set the property to a copy of the selected tank.");
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.75, "If the original tank is modified, this property will not change.");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX - 40, this.centerY - this.objYSpace * 4, Translation.translate("Current tank:"), true);

        if (this.currentTank != null)
        {
            Drawing.drawing.drawInterfaceText(this.centerX + 30, this.centerY - this.objYSpace * 4, this.currentTank.name, false);
            this.currentTank.drawForInterface(this.centerX, this.centerY - this.objYSpace * 4, 0.5);
        }
        else
        {
            if (Level.isDark())
                Drawing.drawing.setColor(255, 127, 127);
            else
                Drawing.drawing.setColor(127, 0, 0);

            Drawing.drawing.drawInterfaceText(this.centerX + 20, this.centerY - this.objYSpace * 4, Translation.translate("none"), false);
        }

        quit.draw();
    }

    @Override
    public void setupLayoutParameters()
    {

    }

    @Override
    public double getOffsetX()
    {
        if (drawBehindScreen)
            return screen.getOffsetX();
        else
            return super.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        if (drawBehindScreen)
            return screen.getOffsetY();
        else
            return super.getOffsetY();
    }

    @Override
    public double getScale()
    {
        if (drawBehindScreen)
            return screen.getScale();
        else
            return super.getScale();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        if (screen instanceof IConditionalOverlayScreen)
            return ((IConditionalOverlayScreen) screen).isOverlayEnabled();

        return screen instanceof ScreenGame || screen instanceof ILevelPreviewScreen || screen instanceof IOverlayScreen;
    }

    @Override
    public void onAttemptClose()
    {
        this.screen.onAttemptClose();
    }
}
