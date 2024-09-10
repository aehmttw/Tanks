package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.net.URL;

public class ScreenAbout extends Screen
{
    public static final String discord_link = "https://discord.gg/aWPaJD3";

    Button link = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "GitHub", () ->
    {
        try
        {
            Game.game.window.openLink(new URL("https://github.com/aehmttw/Tanks"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    );

    Button chatroom = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Discord", () ->
    {
        try
        {
            Game.game.window.openLink(new URL(discord_link));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    );

    Button changelogs = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Changelogs", () ->
    {
        ScreenChangelog s = new ScreenChangelog();
        int p = 0;
        for (ScreenChangelog.Changelog l: ScreenChangelog.Changelog.logs)
        {
            s.add(l.pages);
            p = l.pages.length;
        }

        s.currentPage = s.pages.size() - p;
        s.pageContents = s.pages.get(s.currentPage).split("\n");


        Game.screen = s;
    }
    );

    Button libraries = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Library licenses", () ->
    {
        try
        {
            Game.game.window.openLink(new URL("https://github.com/aehmttw/Tanks/tree/master/src/main/java/licenses"));
    }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    );

    Button license = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "License", () ->
    {
        try
        {
            Game.game.window.openLink(new URL("https://github.com/aehmttw/Tanks/blob/master/LICENSE.md"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    );

    Button privacy = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Privacy policy", () ->
    {
        try
        {
            Game.game.window.openLink(new URL("https://github.com/aehmttw/Tanks/blob/master/PRIVACY.md"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    );

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle()
    );


    public ScreenAbout()
    {
        double imgsize = 25 * Drawing.drawing.interfaceScaleZoom;

        this.link.image = "icons/link.png";
        this.link.imageSizeX = imgsize;
        this.link.imageSizeY = imgsize;
        this.link.imageXOffset = 145 * this.link.sizeX / 350;

        this.libraries.image = "icons/link.png";
        this.libraries.imageSizeX = imgsize;
        this.libraries.imageSizeY = imgsize;
        this.libraries.imageXOffset = 145 * this.libraries.sizeX / 350;

        this.chatroom.image = "icons/link.png";
        this.chatroom.imageSizeX = imgsize;
        this.chatroom.imageSizeY = imgsize;
        this.chatroom.imageXOffset = 145 * this.chatroom.sizeX / 350;

        this.license.image = "icons/link.png";
        this.license.imageSizeX = imgsize;
        this.license.imageSizeY = imgsize;
        this.license.imageXOffset = 145 * this.license.sizeX / 350;

        this.privacy.image = "icons/link.png";
        this.privacy.imageSizeX = imgsize;
        this.privacy.imageSizeY = imgsize;
        this.privacy.imageXOffset = 145 * this.privacy.sizeX / 350;

        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        changelogs.update();
        link.update();
        libraries.update();
        chatroom.update();
        license.update();
        privacy.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        changelogs.draw();
        link.draw();
        libraries.draw();
        chatroom.draw();
        license.draw();
        privacy.draw();
        back.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "About");
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "Version: %s", Game.version);

        int extensions = Game.extensionRegistry.extensions.size();
        if (extensions > 0)
        {
            Drawing.drawing.setInterfaceFontSize(this.textSize * 0.75);

            if (extensions > 1)
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "%d extensions loaded", extensions);
            else
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "%d extension loaded", extensions);
        }
    }
}
