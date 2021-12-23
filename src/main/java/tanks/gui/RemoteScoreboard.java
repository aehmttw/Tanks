package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.Panel;

import java.util.ArrayList;
import java.util.HashMap;

public class RemoteScoreboard implements IFixedMenu
{
    public String name;
    public int id;
    public double titleColR = 255;
    public double titleColG = 255;
    public double titleColB = 0;
    public double titleFontSize = 24;
    public double namesFontSize = 20;
    public ArrayList<String> names = new ArrayList<>();
    public HashMap<String, Double> scores = new HashMap<>();
    public String objectiveType;
    private double sizeX = 200;
    private double sizeY = 300;

    public RemoteScoreboard(String objectiveName, String objectiveType, ArrayList<String> names)
    {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        for (String name : names)
        {
            this.names.add(name);
            scores.put(name, 0.0);
        }

        this.id = (int) (Math.random() * Integer.MAX_VALUE);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - sizeX, Drawing.drawing.interfaceSizeY / 2 - sizeY, sizeX, sizeY);

        Drawing.drawing.setColor(titleColR, titleColG, titleColB);
        Drawing.drawing.setInterfaceFontSize(titleFontSize);
        Drawing.drawing.drawInterfaceText(Panel.windowWidth - sizeX / 2, Panel.windowHeight / 2 - sizeY * 0.9, this.name);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(titleFontSize);

        if (!this.objectiveType.equals("custom"))
            Drawing.drawing.drawInterfaceText(
                    Panel.windowWidth - sizeX / 2,
                    Panel.windowHeight / 2 - sizeY * 0.7,
                    "Objective: " + this.objectiveType.replaceAll("_", " "));

        double longestSX = 0;
        for (int i = 0; i < names.size(); i++)
        {
            double textSizeX = Game.game.window.fontRenderer.getStringSizeX(namesFontSize / 32, names.get(i));

            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(40);
            Drawing.drawing.drawInterfaceText(Panel.windowWidth - sizeX / 4 - 100, Panel.windowHeight / 2 - sizeY / 2 + i * 30, names.get(i));

            if (textSizeX > longestSX)
                longestSX = textSizeX;

            Drawing.drawing.setColor(255, 64, 64);
            Drawing.drawing.setInterfaceFontSize(40);
            Drawing.drawing.drawInterfaceText(Panel.windowWidth - 15, Panel.windowHeight / 2 - sizeY / 2 + i * 30, "" + (int)(double)(scores.get(names.get(i))));
        }

        sizeX = longestSX + 200;
        sizeY = this.names.size() * 30 + 70;
    }

    @Override
    public void update()
    {

    }
}
