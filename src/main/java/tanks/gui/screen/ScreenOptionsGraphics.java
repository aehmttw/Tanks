package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsGraphics extends Screen
{
    public static final String terrainText = "Terrain: ";
    public static final String trailsText = "Bullet trails: ";
    public static final String glowText = "Glow effects: ";
    public static final String vsyncText = "V-Sync: ";

    public static final String graphics3dText = "3D graphics: ";
    public static final String ground3dText = "3D ground: ";
    public static final String perspectiveText = "View: ";
    public static final String antialiasingText = "Antialiasing: ";

    public static final String fullscreenText = "Fullscreen: ";

    public static final String fancyText = "\u00A7000100200255fancy";
    public static final String fastText = "\u00A7200100000255fast";

    public static final String birdsEyeText = "\u00A7000100200255bird's-eye";
    public static final String angledText = "\u00A7200100000255angled";

    public ScreenOptionsGraphics()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.fancyTerrain)
            terrain.text = terrainText + fancyText;
        else
            terrain.text = terrainText + fastText;

        if (Game.bulletTrails)
        {
            if (Game.fancyBulletTrails)
                bulletTrails.text = trailsText + fancyText;
            else
                bulletTrails.text = trailsText + fastText;
        }
        else
            bulletTrails.text = trailsText + ScreenOptions.offText;

        if (Game.glowEnabled)
            glow.text = glowText + ScreenOptions.onText;
        else
            glow.text = glowText + ScreenOptions.offText;

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

        if (!Game.antialiasing)
            antialiasing.text = antialiasingText + ScreenOptions.offText;
        else
            antialiasing.text = antialiasingText + ScreenOptions.onText;

        if (Game.framework == Game.Framework.libgdx)
        {
            vsync.enabled = false;
            altPerspective.enabled = false;
            shadows.enabled = false;
        }

        if (!Game.game.window.antialiasingSupported)
        {
            antialiasing.text = antialiasingText + ScreenOptions.offText;
            antialiasing.enabled = false;
        }

        shadows.text = "Fancy lighting: ";

        if (Game.framework == Game.Framework.libgdx)
            Game.shadowsEnabled = false;

        if (!Game.shadowsEnabled)
            shadows.text += ScreenOptions.offText;
        else
            shadows.text += "\u00A7000200000255" + Game.shadowQuality;

        effects.text = "Particle effects: ";

        if (!Game.effectsEnabled)
            effects.text += ScreenOptions.offText;
        else if (Game.effectMultiplier < 1)
            effects.text += "\u00A7200100000255" + (int) Math.round(Game.effectMultiplier * 100) + "%";
        else
            effects.text += ScreenOptions.onText;
    }

    protected void update3dGroundButton()
    {
        if (Game.fancyTerrain && Game.enable3d)
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

    Button terrain = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fancyTerrain = !Game.fancyTerrain;

            if (Game.fancyTerrain)
                terrain.text = terrainText + fancyText;
            else
                terrain.text = terrainText + fastText;

            update3dGroundButton();
        }
    },
            "Fancy terrain enables varied block---and ground colors------May impact performance on larger levels");

    Button bulletTrails = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (!Game.bulletTrails)
                Game.bulletTrails = true;
            else if (!Game.fancyBulletTrails)
                Game.fancyBulletTrails = true;
            else
            {
                Game.fancyBulletTrails = false;
                Game.bulletTrails = false;
            }

            if (Game.bulletTrails)
            {
                if (Game.fancyBulletTrails)
                    bulletTrails.text = trailsText + fancyText;
                else
                    bulletTrails.text = trailsText + fastText;
            }
            else
                bulletTrails.text = trailsText + ScreenOptions.offText;
        }
    }, "Bullet trails show the paths of bullets------Fancy bullet trails enable some extra particle---effects for certain bullet types");

    Button glow = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.glowEnabled = !Game.glowEnabled;

            if (Game.glowEnabled)
                glow.text = glowText + ScreenOptions.onText;
            else
                glow.text = glowText + ScreenOptions.offText;
        }
    },
            "Glow effects may significantly---impact performance");

    Button graphics3d = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
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

    Button ground3d = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
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


    Button altPerspective = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
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


    Button vsync = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
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

    Button antialiasing = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
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
            "May fix flickering in thin edges---at the cost of performance------Requires restarting the game---to take effect");

    Button fullscreen = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.window.setFullscreen(!Game.game.window.fullscreen);
        }
    }, "Can also be toggled at any time---by pressing " + Game.game.input.fullscreen.getInputs()
    );

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    }
    );

    Button shadows = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptionsShadows();
        }
    }, "Fancy lighting enables shadows and---allows for custom lighting in levels------Fancy lighting is quite graphically intense---and may significantly reduce framerate"
    );

    Button effects = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptionsEffects();
        }
    }, "Particle effects may significantly---impact performance"
    );

    @Override
    public void update()
    {
        terrain.update();
        bulletTrails.update();
        glow.update();
        effects.update();
        vsync.update();

        graphics3d.update();
        ground3d.update();
        altPerspective.update();
        shadows.update();
        antialiasing.update();

        if (Game.framework == Game.Framework.libgdx)
        {
            fullscreen.enabled = false;
            fullscreen.hoverText[0] = "This option is unavailable on mobile";
            fullscreen.hoverText[1] = "because all apps are fullscreen!";
        }

        fullscreen.update();

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

        // Need to update fullscreen text in case user changes it via F11
        fullscreen.text = fullscreenText + (Game.game.window.fullscreen ? ScreenOptions.onText : ScreenOptions.offText);

        if (Game.framework == Game.Framework.libgdx)
            fullscreen.text = fullscreenText + ScreenOptions.onText;

        fullscreen.draw();

        antialiasing.draw();
        shadows.draw();
        altPerspective.draw();
        ground3d.draw();
        graphics3d.draw();

        vsync.draw();
        effects.draw();
        glow.draw();
        bulletTrails.draw();
        terrain.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Graphics options");
    }
}
