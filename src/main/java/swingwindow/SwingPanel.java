package swingwindow;

import swingwindow.input.InputMouse;
import swingwindow.input.InputScroll;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingPanel extends JPanel
{
    public Timer timer;
    public SwingWindow window;

    public SwingPanel(SwingWindow w)
    {
        this.window = w;

        this.addMouseListener(new InputMouse(w));
        this.addMouseWheelListener(new InputScroll());
        this.addMouseMotionListener(new InputMouse(w));

        timer = new Timer(0, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                w.startTiming();

                w.absoluteWidth = getWidth();
                w.absoluteHeight = getHeight();

                w.updater.update();

                repaint();

                w.stopTiming();
            }

        });
    }

    public void startTimer()
    {
        timer.start();
        //new SoundThread().execute();
    }

    @Override
    public void paint(Graphics g)
    {
        window.graphics = g;
        window.drawer.draw();
    }
}
