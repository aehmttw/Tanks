package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.gui.screen.leveleditor.selector.SelectorColorAndNoise;
import tanks.rendering.RendererDrawLayer;
import tanks.rendering.ShaderGroundColor;
import tanks.tankson.MetadataProperty;

public class ObstacleGroundPaint extends Obstacle
{
    @MetadataProperty(id = "color", name = "Color", selector = SelectorColorAndNoise.selector_name, image = "color.png", keybind = "editor.groupID")
    public long color = 16777215;

    public ObstacleGroundPaint(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;

        this.type = ObstacleType.ground;

        this.replaceTiles = true;
        this.tileRenderer = ShaderGroundColor.class;

        this.refreshMetadata();

        this.primaryMetadataID = "color";

        this.description = "A decorative ground tile whose color can be customized";
    }

    @Override
    public void draw3dOutline(double r, double g, double b, double a)
    {
        Drawing.drawing.setColor(r, g, b, a);
        Drawing.drawing.fillRect(this.posX, this.posY,  Game.sampleTerrainGroundHeight(this.posX, this.posY), Obstacle.draw_size, Obstacle.draw_size, false);
    }

    @Override
    public double getTileHeight()
    {
        return 0;
    }

    @Override
    public double getGroundHeight()
    {
        return baseGroundHeight;
    }

    @Override
    public void draw()
    {
        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
    }

    @Override
    public void drawForInterface(double x, double y)
    {
        Drawing drawing = Drawing.drawing;

        drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        drawing.fillInterfaceRect(x, y, draw_size, draw_size);
        drawing.drawInterfaceImage("icons/color.png", x, y, draw_size * 0.8, draw_size * 0.8);
    }

    @Override
    public void drawTile(IBatchRenderableObject tile, double r, double g, double b, double d, double extra)
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        Drawing.drawing.fillBox(tile, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d);
    }

    @Override
    public String getMetadata()
    {
        return Long.toHexString(this.color);
    }

    @Override
    public void setMetadata(String meta)
    {
        this.color = Long.parseLong(meta, 16);
        this.refreshMetadata();
    }

    @Override
    public void refreshMetadata()
    {
        int[] color = new int[7];
        long c = this.color;
        for (int i = 0; i < color.length; i++)
        {
            color[i] = (int) (c % 256);
            c /= 256;
        }

        double r1 = Math.random();
        double r2 = color[6] == 0 ? Math.random() : r1;
        double r3 = color[6] == 0 ? Math.random() : r1;

        this.colorR = color[2] + r1 * color[5];
        this.colorG = color[1] + r2 * color[4];
        this.colorB = color[0] + r3 * color[3];
    }
}
