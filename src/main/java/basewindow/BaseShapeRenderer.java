package basewindow;

public abstract class BaseShapeRenderer
{
    public boolean supportsBatching = false;

    public abstract void fillOval(double x, double y, double sX, double sY);

    public abstract void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillPartialOval(double x, double y, double sX, double sY, double start, double end);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest);

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

    public abstract void fillBox(double x, double y, double z, double sX, double sY, double sZ);

    public abstract void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options);

    public abstract void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4);

    public abstract void fillQuadBox(double x1, double y1,
                                     double x2, double y2,
                                     double x3, double y3,
                                     double x4, double y4,
                                     double z, double sZ,
                                     byte options);

    public abstract void drawRect(double x, double y, double sX, double sY);

    public abstract void drawRect(double x, double y, double sX, double sY, double lineWidth);

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
