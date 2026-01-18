package tanks.gui.screen;

import com.codedisaster.steamworks.SteamUGC;
import tanks.Drawing;
import tanks.Game;

public class ScreenDownloadInfo extends Screen
{
    public long startTime = System.currentTimeMillis();

    public ScreenDownloadInfo()
    {
        this.music = Game.screen.music;
        this.musicID = Game.screen.musicID;
    }

    @Override
    public void update()
    {

    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0);

        SteamUGC.ItemDownloadInfo i = new SteamUGC.ItemDownloadInfo();
        Game.steamNetworkHandler.workshop.workshop.getItemDownloadInfo(Game.steamNetworkHandler.workshop.downloadFile.getPublishedFileID(), i);

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, "Downloading level...");
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, String.format("%.2f%% (%dB / %dB)", 100.0 * i.getBytesDownloaded() / i.getBytesTotal(), i.getBytesDownloaded(), i.getBytesTotal()));

        if (System.currentTimeMillis() - startTime > 500)
        {
            double time = 1.0 * (System.currentTimeMillis() - startTime) / i.getBytesDownloaded() * (i.getBytesTotal() - i.getBytesDownloaded());

            if (time <= 50)
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, "Just a moment...");
            else
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, "About %s left", Game.timeInterval(0, (long) time + 1000, true));
        }

        Drawing.drawing.setColor(0, 0, 0, 64);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 500, 5);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.fillInterfaceProgressRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 500, 5, 1.0 * i.getBytesDownloaded() / i.getBytesTotal());

    }
}
