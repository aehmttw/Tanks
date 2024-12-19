package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.leveleditor.selector.SelectorColor;
import tanks.rendering.ShaderGroundColor;
import tanks.tankson.MetadataProperty;

public class ObstacleColor extends Obstacle
{
    @MetadataProperty(id = "color", name = "Color", selector = SelectorColor.selector_name, image = "color.png", keybind = "editor.groupID")
    public int color = 16777215;

    public ObstacleColor(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;

        this.type = ObstacleType.ground;
        this.update = true;

        this.replaceTiles = true;
        this.tileRenderer = ShaderGroundColor.class;

        this.refreshMetadata();

        this.primaryMetadataID = "color";

        this.description = "A decorative ground tile whose color can be customized";
    }

    @Override
    public void draw3dOutline(double r, double g, double b, double a)
    {
        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(this.posX, this.posY, 0, Obstacle.draw_size, Obstacle.draw_size, 10);
    }

    @Override
    public double getTileHeight()
    {
        return 0;
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
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
        Drawing.drawing.fillBox(tile, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d);
    }

    @Override
    public String getMetadata()
    {
        return Integer.toHexString(this.color);
    }

    @Override
    public void setMetadata(String meta)
    {
        this.color = Integer.parseInt(meta, 16);
        this.refreshMetadata();
    }

    @Override
    public void refreshMetadata()
    {
        this.colorR = this.color / (256 * 256) % 256;
        this.colorG = this.color / (256) % 256;
        this.colorB = this.color % 256;
    }
}
