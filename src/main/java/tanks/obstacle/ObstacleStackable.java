package tanks.obstacle;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.gui.screen.leveleditor.selector.SelectorStackHeight;
import tanks.rendering.ShaderGroundObstacle;
import tanks.tankson.MetadataProperty;

public class ObstacleStackable extends Obstacle
{
    public static final int default_max_height = 8;

    @MetadataProperty(id="stack_height", name = "Block height", image="obstacle_height.png", selector = SelectorStackHeight.selector_name, keybind = "editor.height")
    public double stackHeight = 1;
    public double startHeight = 0;

    public double[] stackColorR = new double[default_max_height];
    public double[] stackColorG = new double[default_max_height];
    public double[] stackColorB = new double[default_max_height];

    public ObstacleStackable(String name, double posX, double posY)
    {
        super(name, posX, posY);
        double[] col = getRandomColor();

        this.primaryMetadataID = "stack_height";

        this.colorR = col[0];
        this.colorG = col[1];
        this.colorB = col[2];

        for (int i = 0; i < default_max_height; i++)
        {
            double[] col2;

            if (i != 0)
                col2 = getRandomColor();
            else
                col2 = col;

            this.stackColorR[i] = col2[0];
            this.stackColorG[i] = col2[1];
            this.stackColorB[i] = col2[2];
        }

        this.tileRenderer = ShaderGroundObstacle.class;
        this.description = "A solid block which can be destroyed by mines";
    }

    public double[] getRandomColor()
    {
        double colorMul = Math.random() * 0.5 + 0.5;
        double[] col = new double[3];

        if (Game.fancyTerrain)
        {
            col[0] = (colorMul * (176 - Math.random() * 70));
            col[1] = (colorMul * (111 - Math.random() * 34));
            col[2] = (colorMul * 14);
        }
        else
            col = new double[]{87, 46, 8};

        return col;
    }

    @Override
    public void draw()
    {
        if (this.stackHeight <= 0)
            return;

        Drawing drawing = Drawing.drawing;

        drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA, this.glow);

        if (Game.enable3d)
            drawStacks();
        else
            drawing.fillRect(this, this.posX, this.posY, draw_size, draw_size);
    }

    public void drawStacks()
    {
        for (int i = 0; i < Math.min(stackHeight, default_max_height); i++)
        {
            byte o = this.getOptionsByte((i + 1) * Game.tile_size);
            Drawing.drawing.setColor(this.stackColorR[i], this.stackColorG[i], this.stackColorB[i], this.colorA, this.glow);
            Drawing.drawing.fillBox(this, this.posX, this.posY, i * Game.tile_size + this.startHeight * Game.tile_size, draw_size, draw_size, draw_size * Math.min(1, stackHeight - i), o);
        }
    }

    @Override
    public void draw3dOutline(double r, double g, double b, double a)
    {
        if (!Game.enable3d)
        {
            drawOutline(r, g, b, a);
            return;
        }

        double sizeZ = this.stackHeight * Game.tile_size;

        Drawing.drawing.setColor(r, g, b, a, 0.5);
        Drawing.drawing.fillBox(this.posX, this.posY, this.startHeight * Game.tile_size,
                Game.tile_size + 1, Game.tile_size + 1, sizeZ + 1, (byte) 0);
    }

    @Override
    public String getMetadata()
    {
        if (this.startHeight > 0)
            return this.stackHeight + "-" + this.startHeight;
        else if (this.stackHeight != 1)
            return this.stackHeight + "";
        else
            return "";
    }

    @Override
    public void setMetadata(String s)
    {
        if (s.isEmpty())
        {
            this.stackHeight = 1;
            return;
        }

        String[] metadata = s.split("-");

        if (metadata.length >= 2)
            this.startHeight = Double.parseDouble(metadata[1]);

        if (metadata.length >= 1)
            this.stackHeight = Double.parseDouble(metadata[0]);

        this.refreshMetadata();
    }

    @Override
    public double getTileHeight()
    {
        if (Obstacle.draw_size < Game.tile_size || this.startHeight > 1)
            return 0;

        return this.stackHeight * Game.tile_size;
    }

    @Override
    public void playDestroyAnimation(double posX, double posY, double radius)
    {
        if (Game.effectsEnabled)
        {
            Effect.EffectType effect = this.destroyEffect;
            double freq = Math.min((Math.sqrt(Math.pow(posX - this.posX, 2) + Math.pow(posY - this.posY, 2)) + Game.tile_size * 2.5) / radius, 1);

            if (Game.enable3d)
            {
                if (effect == Effect.EffectType.obstaclePiece)
                    effect = Effect.EffectType.obstaclePiece3d;

                double h = 0;
                if (this.stackHeight % 1.0 != 0)
                    h = this.stackHeight % 1.0 - 1.0;

                for (; h < this.stackHeight; h++)
                {
                    int block = (int) (default_max_height - 1 - h);
                    destroyAnimation3d(this.posX, this.posY, Math.max(h, 0) * Game.tile_size, posX, posY, Game.tile_size - Math.min(h, 0), effect, this.destroyEffectAmount * freq * freq, radius, this.stackColorR[block], this.stackColorG[block], this.stackColorB[block]);
                }
            }
            else
            {
                for (int j = 0; j < Game.tile_size - 6; j += 4)
                {
                    for (int k = 0; k < Game.tile_size - 6; k += 4)
                    {
                        if (Math.random() > this.destroyEffectAmount * freq * freq * Game.effectMultiplier)
                            continue;

                        Effect e = Effect.createNewEffect(this.posX + j + 5 - Game.tile_size / 2, this.posY + k + 5 - Game.tile_size / 2, effect);

                        e.setColor(this.colorR, this.colorG, this.colorB);

                        double dist = Movable.distanceBetween(this, e);
                        double angle = Movable.getPolarDirection(e.posX - posX, e.posY - posY);
                        double rad = radius - Game.tile_size / 2;
                        e.addPolarMotion(angle, (rad * Math.sqrt(2) - dist) / (rad * 2) + Math.random() * 2);

                        Game.effects.add(e);
                    }
                }
            }
        }
    }

    public static void destroyAnimation3d(double x, double y, double z, double posX, double posY, double height, Effect.EffectType effect, double freq, double radius, double r, double g, double b)
    {
        double s = 12.5;
        for (double j = 0; j < Game.tile_size; j += s)
        {
            for (double k = 0; k < Game.tile_size; k += s)
            {
                for (double l = 0; l < height; l += s)
                {
                    if (Math.random() > freq * Game.effectMultiplier)
                        continue;

                    Effect e = Effect.createNewEffect(x + j + s / 2 - Game.tile_size / 2, y + k + s / 2 - Game.tile_size / 2, l + z, effect);
                    e.setColor(r, g, b);

                    double dist = Math.sqrt(Math.pow(posX - x, 2) + Math.pow(posY - y, 2));
                    double angle = (Math.random() - 0.5) * 0.1 + Movable.getPolarDirection(e.posX - posX, e.posY - posY);
                    double rad = radius - Game.tile_size / 2;
                    double v = (rad * Math.sqrt(2) - dist) / (rad * 2);
                    e.addPolarMotion(angle, (v + Math.random() * 2) * 1.5);
                    e.vZ = 1.5 * (v + Math.random() * 2);

                    Game.effects.add(e);
                }
            }
        }
    }
}
