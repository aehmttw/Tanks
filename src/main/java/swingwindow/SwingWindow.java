package swingwindow;

import basewindow.*;
import swingwindow.input.InputKeyboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class SwingWindow extends BaseWindow
{
    public SwingDrawing drawing;
    public SwingPanel panel;
    public Graphics graphics;
    public SwingWindow self;
    public TextClipboard clipboard;

    public ArrayList<Integer> rawTextInput = new ArrayList<Integer>();

    public SwingWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
    {
        super(name, x, y, z, u, d, w, vsync, showMouse);
        this.self = this;
        this.fontRenderer = new SwingFontRenderer(this);
        this.clipboard = new TextClipboard();
    }

    @Override
    public void run()
    {
        panel = new SwingPanel(self);
        drawing = new SwingDrawing(self, (int)self.absoluteWidth, (int)self.absoluteHeight);

        SwingUtilities.invokeLater(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        drawing.setTitle(self.name);
                        drawing.add(panel);

                        //window.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/icon64.png")));
                        panel.startTimer();
                    }
                });
    }

    @Override
    public void setShowCursor(boolean show)
    {

    }

    @Override
    public void fillOval(double x, double y, double sX, double sY)
    {
        this.graphics.fillOval((int)x, (int)y, (int)sX, (int)sY);
    }

    @Override
    public void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void setColor(double r, double g, double b, double a)
    {
        this.graphics.setColor(new Color((int)r, (int)g, (int)b, (int)a));
    }

    @Override
    public void setColor(double r, double g, double b)
    {
        this.graphics.setColor(new Color((int)r, (int)g, (int)b));
    }

    @Override
    public void drawOval(double x, double y, double sX, double sY)
    {
        this.graphics.drawOval((int)x, (int)y, (int)sX, (int)sY);
    }

    @Override
    public void drawOval(double x, double y, double z, double sX, double sY)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void fillRect(double x, double y, double sX, double sY)
    {
        this.graphics.fillRect((int)x, (int)y, (int)sX, (int)sY);
    }

    @Override
    public void fillBox(double x, double y, double z, double sX, double sY, double sZ)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support quads!");
    }

    @Override
    public void fillQuadBox(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double z, double sZ, byte options)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void drawRect(double x, double y, double sX, double sY)
    {
        this.graphics.drawRect((int)x, (int)y, (int)sX, (int)sY);
    }

    @Override
    public void drawImage(double x, double y, double sX, double sY, String image, boolean scaled)
    {
        this.graphics.drawImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(image)), (int)x, (int)y, (int)sX, (int)sY, null);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void setUpPerspective()
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void applyTransformations()
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void loadPerspective()
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support UV!");
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support 3D!");
    }

    @Override
    public String getClipboard()
    {
        return this.clipboard.getClipboard();
    }

    @Override
    public void setClipboard(String s)
    {
        this.clipboard.setClipboard(s);
    }

    @Override
    public void setVsync(boolean enable)
    {

    }

    @Override
    public ArrayList<Integer> getRawTextKeys()
    {
        return this.rawTextInput;
    }

    @Override
    public String getKeyText(int key)
    {
        return KeyEvent.getKeyText(key).toLowerCase();
    }

    @Override
    public int translateKey(int key)
    {
        return InputKeyboard.translate(key);
    }

    @Override
    public int translateTextKey(int key)
    {
        return InputKeyboard.translate(key);
    }

    @Override
    public void transform(double[] matrix)
    {
        throw new UnsupportedOperationException("The Swing renderer does not support transformations!");
    }

    @Override
    public double getEdgeBounds()
    {
        return 0;
    }
}
