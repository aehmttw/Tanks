package tanks.modapi.menus;

import tanks.Drawing;
import tanks.Panel;
import tanks.modapi.ModAPI;

public class TransitionEffect extends FixedMenu
{
    public enum types
    {fade, fadeIn, fadeOut}

    public types type;
    public int posX;
    public int posY;
    public int sizeX;
    public int sizeY;
    public int colorR;
    public int colorB;
    public int colorG;
    public double colorA;

    public float speed;

    private boolean printed = false;
    private final long defineTime = System.currentTimeMillis();

    public TransitionEffect(types type, float speed, int r, int g, int b)
    {
        this.type = type;
        this.speed = speed;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = type.equals(types.fadeOut) ? 0 : 255;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        switch (this.type)
        {
            case fade:
                ModAPI.fixedShapes.fillBox(0, 0, 200, Panel.windowWidth, Panel.windowHeight, 1);
                this.colorA = Math.sin((System.currentTimeMillis() - defineTime) / 1575.55) * 255 + 0.043;
                break;

            case fadeIn:
                ModAPI.fixedShapes.fillBox(0, 0, 200, Panel.windowWidth, Panel.windowHeight, 1);
                this.colorA -= Panel.frameFrequency / 2 * this.speed;
                break;

            case fadeOut:
                ModAPI.fixedShapes.fillBox(0, 0, 200, Panel.windowWidth, Panel.windowHeight, 1);
                this.colorA += Panel.frameFrequency / 2 * this.speed;
                break;

            default:
                if (!printed)
                {
                    System.err.println("Invalid transition effect '" + this.type + "'");
                    printed = true;
                }
        }
    }

    @Override
    public void update()
    {
        if (this.type.equals(types.fadeOut) && this.colorA >= 255)
            ModAPI.removeMenus.add(this);

        else if ((this.type.equals(types.fadeIn) || this.type.equals(types.fade)) && this.colorA <= 0)
            ModAPI.removeMenus.add(this);
    }
}
