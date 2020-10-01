package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsEditor extends Screen
{
    public static int page = 0;
    public static final int page_count = 5;

    InputSelector pause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Editor menu", Game.game.input.editorPause);
    InputSelector objectMenu = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Object menu", Game.game.input.editorObjectMenu);
    InputSelector play = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Play level", Game.game.input.editorPlay);
    InputSelector toggleControls = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Toggle on-screen buttons", Game.game.input.editorToggleControls);
    InputSelector undo = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Undo", Game.game.input.editorUndo);
    InputSelector redo = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Redo", Game.game.input.editorRedo);

    InputSelector use = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Use tool", Game.game.input.editorUse);
    InputSelector action = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Tool quick action", Game.game.input.editorAction);
    InputSelector team = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Tank team", Game.game.input.editorTeam);
    InputSelector rotate = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Tank orientation", Game.game.input.editorRotate);
    InputSelector height = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Obstacle height", Game.game.input.editorHeight);
    InputSelector group = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Obstacle group ID", Game.game.input.editorGroupID);

    InputSelector build = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Build", Game.game.input.editorBuild);
    InputSelector erase = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Erase", Game.game.input.editorErase);
    InputSelector camera = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Adjust camera", Game.game.input.editorCamera);
    InputSelector zoomIn = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Zoom in", Game.game.input.editorZoomIn);
    InputSelector zoomOut = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Zoom out", Game.game.input.editorZoomOut);
    InputSelector revertCamera = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Re-center camera", Game.game.input.editorRevertCamera);

    InputSelector nextObj = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Next object", Game.game.input.editorNextObj);
    InputSelector prevObj = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Previous object", Game.game.input.editorPrevObj);
    InputSelector nextType = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Next object type", Game.game.input.editorNextType);
    InputSelector prevType = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Previous object type", Game.game.input.editorPrevType);
    InputSelector nextMeta = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Next object property", Game.game.input.editorNextMeta);
    InputSelector prevMeta = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Previous object property", Game.game.input.editorPrevMeta);

    InputSelector select = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Select", Game.game.input.editorSelect);
    InputSelector deselect = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Clear selection", Game.game.input.editorDeselect);
    InputSelector holdSquare = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Square selection", Game.game.input.editorHoldSquare);
    InputSelector lockSquare = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Toggle square selection", Game.game.input.editorLockSquare);
    InputSelector toggleAdd = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Toggle remove from selection", Game.game.input.editorSelectAddToggle);

    Button next = new Button(Drawing.drawing.interfaceSizeX * 2 / 3 + 190, Drawing.drawing.interfaceSizeY / 2 + 350, 350, 40, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            page++;
        }
    }
    );

    Button previous = new Button(Drawing.drawing.interfaceSizeX * 2 / 3 - 190, Drawing.drawing.interfaceSizeY / 2 + 350, 350, 40, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            page--;
        }
    }
    );

    public ScreenControlsEditor()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";

        next.enabled = page < page_count - 1;
        previous.enabled = page > 0;

        this.next.image = "play.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.previous.image = "play.png";
        this.previous.imageSizeX = -25;
        this.previous.imageSizeY = 25;
        this.previous.imageXOffset = -145;
    }

    @Override
    public void update()
    {
        if (page == 0)
        {
            pause.update();
            objectMenu.update();
            play.update();
            toggleControls.update();
            undo.update();
            redo.update();
        }
        else if (page == 1)
        {
            use.update();
            action.update();
            team.update();
            rotate.update();
            height.update();
            group.update();
        }
        else if (page == 2)
        {
            build.update();
            erase.update();
            camera.update();
            zoomIn.update();
            zoomOut.update();
            revertCamera.update();
        }
        else if (page == 3)
        {
            nextObj.update();
            prevObj.update();
            nextType.update();
            prevType.update();
            nextMeta.update();
            prevMeta.update();
        }
        else if (page == 4)
        {
            select.update();
            deselect.update();
            holdSquare.update();
            lockSquare.update();
            toggleAdd.update();
        }

        next.enabled = page < page_count - 1;
        previous.enabled = page > 0;

        next.update();
        previous.update();

        ScreenOptionsInputDesktop.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (page == 0)
        {
            redo.draw();
            undo.draw();
            toggleControls.draw();
            play.draw();
            objectMenu.draw();
            pause.draw();

        }
        else if (page == 1)
        {
            group.draw();
            height.draw();
            rotate.draw();
            team.draw();
            action.draw();
            use.draw();
        }
        else if (page == 2)
        {
            revertCamera.draw();
            zoomOut.draw();
            zoomIn.draw();
            camera.draw();
            erase.draw();
            build.draw();
        }
        else if (page == 3)
        {
            prevMeta.draw();
            nextMeta.draw();
            prevType.draw();
            nextType.draw();
            prevObj.draw();
            nextObj.draw();
        }
        else if (page == 4)
        {
            toggleAdd.draw();
            lockSquare.draw();
            holdSquare.draw();
            deselect.draw();
            select.draw();
        }

        next.draw();
        previous.draw();

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 310, "Page " + (page + 1) + " of " + page_count);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Editor controls");

        ScreenOptionsInputDesktop.overlay.draw();
    }

}
