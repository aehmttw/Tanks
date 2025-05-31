package tanks.gui.input;

import basewindow.BaseFile;
import basewindow.InputCodes;
import tanks.Game;

public class InputBindings
{
    public BaseFile file;

    public InputBindingGroup moveUp = new InputBindingGroup("tank.up", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_UP), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_W));
    public InputBindingGroup moveDown = new InputBindingGroup("tank.down", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_DOWN), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_S));
    public InputBindingGroup moveLeft = new InputBindingGroup("tank.left", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_LEFT), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_A));
    public InputBindingGroup moveRight = new InputBindingGroup("tank.right", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_RIGHT), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_D));
    public InputBindingGroup shoot = new InputBindingGroup("tank.shoot", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_SPACE), new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_1));
    public InputBindingGroup mine = new InputBindingGroup("tank.mine", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_ENTER), new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_2));
    public InputBindingGroup aim = new InputBindingGroup("tank.aim", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_PERIOD), new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_4));

    public InputBindingGroup ability1 = new InputBindingGroup("tank.ability.1", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_Z));
    public InputBindingGroup ability2 = new InputBindingGroup("tank.ability.2", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_X));
    public InputBindingGroup ability3 = new InputBindingGroup("tank.ability.3", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_C));
    public InputBindingGroup ability4 = new InputBindingGroup("tank.ability.4", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_V));
    public InputBindingGroup ability5 = new InputBindingGroup("tank.ability.5", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_B));

    public InputBindingGroup[] abilityBindings = new InputBindingGroup[]{ability1, ability2, ability3, ability4, ability5};

    public InputBindingGroup hotbarToggle = new InputBindingGroup("hotbar.toggle", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_LEFT_SHIFT), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_RIGHT_SHIFT));
    public InputBindingGroup hotbar1 = new InputBindingGroup("hotbar.1", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_1));
    public InputBindingGroup hotbar2 = new InputBindingGroup("hotbar.2", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_2));
    public InputBindingGroup hotbar3 = new InputBindingGroup("hotbar.3", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_3));
    public InputBindingGroup hotbar4 = new InputBindingGroup("hotbar.4", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_4));
    public InputBindingGroup hotbar5 = new InputBindingGroup("hotbar.5", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_5));
    public InputBindingGroup hotbar6 = new InputBindingGroup("hotbar.6", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_6));
    public InputBindingGroup hotbar7 = new InputBindingGroup("hotbar.7", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_7));
    public InputBindingGroup hotbar8 = new InputBindingGroup("hotbar.8", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_8));
    public InputBindingGroup hotbar9 = new InputBindingGroup("hotbar.9", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_9));
    public InputBindingGroup hotbar10 = new InputBindingGroup("hotbar.10", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_0));
    public InputBindingGroup hotbarDeselect = new InputBindingGroup("hotbar.deselect", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_GRAVE_ACCENT));

    public InputBindingGroup[] hotbarBindings = new InputBindingGroup[]{hotbar1, hotbar2, hotbar3, hotbar4, hotbar5, hotbar6, hotbar7, hotbar8, hotbar9, hotbar10};

    public InputBindingGroup pause = new InputBindingGroup("game.pause", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_ESCAPE));
    public InputBindingGroup play = new InputBindingGroup("game.play", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_ENTER));
    public InputBindingGroup zoom = new InputBindingGroup("game.zoom", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_I));
    public InputBindingGroup zoomIn = new InputBindingGroup("game.zoomIn", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_EQUAL));
    public InputBindingGroup zoomOut = new InputBindingGroup("game.zoomOut", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_MINUS));
    public InputBindingGroup zoomAuto = new InputBindingGroup("game.zoomAuto", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_O));
    public InputBindingGroup chat = new InputBindingGroup("game.chat", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_T));
    public InputBindingGroup hidePause = new InputBindingGroup("game.hidePauseMenu", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_F1));
    public InputBindingGroup fullscreen = new InputBindingGroup("game.fullscreen", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_F11), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_F10));
    public InputBindingGroup screenshot = new InputBindingGroup("game.screenshot", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_F12));

    public InputBindingGroup editorPause = new InputBindingGroup("editor.pause", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_ESCAPE));
    public InputBindingGroup editorObjectMenu = new InputBindingGroup("editor.objects", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_SPACE));
    public InputBindingGroup editorPlay = new InputBindingGroup("editor.play", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_ENTER));
    public InputBindingGroup editorToggleControls = new InputBindingGroup("editor.controls", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_TAB));
    public InputBindingGroup editorUndo = new InputBindingGroup("editor.undo", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_Z));
    public InputBindingGroup editorRedo = new InputBindingGroup("editor.redo", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_Y));

    public InputBindingGroup editorUse = new InputBindingGroup("editor.use", new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_1));
    public InputBindingGroup editorAction = new InputBindingGroup("editor.action", new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_2));
    public InputBindingGroup editorTeam = new InputBindingGroup("editor.team", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_T));
    public InputBindingGroup editorRotate = new InputBindingGroup("editor.rotate", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_R));
    public InputBindingGroup editorHeight = new InputBindingGroup("editor.height", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_H));
    public InputBindingGroup editorGroupID = new InputBindingGroup("editor.groupID", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_G));

    public InputBindingGroup editorBuild = new InputBindingGroup("editor.build", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_B));
    public InputBindingGroup editorErase = new InputBindingGroup("editor.erase", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_E));
    public InputBindingGroup editorCamera = new InputBindingGroup("editor.camera", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_I));
    public InputBindingGroup editorZoomIn = new InputBindingGroup("editor.zoomIn", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_EQUAL));
    public InputBindingGroup editorZoomOut = new InputBindingGroup("editor.zoomOut", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_MINUS));
    public InputBindingGroup editorRevertCamera = new InputBindingGroup("editor.revertCamera", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_0));

    public InputBindingGroup editorResetTool = new InputBindingGroup("editor.resetTool", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_0));
    public InputBindingGroup editorSquare = new InputBindingGroup("editor.square", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_Q));
    public InputBindingGroup editorCircle = new InputBindingGroup("editor.circle", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_N));
    public InputBindingGroup editorLine = new InputBindingGroup("editor.line", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_F));
    public InputBindingGroup editorWand = new InputBindingGroup("editor.wand", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_W));
    public InputBindingGroup editorWandDiscontiguous = new InputBindingGroup("editor.wand_discontiguous", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_D));

    public InputBindingGroup editorNextObj = new InputBindingGroup("editor.nextObject", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_RIGHT), new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_5));
    public InputBindingGroup editorPrevObj = new InputBindingGroup("editor.prevObject", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_LEFT), new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_4));
    public InputBindingGroup editorNextType = new InputBindingGroup("editor.nextType", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_DOWN));
    public InputBindingGroup editorPrevType = new InputBindingGroup("editor.prevType", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_UP));
    public InputBindingGroup editorNextMeta = new InputBindingGroup("editor.nextMeta", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_EQUAL));
    public InputBindingGroup editorPrevMeta = new InputBindingGroup("editor.prevMeta", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_MINUS));

    public InputBindingGroup editorSelect = new InputBindingGroup("editor.select", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_S));
    public InputBindingGroup editorDeselect = new InputBindingGroup("editor.deselect", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_BACKSPACE));
    public InputBindingGroup editorHoldSquare = new InputBindingGroup("editor.holdSquare", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_LEFT_SHIFT), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_RIGHT_SHIFT));
    public InputBindingGroup editorLockSquare = new InputBindingGroup("editor.lockSquare", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_L));
    public InputBindingGroup editorSelectAddToggle = new InputBindingGroup("editor.selectToggle", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_A));
    public InputBindingGroup editorPickBlock = new InputBindingGroup("editor.pickBlock", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_P), new InputBinding(InputBinding.InputType.mouse, InputCodes.MOUSE_BUTTON_3));

    public InputBindingGroup editorCopy = new InputBindingGroup("editor.copy", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_C));
    public InputBindingGroup editorCut = new InputBindingGroup("editor.cut", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_X));
    public InputBindingGroup editorPaste = new InputBindingGroup("editor.paste", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_V));
    public InputBindingGroup editorFlipHoriz = new InputBindingGroup("editor.flipHoriz", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_H));
    public InputBindingGroup editorFlipVert = new InputBindingGroup("editor.flipVert", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_J));
    public InputBindingGroup editorRotateClockwise = new InputBindingGroup("editor.rotateClockwise", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_K), new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_R));

    public void save()
    {
        try
        {
            if (!file.exists())
            {
                file.create();
            }

            file.startWriting();

            for (InputBindingGroup i: Game.game.inputBindings.values())
            {
                file.println(i.toString());
            }

            file.stopWriting();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    public void load()
    {
        try
        {
            if (!file.exists())
            {
                this.save();
            }

            file.startReading();

            while (file.hasNextLine())
            {
                String[] s = file.nextLine().split("=");
                for (InputBindingGroup i : Game.game.inputBindings.values())
                {
                    if (s[0].equals(i.name))
                    {
                        String[] s2 = s[1].split(",");

                        String[] i1 = s2[0].split(":");
                        String[] i2 = s2[1].split(":");

                        if (i1[0].equals("null"))
                            i.input1.inputType = null;
                        else
                            i.input1.inputType = InputBinding.InputType.valueOf(i1[0]);

                        i.input1.input = Integer.parseInt(i1[1]);

                        if (i2[0].equals("null"))
                            i.input2.inputType = null;
                        else
                            i.input2.inputType = InputBinding.InputType.valueOf(i2[0]);
                        i.input2.input = Integer.parseInt(i2[1]);
                    }
                }
            }

            file.stopReading();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }
}
