package tanks.gui.screen;

import tanks.Drawing;
import tanks.gui.Button;
import tanks.gui.TextBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ScreenOnline extends Screen implements IOnlineScreen
{
    public HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();
    public HashMap<Integer, TextBox> textboxes = new HashMap<Integer, TextBox>();
    public HashMap<Integer, Text> texts = new HashMap<Integer, Text>();
    public HashMap<Integer, Shape> shapes = new HashMap<Integer, Shape>();

    public ArrayList<Integer> buttonKeys = new ArrayList<Integer>();
    public ArrayList<Integer> textboxKeys = new ArrayList<Integer>();
    public ArrayList<Integer> textKeys = new ArrayList<Integer>();
    public ArrayList<Integer> shapeKeys = new ArrayList<Integer>();

    public Comparator<Integer> intComparator = new Comparator<Integer>()
    {
        @Override
        public int compare(Integer o1, Integer o2)
        {
            return o1 - o2;
        }
    };

    public static class Text
    {
        public String text;
        public double posX;
        public double posY;
        public double size;
        public int alignment;
        public int xAlignment;
        public int yAlignment;

        public Text(String text, double posX, double posY, double size, int alignment)
        {
            this.posX = posX;
            this.posY = posY;
            this.text = text;
            this.size = size;
            this.alignment = alignment;
        }

        public void draw()
        {
            Drawing.drawing.setInterfaceFontSize(size);

            if (alignment == 0)
                Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.text);
            else
                Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.text, alignment > 0);
        }
    }

    public static class Shape
    {
        public double posX;
        public double posY;
        public double sizeX;
        public double sizeY;
        public int type;

        public int xAlignment = 0;
        public int yAlignment = 0;

        public double colorR;
        public double colorG;
        public double colorB;
        public double colorA;

        public Shape(double posX, double posY, double sizeX, double sizeY, int type, double colorR, double colorG, double colorB, double colorA)
        {
            this.posX = posX;
            this.posY = posY;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.type = type;
            this.colorR = colorR;
            this.colorG = colorG;
            this.colorB = colorB;
            this.colorA = colorA;
        }

        public void draw()
        {
            Drawing.drawing.setColor(this.colorR, this.colorB, this.colorB, this.colorA);

            if (type == 0)
                Drawing.drawing.fillRect(this.posX, this.posY, this.sizeX, this.sizeY);
            else if (type == 1)
                Drawing.drawing.fillOval(this.posX, this.posY, this.sizeX, this.sizeY);
            else if (type == 2)
                Drawing.drawing.drawRect(this.posX, this.posY, this.sizeX, this.sizeY);
            else if (type == 3)
                Drawing.drawing.drawOval(this.posX, this.posY, this.sizeX, this.sizeY);
        }
    }

    @Override
    public void update()
    {
        for (int i : this.textboxKeys)
            this.textboxes.get(i).update();

        for (int i : this.buttonKeys)
            this.buttons.get(i).update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        for (int i : this.shapeKeys)
            this.shapes.get(i).draw();

        Drawing.drawing.setColor(0, 0, 0);

        for (int i : this.textKeys)
            this.texts.get(i).draw();

        for (int i : this.buttonKeys)
            this.buttons.get(i).draw();

        for (int i : this.textboxKeys)
            this.textboxes.get(i).draw();
    }

    public void addButton(int id, Button b)
    {
        this.buttonKeys.remove((Integer)id);
        this.buttonKeys.add(id);
        this.buttons.put(id, b);
        Collections.sort(this.buttonKeys, intComparator);
    }

    public void removeButton(int id)
    {
        this.buttonKeys.remove((Integer) id);
        this.buttons.remove(id);
    }

    public void addTextbox(int id, TextBox b)
    {
        this.textboxKeys.remove((Integer)id);
        this.textboxKeys.add(id);
        this.textboxes.put(id, b);
        Collections.sort(this.textboxKeys, intComparator);
    }

    public void removeTextbox(int id)
    {
        this.textboxKeys.remove((Integer) id);
        this.textboxes.remove(id);
    }

    public void addText(int id, Text t)
    {
        this.textKeys.remove((Integer)id);
        this.textKeys.add(id);
        this.texts.put(id, t);
        Collections.sort(this.textKeys, intComparator);
    }

    public void removeText(int id)
    {
        this.textKeys.remove((Integer) id);
        this.texts.remove(id);
    }

    public void addShape(int id, Shape s)
    {
        this.shapeKeys.remove((Integer)id);
        this.shapeKeys.add(id);
        this.shapes.put(id, s);
        Collections.sort(this.shapeKeys, intComparator);
    }

    public void removeShape(int id)
    {
        this.shapeKeys.remove((Integer) id);
        this.shapes.remove(id);
    }
}
