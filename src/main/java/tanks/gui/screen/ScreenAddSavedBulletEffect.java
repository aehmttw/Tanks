package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletEffect;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBoxInstant;

import java.util.ArrayList;

public class ScreenAddSavedBulletEffect extends Screen implements IBlankBackgroundScreen
{
    public static int page;

    public SavedFilesList allEffects;
    public SavedFilesList effects;

    public Screen previousScreen;

    public boolean deleting = false;
    public boolean removeNow = false;
    public int builtInEffectsCount = 0;

    public Consumer<BulletEffect> onComplete;

    SearchBoxInstant search = new SearchBoxInstant(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
    {
        @Override
        public void run()
        {
            createNewEffectsList();
            effects.filter(search.inputText);
            effects.sortButtons();
        }
    }, "");

    public Button quit = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previousScreen;
        }
    }
    );

    public Button deleteMode = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Delete templates", new Runnable()
    {
        @Override
        public void run()
        {
            deleting = !deleting;

            if (deleting)
                deleteMode.setText("Stop deleting");
            else
                deleteMode.setText("Delete templates");

            for (Button b: allEffects.buttons)
                b.enabled = !deleting;
        }
    }
    );

    public Button delete = new Button(0, 0, 32, 32, "x", () -> removeNow = true);

    public ScreenAddSavedBulletEffect(Screen previousScreen, Consumer<BulletEffect> onComplete)
    {
        super(350, 40, 380, 60);

        this.onComplete = onComplete;

        this.allowClose = false;

        this.music = previousScreen.music;
        this.musicID = previousScreen.musicID;
        this.previousScreen = previousScreen;

        allEffects = new SavedFilesList(Game.homedir + Game.bulletEffectsDir, page, 0, -30,
                (name, file) ->
                {
                    try
                    {
                        file.startReading();
                        BulletEffect e = BulletEffect.fromString(file.nextLine());
                        file.stopReading();
                        onComplete.accept(e);
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                }, (file) -> null,
                (file, b) ->
                {
                    try
                    {
                        file.startReading();
                        b.miscData.put("effect", BulletEffect.fromString(file.nextLine()));
                        b.miscData.put("name", b.text);
                        b.miscData.put("particles", new ArrayList<>());
                        b.miscData.put("removeParticles", new ArrayList<>());
                        b.text = "";
                        file.stopReading();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });

        ArrayList<String> effects = Game.game.fileManager.getInternalFileContents("/bullet_effects/bullet_effects.tanks");

        for (String s: effects)
        {
            BulletEffect i = BulletEffect.fromString(s);

            Button b = new Button(0, 0, this.allEffects.objWidth, this.allEffects.objHeight, "", () ->
            {
                BulletEffect e1 = BulletEffect.fromString(s);
                onComplete.accept(e1);
            }
            );

            this.allEffects.buttons.add(builtInEffectsCount, b);
            builtInEffectsCount++;

            b.translated = false;
            b.miscData.put("effect", i);
            b.miscData.put("particles", new ArrayList<>());
            b.miscData.put("removeParticles", new ArrayList<>());
        }

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

        this.effects = this.allEffects.clone();
        this.createNewEffectsList();

        delete.fontSize = this.textSize;
    }

    public void createNewEffectsList()
    {
        effects.buttons.clear();
        effects.buttons.addAll(allEffects.buttons);
        effects.sortButtons();
    }

    @Override
    public void update()
    {
        effects.update();
        quit.update();
//        search.update();
        deleteMode.update();

        if (deleting)
        {
            for (int i = Math.min(effects.page * effects.rows * effects.columns + effects.rows * effects.columns, effects.buttons.size()) - 1; i >= effects.page * effects.rows * effects.columns; i--)
            {
                Button b = effects.buttons.get(i);

                if (allEffects.buttons.indexOf(b) < builtInEffectsCount)
                    continue;

                delete.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                delete.posY = b.posY;
                delete.update();

                if (removeNow)
                {
                    removeNow = false;

                    Button b1 = effects.buttons.remove(i);
                    BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.bulletEffectsDir + "/" + ((String) b1.miscData.get("name")).replace(" ", "_") + ".tanks");

                    allEffects.buttons.remove(b1);

                    while (f.exists())
                    {
                        f.delete();
                    }

                    effects.sortButtons();
                    break;
                }
            }
        }

        page = effects.page;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        effects.draw();
        quit.draw();
//        search.draw();
        deleteMode.draw();

        for (int i = Math.min(effects.page * effects.rows * effects.columns + effects.rows * effects.columns, effects.buttons.size()) - 1; i >= effects.page * effects.rows * effects.columns; i--)
        {
            if (!Game.game.window.drawingShadow)
            {
                Button b = effects.buttons.get(i);
                ((BulletEffect) b.miscData.get("effect")).drawForInterface(b.posX, b.sizeX - b.sizeY, b.posY, Bullet.bullet_size, (ArrayList<Effect>) b.miscData.get("particles"), (ArrayList<Effect>) b.miscData.get("removeParticles"), 1, true);
            }
        }

        if (deleting)
        {
            for (int i = Math.min(effects.page * effects.rows * effects.columns + effects.rows * effects.columns, effects.buttons.size()) - 1; i >= effects.page * effects.rows * effects.columns; i--)
            {
                Button b = effects.buttons.get(i);

                if (allEffects.buttons.indexOf(b) < builtInEffectsCount)
                    continue;

                delete.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                delete.posY = b.posY;
                delete.update();
                delete.draw();
            }
        }

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, "My bullet effect templates");
    }

    @Override
    public void onAttemptClose()
    {
        this.previousScreen.onAttemptClose();
    }
}
