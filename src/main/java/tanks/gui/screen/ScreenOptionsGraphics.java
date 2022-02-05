package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOptionsGraphics extends Screen
{
    public static final String terrainText = "Terrain: ";
    public static final String trailsText = "Bullet trails: ";
    public static final String glowText = "Glow effects: ";

    public static final String graphics3dText = "3D graphics: ";
    public static final String ground3dText = "3D ground: ";
    public static final String perspectiveText = "View: ";
    public static final String antialiasingText = "Antialiasing: ";

    public static final String fancyText = "\u00A7000100200255fancy";
    public static final String fastText = "\u00A7200100000255fast";

    public static final String birdsEyeText = "\u00A7000100200255bird's-eye";
    public static final String angledText = "\u00A7200100000255angled";

    public static int viewNo = 0;

    public ScreenOptionsGraphics()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.fancyTerrain)
            terrain.setText(terrainText, fancyText);
        else
            terrain.setText(terrainText, fastText);

        if (Game.bulletTrails)
        {
            if (Game.fancyBulletTrails)
                bulletTrails.setText(trailsText, fancyText);
            else
                bulletTrails.setText(trailsText, fastText);
        }
        else
            bulletTrails.setText(trailsText, ScreenOptions.offText);

        if (Game.glowEnabled)
            glow.setText(glowText, ScreenOptions.onText);
        else
            glow.setText(glowText, ScreenOptions.offText);

        if (Game.vsync)
            maxFPS.setText("Max FPS: \u00A7200100000255V-Sync");
        else if (Game.maxFPS > 0)
            maxFPS.setText("Max FPS: %s", (Object)("\u00A7000200000255" + Game.maxFPS));
        else
            maxFPS.setText("Max FPS: \u00A7000100200255unlimited");

        if (Game.enable3d)
            graphics3d.setText(graphics3dText, ScreenOptions.onText);
        else
            graphics3d.setText(graphics3dText, ScreenOptions.offText);

        update3dGroundButton();

        switch (viewNo)
        {
            case 0:
                altPerspective.setText(perspectiveText, birdsEyeText);

                Game.angledView = false;
                Game.followingCam = false;
                Game.firstPerson = false;
                break;
            case 1:
                altPerspective.setText(perspectiveText, angledText);

                Game.angledView = true;
                Game.followingCam = false;
                Game.firstPerson = false;
                break;
            case 2:
                altPerspective.setText(perspectiveText, "\u00a7200000000255third person");

                Game.angledView = false;
                Game.followingCam = true;
                Game.firstPerson = false;
                break;
            case 3:
                altPerspective.setText(perspectiveText, "\u00a7255000000255first person");

                Game.angledView = false;
                Game.followingCam = true;
                Game.firstPerson = true;
                break;
        }

        if (!Game.antialiasing)
            antialiasing.setText(antialiasingText, ScreenOptions.offText);
        else
            antialiasing.setText(antialiasingText, ScreenOptions.onText);

        if (Game.framework == Game.Framework.libgdx)
        {
            maxFPS.enabled = false;
            altPerspective.enabled = false;
            shadows.enabled = false;
        }

        if (!Game.game.window.antialiasingSupported)
        {
            antialiasing.setText(antialiasingText, ScreenOptions.offText);
            antialiasing.enabled = false;
        }

        if (Game.framework == Game.Framework.libgdx)
            Game.shadowsEnabled = false;

        if (!Game.shadowsEnabled)
            shadows.setText("Fancy lighting: ", ScreenOptions.offText);
        else
            shadows.setText("Fancy lighting: %s", (Object)("\u00A7000200000255" + Game.shadowQuality));

        if (!Game.effectsEnabled)
            effects.setText("Particle effects: ", ScreenOptions.offText);
        else if (Game.effectMultiplier < 1)
            effects.setText("Particle effects: %s", (Object)("\u00A7200100000255" + (int) Math.round(Game.effectMultiplier * 100) + "%"));
        else
            effects.setText("Particle effects: ", ScreenOptions.onText);
    }

    protected void update3dGroundButton()
    {
        if (Game.fancyTerrain && Game.enable3d)
        {
            ground3d.enabled = true;

            if (Game.enable3dBg)
                ground3d.setText(ground3dText, ScreenOptions.onText);
            else
                ground3d.setText(ground3dText, ScreenOptions.offText);
        }
        else
        {
            ground3d.enabled = false;

            ground3d.setText(ground3dText, ScreenOptions.offText);
        }
    }

    Button terrain = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fancyTerrain = !Game.fancyTerrain;

            if (Game.fancyTerrain)
                terrain.setText(terrainText, fancyText);
            else
                terrain.setText(terrainText, fastText);

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
                    bulletTrails.setText(trailsText, fancyText);
                else
                    bulletTrails.setText(trailsText, fastText);
            }
            else
                bulletTrails.setText(trailsText, ScreenOptions.offText);
        }
    }, "Bullet trails show the paths of bullets------Fancy bullet trails enable some extra particle---effects for certain bullet types");

    Button glow = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.glowEnabled = !Game.glowEnabled;

            if (Game.glowEnabled)
                glow.setText(glowText, ScreenOptions.onText);
            else
                glow.setText(glowText, ScreenOptions.offText);
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
                graphics3d.setText(graphics3dText, ScreenOptions.onText);
            else
                graphics3d.setText(graphics3dText, ScreenOptions.offText);

            update3dGroundButton();

            Drawing.drawing.forceRedrawTerrain();
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
                ground3d.setText(ground3dText, ScreenOptions.onText);
            else
                ground3d.setText(ground3dText, ScreenOptions.offText);

            Drawing.drawing.forceRedrawTerrain();
        }
    },
            "Enabling 3D ground may impact---performance in large levels");


    Button altPerspective = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            viewNo = (viewNo + 1) % 4;
            switch (viewNo)
            {
                case 0:
                    altPerspective.setText(perspectiveText, birdsEyeText);

                    Game.angledView = false;
                    Game.followingCam = false;
                    Game.firstPerson = false;
                    break;
                case 1:
                    altPerspective.setText(perspectiveText, angledText);

                    Game.angledView = true;
                    Game.followingCam = false;
                    Game.firstPerson = false;
                    break;
                case 2:
                    altPerspective.setText(perspectiveText, "\u00a7200000000255third person");

                    Game.angledView = false;
                    Game.followingCam = true;
                    Game.firstPerson = false;
                    break;
                case 3:
                    altPerspective.setText(perspectiveText, "\u00a7255000000255first person");

                    Game.angledView = false;
                    Game.followingCam = true;
                    Game.firstPerson = true;
                    break;
            }
        }
    },
            "Changes the angle at which---you view the game field");


    Button maxFPS = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptionsFramerate();
        }
    },
            "Limiting your framerate may---decrease battery consumption");

    Button antialiasing = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.antialiasing = !Game.antialiasing;

            if (!Game.antialiasing)
                antialiasing.setText(antialiasingText, ScreenOptions.offText);
            else
                antialiasing.setText(antialiasingText, ScreenOptions.onText);

            if (Game.antialiasing != Game.game.window.antialiasingEnabled)
                Game.screen = new ScreenAntialiasingWarning();

            ScreenOptions.saveOptions(Game.homedir);
        }
    },
            "May fix flickering in thin edges---at the cost of performance------Requires restarting the game---to take effect");

    Button window = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Window options", () -> Game.screen = new ScreenOptionsWindow());

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
    );

    Button shadows = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", () -> Game.screen = new ScreenOptionsShadows(), "Fancy lighting enables shadows and---allows for custom lighting in levels------Fancy lighting is quite graphically intense---and may significantly reduce framerate"
    );

    Button effects = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", () -> Game.screen = new ScreenOptionsEffects(), "Particle effects may significantly---impact performance"
    );

    @Override
    public void update()
    {
        terrain.update();
        bulletTrails.update();
        glow.update();
        effects.update();
        maxFPS.update();

        graphics3d.update();
        ground3d.update();
        altPerspective.update();
        shadows.update();
        antialiasing.update();

        if (Game.framework == Game.Framework.libgdx)
            window.enabled = false;

        window.update();

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

        window.draw();

        antialiasing.draw();
        shadows.draw();
        altPerspective.draw();
        ground3d.draw();
        graphics3d.draw();

        maxFPS.draw();
        effects.draw();
        glow.draw();
        bulletTrails.draw();
        terrain.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Graphics options");
    }
}
