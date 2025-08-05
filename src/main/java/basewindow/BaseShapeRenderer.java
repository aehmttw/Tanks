package basewindow;

public abstract class BaseShapeRenderer
{
    public static final byte HIDE_BOTTOM = 1;
    public static final byte HIDE_TOP = 1 << 1;
    public static final byte HIDE_FRONT = 1 << 2;
    public static final byte HIDE_BACK = 1 << 3;
    public static final byte HIDE_LEFT = 1 << 4;
    public static final byte HIDE_RIGHT = 1 << 5;
    public static final byte HIDE_ALL = 63;

    public abstract void fillOval(double x, double y, double sX, double sY);

    public abstract void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillPartialOval(double x, double y, double sX, double sY, double start, double end);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, double oZ, boolean depthTest);

    public abstract void fillPartialRing(double x, double y, double size, double thickness, double start, double end);

    public abstract void fillPartialRing(double x, double y, double z, double size, double thickness, double start, double end);

    public abstract void fillGlow(double x, double y, double sX, double sY);

    public abstract void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillGlow(double x, double y, double sX, double sY, boolean shade);

    public abstract void fillGlow(double x, double y, double sX, double sY, boolean shade, boolean light);

    public abstract void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade);

    public abstract void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light);

    public abstract void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade);

    public abstract void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light);

    public abstract void drawOval(double x, double y, double sX, double sY);

    public abstract void drawOval(double x, double y, double z, double sX, double sY);

    public abstract void fillRect(double x, double y, double sX, double sY);

    public abstract void fillRect(double x, double y, double sX, double sY, double radius);

    public abstract void fillBox(double x, double y, double z, double sX, double sY, double sZ, String texture);

    public abstract void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options, String texture);

    public abstract void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4);

    public abstract void fillQuadBox(double x1, double y1,
                                     double x2, double y2,
                                     double x3, double y3,
                                     double x4, double y4,
                                     double z, double sZ,
                                     byte options);

    public abstract void drawRect(double x, double y, double sX, double sY);

    public abstract void drawRect(double x, double y, double sX, double sY, double borderWidth);

    public abstract void drawRect(double x, double y, double sX, double sY, double borderWidth, double borderRadius);

    public abstract void drawImage(double x, double y, double sX, double sY, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest);

    public abstract void drawImage(double x, double y, double sX, double sY, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled, boolean depthtest);

    public abstract void setBatchMode(boolean enabled, boolean quads, boolean depth);

    public abstract void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow);

    public abstract void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow, boolean depthMask);
}
