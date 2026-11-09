package basewindow;

import basewindow.transformation.Matrix4;
import basewindow.transformation.Quaternion;

public class Camera
{
    public enum Projection
    {
        perspective,
        orthographic
    }

    public static class Bounds
    {
        /** World-space rectangular bounds, used primarily for culling. */
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

    /** Projection from camera space into clip space. */
    public Matrix4 projection = new Matrix4();
    /** Complete world-to-camera transform, including the optional adjustments below. */
    public Matrix4 view = new Matrix4();
    /** Applied to world points before the inverse camera pose; uses world units. */
    public final Matrix4 worldAdjustment = new Matrix4();
    /** Applied after the inverse camera pose; uses camera-space units. */
    public final Matrix4 cameraAdjustment = new Matrix4();
    private final Matrix4 poseView = new Matrix4();
    private final Matrix4 adjustedPoseView = new Matrix4();
    /** Inverse of {@link #view}, used when shaders need world-space positions. */
    public Matrix4 inverseView = new Matrix4();

    /** World-space position of the camera. */
    public double posX;
    public double posY;
    public double posZ;
    /** Camera-local to world-space rotation. */
    private final Quaternion orientation = new Quaternion();
    /** Perspective camera distance that represents zoom 1 at the reference plane. */
    public double referenceZ;
    public Projection projectionType = Projection.perspective;
    /** Orthographic magnification; unused by perspective projections. */
    public double orthoZoom = 1.0;

    /** Viewport aspect ratio used to expand the visible bounds without cropping. */
    public double currentAspectRatio;

    /** Unexpanded dimensions supplied by the caller before aspect-ratio adjustment. */
    private double baseSizeX;
    private double baseSizeY;
    private double baseAspectRatio;

    private double nearClip = 1.0;
    private double farClip = 10000.0;
    private double viewportBottomExtension;

    /**
     * Adds visible space below the nominal viewport, as a fraction of its height.
     * Drawing into the correspondingly taller viewport preserves the original pixel
     * scale and top edge. Nominal size/zoom queries remain unchanged; bounds include
     * the extension through unprojection.
     */
    public void setViewportBottomExtension(double extension)
    {
        double value = Math.max(0, extension);
        if (this.viewportBottomExtension == value)
            return;

        this.viewportBottomExtension = value;
        this.updateProjection();
    }

    /** Reused result for bounds queries; callers must not retain it. */
    private final Bounds reusableBounds = new Bounds(0, 0, 0, 0);
    /** Reused point for rotated camera bounds; callers must not retain it. */
    private final double[] reusablePoint = new double[2];
    private final double[] reusableNearPoint = new double[3];
    private final double[] reusableFarPoint = new double[3];
    private final Matrix4 reusableViewProjection = new Matrix4();
    private final Matrix4 reusableInverseViewProjection = new Matrix4();

    public String name;
    private long matrixRevision;

    /** Changes to public pose/adjustment fields take effect after updateView(). */
    public long getMatrixRevision()
    {
        return this.matrixRevision;
    }

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

    public Quaternion getOrientation()
    {
        return this.orientation;
    }

    public Quaternion.AxisAngle getRotationAxisAngle()
    {
        return this.orientation.toAxisAngle();
    }

    /** Writes the current axis-angle rotation into {@code result} without allocating. */
    public Quaternion.AxisAngle getRotationAxisAngle(Quaternion.AxisAngle result)
    {
        return this.orientation.getAxisAngle(result);
    }

    public void setOrientation(Quaternion orientation)
    {
        if (orientation == null)
            throw new IllegalArgumentException("Camera orientation cannot be null");

        this.orientation.set(orientation);
        this.updateView();
    }

    public void setRotationAxisAngle(double x, double y, double z, double angle)
    {
        this.orientation.setAxisAngle(x, y, z, angle);
        this.updateView();
    }

    /** Applies a rotation around a world-space axis. */
    public void rotateAxisAngle(double x, double y, double z, double angle)
    {
        this.orientation.premultiplyAxisAngle(x, y, z, angle);
        this.updateView();
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
        this.matrixRevision++;
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

        if (this.viewportBottomExtension > 0)
        {
            double scale = 1 / (1 + this.viewportBottomExtension);
            for (int col = 0; col < 4; col++)
                this.projection.values[1][col] = scale * this.projection.values[1][col] +
                        (1 - scale) * this.projection.values[3][col];
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
        if (this.orientation.x != 0 || this.orientation.y != 0 || this.orientation.z != 0 ||
                !this.hasIdentityAdjustments())
        {
            this.getWorldPointAtDepth(0, 0, worldZ, this.reusablePoint);
            double left = this.reusablePoint[0];
            double right = left;
            double top = this.reusablePoint[1];
            double bottom = top;

            this.getWorldPointAtDepth(1, 0, worldZ, this.reusablePoint);
            left = Math.min(left, this.reusablePoint[0]);
            right = Math.max(right, this.reusablePoint[0]);
            top = Math.min(top, this.reusablePoint[1]);
            bottom = Math.max(bottom, this.reusablePoint[1]);

            this.getWorldPointAtDepth(0, 1, worldZ, this.reusablePoint);
            left = Math.min(left, this.reusablePoint[0]);
            right = Math.max(right, this.reusablePoint[0]);
            top = Math.min(top, this.reusablePoint[1]);
            bottom = Math.max(bottom, this.reusablePoint[1]);

            this.getWorldPointAtDepth(1, 1, worldZ, this.reusablePoint);
            return this.reusableBounds.set(Math.min(left, this.reusablePoint[0]), Math.max(right, this.reusablePoint[0]),
                    Math.min(top, this.reusablePoint[1]), Math.max(bottom, this.reusablePoint[1]));
        }

        double halfWidth = this.getHalfWidthAtDepth(worldZ);
        double halfHeight = this.getHalfHeightAtDepth(worldZ);
        return this.reusableBounds.set(this.posX - halfWidth, this.posX + halfWidth,
                this.posY - halfHeight, this.posY + halfHeight * (1 + 2 * this.viewportBottomExtension));
    }

    /**
     * Finds the world-space ground point beneath a normalized viewport coordinate.
     * The coordinate origin is at the top-left of the viewport.
     */
    public void getWorldPointAtDepth(double x, double y, double worldZ, double[] result)
    {
        this.projection.multiply(this.view, this.reusableViewProjection).inverse(this.reusableInverseViewProjection);
        this.unproject(this.reusableInverseViewProjection, x, y, -1, this.reusableNearPoint);
        this.unproject(this.reusableInverseViewProjection, x, y, 1, this.reusableFarPoint);
        double dz = this.reusableFarPoint[2] - this.reusableNearPoint[2];
        double fraction = Math.abs(dz) < 0.0000001 ? 0 : (worldZ - this.reusableNearPoint[2]) / dz;

        result[0] = this.reusableNearPoint[0] + (this.reusableFarPoint[0] - this.reusableNearPoint[0]) * fraction;
        result[1] = this.reusableNearPoint[1] + (this.reusableFarPoint[1] - this.reusableNearPoint[1]) * fraction;
    }

    private void unproject(Matrix4 matrix, double x, double y, double z, double[] result)
    {
        double clipX = x * 2 - 1;
        double clipY = 1 - y * 2;
        double worldX = matrix.values[0][0] * clipX + matrix.values[0][1] * clipY + matrix.values[0][2] * z + matrix.values[0][3];
        double worldY = matrix.values[1][0] * clipX + matrix.values[1][1] * clipY + matrix.values[1][2] * z + matrix.values[1][3];
        double worldZ = matrix.values[2][0] * clipX + matrix.values[2][1] * clipY + matrix.values[2][2] * z + matrix.values[2][3];
        double worldW = matrix.values[3][0] * clipX + matrix.values[3][1] * clipY + matrix.values[3][2] * z + matrix.values[3][3];

        result[0] = worldX / worldW;
        result[1] = worldY / worldW;
        result[2] = worldZ / worldW;
    }

    public void updateView()
    {
        this.matrixRevision++;
        this.setRotation(this.poseView, -this.orientation.x, -this.orientation.y, -this.orientation.z, this.orientation.w);
        for (int row = 0; row < 3; row++)
            this.poseView.values[row][3] = -(this.poseView.values[row][0] * this.posX +
                    this.poseView.values[row][1] * this.posY + this.poseView.values[row][2] * this.posZ);

        this.cameraAdjustment.multiply(this.poseView, this.adjustedPoseView);
        this.adjustedPoseView.multiply(this.worldAdjustment, this.view);
        this.view.inverse(this.inverseView);
    }

    public void clearAdjustments()
    {
        this.worldAdjustment.setIdentity();
        this.cameraAdjustment.setIdentity();
        this.updateView();
    }

    private boolean hasIdentityAdjustments()
    {
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < 4; col++)
                if (this.worldAdjustment.values[row][col] != (row == col ? 1 : 0) ||
                        this.cameraAdjustment.values[row][col] != (row == col ? 1 : 0))
                    return false;
        return true;
    }

    private void setRotation(Matrix4 matrix, Quaternion rotation)
    {
        this.setRotation(matrix, rotation.x, rotation.y, rotation.z, rotation.w);
    }

    private void setRotation(Matrix4 matrix, double x, double y, double z, double w)
    {
        double xx = x * x;
        double yy = y * y;
        double zz = z * z;
        double xy = x * y;
        double xz = x * z;
        double yz = y * z;
        double xw = x * w;
        double yw = y * w;
        double zw = z * w;

        matrix.identity();
        matrix.values[0][0] = 1 - 2 * (yy + zz);
        matrix.values[0][1] = 2 * (xy - zw);
        matrix.values[0][2] = 2 * (xz + yw);
        matrix.values[1][0] = 2 * (xy + zw);
        matrix.values[1][1] = 1 - 2 * (xx + zz);
        matrix.values[1][2] = 2 * (yz - xw);
        matrix.values[2][0] = 2 * (xz - yw);
        matrix.values[2][1] = 2 * (yz + xw);
        matrix.values[2][2] = 1 - 2 * (xx + yy);
    }
}
