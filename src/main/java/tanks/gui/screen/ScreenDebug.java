package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.tank.TankPlayer;

public class ScreenDebug extends Screen
{
    public String traceText = "Trace rays: ";
    public String firstPersonText = "First person: ";
    public String followingCamText = "Immersive camera: ";
    public String tankIDsText = "Show tank IDs: ";
    public String invulnerableText = "Invulnerable: ";
    public String fancyLightsText = "Fancy lighting: ";
    public String destroyCheatText = "Destroy cheat: ";
    public String facesText = "Draw faces: ";

    public ScreenDebug()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.traceAllRays)
            traceAllRays.setText(traceText, ScreenOptions.onText);
        else
            traceAllRays.setText(traceText, ScreenOptions.offText);

        if (Game.firstPerson)
            firstPerson.setText(firstPersonText, ScreenOptions.onText);
        else
            firstPerson.setText(firstPersonText, ScreenOptions.offText);

        if (Game.followingCam)
            followingCam.setText(followingCamText, ScreenOptions.onText);
        else
            followingCam.setText(followingCamText, ScreenOptions.offText);

        if (Game.showTankIDs)
            tankIDs.setText(tankIDsText, ScreenOptions.onText);
        else
            tankIDs.setText(tankIDsText, ScreenOptions.offText);

        if (Game.invulnerable)
            invulnerable.setText(invulnerableText, ScreenOptions.onText);
        else
            invulnerable.setText(invulnerableText, ScreenOptions.offText);

        if (Game.fancyLights)
            fancyLighting.setText(fancyLightsText, ScreenOptions.onText);
        else
            fancyLighting.setText(fancyLightsText, ScreenOptions.offText);

        if (TankPlayer.enableDestroyCheat)
            destroyCheat.setText(destroyCheatText, ScreenOptions.onText);
        else
            destroyCheat.setText(destroyCheatText, ScreenOptions.offText);

        if (Game.drawFaces)
            drawFaces.setText(facesText, ScreenOptions.onText);
        else
            drawFaces.setText(facesText, ScreenOptions.offText);
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle());

    Button test = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Test stuff", () -> Game.screen = new ScreenTestDebug());

    Button traceAllRays = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.traceAllRays = !Game.traceAllRays;

            if (Game.traceAllRays)
                traceAllRays.setText(traceText, ScreenOptions.onText);
            else
                traceAllRays.setText(traceText, ScreenOptions.offText);
        }
    });

    Button firstPerson = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.firstPerson = !Game.firstPerson;

            if (Game.firstPerson)
                firstPerson.setText(firstPersonText, ScreenOptions.onText);
            else
                firstPerson.setText(firstPersonText, ScreenOptions.offText);
        }
    });

    Button followingCam = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.followingCam = !Game.followingCam;

            if (Game.followingCam)
                followingCam.setText(followingCamText, ScreenOptions.onText);
            else
                followingCam.setText(followingCamText, ScreenOptions.offText);
        }
    });

    Button tankIDs = new Button(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showTankIDs = !Game.showTankIDs;

            if (Game.showTankIDs)
                tankIDs.setText(tankIDsText, ScreenOptions.onText);
            else
                tankIDs.setText(tankIDsText, ScreenOptions.offText);
        }
    });

    Button invulnerable = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.invulnerable = !Game.invulnerable;

            if (Game.invulnerable)
                invulnerable.setText(invulnerableText, ScreenOptions.onText);
            else
                invulnerable.setText(invulnerableText, ScreenOptions.offText);
        }
    });

    Button fancyLighting = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.fancyLights = !Game.fancyLights;

            if (Game.fancyLights)
                fancyLighting.setText(fancyLightsText, ScreenOptions.onText);
            else
                fancyLighting.setText(fancyLightsText, ScreenOptions.offText);
        }
    });

    Button destroyCheat = new Button(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            TankPlayer.enableDestroyCheat = !TankPlayer.enableDestroyCheat;

            if (TankPlayer.enableDestroyCheat)
                destroyCheat.setText(destroyCheatText, ScreenOptions.onText);
            else
                destroyCheat.setText(destroyCheatText, ScreenOptions.offText);
        }
    });

    Button drawFaces = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.drawFaces = !Game.drawFaces;
            if (Game.drawFaces)
                drawFaces.setText(facesText, ScreenOptions.onText);
            else
                drawFaces.setText(facesText, ScreenOptions.offText);
        }
    });




    @Override
    public void update()
    {
        test.update();
        traceAllRays.update();
        followingCam.update();
        firstPerson.update();
        invulnerable.update();
        tankIDs.update();
        fancyLighting.update();
        destroyCheat.update();
        drawFaces.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Debug menu");

        firstPerson.draw();
        followingCam.draw();
        test.draw();
        traceAllRays.draw();
        tankIDs.draw();
        invulnerable.draw();
        fancyLighting.draw();
        destroyCheat.draw();
        drawFaces.draw();
        back.draw();
    }
}
