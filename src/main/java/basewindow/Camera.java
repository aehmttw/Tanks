package basewindow;

import basewindow.transformation.Matrix4;

public class Camera
{
    public enum Projection
    {
        perspective,
        orthographic
    }

    public static class Bounds
    {
        public double left;
        public double right;
        public double top;
        public double bottom;

        public Bounds(double left, double right, double top, double bottom)
        {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public Bounds set(double left, double right, double top, double bottom)
        {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
            return this;
        }

        public boolean intersects(double left, double top, double right, double bottom)
        {
            return right >= this.left && left <= this.right && bottom >= this.top && top <= this.bottom;
        }
    }

    public Matrix4 projection = new Matrix4();
    public Matrix4 view = new Matrix4();

    public double posX;
    public double posY;
    public double posZ;
    public double referenceZ;
    public Projection projectionType = Projection.perspective;
    public double orthoZoom = 1.0;

    public double currentAspectRatio;

    private double baseSizeX;
    private double baseSizeY;
    private double baseAspectRatio;

    private double nearClip = 1.0;
    private double farClip = 10000.0;
    private final Bounds reusableBounds = new Bounds(0, 0, 0, 0);

    public String name;

    public Camera(String name, double l, double r, double t, double b, double z)
    {
        this.name = name;
        this.resetToBounds(l, r, t, b, z);
    }

    /**
     * The camera is set such that it is located at z-position z, facing z=0.
     * On the z=0 plane, the camera sees anything with x between l and r, and y between t and b.
     *
     * @param l Left x bound
     * @param r Right x bound
     * @param t Top y bound
     * @param b Bottom y bound
     * @param z Z position of the camera
     */
    public void resetToBounds(double l, double r, double t, double b, double z)
    {
        this.posX = (l + r) / 2.0;
        this.posY = (t + b) / 2.0;
        this.posZ = z;
        this.referenceZ = z;
        this.orthoZoom = 1.0;

        this.setBaseSize(l, r, t, b);
        this.currentAspectRatio = this.baseAspectRatio;

        updateProjection();
        updateView();
    }

    public void updateSize(double l, double r, double t, double b, double z)
    {
        this.setBaseSize(l, r, t, b);
        this.currentAspectRatio = this.baseAspectRatio;

        updateProjection();
        updateView();
    }

    protected void setBaseSize(double l, double r, double t, double b)
    {
        this.baseSizeX = r - l;
        this.baseSizeY = b - t;
        this.baseAspectRatio = this.baseSizeX / this.baseSizeY;
    }

    public void setAspectRatio(double newAspectRatio)
    {
        this.currentAspectRatio = newAspectRatio;
        updateProjection();
    }

    public void setReferenceZ(double newReferenceZ)
    {
        this.referenceZ = newReferenceZ;
        updateProjection();
    }

    public void setProjectionType(Projection projectionType)
    {
        this.projectionType = projectionType;
        updateProjection();
    }

    public void setClipPlanes(double nearClip, double farClip)
    {
        this.nearClip = Math.max(0.0001, nearClip);
        this.farClip = Math.max(this.nearClip + 0.0001, farClip);
        this.updateProjection();
    }

    public double getNearClip()
    {
        return this.nearClip;
    }

    public double getFarClip()
    {
        return this.farClip;
    }

    public void setZoom(double zoom)
    {
        double z = Math.max(0.0001, zoom);

        if (this.projectionType == Projection.orthographic)
            this.orthoZoom = z;
        else
            this.posZ = this.referenceZ / z;

        updateProjection();
        updateView();
    }

    public double getZoom()
    {
        if (this.projectionType == Projection.orthographic)
            return this.orthoZoom;

        return this.referenceZ / Math.max(0.0001, this.posZ);
    }

    public void updateProjection()
    {
        double currentSizeX = this.getCurrentSizeX();
        double currentSizeY = this.getCurrentSizeY();

        if (this.projectionType == Projection.orthographic)
        {
            double zoom = Math.max(0.0001, this.orthoZoom);
            this.projection.setOrtho(
                    -currentSizeX / (2.0 * zoom), currentSizeX / (2.0 * zoom),
                    currentSizeY / (2.0 * zoom), -currentSizeY / (2.0 * zoom),
                    this.nearClip, this.farClip
            );
        }
        else
        {
            // To map the bounds exactly at z=0, we scale the bounds down to the near clip plane
            double scale = this.nearClip / this.referenceZ;

            double halfW = (currentSizeX / 2.0) * scale;
            double halfH = (currentSizeY / 2.0) * scale;

            this.projection.setFrustum(
                    -halfW, halfW,
                    halfH, -halfH,
                    this.nearClip, this.farClip
            );
        }
    }

    public double getCurrentSizeX()
    {
        if (this.currentAspectRatio > this.baseAspectRatio)
            return this.baseSizeY * this.currentAspectRatio;
        else
            return this.baseSizeX;
    }

    public double getCurrentSizeY()
    {
        if (this.currentAspectRatio > this.baseAspectRatio)
            return this.baseSizeY;
        else
            return this.baseSizeX / this.currentAspectRatio;
    }

    public double getDepthScale(double worldZ)
    {
        if (this.projectionType == Projection.orthographic)
            return 1.0 / Math.max(0.0001, this.orthoZoom);

        return (this.posZ - worldZ) / this.referenceZ;
    }

    public double getVisibleSizeX(double worldZ)
    {
        return this.getHalfWidthAtDepth(worldZ) * 2.0;
    }

    public double getVisibleSizeY(double worldZ)
    {
        return this.getHalfHeightAtDepth(worldZ) * 2.0;
    }

    public double getHalfWidthAtDepth(double worldZ)
    {
        return this.getCurrentSizeX() * this.getDepthScale(worldZ) / 2.0;
    }

    public double getHalfHeightAtDepth(double worldZ)
    {
        return this.getCurrentSizeY() * this.getDepthScale(worldZ) / 2.0;
    }

    public Bounds getBoundsAtDepth(double worldZ)
    {
        double halfWidth = this.getHalfWidthAtDepth(worldZ);
        double halfHeight = this.getHalfHeightAtDepth(worldZ);
        return this.reusableBounds.set(this.posX - halfWidth, this.posX + halfWidth, this.posY - halfHeight, this.posY + halfHeight);
    }

    public void updateView()
    {
        this.view.identity();
        this.view.translate(-this.posX, -this.posY, -this.posZ);
    }
}
