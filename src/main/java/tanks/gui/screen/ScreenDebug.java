package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.tank.TankPlayer;

import java.util.ArrayList;
import java.util.Arrays;

public class ScreenDebug extends Screen
{
    Button test = new Button(
            0, 0, this.objWidth, this.objHeight,
            "Test stuff", () -> Game.screen = new ScreenTestDebug()
    );

    Button traceAllRays = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Trace rays: ", b -> Game.traceAllRays = b, () -> Game.traceAllRays
    );

    Button firstPerson = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "First person: ", b -> Game.firstPerson = b, () -> Game.firstPerson
    );

    Button followingCam = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Immersive camera: ", b -> Game.followingCam = b, () -> Game.followingCam
    );

    Button showPathfinding = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Show pathfinding: ", b -> Game.showPathfinding = b, () -> Game.showPathfinding
    );

    Button tankIDs = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Tank IDs: ", b -> Game.showNetworkIDs = b, () -> Game.showNetworkIDs
    );

    Button invulnerable = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Invulnerable: ", b -> Game.invulnerable = b, () -> Game.invulnerable
    );

    Button fancyLighting = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Fancy lighting: ", b -> Game.fancyLights = b, () -> Game.fancyLights
    );

    Button destroyCheat = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Destroy cheat: ", b -> TankPlayer.enableDestroyCheat = b, () -> TankPlayer.enableDestroyCheat
    );

    Button drawFaces = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Draw faces: ", b -> Game.drawFaces = b, () -> Game.drawFaces
    );

    Button immutableFaces = new Button.Toggle(
            0, 0, this.objWidth, this.objHeight,
            "Immutable faces: ", b -> Game.immutableFaces = b, () -> Game.immutableFaces
    );

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle());

    public ButtonList debugButtons = new ButtonList(new ArrayList<>(Arrays.asList(
            test, traceAllRays, firstPerson, followingCam, destroyCheat, invulnerable,
            fancyLighting, tankIDs, showPathfinding, drawFaces, immutableFaces
    )), 0, 0, -30);

    public ScreenDebug()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        debugButtons.setRowsAndColumns(6, 3);
    }

    @Override
    public void update()
    {
        debugButtons.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 4, "Debug menu");

        debugButtons.draw();
        back.draw();
    }
}
