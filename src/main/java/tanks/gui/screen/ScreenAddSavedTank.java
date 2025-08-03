package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.tank.*;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

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

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", () -> tankPage++);

    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", () -> tankPage--);

    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + this.objYSpace * 3, 40, 40, "", () -> tankPage = 0);

    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + this.objYSpace * 3, 40, 40, "", () -> tankPage = (tankButtons.size() - 1) / objectButtonRows / objectButtonCols);

    Button openFolder = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 4, this.objHeight, this.objHeight, "", () ->
    {
        Game.game.fileManager.openFileManager(Game.homedir + Game.tankDir);
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

    public ScreenAddSavedTank(ITankScreen tankScreen)
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
                }
                else
                    continue;
            }

            final TankAIControlled tt = t;

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                TankAIControlled clone = tt.instantiate(tt.name, tt.posX, tt.posY, tt.angle);
                clone.name = System.currentTimeMillis() + "";
                Pointer<TankAIControlled> p = tankScreen.addTank(clone);
                ScreenEditorTank s = new ScreenEditorTank(p, (Screen) tankScreen);
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

        ArrayList<String> files = searchDirectory(Game.tankDir);

        if (tankScreen instanceof OverlayObjectMenu)
        {
            for (TankAIControlled t: ((OverlayObjectMenu) tankScreen).editor.level.customTanks)
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
                    Pointer<TankAIControlled> p = tankScreen.addTank(clone);
                    ScreenEditorTank s = new ScreenEditorTank(p, (Screen) tankScreen);
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
                b.disabledColR = 127;

                this.tankButtons.add(b);
            }
        }

        levelTankCount = count;

        final ArrayList<TankAIControlled> savedTanks = new ArrayList<>();
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
                savedTanks.add(t);

                String desc = t.description;
                if (!t.description.equals(""))
                    desc += " \n \n ";

                desc += "\u00A7000255000255Saved custom tank template";

                ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
                {
                    BiConsumer<TankAIControlled, Pointer<TankAIControlled>> resolve = (TankAIControlled tank, Pointer<TankAIControlled> p1) ->
                    {
                        ArrayList<TankAIControlled> tanks = Game.currentLevel.customTanks;
                        Game.currentLevel.customTanks = savedTanks;

                        HashSet<String> linkedTanks = new HashSet<>();
                        tank.getAllLinkedTankNames(linkedTanks);
                        Game.currentLevel.customTanks = tanks;

                        ArrayList<TankAIControlled> inLevel = new ArrayList<>();
                        ArrayList<TankAIControlled> notInLevel = new ArrayList<>();

                        for (TankAIControlled ta : tanks)
                        {
                            if (ta.name.equals(tank.name))
                                continue;

                            if (linkedTanks.contains(ta.name))
                            {
                                linkedTanks.remove(ta.name);
                                inLevel.add(ta);
                            }
                        }

                        for (TankAIControlled ta : savedTanks)
                        {
                            if (ta.name.equals(tank.name))
                                continue;

                            if (linkedTanks.contains(ta.name))
                                notInLevel.add(ta);
                        }

                        TankAIControlled clone = tank.instantiate(tank.name, tank.posX, tank.posY, tank.angle);
                        if (p1 != null)
                            p1.set(clone);

                        final Pointer<TankAIControlled> p = p1 == null ? tankScreen.addTank(clone) : p1;

                        for (TankAIControlled ta : notInLevel)
                        {
                            tankScreen.addTank(ta.instantiate(ta.name, ta.posX, ta.posY, ta.angle), false);
                        }

                        clone.removeBrokenLinks();

                        ScreenEditorTank s = new ScreenEditorTank(p, (Screen) tankScreen);
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

                        if (inLevel.size() > 0 || notInLevel.size() > 0)
                        {
                            ScreenTankSavedInfo sc = new ScreenTankSavedInfo(s, clone, new ArrayList<>(notInLevel), new ArrayList<>(inLevel));
                            sc.copiedToTemplate = false;
                            Game.screen = sc;
                        }
                        else
                            Game.screen = s;
                    };

                    Pointer<TankAIControlled> duplicate = null;
                    for (int i = 0; i < Game.currentLevel.customTanks.size(); i++)
                    {
                        TankAIControlled t1 = Game.currentLevel.customTanks.get(i);
                        if (t1.name.equals(tt.name))
                        {
                            duplicate = new ArrayListIndexPointer<>(Game.currentLevel.customTanks, i);
                            break;
                        }
                    }

                    if (duplicate == null)
                        resolve.accept(tt, null);
                    else
                        Game.screen = new ScreenTankLoadOverwrite((OverlayObjectMenu) this.tankScreen, tt, duplicate, resolve, Game.currentLevel.customTanks, savedTanks);
                }
                        , desc);
                b.disabledColR = 60;
                b.disabledColB = 60;
                b.disabledColG = 160;
                b.text = l;

                this.tankButtons.add(b);
            }
            catch (Exception e)
            {
                System.err.println("Failed to load a custom tank from file: " + l);
                e.printStackTrace();
            }
        }

        for (TankPlayer t: Game.currentLevel.playerBuilds)
        {
            String desc = t.description;
            if (!t.description.equals(""))
                desc += " \n \n ";

            desc += "\u00A7255000255255Player build from this level";

            this.addTank(t, count, desc).disabledColG = 127;
            count++;
        }

        files = searchDirectory(Game.buildDir);
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
            TankAIControlled clone;

            if (t instanceof TankAIControlled)
                clone = ((TankAIControlled) t).instantiate(t.name, t.posX, t.posY, t.angle);
            else
                clone = ((TankPlayer) t).clonePropertiesTo((TankAIControlled) new TankPurple(t.name, t.posX, t.posY, t.angle).setDefaultPlayerColor());

            boolean duplicate = t.name.equals("player");
            for (TankAIControlled t1: Game.currentLevel.customTanks)
            {
                if (t1.name.equals(t.name))
                {
                    duplicate = true;
                    break;
                }
            }

            if (Game.registryTank.getEntry(t.name) != null)
                duplicate = true;

            if (duplicate)
                clone.name = System.currentTimeMillis() + "";

            Pointer<TankAIControlled> p = tankScreen.addTank(clone);
            ScreenEditorTank s = new ScreenEditorTank(p, (Screen) tankScreen);
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
                Button b = this.tankButtons.get(i);

                if (!b.text.isEmpty() && deleting)
                {
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
                Button b = this.tankButtons.get(i);

                if (!b.text.isEmpty() && deleting)
                {
                    this.delete.posX = b.posX + 35;
                    this.delete.posY = b.posY + 35;
                    this.delete.update();

                    if (delete.selected)
                        ((ButtonObject) this.tankButtons.get(i)).tempDisableHover = true;

                ((ButtonObject) this.tankButtons.get(i)).drawBeforeTooltip = this.drawDelete;}

                tankButtons.get(i).draw();
            }
        }

        Drawing.drawing.setColor(255, 255, 255);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4, "Tank templates");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select an existing tank to serve as the base for your new tank");

        deleteMode.draw();
        openFolder.draw();
        quit.draw();
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
