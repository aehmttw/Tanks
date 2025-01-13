package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.leveleditor.selector.SelectorBeatPattern;
import tanks.rendering.ShaderBeatBlocks;
import tanks.rendering.ShaderGroundObstacleBeatBlock;
import tanks.tankson.MetadataProperty;

public class ObstacleBeatBlock extends ObstacleStackable
{
    @MetadataProperty(id = "beat_pattern", name = "Beat pattern", image = "obstacle_beat.png", selector = SelectorBeatPattern.selector_name, keybind = "editor.groupID")
    public int beatPattern = 0;

    public double beatFrequency = 1;
    public boolean alternate;

    public double outlineColorR;
    public double outlineColorG;
    public double outlineColorB;

    protected boolean lastOn = false;
    protected boolean firstUpdate = true;

    public ObstacleBeatBlock(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.initialize();
        this.renderer = ShaderBeatBlocks.class;
        this.tileRenderer = ShaderGroundObstacleBeatBlock.class;
        this.destructible = false;
        this.type = ObstacleType.top;

        this.primaryMetadataID = "beat_pattern";
        this.secondaryMetadataID = "stack_height";

        this.description = "A block that appears and disappears to the beat of the music";
    }

    public static boolean isOn(double freq, boolean alt)
    {
        double pos = (Panel.panel.frameStartTime - Game.game.window.soundPlayer.getMusicStartTime()) / 10.0;
        if (Game.screen instanceof ScreenGame)
            pos = 600 + ScreenGame.lastTimePassed - ((ScreenGame) Game.screen).introBattleMusicEnd / 10.0;

        pos /= 100.0;
        return ((int) (6.0 + pos / (6.0 / freq)) % 2 == (alt ? 1 : 0));
    }

    public static double timeTillChange(double freq)
    {
        double pos = (Panel.panel.frameStartTime - Game.game.window.soundPlayer.getMusicStartTime()) / 10.0;
        if (Game.screen instanceof ScreenGame)
            pos = 600 + ScreenGame.lastTimePassed - ((ScreenGame) Game.screen).introBattleMusicEnd / 10.0;

        return (600 / freq - pos % (600.0 / freq));
    }

    @Override
    public void update()
    {
        boolean on = isOn(this.beatFrequency, this.alternate);
        this.bulletCollision = on;
        this.tankCollision = on;

        if (this.tankCollision != lastOn || firstUpdate)
        {
            if (firstUpdate)
                this.postOverride();

            this.firstUpdate = false;
            int x = (int) (this.posX / Game.tile_size);
            int y = (int) (this.posY / Game.tile_size);

            if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                Game.game.solidGrid[x][y] = this.tankCollision;

            this.verticalFaces = null;
            this.horizontalFaces = null;
            this.allowBounce = false;
            this.shouldClip = true;

            this.lastOn = this.tankCollision;
        }
        else
        {
            this.allowBounce = true;
            this.shouldClip = false;
        }
    }

    @Override
    public double getTileHeight()
    {
        return 0;
    }

    @Override
    public void draw()
    {
        if (this.stackHeight <= 0)
            return;

        Drawing drawing = Drawing.drawing;

        drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA, this.glow);

        if (Game.enable3d)
        {
            for (int i = 0; i < Math.min(this.stackHeight, default_max_height); i++)
            {
                int in = default_max_height - 1 - i;
                drawing.setColor(this.stackColorR[in], this.stackColorG[in], this.stackColorB[in], this.colorA, this.glow);

                byte option = 0;

                if (stackHeight % 1 == 0)
                {
                    byte o = (byte) (option | this.getOptionsByte(((i + 1) + stackHeight % 1.0) * Game.tile_size));

                    if (Game.game.window.drawingShadow || !Game.shadowsEnabled)
                        options[i] = o;

                    drawBox( i * Game.tile_size + this.startHeight * Game.tile_size, o);
                }
                else
                {
                    byte o = (byte) (option | this.getOptionsByte((i + stackHeight % 1.0) * Game.tile_size));

                    if (Game.game.window.drawingShadow || !Game.shadowsEnabled)
                        options[i] = o;

                    drawBox((i - 1 + stackHeight % 1.0) * Game.tile_size + this.startHeight * Game.tile_size, o);
                }
            }
        }
        else
        {
            int freq = (int) this.beatFrequency;
            boolean alt = this.alternate;

            double timeTillChange = ObstacleBeatBlock.timeTillChange(freq) % (600 / freq);

            double beatTime = 37.5;
            int warningBeats = freq == 1 ? 3 : freq == 2 ? 2 : freq == 4 ? 1 : 0;
            float flash = 0f;

            if (timeTillChange < beatTime * warningBeats)
                flash = (float) Math.max(0, ((timeTillChange + 600.0) % beatTime) / beatTime - 0.5);

            boolean on = ObstacleBeatBlock.isOn(freq, alt);
            float f = (float) (Obstacle.draw_size);

            float small = 0.25f;
            float large = 1.0f;

            float size;
            if (warningBeats > 0)
            {
                if (on)
                    size = (float) (small + (large - small) * Math.min(1, timeTillChange / 8));
                else
                    size = (float) (small + (large - small) * (1.0 - Math.min(1, timeTillChange / 8)));
            }
            else
                size = (on ? large : small);

            if (!on)
                flash *= 2;

            drawing.setColor(this.outlineColorR * (1 - flash), this.outlineColorG * (1 - flash), this.outlineColorB * (1 - flash), this.colorA, this.glow);
            drawing.fillRect(this, this.posX, this.posY, f * size, f * size);

            drawing.setColor(this.colorR * (1 - flash) + 255 * flash, this.colorG * (1 - flash) + 255 * flash, this.colorB * (1 - flash) + 255 * flash, this.colorA, this.glow);
            drawing.fillRect(this, this.posX, this.posY, f * size * 0.9, f * size * 0.9);
        }
    }

    public void drawBox(double z, byte o)
    {
        float cx = (float) this.posX;
        float cy = (float) this.posY;
        float h = (float) (Game.sampleGroundHeight(this.posX, this.posY));
        float cz = (float) (h);

        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX, this.posY, z + Game.tile_size * 0.04, Game.tile_size * 0.92, Game.tile_size * 0.92, Game.tile_size * 0.92, o, false, cx, cy, cz);

        Drawing.drawing.setColor(outlineColorR, outlineColorG, outlineColorB);
        double thickness = 2;
        double dist = Game.tile_size / 2 - thickness / 2;

        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX, this.posY - dist, z, Game.tile_size, thickness, thickness, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX, this.posY + dist, z, Game.tile_size, thickness, thickness, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX - dist, this.posY, z, thickness, Game.tile_size, thickness, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX + dist, this.posY, z, thickness, Game.tile_size, thickness, (byte) 0, true, cx, cy, cz);

        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX, this.posY - dist, z + Game.tile_size - thickness, Game.tile_size, thickness, thickness, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX, this.posY + dist, z + Game.tile_size - thickness, Game.tile_size, thickness, thickness, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX - dist, this.posY, z + Game.tile_size - thickness, thickness, Game.tile_size, thickness, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX + dist, this.posY, z + Game.tile_size - thickness, thickness, Game.tile_size, thickness, (byte) 0, true, cx, cy, cz);

        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX - dist, this.posY - dist, z, thickness, thickness, Game.tile_size, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX + dist, this.posY - dist, z, thickness, thickness, Game.tile_size, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX - dist, this.posY + dist, z, thickness, thickness, Game.tile_size, (byte) 0, true, cx, cy, cz);
        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX + dist, this.posY + dist, z, thickness, thickness, Game.tile_size, (byte) 0, true, cx, cy, cz);
    }

    public void initialize()
    {
        double f = (Math.log(beatFrequency) / Math.log(2) / 8.0 + (alternate ? 0.5 : 0) + 0.4) % 1.0;
        double[] col = Game.getRainbowColor(f);
        this.colorR = col[0] * 0.75 + 255 * 0.25;
        this.colorG = col[1] * 0.75 + 255 * 0.25;
        this.colorB = col[2] * 0.75 + 255 * 0.25;

        this.update = true;
        this.tankCollision = false;
        this.bulletCollision = false;

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = this.colorR;
            this.stackColorG[i] = this.colorG;
            this.stackColorB[i] = this.colorB;
        }

        col = Game.getRainbowColor((f + 0.1) % 1.0);
        this.outlineColorR = col[0] * 0.75 + 0 * 0.25;
        this.outlineColorG = col[1] * 0.75 + 0 * 0.25;
        this.outlineColorB = col[2] * 0.75 + 0 * 0.25;
    }

    @Override
    public void drawForInterface(double x, double y)
    {
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, this.colorA);
        Drawing.drawing.fillInterfaceRect(x, y, draw_size, draw_size);

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        Drawing.drawing.fillInterfaceRect(x, y, draw_size * 0.9, draw_size * 0.9);
    }

    @Override
    public void setMetadata(String s)
    {
        if (s.contains("#"))
        {
            String[] p = s.split("#");
            this.beatPattern = (int) Double.parseDouble(p[0]);
            this.stackHeight = Double.parseDouble(p[1]);
        }
        else
            this.beatPattern = (int) Double.parseDouble(s);

        this.refreshMetadata();
    }

    @Override
    public void refreshMetadata()
    {
        this.rendererNumber = this.beatPattern;
        this.tileRendererNumber = this.beatPattern;
        this.alternate = this.beatPattern % 2 == 1;
        this.beatFrequency = Math.pow(2, this.beatPattern / 2);
        this.initialize();
    }

    @Override
    public String getMetadata()
    {
        return this.beatPattern + "#" + this.stackHeight;
    }
}
