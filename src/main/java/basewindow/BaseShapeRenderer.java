package basewindow;

public abstract class BaseShapeRenderer
{
    public static final byte hide_behind_face = 1;
    public static final byte hide_front_face = 2;
    public static final byte hide_low_face = 4;
    public static final byte hide_high_face = 8;
    public static final byte hide_left_face = 16;
    public static final byte hide_right_face = 32;
    public static final byte hide_all_faces = 63;
    public static final byte hide_draw_on_top = 64;

    public boolean supportsBatching = false;

    public abstract void fillOval(double x, double y, double sX, double sY);

    public abstract void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillPartialOval(double x, double y, double sX, double sY, double start, double end);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, double oZ, boolean depthTest);

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
