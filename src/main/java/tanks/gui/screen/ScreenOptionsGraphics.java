package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsGraphics extends Screen
{
    public static final String graphicsText = "Graphics: ";
    public static final String vsyncText = "V-Sync: ";
    public static final String graphics3dText = "3D graphics: ";
    public static final String ground3dText = "3D ground: ";
    public static final String perspectiveText = "View: ";

    public static final String superText = "\u00A7200000200255super";
    public static final String fancyText = "\u00A7000100200255fancy";
    public static final String fastText = "\u00A7200100000255fast";

    public static final String birdsEyeText = "\u00A7000100200255bird's-eye";
    public static final String angledText = "\u00A7200100000255angled";

    public static final String antialiasingText = "Antialiasing: ";


    public ScreenOptionsGraphics()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";

        if (Game.fancyGraphics)
        {
            if (Game.superGraphics)
                graphics.text = graphicsText + superText;
            else
                graphics.text = graphicsText + fancyText;
        }
        else
            graphics.text = graphicsText + fastText;

        if (Game.vsync)
            vsync.text = vsyncText + ScreenOptions.onText;
        else
            vsync.text = vsyncText + ScreenOptions.offText;

        if (Game.enable3d)
            graphics3d.text = graphics3dText + ScreenOptions.onText;
        else
            graphics3d.text = graphics3dText + ScreenOptions.offText;

        update3dGroundButton();

        if (Game.angledView)
            altPerspective.text = perspectiveText + angledText;
        else
            altPerspective.text = perspectiveText + birdsEyeText;

        if (Game.framework == Game.Framework.swing)
        {
            vsync.enabled = false;
            altPerspective.enabled = false;
            graphics3d.enabled = false;
            ground3d.enabled = false;

            vsync.hoverText = new String[]{"Unavailable in Swing mode"};
            altPerspective.hoverText = new String[]{"Unavailable in Swing mode"};
            graphics3d.hoverText = new String[]{"Unavailable in Swing mode"};
            ground3d.hoverText = new String[]{"Unavailable in Swing mode"};
        }

        if (!Game.antialiasing)
            antialiasing.text = antialiasingText + ScreenOptions.offText;
        else
            antialiasing.text = antialiasingText + ScreenOptions.onText;

        if (Game.framework == Game.Framework.libgdx)
        {
            vsync.enabled = false;
            altPerspective.enabled = false;
        }

        if (!Game.game.window.antialiasingSupported)
        {
            antialiasing.text = antialiasingText + ScreenOptions.offText;
            antialiasing.enabled = false;
        }
    }

    protected void update3dGroundButton()
    {
        if (Game.fancyGraphics && Game.enable3d)
        {
            ground3d.enabled = true;

            if (Game.enable3dBg)
                ground3d.text = ground3dText + ScreenOptions.onText;
            else
                ground3d.text = ground3dText + ScreenOptions.offText;
        }
        else
        {
            ground3d.enabled = false;

            ground3d.text = ground3dText + ScreenOptions.offText;
        }
    }

    Button graphics = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (!Game.fancyGraphics)
                Game.fancyGraphics = true;
            else if (!Game.superGraphics && Game.framework != Game.Framework.swing)
                Game.superGraphics = true;
            else
            {
                Game.fancyGraphics = false;
                Game.superGraphics = false;
            }

            if (Game.fancyGraphics)
            {
                if (Game.superGraphics)
                    graphics.text = graphicsText + superText;
                else
                    graphics.text = graphicsText + fancyText;
            }
            else
                graphics.text = graphicsText + fastText;

            update3dGroundButton();
        }
    },
            "Fast graphics disable most graphical effects---and use solid colors for the background------Fancy graphics enable these effects but---may significantly reduce framerate------Super graphics enable glow and shadow---effects but may further reduce framerate");

    Button graphics3d = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 0, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.enable3d = !Game.enable3d;

            if (Game.enable3d)
                graphics3d.text = graphics3dText + ScreenOptions.onText;
            else
                graphics3d.text = graphics3dText + ScreenOptions.offText;

            update3dGroundButton();
        }
    },
            "3D graphics may impact performance");

    Button ground3d = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.enable3dBg = !Game.enable3dBg;

            if (Game.enable3dBg)
                ground3d.text = ground3dText + ScreenOptions.onText;
            else
                ground3d.text = ground3dText + ScreenOptions.offText;
        }
    },
            "Enabling 3D ground may impact---performance in large levels");


    Button altPerspective = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.angledView = !Game.angledView;

            if (Game.angledView)
                altPerspective.text = perspectiveText + angledText;
            else
                altPerspective.text = perspectiveText + birdsEyeText;

        }
    },
            "Changes the angle at which---you view the game field");


    Button vsync = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 0, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.vsync = !Game.vsync;
            Game.game.window.setVsync(Game.vsync);

            if (Game.vsync)
                vsync.text = vsyncText + ScreenOptions.onText;
            else
                vsync.text = vsyncText + ScreenOptions.offText;
        }
    },
            "Limits framerate to your---screen's refresh rate------May decrease battery---consumption------Also, might fix issues with---inconsistent game speed");

    Button antialiasing = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.antialiasing = !Game.antialiasing;

            if (!Game.antialiasing)
                antialiasing.text = antialiasingText + ScreenOptions.offText;
            else
                antialiasing.text = antialiasingText + ScreenOptions.onText;

            if (Game.antialiasing != Game.game.window.antialiasingEnabled)
                Game.screen = new ScreenAntialiasingWarning();

            ScreenOptions.saveOptions(Game.homedir);
        }
    },
            "May fix flickering in thin edges---at the cost of performance.------Requires restarting the game---to take effect.");


    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    }
    );

    @Override
    public void update()
    {
        graphics.update();
        graphics3d.update();
        ground3d.update();
        altPerspective.update();
        vsync.update();
        antialiasing.update();
        back.update();

        if (Game.antialiasing != Game.game.window.antialiasingEnabled)
        {
            antialiasing.unselectedColG = 238;
            antialiasing.unselectedColB = 220;
        }
        else
        {
            antialiasing.unselectedColG = 255;
            antialiasing.unselectedColB = 255;
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        antialiasing.draw();
        ground3d.draw();
        vsync.draw();
        altPerspective.draw();
        graphics3d.draw();
        graphics.draw();

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Graphics options");
    }
}
