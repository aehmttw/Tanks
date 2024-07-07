package tanks.gui.screen;

import basewindow.InputCodes;
import basewindow.Model;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;

public class ScreenTestModel extends Screen
{
    public Model model;
    public double yaw;
    public double pitch;
    public double roll;
    public double scale = 50;

    public double posX = Drawing.drawing.sizeX / 2;
    public double posY = Drawing.drawing.sizeY / 2;
    public double posZ = 0;

    //PosedModelAnimation animation;
    //PosedModelPose pose;

    public ScreenTestModel(Model m)
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
        this.model = m;

        //animation = new PosedModelAnimation(Game.game.fileManager, "/models/mustard-test/walk.pma");
        //pose = new PosedModelPose(Game.game.fileManager, "/models/mustard-test/idlehands.pmp");
    }

    @Override
    public void update()
    {
        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_ESCAPE))
            Game.screen = new ScreenDebug();

        double frac = 0.02;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT))
            this.yaw += Panel.frameFrequency * frac;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT))
            this.yaw -= Panel.frameFrequency * frac;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_UP))
            this.pitch += Panel.frameFrequency * frac;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_DOWN))
            this.pitch -= Panel.frameFrequency * frac;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_9))
            this.roll += Panel.frameFrequency * frac;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_0))
            this.roll -= Panel.frameFrequency * frac;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_EQUAL))
            this.scale += Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_MINUS))
            this.scale -= Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_W))
            this.posY -= Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_S))
            this.posY += Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_A))
            this.posX -= Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_D))
            this.posX += Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_Q))
            this.posZ -= Panel.frameFrequency;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_R))
            this.posZ += Panel.frameFrequency;

        /*for (PosedModel.PoseBone b: model.bones)
        {
            b.yaw = 0;
            b.pitch = 0;
            b.roll = 0;
            b.offX = 0;
            b.offY = 0;
            b.offZ = 0;
        }*/

        //animation.apply(model, System.currentTimeMillis() / 10.0, 1);
        //pose.apply(model, 1);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawModel(this.model, this.posX, this.posY, this.posZ, scale, scale, scale, this.yaw, this.pitch, this.roll);
    }
}
