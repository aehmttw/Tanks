package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayable;
import tanks.tank.TankPlayer;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ScreenAddSavedTankBuild extends Screen implements IConditionalOverlayScreen
{
    public static int tankPage;

    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public int buildsIndex = 0;
    public int buildTemplatesIndex = 0;
    public int registryIndex = 0;
    public int customTanksIndex = 0;
    public int customTankTemplatesIndex = 0;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public boolean drawBehindScreen;

    public ITankBuildScreen tankScreen;
    public ArrayList<? extends TankPlayer> buildsList;

    public boolean deleting = false;

    public boolean removeNow = false;

    public Runnable drawDelete = () -> this.delete.draw();

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> tankPage++);

    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> tankPage--);

    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + this.objYSpace * 3, 40, 40, "", () -> tankPage = 0);

    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + this.objYSpace * 3, 40, 40, "", () -> tankPage = (tankButtons.size() - 1) / objectButtonRows / objectButtonCols);

    Button openFolder = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace * 0.75, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 4, this.objHeight, this.objHeight, "", () ->
    {
        Game.game.fileManager.openFileManager(Game.homedir + Game.buildDir);
    }, "Open folder in file manager");


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

    public ScreenAddSavedTankBuild(ITankBuildScreen tankScreen, ArrayList<? extends TankPlayer> builds)
    {
        super(350, 40, 380, 60);

        this.allowClose = false;

        this.music = ((Screen) tankScreen).music;
        this.musicID = ((Screen) tankScreen).musicID;
        this.tankScreen = tankScreen;

        this.openFolder.image = "icons/folder.png";
        this.openFolder.fullInfo = true;
        this.openFolder.imageSizeX = 30;
        this.openFolder.imageSizeY = 30;

        this.buildsList = builds;

        int rows = objectButtonRows;
        int cols = objectButtonCols;

        ArrayList<String> files = searchDirectory(Game.buildDir);
        int count = 0;

        for (TankPlayer t: builds)
        {
            String desc = t.description;
            if (!t.description.equals(""))
                desc += " \n \n ";

            desc += "\u00A7255000255255Player build from this level";

            this.addTank(t, count, desc).disabledColG = 127;
            count++;
        }

        buildsIndex = count;

        for (String l: files)
        {
            BaseFile file = Game.game.fileManager.getFile(l);

            try
            {
                file.startReading();
                String tankStr = file.nextLine();
                file.stopReading();
                TankPlayer t = TankPlayer.fromString(tankStr);

                String desc = t.description;
                if (!t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7255127000255Saved player build";

                ButtonObject b = this.addTank(t, count, desc);
                b.disabledColR = 255;
                b.disabledColG = 160;
                b.disabledColB = 60;
                b.text = l;
            }
            catch (Exception e)
            {
                System.err.println("Failed to load a custom tank build from file: " + l);
                e.printStackTrace();
            }

            count++;
        }

        buildTemplatesIndex = count;

        for (int i = 0; i < Game.registryTank.tankEntries.size(); i++)
        {
            int index = count % (rows * cols);
            count++;
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            TankAIControlled t = null;

            Tank t1 = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);
            if (t1 instanceof TankAIControlled)
            {
                t = (TankAIControlled) t1;
                HashSet<String> m = Game.registryTank.tankMusics.get(t.name);

                if (m != null)
                    t.musicTracks.addAll(m);
            }
            else
                continue;

            final TankAIControlled tt = t;
            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                TankPlayer.ShopTankBuild clone = new TankPlayer.ShopTankBuild(tt.convertToPlayer(tt.posX, tt.posY, tt.angle));
                for (TankPlayer t2: this.buildsList)
                {
                    if (t2.name.equals(clone.name))
                    {
                        clone.name = System.currentTimeMillis() + "";
                        break;
                    }
                }
                Pointer<TankPlayer.ShopTankBuild> p = tankScreen.addTank(clone);
                ScreenEditorPlayerTankBuild<TankPlayer.ShopTankBuild> s = new ScreenEditorPlayerTankBuild<>(p, (Screen) tankScreen);
                s.onComplete = () ->
                {
                    if (p.get() == null)
                    {
                        this.tankScreen.removeTank(clone);
                        this.tankScreen.refreshTanks(clone);
                    }
                    else
                        this.tankScreen.refreshTanks(p.get());
                };
                s.drawBehindScreen = true;
                Game.screen = s;
            }
                , t.description);

            if (t.description.equals(""))
                b.enableHover = false;

            this.tankButtons.add(b);
        }

        registryIndex = count;

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.nextTankPage.enabled = false;
        this.previousTankPage.enabled = false;

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

        delete.textOffsetY = -2.5;

        delete.textColR = 255;
        delete.textColG = 255;
        delete.textColB = 255;

        delete.bgColR = 160;
        delete.bgColG = 160;
        delete.bgColB = 160;

        delete.selectedColR = 255;
        delete.selectedColG = 0;
        delete.selectedColB = 0;

        delete.fontSize = this.textSize;

        files = searchDirectory(Game.tankDir);

        if (tankScreen instanceof OverlayObjectMenu)
        {
            for (TankAIControlled t: ((OverlayObjectMenu) tankScreen).editor.level.customTanks)
            {
                String desc = t.description;
                if (!t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7000255255255Custom tank from this level";

                this.addTank(t, count, desc).disabledColR = 127;
                count++;
            }
        }

        customTanksIndex = count;

        for (String l: files)
        {
            BaseFile file = Game.game.fileManager.getFile(l);

            try
            {
                file.startReading();
                String tankStr = file.nextLine();
                file.stopReading();
                TankAIControlled t = TankAIControlled.fromString(tankStr);

                String desc = t.description;
                if (!t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7000255000255Saved custom tank template";

                ButtonObject b = this.addTank(t, count, desc);
                count++;
                b.disabledColR = 60;
                b.disabledColB = 60;
                b.disabledColG = 160;
                b.text = l;
            }
            catch (Exception e)
            {
                System.err.println("Failed to load a custom tank from file: " + l);
                e.printStackTrace();
            }

        }

        customTankTemplatesIndex = count;
    }

    public ArrayList<String> searchDirectory(String s)
    {
        BaseFile directory = Game.game.fileManager.getFile(Game.homedir + s);
        if (!directory.exists())
            directory.mkdirs();

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
        return files;
    }

    public ButtonObject addTank(Tank t, int count, String desc)
    {
        int rows = objectButtonRows;
        int cols = objectButtonCols;
        int index = count % (rows * cols);
        double x = this.centerX - 450 + 100 * (index % cols);
        double y = this.centerY - 100 + 100 * ((index / cols) % rows);
        ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
        {
            TankPlayer.ShopTankBuild clone = t instanceof TankAIControlled
                    ? new TankPlayer.ShopTankBuild(((TankAIControlled) t).convertToPlayer(t.posX, t.posY, t.angle))
                    : (TankPlayer.ShopTankBuild) ((TankPlayable) t).copyPropertiesTo(new TankPlayer.ShopTankBuild());
            for (TankPlayer t1: this.buildsList)
            {
                if (t1.name.equals(t.name))
                {
                    clone.name = System.currentTimeMillis() + "";
                    break;
                }
            }

            Pointer<TankPlayer.ShopTankBuild> p = tankScreen.addTank(clone);
            ScreenEditorPlayerTankBuild<TankPlayer.ShopTankBuild> s = new ScreenEditorPlayerTankBuild<>(p, (Screen) tankScreen);
            s.onComplete = () ->
            {
                if (p.get() == null)
                    this.tankScreen.removeTank(clone);
                else
                    this.tankScreen.refreshTanks(p.get());
            };
            s.drawBehindScreen = true;
            Game.screen = s;
        }
                , desc);

        this.tankButtons.add(b);
        return b;
    }

    @Override
    public void update()
    {
        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            Game.screen = (Screen) tankScreen;
        }

        int pageCount = (this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols);
        if (tankPage > pageCount)
            tankPage = pageCount;

        for (int i = 0; i < this.tankButtons.size(); i++)
        {
            if (i / (this.objectButtonCols * this.objectButtonRows) == tankPage)
            {
                this.tankButtons.get(i).update();

                if (((i >= buildsIndex && i < buildTemplatesIndex) || (i >= customTanksIndex && i < customTankTemplatesIndex)) && deleting)
                {
                    Button b = this.tankButtons.get(i);
                    this.delete.posX = b.posX + 35;
                    this.delete.posY = b.posY + 35;
                    this.delete.update();

                    if (removeNow)
                    {
                        Game.game.fileManager.getFile(b.text).delete();
                        removeNow = false;
                        ScreenAddSavedTankBuild s = new ScreenAddSavedTankBuild(this.tankScreen, buildsList);
                        s.drawBehindScreen = this.drawBehindScreen;
                        s.deleteMode.function.run();
                        Game.screen = s;

                        if (tankPage > ((s.tankButtons.size() - 1) / (objectButtonCols * objectButtonRows)))
                            tankPage--;

                        s.nextTankPage.enabled = this.nextTankPage.enabled;
                        s.previousTankPage.enabled = this.previousTankPage.enabled;
                        s.lastTankPage.enabled = this.lastTankPage.enabled;
                        s.firstTankPage.enabled = this.firstTankPage.enabled;
                    }
                }
            }
        }

        this.nextTankPage.enabled = ((this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > tankPage);
        this.previousTankPage.enabled = (tankPage > 0);
        this.lastTankPage.enabled = this.nextTankPage.enabled;
        this.firstTankPage.enabled = this.previousTankPage.enabled;

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

        deleteMode.update();
        openFolder.update();
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

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 600);

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
            {
                if (((i >= buildsIndex && i < buildTemplatesIndex) || (i >= customTanksIndex && i < customTankTemplatesIndex)) && deleting)
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

        Drawing.drawing.setColor(255, 255, 255);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4, "Player tank build templates");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select an existing tank to serve as the base for your new tank build");

        deleteMode.draw();
        openFolder.draw();
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
