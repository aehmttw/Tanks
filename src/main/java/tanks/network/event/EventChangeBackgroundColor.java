package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Level;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;

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
            l = ((ScreenLevelEditorOverlay) Game.screen).editor.level;
        else
            l = new Level("{28,18||0-0-player}");

        l.color.red = this.colorR;
        l.color.green = this.colorG;
        l.color.blue = this.colorB;

        if (this.noiseR >= 0 && this.noiseG >= 0 && this.noiseB >= 0)
        {
            l.colorVar.red = this.noiseR;
            l.colorVar.green = this.noiseG;
            l.colorVar.blue = this.noiseB;
        }
        else
        {
            l.colorVar.red = Math.min(20, 255 - l.color.red);
            l.colorVar.green = Math.min(20, 255 - l.color.green);
            l.colorVar.blue = Math.max(20, 255 - l.color.blue);
        }

        l.reloadTiles();
    }
}
