package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Level;
import tanks.event.PersonalEvent;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

public class EventChangeBackgroundColor extends PersonalEvent
{
    public int colorR;
    public int colorG;
    public int colorB;

    public int noiseR;
    public int noiseG;
    public int noiseB;

    public EventChangeBackgroundColor()
    {

    }

    public EventChangeBackgroundColor(int r, int g, int b, int noiseR, int noiseG, int noiseB)
    {
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        this.noiseR = noiseR;
        this.noiseG = noiseG;
        this.noiseB = noiseB;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.colorR);
        b.writeInt(this.colorG);
        b.writeInt(this.colorB);

        b.writeInt(this.noiseR);
        b.writeInt(this.noiseG);
        b.writeInt(this.noiseB);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.colorR = b.readInt();
        this.colorG = b.readInt();
        this.colorB = b.readInt();

        this.noiseR = b.readInt();
        this.noiseG = b.readInt();
        this.noiseB = b.readInt();
    }

    @Override
    public void execute()
    {
        Level l;
        if (Game.currentLevel != null)
            l = Game.currentLevel;
        else if (Game.screen instanceof ScreenLevelEditor)
            l = ((ScreenLevelEditor) Game.screen).level;
        else if (Game.screen instanceof ScreenLevelEditorOverlay)
            l = ((ScreenLevelEditorOverlay) Game.screen).screenLevelEditor.level;
        else
            l = new Level("{28,18||0-0-player}");

        l.colorR = this.colorR;
        l.colorG = this.colorG;
        l.colorB = this.colorB;

        if (this.noiseR >= 0 && this.noiseG >= 0 && this.noiseB >= 0)
        {
            l.colorVarR = this.noiseR;
            l.colorVarG = this.noiseG;
            l.colorVarB = this.noiseB;
        }
        else
        {
            l.colorVarR = Math.min(20, 255 - l.colorR);
            l.colorVarG = Math.min(20, 255 - l.colorG);
            l.colorVarB = Math.max(20, 255 - l.colorB);
        }

        l.reloadTiles();
    }
}
