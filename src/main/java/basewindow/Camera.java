package basewindow;

import basewindow.transformation.Matrix4;

public class Camera
{
    public Matrix4 projection = new Matrix4();
    public Matrix4 view = new Matrix4();

    public double posX;
    public double posY;
    public double posZ;

    public double currentAspectRatio;

    private double baseSizeX;
    private double baseSizeY;
    private double baseAspectRatio;

    private double nearClip = 1.0;
    private double farClip = 10000.0;

    public Camera(double l, double r, double t, double b, double z)
    {
        this.updateSize(l, r, t, b, z);
    }

    public void updateSize(double l, double r, double t, double b, double z)
    {
        this.posX = (l + r) / 2.0;
        this.posY = (t + b) / 2.0;
        this.posZ = z;

        this.baseSizeX = r - l;
        this.baseSizeY = b - t;
        this.baseAspectRatio = this.baseSizeX / this.baseSizeY;

        this.currentAspectRatio = this.baseAspectRatio;

        updateProjection();
        updateView();
    }

    public void setAspectRatio(double newAspectRatio)
    {
        this.currentAspectRatio = newAspectRatio;
        updateProjection();
    }

    public void updateProjection()
    {
        double currentSizeX;
        double currentSizeY;

        if (this.currentAspectRatio > this.baseAspectRatio)
        {
            currentSizeY = this.baseSizeY;
            currentSizeX = currentSizeY * this.currentAspectRatio;
        }
        else
        {
            currentSizeX = this.baseSizeX;
            currentSizeY = currentSizeX / this.currentAspectRatio;
        }

        // To map the bounds exactly at z=0, we scale the bounds down to the near clip plane
        double scale = this.nearClip / this.posZ;

        double halfW = (currentSizeX / 2.0) * scale;
        double halfH = (currentSizeY / 2.0) * scale;

        this.projection.setFrustum(
                -halfW, halfW,
                halfH, -halfH,
                this.nearClip, this.farClip
        );
    }

    public void updateView()
    {
        this.view.identity();
        this.view.translate(-this.posX, -this.posY, -this.posZ);
    }
}