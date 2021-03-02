package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenDebug extends Screen
{
    public String traceText = "Trace rays: ";
    public String firstPersonText = "First person: ";
    public String followingCamText = "Immersive camera: ";

    public ScreenDebug()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";

        if (Game.traceAllRays)
            traceAllRays.text = traceText + ScreenOptions.onText;
        else
            traceAllRays.text = traceText + ScreenOptions.offText;

        if (Game.firstPerson)
            firstPerson.text = firstPersonText + ScreenOptions.onText;
        else
            firstPerson.text = firstPersonText + ScreenOptions.offText;

        if (Game.followingCam)
            followingCam.text = followingCamText + ScreenOptions.onText;
        else
            followingCam.text = followingCamText + ScreenOptions.offText;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTitle();
        }
    }
    );

    Button keyboardTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, this.objWidth, this.objHeight, "Test keyboard", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTestKeyboard();
        }
    }
    );

    Button textboxTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Test text boxes", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTestTextbox();
        }
    }
    );

    Button traceAllRays = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.traceAllRays = !Game.traceAllRays;

            if (Game.traceAllRays)
                traceAllRays.text = traceText + ScreenOptions.onText;
            else
                traceAllRays.text = traceText + ScreenOptions.offText;
        }
    });

    Button firstPerson = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.firstPerson = !Game.firstPerson;

            if (Game.firstPerson)
                firstPerson.text = firstPersonText + ScreenOptions.onText;
            else
                firstPerson.text = firstPersonText + ScreenOptions.offText;
        }
    });

    Button followingCam = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.followingCam = !Game.followingCam;

            if (Game.followingCam)
                followingCam.text = followingCamText + ScreenOptions.onText;
            else
                followingCam.text = followingCamText + ScreenOptions.offText;
        }
    });

    @Override
    public void update()
    {
        keyboardTest.update();
        textboxTest.update();
        traceAllRays.update();
        followingCam.update();
        firstPerson.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Debug menu");

        firstPerson.draw();
        followingCam.draw();
        keyboardTest.draw();
        textboxTest.draw();
        traceAllRays.draw();
        back.draw();
    }
}
