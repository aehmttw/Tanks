package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.ITrigger;
import tanks.gui.Selector;
import tanks.gui.SelectorDrawable;

import java.util.ArrayList;

public class ScreenArrayListSelector extends Screen implements IConditionalOverlayScreen, IDarkScreen
{
    public Screen screen;

    public boolean drawBehindScreen = false;
    public String title;

    public ArrayList<Entry> entries;

    public int entriesPerPage = 6;
    public int page = 0;

    public Consumer<Entry> saveEntry;
    public Producer<Entry> defaultEntry;

    public Button create = new Button(this.centerX, 0, 60, 60, "+", () ->
    {
        Entry e = defaultEntry.produce();
        this.entries.add(e);
        this.arrangeEntries();

        if (e.element1 instanceof Selector)
            ((Selector) e.element1).setScreen();
        else if (e.element1 instanceof SelectorDrawable)
            ((SelectorDrawable) e.element1).function.run();
    });

    public void apply()
    {
        for (Entry e: this.entries)
        {
            saveEntry.accept(e);
        }
    }

    public Button done = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Done", () ->
    {
        this.apply();
        Game.screen = screen;
    });

    Button next = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    Button previous = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public static class Entry
    {
        public ITrigger element1;
        public ITrigger element2;
        public ScreenArrayListSelector screen;
        public Button delete = new Button(0, 0, 60, 60, "x", () ->
        {
            screen.entries.remove(this);
            screen.arrangeEntries();
        });

        public Entry(ITrigger e1, ITrigger e2, ScreenArrayListSelector screen)
        {
            this.element1 = e1;
            this.element2 = e2;
            this.screen = screen;

            delete.textOffsetY = -2.5;

            delete.unselectedColR = 160;
            delete.unselectedColG = 160;
            delete.unselectedColB = 160;

            delete.selectedColR = 255;
            delete.selectedColG = 0;
            delete.selectedColB = 0;

            delete.textColR = 255;
            delete.textColG = 255;
            delete.textColB = 255;
        }

        public void setPosition(double y)
        {
            if (element2 == null)
                element1.setPosition(Drawing.drawing.interfaceSizeX / 2, y);
            else
            {
                element1.setPosition(Drawing.drawing.interfaceSizeX / 2 - screen.objXSpace / 2, y);
                element2.setPosition(Drawing.drawing.interfaceSizeX / 2 + screen.objXSpace / 2, y);
            }

            delete.setPosition(Drawing.drawing.interfaceSizeX / 2 - screen.objXSpace * 1.2, y - 15);
        }

        public void update()
        {
            this.element1.update();

            if (this.element2 != null)
                this.element2.update();

            this.delete.update();
        }

        public void draw()
        {
            this.element1.draw();

            if (this.element2 != null)
                this.element2.draw();

            this.delete.draw();
        }
    }

    public ScreenArrayListSelector(Screen prev, String title)
    {
        super(350, 40, 380, 60);

        this.screen = prev;
        this.title = title;
        this.allowClose = false;

        this.music = prev.music;
        this.musicID = prev.musicID;

        this.next.image = "icons/forward.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "icons/back.png";
        this.previous.imageSizeX = 25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;
    }

    public void setContent(ArrayList<Entry> entries, Producer<Entry> defaultEntry, Consumer<Entry> saveEntry)
    {
        this.entries = entries;
        this.defaultEntry = defaultEntry;
        this.saveEntry = saveEntry;
        this.arrangeEntries();
    }

    public void arrangeEntries()
    {
        for (int i = 0; i < entries.size(); i++)
        {
            this.entries.get(i).setPosition(this.centerY + 90 * ((i % entriesPerPage) - (entriesPerPage - 1) / 2.0));
        }

        this.create.posY = this.centerY + 90 * ((this.entries.size() % entriesPerPage) - (entriesPerPage - 1) / 2.0) - 15;
    }

    @Override
    public void update()
    {
        for (int i = this.page * this.entriesPerPage; i < Math.min((this.page + 1) * this.entriesPerPage, this.entries.size() + 1); i++)
        {
            if (i >= this.entries.size())
                this.create.update();
            else
                this.entries.get(i).update();
        }

        if (entries.size() >= entriesPerPage)
        {
            previous.enabled = page > 0;
            next.enabled = page < entries.size() / entriesPerPage;

            previous.update();
            next.update();
        }

        done.update();
    }

    @Override
    public void draw()
    {
        if (this.drawBehindScreen)
        {
            this.enableMargins = this.screen.enableMargins;
            this.screen.draw();
        }
        else
        {
            Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
            this.drawDefaultBackground();
        }

        if (Game.screen != this)
            return;

        for (int i = Math.min((this.page + 1) * this.entriesPerPage, this.entries.size() + 1) - 1; i >= this.page * this.entriesPerPage; i--)
        {
            if (i >= this.entries.size())
                this.create.draw();
            else
                this.entries.get(i).draw();
        }

        done.draw();

        if (entries.size() >= entriesPerPage)
        {
            previous.draw();
            next.draw();
        }

        Drawing.drawing.setInterfaceFontSize(this.titleSize);

        if (Level.isDark() || Panel.darkness > 64)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, this.title);
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
