package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.network.ConnectedPlayer;
import tanks.tank.Tank;

public class OverlayPlayerRankings
{
    public ScreenGame screen;
    public double time;
    public double lastNewName;
    public int namesCount;
    public int prevNames;

    public OverlayPlayerRankings(ScreenGame s)
    {
        this.screen = s;
    }

    public void draw()
    {
        if (Game.game.window.drawingShadow)
            return;

        double s = time;

        double delay = screen.introResultsMusicEnd / 10.0 - 15;

        double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
        double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, Math.max(0, 127 * Math.min(1, (time * 10) / 200) * Math.min(s / 25, 1)));
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - Math.min(time / delay, 1) * 200, Drawing.drawing.interfaceSizeY / 2,
                Math.min(time / delay, 1) * 400, height);

        double c = time - delay - 15;

        double opacity = Math.max(Math.min(s / delay, 1) * 255, 0);
        Drawing.drawing.setColor(255, 255, 255, opacity);
        Drawing.drawing.setInterfaceFontSize(this.screen.titleSize);

        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX + 200 - Math.min(time / delay, 1) * 400, 50, "Rankings:");

        int includedPlayers = 0;

        if (ScreenPartyHost.isServer)
            includedPlayers = ScreenPartyHost.includedPlayers.size();
        else if (ScreenPartyLobby.isClient)
            includedPlayers = ScreenPartyLobby.includedPlayers.size();

        double spacing = Math.max(2, Math.min(10, 50.0 / (screen.eliminatedPlayers.size() - namesCount)));

        if (screen.eliminatedPlayers.size() < namesCount)
            namesCount = screen.eliminatedPlayers.size();

        while (screen.eliminatedPlayers.size() > namesCount && c > lastNewName + spacing)
        {
            lastNewName = lastNewName + spacing;
            namesCount++;
        }

        int slots = (int) ((Drawing.drawing.interfaceSizeY - 100) / 40) - 1;

        int j = 0;

        if (includedPlayers <= namesCount)
            j = -1;

        if (c > 0)
        {
            for (int i = namesCount - 1; i >= 0; i--)
            {
                if (j < slots)
                {
                    j++;
                    ConnectedPlayer cp = screen.eliminatedPlayers.get(i);
                    Drawing.drawing.setColor(cp.teamColorR, cp.teamColorG, cp.teamColorB, opacity);

                    String name;
                    if (Game.enableChatFilter)
                        name = Game.chatFilter.filterChat(cp.username);
                    else
                        name = cp.username;

                    int n = includedPlayers - i;
                    if (!(Game.screen instanceof ScreenGame))
                        n = namesCount - i;

                    Drawing.drawing.setBoundedInterfaceFontSize(this.screen.textSize, 250, name);
                    Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 180, 40 * j + 100, n + ". " + name);
                    Tank.drawTank(Drawing.drawing.interfaceSizeX - 220 - Drawing.drawing.getStringWidth(n + ". " + name) / 2, 40 * j + 100, cp.colorR, cp.colorG, cp.colorB, cp.colorR2, cp.colorG2, cp.colorB2, cp.colorR3, cp.colorG3, cp.colorB3, opacity / 255 * 25);
                }
            }

            if (includedPlayers > namesCount)
            {
                Drawing.drawing.setColor(255, 255, 255, opacity / 2);
                Drawing.drawing.setInterfaceFontSize(this.screen.textSize);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX - 200, 100, "%d remaining...", includedPlayers - namesCount);
            }
        }

        if (prevNames != namesCount)
        {
            Drawing.drawing.playSound("bullet_explode.ogg", 0.5f + namesCount * 1.5f / includedPlayers);
        }

        prevNames = namesCount;
        time += Panel.frameFrequency;
    }
}
