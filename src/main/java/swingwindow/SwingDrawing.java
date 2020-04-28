package swingwindow;

import swingwindow.input.InputKeyboard;

import javax.swing.*;
import java.awt.*;

public class SwingDrawing extends JFrame
{
    public SwingDrawing(SwingWindow w, int sizeX, int sizeY)
    {
        this.addKeyListener(new InputKeyboard(w));

        Container contentPane = this.getContentPane();
        w.panel.setPreferredSize(new Dimension(sizeX, sizeY));
        contentPane.add(w.panel);
        contentPane.setSize(sizeX, sizeY);

        this.pack();
        this.setVisible(true);

        this.setResizable(true);
        this.setMinimumSize(new Dimension(350, 265));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
