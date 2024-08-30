package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.leveleditor.OverlayEditorMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.rendering.ShaderBeatBlocks;
import tanks.rendering.ShaderGroundObstacleBeatBlock;

public class ObstacleBeatBlock extends Obstacle
{
    public double beatFrequency = 1;
    public boolean alternate;

    public double outlineColorR;
    public double outlineColorG;
    public double outlineColorB;

    protected boolean lastOn = false;

    public ObstacleBeatBlock(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.initialize();
        this.renderer = ShaderBeatBlocks.class;
        this.tileRenderer = ShaderGroundObstacleBeatBlock.class;
        this.destructible = false;

        this.description = "A block that appears and disappears to the beat of the music";
    }

    public static boolean isOn(double freq, boolean alt)
    {
        double pos = Game.screen.screenAge;
        if (Game.screen instanceof ScreenGame)
            pos = ScreenGame.lastTimePassed - ((ScreenGame) Game.screen).introBattleMusicEnd / 10.0;
        else if (Game.screen instanceof ScreenLevelEditorOverlay || Game.screen instanceof ScreenLevelEditor)
            pos = Game.game.window.soundPlayer.getMusicPos() * 100;

        pos /= 100.0;
        return ((int) ((pos + 6.0) / (6.0 / freq)) % 2 == (alt ? 1 : 0));
    }

    public static double timeTillChange(double freq)
    {
        double pos = Game.screen.screenAge;
        if (Game.screen instanceof ScreenGame)
            pos = ScreenGame.lastTimePassed - ((ScreenGame) Game.screen).introBattleMusicEnd / 10.0;
        else if (Game.screen instanceof ScreenLevelEditorOverlay || Game.screen instanceof ScreenLevelEditor)
            pos = Game.game.window.soundPlayer.getMusicPos() * 100;

        return 600 / freq - pos % (600.0 / freq);
    }

    @Override
    public void update()
    {
        boolean on = isOn(this.beatFrequency, this.alternate);
        this.bulletCollision = on;
        this.tankCollision = on;

        if (this.tankCollision != lastOn)
        {
            int x = (int) (this.posX / Game.tile_size);
            int y = (int) (this.posY / Game.tile_size);

            if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
            {
                Game.game.solidGrid[x][y] = this.tankCollision;
            }
            this.verticalFaces = null;
            this.horizontalFaces = null;
            this.allowBounce = false;

            this.lastOn = this.tankCollision;
        }
        else
            this.allowBounce = true;
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

        double size = this.tankCollision ? draw_size : 10;
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
        else //TODO
            drawing.fillRect(this, this.posX, this.posY, size, size);
    }

    public void drawBox(double z, byte o)
    {
        float cx = (float) this.posX;
        float cy = (float) this.posY;
        float h = (float) (Game.sampleGroundHeight(this.posX, this.posY));
        float cz = (float) (z + h);

        Drawing.drawing.terrainRenderer.addBoxWithCenter(this, this.posX, this.posY, z, Game.tile_size, Game.tile_size, Game.tile_size, o, false, cx, cy, cz);

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
        double f = (Math.log(beatFrequency) / 6.0 + (alternate ? 0.5 : 0) + 0.4) % 1.0;
        double[] col = Game.getRainbowColor(f);
        this.colorR = col[0] * 0.75 + 255 * 0.25;
        this.colorG = col[1] * 0.75 + 255 * 0.25;
        this.colorB = col[2] * 0.75 + 255 * 0.25;

        this.update = true;
        this.tankCollision = false;
        this.bulletCollision = false;

        this.enableGroupID = true;
        this.enableStacking = false;

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
    public void setMetadata(String s)
    {
        this.groupID = (int) Double.parseDouble(s);
        this.rendererNumber = this.groupID;
        this.tileRendererNumber = this.groupID;
        this.alternate = this.groupID % 2 == 1;
        this.beatFrequency = Math.pow(2, this.groupID / 2);
        this.initialize();
    }

}
