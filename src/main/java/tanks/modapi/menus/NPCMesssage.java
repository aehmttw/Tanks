package tanks.modapi.menus;

import tanks.Drawing;
import tanks.modapi.TankNPC;

import java.util.ArrayList;

public class NPCMesssage extends FixedMenu
{
    public TankNPC tank;
    public String[] text;
    public int textNum = 0;

    public String currentLine = "";

    public NPCMesssage(TankNPC t)
    {
        this.tank = t;
        this.text = this.tank.messages;
    }

    @Override
    public void draw()
    {
        if (!tank.draw || this.text == null || this.text[textNum] == null || this.text[textNum].equals("/shop"))
            return;

        Drawing.drawing.setColor(120, 66, 18, 200);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 8, Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY / 3);
        Drawing.drawing.setColor(175, 96, 26, 200);
        Drawing.drawing.drawInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 8, Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY / 3, 10);

        tank.icon.drawForInterface(Drawing.drawing.interfaceSizeX * 0.025 + 80, Drawing.drawing.interfaceSizeY / 8, 1.75);

        Drawing.drawing.setFontSize(24);
        Drawing.drawing.setColor(255, 255, 255);

        ArrayList<String> lines = Drawing.drawing.wrapText(this.currentLine, Drawing.drawing.interfaceSizeX - 250, 24);
        for (int i = 0; i < lines.size(); i++)
            Drawing.drawing.drawUncenteredInterfaceText(250, 40 * i + 50, lines.get(i));
    }

    @Override
    public void update()
    {
        this.text = this.tank.messages;
        this.textNum = this.tank.messageNum;
        this.currentLine = this.tank.currentLine;
    }
}
