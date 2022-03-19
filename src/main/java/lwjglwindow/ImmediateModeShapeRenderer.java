package lwjglwindow;

import basewindow.BaseShapeRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;

public class ImmediateModeShapeRenderer extends BaseShapeRenderer
{
    public LWJGLWindow window;
    
    public ImmediateModeShapeRenderer(LWJGLWindow window)
    {
        this.window = window;
        this.supportsBatching = true;
    }
    
    public void fillOval(double x, double y, double sX, double sY)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY) / 4 + 5);

        glBegin(GL_TRIANGLE_FAN);
        for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
        {
            glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
        }

        glEnd();
    }

    public void fillGlow(double x, double y, double sX, double sY, boolean shade)
    {
        this.fillGlow(x, y, sX, sY, shade, false);
    }

    public void fillGlow(double x, double y, double sX, double sY, boolean shade, boolean light)
    {
        if (this.window.drawingShadow)
            return;

        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY) / 16 + 5);

        if (!shade)
            this.window.setGlowBlendFunc();

        if (light)
            this.window.setLightBlendFunc();

        glBegin(GL_TRIANGLES);
        double step = Math.PI * 2 / sides;

        double pX = x + Math.cos(0) * sX / 2;
        double pY = y + Math.sin(0) * sY / 2;
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            if (!shade)
                glColor3d(0, 0, 0);
            else
                glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, 0);

            glVertex2d(pX, pY);
            pX = x + Math.cos(d) * sX / 2;
            pY = y + Math.sin(d) * sY / 2;
            glVertex2d(pX, pY);

            if (!shade)
                glColor3d(this.window.colorR * this.window.colorA, this.window.colorG * this.window.colorA, this.window.colorB * this.window.colorA);
            else
                glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);

            glVertex2d(x, y);
        }

        glEnd();

        if (!shade)
            this.window.setTransparentBlendFunc();
    }

    @Override
    public void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        if (depthTest)
        {
            this.window.enableDepthtest();

            if (this.window.colorA < 1)
                glDepthMask(false);
        }

        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY + Math.max(z / 20, 0)) / 4 + 5);

        glBegin(GL_TRIANGLE_FAN);
        for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
        {
            glVertex3d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2, z);
        }

        glEnd();

        if (depthTest)
        {
            glDepthMask(true);
            this.window.disableDepthtest();
        }
    }

    @Override
    public void fillPartialOval(double x, double y, double sX, double sY, double start, double end)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY) / 4 + 5);

        glBegin(GL_TRIANGLES);
        for (double i = start; i < end; i += (end - start) / sides)
        {
            glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
            glVertex2d(x + Math.cos(i + 1) * sX / 2, y + Math.sin(i + 1) * sY / 2);
            glVertex2d(x, y);
        }

        glEnd();
    }

    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade)
    {
        this.fillGlow(x, y, z, sX, sY, depthTest, shade, false);
    }

    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light)
    {
        if (this.window.drawingShadow)
            return;

        if (depthTest)
        {
            this.window.enableDepthtest();
            glDepthMask(false);
        }

        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY + Math.max(z / 20, 0)) / 16 + 5);

        if (!shade)
            this.window.setGlowBlendFunc();

        if (light)
            this.window.setLightBlendFunc();

        glBegin(GL_TRIANGLES);
        double step = Math.PI * 2 / sides;

        double pX = x + Math.cos(0) * sX / 2;
        double pY = y + Math.sin(0) * sY / 2;
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            if (!shade)
                glColor3d(0, 0, 0);
            else
                glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, 0);

            glVertex3d(pX, pY, z);
            pX = x + Math.cos(d) * sX / 2;
            pY = y + Math.sin(d) * sY / 2;
            glVertex3d(pX, pY, z);

            if (!shade)
                glColor3d(this.window.colorR * this.window.colorA, this.window.colorG * this.window.colorA, this.window.colorB * this.window.colorA);
            else
                glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);

            glVertex3d(x, y, z);
        }

        glEnd();

        if (!shade)
            this.window.setTransparentBlendFunc();

        if (depthTest)
        {
            glDepthMask(true);
            this.window.disableDepthtest();
        }
    }

    public void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        if (depthTest)
        {
            this.window.enableDepthtest();

            if (this.window.colorA < 1)
                glDepthMask(false);
        }

        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY + Math.max(z / 20, 0)) / 4 + 5);

        glBegin(GL_TRIANGLE_FAN);
        for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
        {
            double ox = Math.cos(i) * sX / 2;
            double oy = Math.sin(i) * sY / 2;
            glVertex3d(x + ox * this.window.bbx1 + oy * this.window.bbx2, y + ox * this.window.bby1 + oy * this.window.bby2, z + ox * this.window.bbz1 + oy * this.window.bbz2);
        }

        glEnd();

        if (depthTest)
        {
            glDepthMask(true);
            this.window.disableDepthtest();
        }
    }

    @Override
    public void fillGlow(double x, double y, double sX, double sY)
    {
        this.fillGlow(x, y, sX, sY, false);
    }

    @Override
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        this.fillGlow(x, y, z, sX, sY, false, false);
    }

    @Override
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        this.fillFacingGlow(x, y, z, sX, sY, depthTest,false);
    }

    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade)
    {
        this.fillFacingGlow(x, y, z, sX, sY, depthTest, shade,false);
    }

    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light)
    {
        if (this.window.drawingShadow)
            return;

        if (depthTest)
        {
            this.window.enableDepthtest();
            glDepthMask(false);
        }

        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY + Math.max(z / 20, 0)) / 16 + 5);

        if (!shade)
            this.window.setGlowBlendFunc();

        if (light)
            this.window.setLightBlendFunc();

        glBegin(GL_TRIANGLES);
        double step = Math.PI * 2 / sides;

        double ox = Math.cos(0) * sX / 2;
        double oy = Math.sin(0) * sY / 2;
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            if (!shade)
                glColor3d(0, 0, 0);
            else
                glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, 0);

            glVertex3d(x + ox * this.window.bbx1 + oy * this.window.bbx2, y + ox * this.window.bby1 + oy * this.window.bby2, z + ox * this.window.bbz1 + oy * this.window.bbz2);
            ox = Math.cos(d) * sX / 2;
            oy = Math.sin(d) * sY / 2;
            glVertex3d(x + ox * this.window.bbx1 + oy * this.window.bbx2, y + ox * this.window.bby1 + oy * this.window.bby2, z + ox * this.window.bbz1 + oy * this.window.bbz2);

            if (!shade)
                glColor3d(this.window.colorR * this.window.colorA, this.window.colorG * this.window.colorA, this.window.colorB * this.window.colorA);
            else
                glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);

            glVertex3d(x, y, z);
        }

        glEnd();

        if (!shade)
            this.window.setTransparentBlendFunc();

        if (depthTest)
        {
            glDepthMask(true);
            this.window.disableDepthtest();
        }
    }

    public void drawOval(double x, double y, double sX, double sY)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY) / 4 + 5);

        for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
        {
            glBegin(GL_LINES);
            glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
            glVertex2d(x + Math.cos(i + Math.PI * 2 / sides) * sX / 2, y + Math.sin(i + Math.PI * 2 / sides) * sY / 2);
            glEnd();
        }
    }

    public void drawOval(double x, double y, double z, double sX, double sY)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.max(4, (int) (sX + sY + Math.max(z / 20, 0)) / 4 + 5);

        for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
        {
            glBegin(GL_LINES);
            glVertex3d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2, z);
            glVertex3d(x + Math.cos(i + Math.PI * 2 / sides) * sX / 2, y + Math.sin(i + Math.PI * 2 / sides) * sY / 2, z);
            glEnd();
        }
    }

    public void fillRect(double x, double y, double sX, double sY)
    {
        glBegin(GL_TRIANGLE_FAN);

        glVertex2d(x, y);
        glVertex2d(x + sX, y);
        glVertex2d(x + sX, y + sY);
        glVertex2d(x, y + sY);

        glEnd();
    }

    public void fillBox(double x, double y, double z, double sX, double sY, double sZ)
    {
        fillBox(x, y, z, sX, sY, sZ, (byte) 0);
    }

    /**
     * Options byte:
     *
     * 0: default
     *
     * +1 hide behind face
     * +2 hide front face
     * +4 hide bottom face
     * +8 hide top face
     * +16 hide left face
     * +32 hide right face
     *
     * +64 draw on top
     * */
    public void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
        if (!this.window.batchMode)
        {
            this.window.enableDepthtest();

            if (this.window.colorA < 1)
                glDepthMask(false);

            if ((options >> 6) % 2 == 0)
                glDepthFunc(GL_LEQUAL);
            else
                glDepthFunc(GL_ALWAYS);

            GL11.glBegin(GL11.GL_QUADS);
        }

        if (options % 2 == 0)
        {
            GL11.glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);
            GL11.glVertex3d(x + sX, y, z);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x, y + sY, z);
            GL11.glVertex3d(x + sX, y + sY, z);
        }

        if ((options >> 2) % 2 == 0)
        {
            GL11.glColor4d(this.window.colorR * 0.8, this.window.colorG * 0.8, this.window.colorB * 0.8, this.window.colorA);
            GL11.glVertex3d(x + sX, y + sY, z + sZ);
            GL11.glVertex3d(x, y + sY, z + sZ);
            GL11.glVertex3d(x, y + sY, z);
            GL11.glVertex3d(x + sX, y + sY, z);
        }

        if ((options >> 3) % 2 == 0)
        {
            GL11.glColor4d(this.window.colorR * 0.8, this.window.colorG * 0.8, this.window.colorB * 0.8, this.window.colorA);
            GL11.glVertex3d(x + sX, y , z + sZ);
            GL11.glVertex3d(x, y, z + sZ);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x + sX, y, z);
        }

        if ((options >> 4) % 2 == 0)
        {
            GL11.glColor4d(this.window.colorR * 0.6, this.window.colorG * 0.6, this.window.colorB * 0.6, this.window.colorA);
            GL11.glVertex3d(x, y + sY, z + sZ);
            GL11.glVertex3d(x, y + sY, z);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x, y, z + sZ);
        }

        if ((options >> 5) % 2 == 0)
        {
            GL11.glColor4d(this.window.colorR * 0.6, this.window.colorG * 0.6, this.window.colorB * 0.6, this.window.colorA);
            GL11.glVertex3d(x + sX, y + sY, z);
            GL11.glVertex3d(x + sX, y + sY, z + sZ);
            GL11.glVertex3d(x + sX, y, z + sZ);
            GL11.glVertex3d(x + sX, y, z);
        }

        if ((options >> 1) % 2 == 0)
        {
            GL11.glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);
            GL11.glVertex3d(x + sX, y + sY, z + sZ);
            GL11.glVertex3d(x, y + sY, z + sZ);
            GL11.glVertex3d(x, y, z + sZ);
            GL11.glVertex3d(x + sX, y, z + sZ);
        }

        if (!this.window.batchMode)
        {
            GL11.glEnd();
            this.window.disableDepthtest();
            glDepthMask(true);
        }
    }

    public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        glBegin(GL_TRIANGLE_FAN);

        glVertex2d(x1, y1);
        glVertex2d(x2, y2);
        glVertex2d(x3, y3);
        glVertex2d(x4, y4);

        glEnd();
    }

    public void fillQuadBox(double x1, double y1,
                            double x2, double y2,
                            double x3, double y3,
                            double x4, double y4,
                            double z, double sZ,
                            byte options)
    {
        this.window.enableDepthtest();

        if ((options >> 6) % 2 == 0)
            glDepthFunc(GL_LEQUAL);
        else
            glDepthFunc(GL_ALWAYS);

        if (options % 2 == 0)
        {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);
            GL11.glVertex3d(x1, y1, z);
            GL11.glVertex3d(x2, y2, z);
            GL11.glVertex3d(x3, y3, z);
            GL11.glVertex3d(x4, y4, z);
            GL11.glEnd();
        }

        if ((options >> 2) % 2 == 0)
        {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4d(this.window.colorR * 0.6, this.window.colorG * 0.6, this.window.colorB * 0.6, this.window.colorA);
            GL11.glVertex3d(x1, y1, z + sZ);
            GL11.glVertex3d(x2, y2, z + sZ);
            GL11.glVertex3d(x2, y2, z);
            GL11.glVertex3d(x1, y1, z);
            GL11.glEnd();
        }

        if ((options >> 3) % 2 == 0)
        {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4d(this.window.colorR * 0.6, this.window.colorG * 0.6, this.window.colorB * 0.6, this.window.colorA);
            GL11.glVertex3d(x3, y3, z + sZ);
            GL11.glVertex3d(x4, y4, z + sZ);
            GL11.glVertex3d(x4, y4, z);
            GL11.glVertex3d(x3, y3, z);
            GL11.glEnd();
        }

        if ((options >> 4) % 2 == 0)
        {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4d(this.window.colorR * 0.8, this.window.colorG * 0.8, this.window.colorB * 0.8, this.window.colorA);
            GL11.glVertex3d(x1, y1, z + sZ);
            GL11.glVertex3d(x4, y4, z + sZ);
            GL11.glVertex3d(x4, y4, z);
            GL11.glVertex3d(x1, y1, z);
            GL11.glEnd();
        }

        if ((options >> 5) % 2 == 0)
        {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4d(this.window.colorR * 0.6, this.window.colorG * 0.6, this.window.colorB * 0.6, this.window.colorA);
            GL11.glVertex3d(x3, y3, z + sZ);
            GL11.glVertex3d(x2, y2, z + sZ);
            GL11.glVertex3d(x2, y2, z);
            GL11.glVertex3d(x3, y3, z);
            GL11.glEnd();
        }

        if ((options >> 1) % 2 == 0)
        {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4d(this.window.colorR, this.window.colorG, this.window.colorB, this.window.colorA);
            GL11.glVertex3d(x1, y1, z + sZ);
            GL11.glVertex3d(x2, y2, z + sZ);
            GL11.glVertex3d(x3, y3, z + sZ);
            GL11.glVertex3d(x4, y4, z + sZ);
            GL11.glEnd();
        }

        this.window.disableDepthtest();
    }

    public void drawRect(double x, double y, double sX, double sY)
    {
        glBegin(GL_LINES);
        glVertex2d(x, y);
        glVertex2d(x + sX, y);
        glEnd();

        glBegin(GL_LINES);
        glVertex2d(x, y);
        glVertex2d(x, y + sY);
        glEnd();

        glBegin(GL_LINES);
        glVertex2d(x, y + sY);
        glVertex2d(x + sX, y + sY);
        glEnd();

        glBegin(GL_LINES);
        glVertex2d(x + sX, y);
        glVertex2d(x + sX, y + sY);
        glEnd();
    }

    public void drawRect(double x, double y, double sX, double sY, double lineWidth)
    {
        if (lineWidth == 1)
            drawRect(x, y, sX, sY);

        fillRect(x, y, sX, lineWidth);
        fillRect(x + lineWidth, y, lineWidth, sY);
        fillRect(x + sX, y, lineWidth, sY);
        fillRect(x + lineWidth, y + sY, sX, lineWidth);
    }

    public void drawImage(double x, double y, double sX, double sY, String image, boolean scaled)
    {
        drawImage(x, y, sX, sY, 0, 0, 1, 1, image, scaled);
    }

    public void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled)
    {
        drawImage(x, y, z, sX, sY, 0, 0, 1, 1, image, scaled);
    }

    public void drawImage(double x, double y, double sX, double sY, String image, double rotation, boolean scaled)
    {
        drawImage(x, y, sX, sY, 0, 0, 1, 1, image, rotation, scaled);
    }

    public void drawImage(double x, double y, double z, double sX, double sY, String image, double rotation, boolean scaled)
    {
        drawImage(x, y, z, sX, sY, 0, 0, 1, 1, image, rotation, scaled);
    }

    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
    {
        if (this.window.drawingShadow)
            return;

        if (!this.window.textures.containsKey(image))
            this.window.createImage(image);

        //loadPerspective();

        glMatrixMode(GL_MODELVIEW);
        this.window.enableTexture();

        glEnable(GL_BLEND);
        this.window.setTransparentBlendFunc();

        glBindTexture(GL_TEXTURE_2D, this.window.textures.get(image));

        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        if (scaled)
        {
            width *= this.window.textureSX.get(image);
            height *= this.window.textureSY.get(image);
        }

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2d(u1, v1);
        glVertex2d(x, y);
        glTexCoord2d(u1, v2);
        glVertex2d(x, y + height);
        glTexCoord2d(u2, v2);
        glVertex2d(x + width, y + height);
        glTexCoord2d(u2, v1);
        glVertex2d(x + width, y);

        glEnd();

        glMatrixMode(GL_PROJECTION);
        this.window.disableTexture();

        if (!this.window.drawingShadow)
            GL20.glUniform1i(this.window.textureFlag, 0);
    }

    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled)
    {
        if (this.window.drawingShadow)
            return;

        if (!this.window.textures.containsKey(image))
            this.window.createImage(image);

        //loadPerspective();

        glMatrixMode(GL_MODELVIEW);
        this.window.enableTexture();

        glEnable(GL_BLEND);
        this.window.setTransparentBlendFunc();

        glBindTexture(GL_TEXTURE_2D, this.window.textures.get(image));

        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        if (scaled)
        {
            width *= this.window.textureSX.get(image);
            height *= this.window.textureSY.get(image);
        }

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2d(u1, v1);
        glVertex2d(rotateX(-width / 2, -height / 2, x, rotation), rotateY(-width / 2, -height / 2, y, rotation));
        glTexCoord2d(u1, v2);
        glVertex2d(rotateX(width / 2, -height / 2, x, rotation), rotateY(width / 2, -height / 2, y, rotation));
        glTexCoord2d(u2, v2);
        glVertex2d(rotateX(width / 2, height / 2, x, rotation), rotateY(width / 2, height / 2, y, rotation));
        glTexCoord2d(u2, v1);
        glVertex2d(rotateX(-width / 2, height / 2, x, rotation), rotateY(-width / 2, height / 2, y, rotation));

        glEnd();

        glMatrixMode(GL_PROJECTION);
        this.window.disableTexture();
    }

    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
    {
        this.drawImage(x, y, z, sX, sY, u1, v1, u2, v2, image, scaled, true);
    }

    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest)
    {
        if (this.window.drawingShadow)
            return;

        if (!this.window.textures.containsKey(image))
            this.window.createImage(image);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        if (depthtest)
            this.window.enableDepthtest();

        this.window.loadPerspective();

        glMatrixMode(GL_MODELVIEW);

        this.window.enableTexture();
        glEnable(GL_BLEND);
        this.window.setTransparentBlendFunc();

        glDepthMask(false);

        glBindTexture(GL_TEXTURE_2D, this.window.textures.get(image));

        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        if (scaled)
        {
            width *= this.window.textureSX.get(image);
            height *= this.window.textureSY.get(image);
        }

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2d(u1, v1);
        glVertex3d(x, y, z);
        glTexCoord2d(u1, v2);
        glVertex3d(x, y + height, z);
        glTexCoord2d(u2, v2);
        glVertex3d(x + width, y + height, z);
        glTexCoord2d(u2, v1);
        glVertex3d(x + width, y, z);

        glEnd();

        glMatrixMode(GL_PROJECTION);
        this.window.disableTexture();

        glDepthMask(true);

        if (depthtest)
            this.window.disableDepthtest();
    }

    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled)
    {
        this.drawImage(x, y, z, sX, sY, u1, v1, u2, v2, image, rotation, scaled, true);
    }

    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled, boolean depthtest)
    {
        if (this.window.drawingShadow)
            return;

        if (!this.window.textures.containsKey(image))
            this.window.createImage(image);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        if (depthtest)
            this.window.enableDepthtest();

        this.window.loadPerspective();
        glMatrixMode(GL_MODELVIEW);

        this.window.enableTexture();
        glEnable(GL_BLEND);
        this.window.setTransparentBlendFunc();

        glDepthMask(false);

        glBindTexture(GL_TEXTURE_2D, this.window.textures.get(image));

        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        if (scaled)
        {
            width *= this.window.textureSX.get(image);
            height *= this.window.textureSY.get(image);
        }

        glBegin(GL_TRIANGLE_FAN);
        glTexCoord2d(u1, v1);
        glVertex3d(rotateX(-width / 2, -height / 2, x, rotation), rotateY(-width / 2, -height / 2, y, rotation), z);
        glTexCoord2d(u1, v2);
        glVertex3d(rotateX(width / 2, -height / 2, x, rotation), rotateY(width / 2, -height / 2, y, rotation), z);
        glTexCoord2d(u2, v2);
        glVertex3d(rotateX(width / 2, height / 2, x, rotation), rotateY(width / 2, height / 2, y, rotation), z);
        glTexCoord2d(u2, v1);
        glVertex3d(rotateX(-width / 2, height / 2, x, rotation), rotateY(-width / 2, height / 2, y, rotation), z);

        glEnd();

        glMatrixMode(GL_PROJECTION);
        this.window.disableTexture();

        glDepthMask(true);

        if (depthtest)
            this.window.disableDepthtest();
    }

    @Override
    public void setBatchMode(boolean enabled, boolean quads, boolean depth)
    {
        this.setBatchMode(enabled, quads, depth, false);
    }

    @Override
    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow)
    {
        this.setBatchMode(enabled, quads, depth, glow, !(this.window.colorA < 1 || glow));
    }

    @Override
    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow, boolean depthMask)
    {
        this.window.batchMode = enabled;
        this.window.batchQuads = quads;
        this.window.batchDepth = depth;
        this.window.batchGlow = glow;
        this.window.batchDepthMask = depthMask;

        if (enabled)
        {
            glDepthMask(depthMask);

            if (depth)
            {
                window.enableDepthtest();
                glDepthFunc(GL_LEQUAL);
            }

            if (glow)
                window.setGlowBlendFunc();
            else
                window.setTransparentBlendFunc();

            if (quads)
                glBegin(GL_QUADS);
            else
                glBegin(GL_TRIANGLES);
        }
        else
        {
            GL11.glEnd();
            window.disableDepthtest();
            glDepthMask(true);
            window.setTransparentBlendFunc();
        }
    }

    public double rotateX(double px, double py, double posX, double rotation)
    {
        return (px * Math.cos(rotation) - py * Math.sin(rotation)) + posX;
    }

    public double rotateY(double px, double py, double posY, double rotation)
    {
        return (py * Math.cos(rotation) + px * Math.sin(rotation)) + posY;
    }
}
