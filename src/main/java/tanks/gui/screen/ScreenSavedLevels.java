package tanks.gui.screen;

import basewindow.ComputerFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.ScreenElement;
import tanks.gui.SearchBoxInstant;
import tanks.gui.screen.leveleditor.OverlayEditorMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScreenSavedLevels extends Screen
{
    public static int page = 0;
    public static boolean sortByTime = false;

    public SavedFilesList fullSavedLevelsList;
    public SavedFilesList savedLevelsList;

    Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlaySingleplayer());
    Button newLevel = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "New level", () ->
    {
        String name = System.currentTimeMillis() + ".tanks";

        Level l = new Level("{28,18||0-0-player}");
        ScreenLevelEditor sl = new ScreenLevelEditor(name, l);
        sl.modified = true;
        Game.screen = sl;
        l.loadLevel((ILevelPreviewScreen) Game.screen);
    }
    );    SearchBoxInstant search = new SearchBoxInstant(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
    {
        @Override
        public void run()
        {
            createNewLevelsList();
            savedLevelsList.filter(search.inputText);
            savedLevelsList.sortButtons();

            if (search.inputText.length() <= 0)
                savedLevelsList.page = page;
        }
    }, "");

    public ScreenSavedLevels()
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        fullSavedLevelsList = new SavedFilesList(Game.homedir + Game.levelDir, page, 0, -30,
                (name, file) ->
                {
                    ScreenLevelEditor s = new ScreenLevelEditor(name + ".tanks", null);

                    if (Game.loadLevel(file, s))
                    {
                        s.level = Game.currentLevel;
                        s.paused = true;
                        Game.screen = new OverlayEditorMenu(s, s);
                    }
                },
                (file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");

        fullSavedLevelsList.drawOpenFileButton = true;
        fullSavedLevelsList.sortedByTime = sortByTime;
        fullSavedLevelsList.sort(sortByTime);

        if (fullSavedLevelsList.sortedByTime)
            sort.setHoverText("Sorting by last modified");
        else
            sort.setHoverText("Sorting by name");

        savedLevelsList = fullSavedLevelsList.clone();
        createNewLevelsList();

        search.enableCaps = true;
    }    Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            fullSavedLevelsList.sortedByTime = !fullSavedLevelsList.sortedByTime;
            fullSavedLevelsList.sort(fullSavedLevelsList.sortedByTime);
            createNewLevelsList();
            savedLevelsList.filter(search.inputText);
            savedLevelsList.sortButtons();

            if (fullSavedLevelsList.sortedByTime)
                sort.setHoverText("Sorting by last modified");
            else
                sort.setHoverText("Sorting by name");
        }
    }, "Sorting by name");

    public static void importLevels(String[] filePaths, String dir, String levelType)
    {
        List<String> paths = Arrays.stream(filePaths).filter(path -> path.endsWith(".tanks")).collect(Collectors.toList());
        if (paths.isEmpty()) return;

        ArrayList<ComputerFile> existing = new ArrayList<>();
        for (String path : paths)
        {
            ComputerFile file = (ComputerFile) Game.game.fileManager.getFile(path);
            if (!file.moveTo(Game.homedir + dir))
                existing.add(file);
        }

        if (!existing.isEmpty())
        {
            Game.screen = new ScreenPopupWarning(Game.screen,
                    getNumberString(existing.size(), "file") + " already exist" + (existing.size() == 1 ? "s" : "") + "!",
                    existing.stream().limit(10).map(f -> f.file.getName()).collect(Collectors.joining(", ")),
                    () ->
                    {
                        existing.forEach(f -> f.moveTo(Game.homedir + Game.levelDir, true));
                        Panel.notifs.add(new ScreenElement.Notification("Imported " + getNumberString(paths.size(), levelType)));
                    })
                    .setContinueText("Replace all");
        }

        if (paths.size() > existing.size())
            Panel.notifs.add(new ScreenElement.Notification("Imported " + getNumberString(paths.size() - existing.size(), levelType)));
    }

    public static String getNumberString(int count, String s)
    {
        if (count == 0)
            return "no " + s + "s";
        return count + " " + s + (count > 1 ? "s" : "");
    }

    public void createNewLevelsList()
    {
        savedLevelsList.buttons.clear();
        savedLevelsList.buttons.addAll(fullSavedLevelsList.buttons);
        savedLevelsList.sortButtons();
    }

    @Override
    public void update()
    {
        savedLevelsList.update();

        if (search.inputText.length() <= 0)
            page = savedLevelsList.page;

        quit.update();
        search.update();
        newLevel.update();

        this.sort.imageSizeX = 25;
        this.sort.imageSizeY = 25;
        this.sort.fullInfo = true;

        sortByTime = fullSavedLevelsList.sortedByTime;

        if (this.fullSavedLevelsList.sortedByTime)
            this.sort.image = "icons/sort_chronological.png";
        else
            this.sort.image = "icons/sort_alphabetical.png";

        this.sort.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        savedLevelsList.draw();

        if (savedLevelsList.buttons.size() <= 0)
        {
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.setInterfaceFontSize(24);

            if (!search.inputText.isEmpty())
            {
                Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No levels found");
            }
            else
            {
                Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 30, "You have no levels");
                Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 30, "Create a level with the 'New level' button!");
            }
        }

        quit.draw();
        search.draw();
        newLevel.draw();

        this.sort.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "My levels");
    }

    @Override
    public void onFilesDropped(String... filePaths)
    {
        importLevels(filePaths, Game.levelDir, "level");
        fullSavedLevelsList.refresh();
        createNewLevelsList();
    }
}
