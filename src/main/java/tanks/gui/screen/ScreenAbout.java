package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.net.URL;

public class ScreenAbout extends Screen
{
    Button link = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Web page", new Runnable()
    {
        @Override
        public void run()
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
    }
    );

    Button chatroom = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Chatroom", new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                Game.game.window.openLink(new URL("https://discord.gg/aWPaJD3"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    );

    Button changelogs = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "Changelogs", new Runnable()
    {
        @Override
        public void run()
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
    }
    );

    Button libraries = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace, this.objWidth, this.objHeight, "Library licenses", new Runnable()
    {
        @Override
        public void run()
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
    }
    );

    Button license = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace, this.objWidth, this.objHeight, "License", new Runnable()
    {
        @Override
        public void run()
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
    }
    );

    Button privacy = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Privacy policy", new Runnable()
    {
        @Override
        public void run()
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
    }
    );

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 4, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTitle();
        }
    }
    );


    public ScreenAbout()
    {
        this.link.image = "link.png";
        this.link.imageSizeX = 25;
        this.link.imageSizeY = 25;
        this.link.imageXOffset = 145;

        this.libraries.image = "link.png";
        this.libraries.imageSizeX = 25;
        this.libraries.imageSizeY = 25;
        this.libraries.imageXOffset = 145;

        this.chatroom.image = "link.png";
        this.chatroom.imageSizeX = 25;
        this.chatroom.imageSizeY = 25;
        this.chatroom.imageXOffset = 145;

        this.license.image = "link.png";
        this.license.imageSizeX = 25;
        this.license.imageSizeY = 25;
        this.license.imageXOffset = 145;

        this.privacy.image = "link.png";
        this.privacy.imageSizeX = 25;
        this.privacy.imageSizeY = 25;
        this.privacy.imageXOffset = 145;

        this.music = "tomato_feast_1_options.ogg";
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
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 3.5, "About");
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 2.5, "Version: " + Game.version);

    }
}
