package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.tank.Tank;

import java.util.ArrayList;

public class ScreenTankSavedInfo extends Screen implements IBlankBackgroundScreen
{
    public Screen previous;

    public ButtonObject mainButton;

    public int copiedPage = 0;
    public int notCopiedPage = 0;

    public boolean copiedToTemplate = true;

    public ArrayList<ButtonObject> linkedTanksCopied = new ArrayList<>();
    public ArrayList<ButtonObject> linkedTanksNotCopied = new ArrayList<>();

    public double row1Y = this.centerY - 150;
    public double row2Y = this.centerY;
    public double row3Y = this.centerY + 150;
    public double rowSpacing = 150;

    public Button nextCopiedPage;
    public Button prevCopiedPage;

    public Button nextNotCopiedPage;
    public Button prevNotCopiedPage;

    public int pageEntries = 10;

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 4.5, this.objWidth, this.objHeight, "Ok", () ->
    {
        Game.screen = this.previous;
    }
    );

    public ScreenTankSavedInfo(Screen s, Tank tank, ArrayList<Tank> copied, ArrayList<Tank> notCopied)
    {
        this.previous = s;
        this.music = this.previous.music;
        this.musicID = this.previous.musicID;

        double cY = this.centerY + 40;

        if (copied.isEmpty())
        {
            if (notCopied.isEmpty())
                row1Y = cY;
            else
            {
                row1Y = cY - rowSpacing / 2;
                row3Y = cY + rowSpacing / 2;
            }
        }
        else
        {
            if (notCopied.isEmpty())
            {
                row1Y = cY - rowSpacing / 2;
                row2Y = cY + rowSpacing / 2;
            }
            else
            {
                row1Y = cY - rowSpacing;
                row2Y = cY;
                row3Y = cY + rowSpacing;
            }
        }

        this.mainButton = new ButtonObject(tank, this.centerX, row1Y, 75, 75);
        this.mainButton.setHoverTextUntranslated("\u00A7000255255255" + tank.name + "\u00A7255255255255------" + tank.description);
        this.mainButton.disabledColA = 0;

        double startX = this.centerX - 450;
        double extra = Math.max(pageEntries - copied.size(), 0) * 50;
        for (int i = 0; i < copied.size(); i++)
        {
            ButtonObject b = new ButtonObject(copied.get(i), startX + extra + i % pageEntries * 100, row2Y, 75, 75);
            b.setHoverTextUntranslated("\u00A7000255255255" + copied.get(i).name + "\u00A7255255255255------" + copied.get(i).description);
            b.disabledColA = 0;
            this.linkedTanksCopied.add(b);
        }

        extra = Math.max(pageEntries - notCopied.size(), 0) * 50;
        for (int i = 0; i < notCopied.size(); i++)
        {
            ButtonObject b = new ButtonObject(notCopied.get(i), startX + extra + i % pageEntries * 100, row3Y, 75, 75);
            b.setHoverTextUntranslated("\u00A7000255255255" + notCopied.get(i).name + "\u00A7255255255255------" + notCopied.get(i).description);
            b.disabledColA = 0;
            this.linkedTanksNotCopied.add(b);
        }

        this.nextCopiedPage = new Button(this.centerX + 550, this.row2Y, 60, 60, "", () -> { this.copiedPage++; });
        this.prevCopiedPage = new Button(this.centerX - 550, this.row2Y, 60, 60, "", () -> { this.copiedPage--; });
        this.nextNotCopiedPage = new Button(this.centerX + 550, this.row3Y, 60, 60, "", () -> { this.notCopiedPage++; });
        this.prevNotCopiedPage = new Button(this.centerX - 550, this.row3Y, 60, 60, "", () -> { this.notCopiedPage--; });

        this.nextCopiedPage.image = "icons/forward.png";
        this.nextCopiedPage.imageSizeX = 35;
        this.nextCopiedPage.imageSizeY = 35;
        this.nextCopiedPage.imageXOffset = 0;

        this.prevCopiedPage.image = "icons/back.png";
        this.prevCopiedPage.imageSizeX = 35;
        this.prevCopiedPage.imageSizeY = 35;
        this.prevCopiedPage.imageXOffset = 0;

        this.nextNotCopiedPage.image = "icons/forward.png";
        this.nextNotCopiedPage.imageSizeX = 35;
        this.nextNotCopiedPage.imageSizeY = 35;
        this.nextNotCopiedPage.imageXOffset = 0;

        this.prevNotCopiedPage.image = "icons/back.png";
        this.prevNotCopiedPage.imageSizeX = 35;
        this.prevNotCopiedPage.imageSizeY = 35;
        this.prevNotCopiedPage.imageXOffset = 0;
    }

    @Override
    public void update()
    {
        this.quit.update();

        this.mainButton.update();

        for (int i = copiedPage * pageEntries; i < Math.min(linkedTanksCopied.size(), (copiedPage + 1) * pageEntries); i++)
        {
            this.linkedTanksCopied.get(i).update();
        }

        for (int i = notCopiedPage * pageEntries; i < Math.min(linkedTanksNotCopied.size(), (notCopiedPage + 1) * pageEntries); i++)
        {
            this.linkedTanksNotCopied.get(i).update();
        }

        prevCopiedPage.enabled = copiedPage > 0;
        nextCopiedPage.enabled = ((linkedTanksCopied.size() - 1) / pageEntries) > copiedPage;
        prevNotCopiedPage.enabled = notCopiedPage > 0;
        nextNotCopiedPage.enabled = ((linkedTanksNotCopied.size() - 1) / pageEntries) > notCopiedPage;

        if (prevCopiedPage.enabled || nextCopiedPage.enabled)
        {
            prevCopiedPage.update();
            nextCopiedPage.update();
        }

        if (prevNotCopiedPage.enabled || nextNotCopiedPage.enabled)
        {
            prevNotCopiedPage.update();
            nextNotCopiedPage.update();
        }
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        if (this.copiedToTemplate)
            Drawing.drawing.displayInterfaceText(this.centerX, row1Y - 80, "Tank saved to templates!");
        else
            Drawing.drawing.displayInterfaceText(this.centerX, row1Y - 80, "Tank added to level!");

        Drawing.drawing.setInterfaceFontSize(this.textSize);

        if (this.copiedToTemplate)
        {
            if (!this.linkedTanksCopied.isEmpty())
                Drawing.drawing.displayInterfaceText(this.centerX, row2Y - 80, "Tanks referenced by this tank also saved to templates");

            if (!this.linkedTanksNotCopied.isEmpty())
                Drawing.drawing.displayInterfaceText(this.centerX, row3Y - 80, "Tanks referenced by this tank already present in templates");
        }
        else
        {
            if (!this.linkedTanksCopied.isEmpty())
                Drawing.drawing.displayInterfaceText(this.centerX, row2Y - 80, "Tank templates referenced by this tank also added to level");

            if (!this.linkedTanksNotCopied.isEmpty())
                Drawing.drawing.displayInterfaceText(this.centerX, row3Y - 80, "Tank templates referenced by this tank already present in level");
        }

        this.quit.draw();

        this.mainButton.draw();

        for (int i = Math.min(linkedTanksCopied.size(), (copiedPage + 1) * pageEntries) - 1; i >= copiedPage * pageEntries; i--)
        {
            this.linkedTanksCopied.get(i).draw();
        }

        for (int i = Math.min(linkedTanksNotCopied.size(), (notCopiedPage + 1) * pageEntries) - 1; i >= notCopiedPage * pageEntries; i--)
        {
            this.linkedTanksNotCopied.get(i).draw();
        }

        if (prevCopiedPage.enabled || nextCopiedPage.enabled)
        {
            prevCopiedPage.draw();
            nextCopiedPage.draw();
        }

        if (prevNotCopiedPage.enabled || nextNotCopiedPage.enabled)
        {
            prevNotCopiedPage.draw();
            nextNotCopiedPage.draw();
        }
    }
}
