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
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.traceAllRays)
            traceAllRays.setText(traceText, ScreenOptions.onText);
        else
            traceAllRays.setText(traceText, ScreenOptions.offText);

        showPathfinding.setText("Show Pathfinding: ", Game.showPathfinding ? ScreenOptions.onText : ScreenOptions.offText);

        if (Game.firstPerson)
            firstPerson.setText(firstPersonText, ScreenOptions.onText);
        else
            firstPerson.setText(firstPersonText, ScreenOptions.offText);

        if (Game.followingCam)
            followingCam.setText(followingCamText, ScreenOptions.onText);
        else
            followingCam.setText(followingCamText, ScreenOptions.offText);
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 250, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle()
    );

    Button keyboardTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, this.objWidth, this.objHeight, "Test keyboard", () -> Game.screen = new ScreenTestKeyboard()
    );

    Button textboxTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, this.objWidth, this.objHeight, "Test text boxes", () -> Game.screen = new ScreenTestTextbox()
    );

    Button modelTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Test models", () -> Game.screen = new ScreenTestModel(Drawing.drawing.createModel("/models/tankcamoflauge/base/"))
    );

    Button traceAllRays = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "", new Runnable()
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

    Button showPathfinding = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.showPathfinding = !Game.showPathfinding;
            showPathfinding.setText("Show Pathfinding: ", Game.showPathfinding ? ScreenOptions.onText : ScreenOptions.offText);
        }
    });

    Button followingCam = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "", new Runnable()
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

    Button firstPerson = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "", new Runnable()
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

    @Override
    public void update()
    {
        keyboardTest.update();
        textboxTest.update();
        modelTest.update();
        traceAllRays.update();
        showPathfinding.update();
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
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 300, "Debug menu");

        firstPerson.draw();
        followingCam.draw();
        modelTest.draw();
        keyboardTest.draw();
        textboxTest.draw();
        traceAllRays.draw();
        showPathfinding.draw();
        back.draw();
    }
}
