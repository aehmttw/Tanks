package tanks.modapi.menus;

import tanks.Drawing;
import tanks.Panel;
import tanks.modapi.ModAPI;

import java.util.ArrayList;
import java.util.HashMap;

public class RemoteScoreboard extends FixedMenu {
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

    public RemoteScoreboard(String objectiveName, String objectiveType, ArrayList<String> names) {
        this.name = objectiveName;
        this.objectiveType = objectiveType;

        for (String name : names) {
            this.names.add(name);
            scores.put(name, 0.0);
        }

        this.id = (int) (Math.random() * Integer.MAX_VALUE);
    }

    @Override
    public void draw() {
        Drawing.drawing.setColor(0, 0, 0, 128);
        ModAPI.fixedShapes.fillRect(Panel.windowWidth - sizeX, Panel.windowHeight / 2 - sizeY, sizeX, sizeY);

        Drawing.drawing.setColor(titleColR, titleColG, titleColB);
        ModAPI.fixedText.drawString(
                Panel.windowWidth - sizeX / 2 - ModAPI.fixedText.getStringSizeX(titleFontSize / 40, this.name) / 2,
                Panel.windowHeight / 2 - sizeY * 0.9,
                titleFontSize / 40, titleFontSize / 40,
                this.name);

        Drawing.drawing.setColor(255, 255, 255);

        if (!this.objectiveType.equals("custom"))
            ModAPI.fixedText.drawString(
                    Panel.windowWidth - sizeX / 2 - ModAPI.fixedText.getStringSizeX(titleFontSize / 40, this.name) / 2,
                    Panel.windowHeight / 2 - sizeY * 0.7,
                    titleFontSize / 50, titleFontSize / 50,
                    "Objective: " + this.objectiveType.replaceAll("_", " "));

        double longestSX = 0;
        for (int i = 0; i < names.size(); i++) {
            double textSizeX = ModAPI.fixedText.getStringSizeX(namesFontSize / 40, names.get(i));

            Drawing.drawing.setColor(255, 255, 255);
            ModAPI.fixedText.drawString(
                    Panel.windowWidth - sizeX / 4 - 100 - textSizeX / 2,
                    Panel.windowHeight / 2 - sizeY / 2 + i * 30,
                    namesFontSize / 40, namesFontSize / 40,
                    names.get(i)
            );

            if (textSizeX > longestSX)
                longestSX = textSizeX;

            Drawing.drawing.setColor(255, 64, 64);
            ModAPI.fixedText.drawString(
                    Panel.windowWidth - 15 - ModAPI.fixedText.getStringSizeX(namesFontSize / 40, scores.get(names.get(i)).toString()),
                    Panel.windowHeight / 2 - sizeY / 2 + i * 30,
                    namesFontSize / 40, namesFontSize / 40,
                    ModAPI.convertToString(scores.get(names.get(i)))
            );
        }

        sizeX = longestSX + 200;
        sizeY = this.names.size() * 30 + 70;
    }

    @Override
    public void update()
    {

    }
}
