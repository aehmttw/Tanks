package basewindow.transformation;

/**
 * Mutable, normalized rotation quaternion.  The scalar component is kept
 * non-negative so axis-angle conversion has a stable angle in [0, pi].
 */
public class Quaternion
{
    public double x;
    public double y;
    public double z;
    public double w;

    public static class AxisAngle
    {
        public double x;
        public double y;
        public double z;
        public double angle;

        public AxisAngle(double x, double y, double z, double angle)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.angle = angle;
        }
    }

    public Quaternion()
    {
        this.setIdentity();
    }

    public Quaternion(double x, double y, double z, double w)
    {
        this.set(x, y, z, w);
    }

    public Quaternion setIdentity()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 1;
        return this;
    }

    public Quaternion set(Quaternion other)
    {
        return this.set(other.x, other.y, other.z, other.w);
    }

    public Quaternion set(double x, double y, double z, double w)
    {
        double length = Math.sqrt(x * x + y * y + z * z + w * w);

        if (length < 0.0000001)
            return this.setIdentity();

        double sign = w < 0 ? -1 : 1;
        this.x = x * sign / length;
        this.y = y * sign / length;
        this.z = z * sign / length;
        this.w = w * sign / length;
        return this;
    }

    public Quaternion setAxisAngle(double x, double y, double z, double angle)
    {
        double length = Math.sqrt(x * x + y * y + z * z);

        if (length < 0.0000001)
            return this.setIdentity();

        double sine = Math.sin(angle / 2.0) / length;
        return this.set(x * sine, y * sine, z * sine, Math.cos(angle / 2.0));
    }

    /** Applies {@code other} in this quaternion's local coordinate system. */
    public Quaternion multiply(Quaternion other)
    {
        return this.set(
                this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y,
                this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x,
                this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w,
                this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z);
    }

    /** Applies {@code other} in world coordinates before this rotation. */
    public Quaternion premultiply(Quaternion other)
    {
        return this.set(
                other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y,
                other.w * this.y - other.x * this.z + other.y * this.w + other.z * this.x,
                other.w * this.z + other.x * this.y - other.y * this.x + other.z * this.w,
                other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z);
    }

    /** Applies an axis-angle rotation in world coordinates without allocating an intermediate quaternion. */
    public Quaternion premultiplyAxisAngle(double x, double y, double z, double angle)
    {
        double length = Math.sqrt(x * x + y * y + z * z);

        if (length < 0.0000001)
            return this;

        double sine = Math.sin(angle / 2.0) / length;
        double rotationX = x * sine;
        double rotationY = y * sine;
        double rotationZ = z * sine;
        double rotationW = Math.cos(angle / 2.0);

        return this.set(
                rotationW * this.x + rotationX * this.w + rotationY * this.z - rotationZ * this.y,
                rotationW * this.y - rotationX * this.z + rotationY * this.w + rotationZ * this.x,
                rotationW * this.z + rotationX * this.y - rotationY * this.x + rotationZ * this.w,
                rotationW * this.w - rotationX * this.x - rotationY * this.y - rotationZ * this.z);
    }

    public AxisAngle toAxisAngle()
    {
        return this.getAxisAngle(new AxisAngle(0, 0, 1, 0));
    }

    /** Writes this quaternion's axis-angle representation into {@code result}. */
    public AxisAngle getAxisAngle(AxisAngle result)
    {
        double sine = Math.sqrt(Math.max(0, 1 - this.w * this.w));

        if (sine < 0.0000001)
        {
            result.x = 0;
            result.y = 0;
            result.z = 1;
            result.angle = 0;
            return result;
        }

        result.x = this.x / sine;
        result.y = this.y / sine;
        result.z = this.z / sine;
        result.angle = 2 * Math.acos(this.w);
        return result;
    }
}
