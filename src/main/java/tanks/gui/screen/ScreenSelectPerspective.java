package tanks.gui.screen;

import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.Translation;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenSelectPerspective extends Screen implements ILevelPreviewScreen
{
    DisplayLevel level = new DisplayLevel();
    ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

    public ButtonList viewOptions;

    public RotationAboutPoint slantRotation = new RotationAboutPoint(Game.game.window, 0, -Math.PI / 16, 0, 0, 0.5, -1);
    public Translation slantTranslation = new Translation(Game.game.window, 0, -0.05, 0);

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.cleanUp();
        Game.screen = new ScreenOptionsGraphics();
    });

    public ScreenSelectPerspective()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        Level l = new Level("{28,18,235,207,166,20,20,20|18-6...11,3-3-hard-2.0,3-4-hard-1.5,3-5-hard-2.0,3-6-hard-1.5,3-7-hard-2.0,3-8-hard-1.5,3-9-hard-2.0,3-10-hard-1.5,3-11-hard-2.0,3-12-hard-1.5,3-13-hard-2.0,3-14-hard-1.5,4-3-hard-1.5,4-14-hard-2.0,5-3-hard-2.0,5-14-hard-1.5,6-3-hard-1.5,6-14-hard-2.0,7-3-hard-2.0,7-14-hard-1.5,8-3-hard-1.5,8-14-hard-2.0,9-3-hard-2.0,9-14-hard-1.5,10-3-hard-1.5,10-14-hard-2.0,11-3-hard-2.0,11-14-hard-1.5,12-3-hard-1.5,12-14-hard-2.0,13-3-hard-2.0,13-14-hard-1.5,14-3-hard-1.5,14-14-hard-2.0,15-3-hard-2.0,15-14-hard-1.5,16-3-hard-1.5,16-14-hard-2.0,17-3-hard-2.0,17-14-hard-1.5,18-3-hard-1.5,18-14-hard-2.0,19-3-hard-2.0,19-14-hard-1.5,20-3-hard-1.5,20-14-hard-2.0,21-3-hard-2.0,21-14-hard-1.5,22-3-hard-1.5,22-14-hard-2.0,23-3-hard-2.0,23-14-hard-1.5,24-3-hard-1.5,24-4-hard-2.0,24-5-hard-1.5,24-6-hard-2.0,24-7-hard-1.5,24-8-hard-2.0,24-9-hard-1.5,24-10-hard-2.0,24-11-hard-1.5,24-12-hard-2.0,24-13-hard-1.5,24-14-hard-2.0,0...27-0-shrub,0...27-1-shrub,0...27-2-shrub,0-3...17-shrub,1-3...17-shrub,2-3...17-shrub,3...27-15-shrub,3...27-16-shrub,3...27-17-shrub,25-3...14-shrub,26-3...14-shrub,27-3...14-shrub|21-7-brown-2-enemy,6-10-player-0-ally|ally-true,enemy-true}");
        l.loadLevel(this);

        Button birdsEye = new Button(0, 0, this.objWidth, this.objHeight, "Bird's-eye", () ->
        {
            ScreenOptionsGraphics.viewNo = 0;
        }, "See the game field from above");

        Button angled = new Button(0, 0, this.objWidth, this.objHeight, "Angled", () ->
        {
            ScreenOptionsGraphics.viewNo = 1;
        }, "See the game field from an angle");

        Button orthographic = new Button(0, 0, this.objWidth, this.objHeight, "Orthographic", () ->
        {
            ScreenOptionsGraphics.viewNo = 2;
        }, "Walls will appear the same size---regardless of how tall they are");

        Button thirdPerson = new Button(0, 0, this.objWidth, this.objHeight, "Third person", () ->
        {
            ScreenOptionsGraphics.viewNo = 3;
        }, "See the game from right behind your tank");

        Button firstPerson = new Button(0, 0, this.objWidth, this.objHeight, "First person", () ->
        {
            ScreenOptionsGraphics.viewNo = 4;
        }, "See the game directly from your tank");


        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(birdsEye);
        buttons.add(angled);
        buttons.add(orthographic);
        if (Game.debug)
        {
            buttons.add(thirdPerson);
            buttons.add(firstPerson);
        }

        viewOptions = new ButtonList(buttons, 0, 0, 0);
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return spawns;
    }

    @Override
    public void update()
    {
        viewOptions.update();
        back.update();

        Game.angledView = ScreenOptionsGraphics.viewNo == 1;
        Game.orthographicView = ScreenOptionsGraphics.viewNo == 2;
        Game.followingCam = ScreenOptionsGraphics.viewNo >= 3;
        Game.firstPerson = ScreenOptionsGraphics.viewNo == 4;

        for (int i = 0; i < 5; i++)
        {
            viewOptions.buttons.get(i).enabled = ScreenOptionsGraphics.viewNo != i;
        }
    }

    @Override
    public void draw()
    {
        if (ScreenOptionsGraphics.viewNo == 1)
        {
            Game.game.window.transformations.add(this.slantTranslation);
            Game.game.window.transformations.add(this.slantRotation);
        }
        Game.game.window.loadPerspective();
        level.draw();

        Game.game.window.transformations.clear();
        Game.game.window.loadPerspective();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, this.objXSpace * 2, this.objYSpace * 9);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Game field view options");

        back.draw();

        viewOptions.draw();
    }
}
