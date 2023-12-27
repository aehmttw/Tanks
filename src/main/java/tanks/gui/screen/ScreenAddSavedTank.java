package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.hotbar.item.ItemBullet;
import tanks.registry.RegistryBullet;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ScreenAddSavedTank extends Screen implements IConditionalOverlayScreen
{
    public static int tankPage;

    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public int levelTankCount = 0;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public boolean drawBehindScreen;

    public ITankScreen tankScreen;

    public boolean deleting = false;

    public boolean removeNow = false;

    public Runnable drawDelete = () -> this.delete.draw();

    public Button nextTankPage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> tankPage++);

    public Button previousTankPage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> tankPage--);

    public Button quit = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ((Screen) tankScreen);
        }
    }
    );

    public Button deleteMode = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Delete templates", new Runnable()
    {
        @Override
        public void run()
        {
            deleting = !deleting;

            if (deleting)
                deleteMode.setText("Stop deleting");
            else
                deleteMode.setText("Delete templates");

            for (Button b: tankButtons)
                b.enabled = !deleting;
        }
    }
    );

    public Button delete = new Button(0, 0, 32, 32, "x", () -> removeNow = true);

    public ScreenAddSavedTank(ITankScreen tankScreen)
    {
        super(350, 40, 380, 60);

        this.allowClose = false;

        this.music = ((Screen) tankScreen).music;
        this.musicID = ((Screen) tankScreen).musicID;
        this.tankScreen = tankScreen;

        int rows = objectButtonRows;
        int cols = objectButtonCols;

        int count = Game.registryTank.tankEntries.size();
        for (int i = 0; i < count; i++)
        {
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            TankAIControlled t = null;

            if (i < Game.registryTank.tankEntries.size())
            {
                Tank tt = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);
                if (tt instanceof TankAIControlled)
                {
                    t = (TankAIControlled) tt;
                    HashSet<String> m = Game.registryTank.tankMusics.get(t.name);

                    if (m != null)
                        t.musicTracks.addAll(m);

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

            final TankAIControlled tt = t;

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                TankAIControlled clone = tt.instantiate(tt.name, tt.posX, tt.posY, tt.angle);
                clone.name = System.currentTimeMillis() + "";
                ScreenTankEditor s = new ScreenTankEditor(clone, tankScreen);
                s.drawBehindScreen = true;
                Game.screen = s;
                tankScreen.addTank(clone);
            }
                , t.description);

            if (t.description.equals(""))
                b.enableHover = false;

            this.tankButtons.add(b);
        }

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.previousTankPage.image = "icons/back.png";
        this.previousTankPage.imageSizeX = 25;
        this.previousTankPage.imageSizeY = 25;
        this.previousTankPage.imageXOffset = -145;

        delete.textOffsetY = -2.5;

        delete.textColR = 255;
        delete.textColG = 255;
        delete.textColB = 255;

        delete.unselectedColR = 160;
        delete.unselectedColG = 160;
        delete.unselectedColB = 160;

        delete.selectedColR = 255;
        delete.selectedColG = 0;
        delete.selectedColB = 0;

        delete.fontSize = this.textSize;

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

        if (tankScreen instanceof OverlayObjectMenu)
        {
            for (TankAIControlled t: ((OverlayObjectMenu) tankScreen).screenLevelEditor.level.customTanks)
            {
                int index = count % (rows * cols);
                double x = this.centerX - 450 + 100 * (index % cols);
                double y = this.centerY - 100 + 100 * ((index / cols) % rows);
                count++;

                String desc = t.description;
                if (!t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7000255255255Custom tank from this level";

                ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
                {
                    TankAIControlled clone = t.instantiate(t.name, t.posX, t.posY, t.angle);
                    clone.name = System.currentTimeMillis() + "";
                    ScreenTankEditor s = new ScreenTankEditor(clone, tankScreen);
                    s.drawBehindScreen = true;
                    Game.screen = s;
                    tankScreen.addTank(clone);
                }
                        , desc);

                this.tankButtons.add(b);
            }
        }

        levelTankCount = count;

        for (String l: files)
        {
            BaseFile file = Game.game.fileManager.getFile(l);

            int index = count % (rows * cols);
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
                    TankAIControlled clone = tt.instantiate(tt.name, tt.posX, tt.posY, tt.angle);
                    ScreenTankEditor s = new ScreenTankEditor(clone, tankScreen);
                    s.drawBehindScreen = true;
                    Game.screen = s;
                    tankScreen.addTank(clone);
                }
                        , desc);
                b.text = l;

                this.tankButtons.add(b);
            }
            catch (Exception e)
            {
                System.err.println("Failed to load a custom tank from file: " + l);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update()
    {
        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            Game.screen = (Screen) tankScreen;
        }

        for (int i = 0; i < this.tankButtons.size(); i++)
        {
            if (i / (this.objectButtonCols * this.objectButtonRows) == tankPage)
            {
                this.tankButtons.get(i).update();

                if (i >= levelTankCount && deleting)
                {
                    Button b = this.tankButtons.get(i);
                    this.delete.posX = b.posX + 35;
                    this.delete.posY = b.posY + 35;
                    this.delete.update();

                    if (removeNow)
                    {
                        Game.game.fileManager.getFile(b.text).delete();
                        removeNow = false;
                        ScreenAddSavedTank s = new ScreenAddSavedTank(this.tankScreen);
                        s.drawBehindScreen = true;
                        s.deleteMode.function.run();
                        Game.screen = s;
                    }
                }
            }
        }

        if ((this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > tankPage)
            nextTankPage.update();

        if (tankPage > 0)
            previousTankPage.update();

        deleteMode.update();
        quit.update();
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
        {
            this.enableMargins = ((Screen) this.tankScreen).enableMargins;
            ((Screen) this.tankScreen).draw();
        }
        else
            this.drawDefaultBackground();

        if ((tankButtons.size() - 1) / (objectButtonRows * objectButtonCols) > tankPage)
            nextTankPage.draw();

        if (tankPage > 0)
            previousTankPage.draw();

        for (int i = tankButtons.size() - 1; i >= 0; i--)
        {
            if (i / (objectButtonCols * objectButtonRows) == tankPage)
            {
                if (i >= levelTankCount && deleting)
                {
                    Button b = this.tankButtons.get(i);
                    this.delete.posX = b.posX + 35;
                    this.delete.posY = b.posY + 35;
                    this.delete.update();

                    if (delete.selected)
                        ((ButtonObject) this.tankButtons.get(i)).tempDisableHover = true;

                ((ButtonObject) this.tankButtons.get(i)).drawBeforeTooltip = this.drawDelete;
                }

                tankButtons.get(i).draw();
            }
        }

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4, "Tank templates");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select an existing tank to serve as the base for your new tank");

        deleteMode.draw();
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
            return ((Screen) tankScreen).getOffsetX();
        else
            return super.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        if (drawBehindScreen)
            return ((Screen) tankScreen).getOffsetY();
        else
            return super.getOffsetY();
    }

    @Override
    public double getScale()
    {
        if (drawBehindScreen)
            return ((Screen) tankScreen).getScale();
        else
            return super.getScale();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        if (tankScreen instanceof IConditionalOverlayScreen)
            return ((IConditionalOverlayScreen) tankScreen).isOverlayEnabled();

        return tankScreen instanceof ScreenGame || tankScreen instanceof ILevelPreviewScreen || tankScreen instanceof IOverlayScreen;
    }

    @Override
    public void onAttemptClose()
    {
        ((Screen)this.tankScreen).onAttemptClose();
    }
}
