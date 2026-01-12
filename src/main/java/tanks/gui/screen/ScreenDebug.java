package tanks.gui.screen;

import tanks.Consumer;
import tanks.Drawing;
import tanks.Game;
import tanks.Producer;
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

    Button traceAllRays = createToggle("Trace rays: ", b -> Game.traceAllRays = b, () -> Game.traceAllRays);
    Button firstPerson = createToggle("First person: ", b -> Game.firstPerson = b, () -> Game.firstPerson);
    Button followingCam = createToggle("Following camera: ", b -> Game.followingCam = b, () -> Game.followingCam);
    Button showPathfinding = createToggle("Show pathfinding: ", b -> Game.showPathfinding = b, () -> Game.showPathfinding);
    Button tankIDs = createToggle("Tank IDs: ", b -> Game.showNetworkIDs = b, () -> Game.showNetworkIDs);
    Button invulnerable = createToggle("Invulnerable: ", b -> Game.invulnerable = b, () -> Game.invulnerable);
    Button fancyLighting = createToggle("Fancy lighting: ", b -> Game.fancyLights = b, () -> Game.fancyLights);
    Button destroyCheat = createToggle("Destroy cheat: ", b -> TankPlayer.enableDestroyCheat = b, () -> TankPlayer.enableDestroyCheat);
    Button drawFaces = createToggle("Draw faces: ", b -> Game.drawFaces = b, () -> Game.drawFaces);
    Button drawAutoZoom = createToggle("Draw auto zoom: ", b -> Game.drawAutoZoom = b, () -> Game.drawAutoZoom);
    Button showUpdatingObstacles = createToggle("Show updating obstacles: ", b -> Game.showUpdatingObstacles = b, () -> Game.showUpdatingObstacles);
    Button immutableFaces = createToggle("Immutable faces: ", b -> Game.immutableFaces = b, () -> Game.immutableFaces);
    Button drawAvoidObjects = createToggle("Draw avoid objects: ", b -> Game.drawAvoidObjects = b, () -> Game.drawAvoidObjects);
    Button disableFixes = createToggle("Disable fixes: ", b -> Game.disableErrorFixing = b, () -> Game.disableErrorFixing);
    Button recordMovableData = createToggle("Record movable data: ", b -> Game.recordMovableData = b, () -> Game.recordMovableData);

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTitle());

    public ButtonList debugButtons = new ButtonList(new ArrayList<>(Arrays.asList(
            test, traceAllRays, firstPerson, followingCam, destroyCheat, invulnerable,
            fancyLighting, tankIDs, showPathfinding, drawFaces, showUpdatingObstacles,
            drawAutoZoom, immutableFaces, drawAvoidObjects, disableFixes, recordMovableData
    )), 0, 0, -30);

    public Button createToggle(String text, Consumer<Boolean> setter, Producer<Boolean> getter)
    {
        return new Button.Toggle(0, 0, this.objWidth, this.objHeight, text, setter, getter);
    }

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
