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

    public boolean minimized = false;
    public double minFrac = 0;
    public double minCooldownMax = 500;
    public double minCooldown = minCooldownMax;
    public boolean initialAddDone = false;

    public OverlayPlayerRankings(ScreenGame s)
    {
        this.screen = s;
    }

    public void draw()
    {
        if (Game.game.window.drawingShadow)
            return;

        minCooldown -= Panel.frameFrequency;
        minimized = minCooldown < 0 && !(ScreenGame.finishedQuick);

        if (minimized)
            minFrac += Panel.frameFrequency / 100;
        else
            minFrac -= Panel.frameFrequency / 25;

        minFrac = Math.min(1, Math.max(0, minFrac));

        double s = time;

        double delay = screen.introResultsMusicEnd / 10.0 - 15;

        double extraWidth = Drawing.drawing.getHorizontalInterfaceMargin();
        double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;
        double width = Math.min(time / delay, 1) * 400 - (minFrac * 300);

        double x = Drawing.drawing.interfaceSizeX + 200 - width + extraWidth - minFrac * 150;
        if (Drawing.drawing.getInterfaceMouseX() > x - width / 2 || !initialAddDone)
        {
            minimized = false;
            minCooldown = minCooldownMax;
        }

        Drawing.drawing.setColor(0, 0, 0, Math.max(0, 127 * Math.min(1, (time * 10) / 200) * Math.min(s / 25, 1)));
        Drawing.drawing.fillInterfaceRect(x, Drawing.drawing.interfaceSizeY / 2,
                width, height);

        double c = time - delay - 15;

        double opacity = Math.max(Math.min(s / delay, 1) * 255, 0);
        Drawing.drawing.setColor(255, 255, 255, opacity);
        Drawing.drawing.setInterfaceFontSize(this.screen.titleSize * (0.5 - minFrac * 0.5 + 0.5));
        Drawing.drawing.displayInterfaceText(x, 50, "Rankings:");

        int includedPlayers = 0;

        if (ScreenPartyHost.isServer)
            includedPlayers = ScreenPartyHost.includedPlayers.size();
        else if (ScreenPartyLobby.isClient)
            includedPlayers = ScreenPartyLobby.includedPlayers.size();

        double spacing = Math.max(2, Math.min(10, 50.0 / Math.max(1, screen.eliminatedPlayers.size() - namesCount)));

        if (screen.eliminatedPlayers.size() < namesCount)
            namesCount = screen.eliminatedPlayers.size();

        boolean added = false;
        while (screen.eliminatedPlayers.size() > namesCount && c > lastNewName + spacing)
        {
            added = true;
            lastNewName = lastNewName + spacing;
            namesCount++;
        }

        if (c > lastNewName + spacing && !added)
            initialAddDone = true;

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
                    Drawing.drawing.setColor(cp.teamColor, opacity * (1 - minFrac));

                    String name;
                    if (Game.enableChatFilter)
                        name = Game.chatFilter.filterChat(cp.username);
                    else
                        name = cp.username;

                    int n = includedPlayers - i;
                    if (!(Game.screen instanceof ScreenGame))
                        n = namesCount - i;

                    Drawing.drawing.setBoundedInterfaceFontSize(this.screen.textSize * (1 - minFrac), 250, name);
                    Drawing.drawing.drawInterfaceText(x + 20, 40 * j + 100, n + ". " + name);

                    double w = Drawing.drawing.getStringWidth(n + ". " + name);
                    Drawing.drawing.setInterfaceFontSize(this.screen.textSize);
                    Drawing.drawing.setColor(cp.teamColor, opacity * minFrac);
                    Drawing.drawing.drawInterfaceText(x - (20 + w / 2) * (1 - minFrac) - 22 * minFrac, 40 * j + 100, n + ".");

                    Tank.drawTank(x - (20 + w / 2) * (1 - minFrac) + 18 * minFrac, 40 * j + 100, cp.color, cp.color2, cp.color3, opacity / 255 * 25);
                }
            }

            if (includedPlayers > namesCount)
            {
                Drawing.drawing.setInterfaceFontSize(this.screen.textSize);
                Drawing.drawing.setColor(255, 255, 255, opacity / 2 * (1 - minFrac));
                Drawing.drawing.displayInterfaceText(x, 100, "%d remaining...", includedPlayers - namesCount);
                Drawing.drawing.setColor(255, 255, 255, opacity / 2 * (minFrac));
                Drawing.drawing.displayInterfaceText(x, 100, "+%d...", includedPlayers - namesCount);
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
