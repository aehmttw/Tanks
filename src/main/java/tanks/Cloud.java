package tanks;

import java.util.ArrayList;

public class Cloud extends Movable
{
    public ArrayList<Double> posX = new ArrayList<>();
    public ArrayList<Double> posY = new ArrayList<>();
    public double posZ = Math.random() * 100 + 500;
    public double size = Math.random() * 300 + 100;

    public Cloud(double x, double y)
    {
        super(x, y);

        this.drawLevel = 8;
        for (int i = 0; i < 5; i++)
        {
            this.posX.add(x + Math.random() * 100);
            this.posY.add(y + Math.random() * 100);
        }
    }

    @Override
    public void draw()
    {
        if (!Game.followingCam || !Game.enable3d || !Drawing.drawing.movingCamera)
            return;

        for (int i = 0; i < this.posY.size(); i++)
        {
            Drawing.drawing.setColor(255 * Level.currentLightIntensity, 255 * Level.currentLightIntensity, 255 * Level.currentLightIntensity, 255);
            Drawing.drawing.fillBox(this.posX.get(i), this.posY.get(i), this.posZ, size, size, 30, (byte) 0);
        }
    }

    @Override
    public void update()
    {
        ArrayList<Double> newXs = new ArrayList<>();
        for (double x : this.posX)
            newXs.add(x - Panel.frameFrequency / 2);

        this.posX = newXs;

        if (this.posX.get(0) < 0 || this.posX.get(0) > Game.currentSizeX * 50)
            Game.removeClouds.add(this);
    }
}
