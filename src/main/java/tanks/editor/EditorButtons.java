package tanks.editor;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.ToBooleanFunction;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Collections;

public class EditorButtons
{
    public static boolean slideAnimation = false;
    public static double animationMultiplier = 0;

    public ScreenLevelEditor editor;
    public ArrayList<EditorButton>[] buttons;

    public boolean prevShowControls = true;
    public double animationTimer = 50;

    public double[] xs;
    public double[] ys;

    public ArrayList<EditorButton> topLeft = new ArrayList<>();
    public ArrayList<EditorButton> topRight = new ArrayList<>();
    public ArrayList<EditorButton> bottomLeft = new ArrayList<>();
    public ArrayList<EditorButton> bottomRight = new ArrayList<>();

    public EditorButtons(ScreenLevelEditor editor)
    {
        this.editor = editor;
    }

    public void draw()
    {
        if (editor.paused)
            return;

        updateAnimation();

        if ((!editor.showControls && !slideAnimation) || buttons == null)
            return;

        for (ArrayList<EditorButton> bl : buttons)
            for (EditorButton b : bl)
                if (b.shown)
                    b.draw();
    }

    public void update()
    {
        buttons = new ArrayList[]{topLeft, topRight, bottomLeft, bottomRight};

        if (Game.game.window.hasResized)
            refreshButtons();

        for (ArrayList<EditorButton> bl : buttons)
        {
            for (EditorButton b : bl)
            {
                if (!b.fullInfo)
                    refreshButtons();

                boolean prev = b.shown;
                //noinspection AssignmentUsedAsCondition
                if (b.shown = b.shownFunc.apply())
                {
                    b.enabled = !b.disabledFunc.apply();

                    if (editor.showControls || slideAnimation)
                        b.update();

                    if (b.keybind.isValid())
                    {
                        b.function.run();
                        b.keybind.invalidate();
                    }
                }

                if (b.shown != prev)
                    refreshButtons();
            }
        }
    }

    public void updateAnimation()
    {
        if (editor.showControls != prevShowControls)
        {
            slideAnimation = true;
            animationTimer = 50 - animationTimer;
            prevShowControls = editor.showControls;
        }

        if (slideAnimation)
        {
            animationTimer += Panel.frameFrequency * 2;
            animationMultiplier = Math.sin(animationTimer / 50 * Math.PI / 2);
            refreshButtons();

            if (animationTimer > 50)
                slideAnimation = false;
        }
    }

    public void refreshButtons()
    {
        if (buttons == null)
            return;

        updateCornerCoords();

        boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >=
                Game.game.window.absoluteHeight - Drawing.drawing.statsHeight - 0.001;
        double vStep = 0;
        double hStep = 0;

        if (vertical)
            vStep = 100 * editor.controlsSizeMultiplier;
        else
            hStep = 100 * editor.controlsSizeMultiplier;

        for (int i = 0; i < 4; i++)
            setPositionAndParams(buttons[i], i, hStep, vStep);
    }

    public void updateCornerCoords()
    {
        double x1 = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
                + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * editor.controlsSizeMultiplier;
        double y1 = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale
                - Drawing.drawing.interfaceSizeY) / 2 + 50 * editor.controlsSizeMultiplier;
        double x2 = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
                + Drawing.drawing.interfaceSizeX - 50 * editor.controlsSizeMultiplier - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
        double y2 = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
                + Drawing.drawing.interfaceSizeY - 50 * editor.controlsSizeMultiplier;

        xs = new double[]{x1, x2, x1, x2};
        ys = new double[]{y1, y1, y2, y2};
    }

    public void setPositionAndParams(ArrayList<EditorButton> arr, int pos, double h, double v)
    {
        if (pos == 1 || pos == 3)
            h = -h;

        if (pos == 2 || pos == 3)
            v = -v;

        if (slideAnimation)
        {
            double[] axis = v != 0 ? xs : ys;
            boolean direction = v != 0 ? pos % 2 == 0 : pos < 2;
            if (!editor.showControls)
                direction = !direction;

            axis[pos] += (animationMultiplier * (direction ? 1 : -1) * 100 * editor.controlsSizeMultiplier) + (editor.showControls ? -1 : 0) * (pos % 2 == 1 ? -1 : 1) * 100 * editor.controlsSizeMultiplier;
        }

        EditorButton prev = null;

        for (int i = 0; i < arr.size(); i++)
        {
            EditorButton b = arr.get(i);
            if ((v != 0 ? v : h) < 0)
                b = arr.get(arr.size() - i - 1);

            if (i > 0 && prev != null)
                b.setPosition(prev.posX + h, prev.posY + v);
            else
                b.setPosition(xs[pos], ys[pos]);

            b.fullInfo = true;
            b.sizeX = b.sizeY = 70 * editor.controlsSizeMultiplier;
            b.imageSizeX = b.baseImageSX * editor.controlsSizeMultiplier;
            b.imageSizeY = b.baseImageSY * editor.controlsSizeMultiplier;

            //noinspection AssignmentUsedAsCondition
            if (b.shown = b.shownFunc.apply())
                prev = b;
        }
    }

    public static class EditorButton extends Button
    {
        public ArrayList<EditorButton> location;
        public boolean firstFrame = true;

        public ToBooleanFunction disabledFunc;
        public ToBooleanFunction shownFunc;
        public Runnable resetFunc;

        public ArrayList<EditorButton> subMenuButtons = new ArrayList<>();
        public double keyHoldTime = 0;
        public double menuOpenAge = -9999;
        public int option = 0;
        public boolean showSubButtons = false;

        public String secondaryImage;
        public double baseImageSX;
        public double baseImageSY;
        public boolean shown;

        public EditorButton(ArrayList<EditorButton> location, String image, double imageSX, double imageSY, Runnable f, String description, InputBindingGroup keybind)
        {
            this(location, image, imageSX, imageSY, f, () -> false, () -> true, description, keybind);
        }

        public EditorButton(
                ArrayList<EditorButton> location, String image, double imageSX, double imageSY, Runnable f,
                ToBooleanFunction disabledFunc, String description, InputBindingGroup keybind
        )
        {
            this(location, image, imageSX, imageSY, f, disabledFunc, () -> true, description, keybind);
        }

        public EditorButton(
                String image, double imageSX, double imageSY, Runnable f,
                ToBooleanFunction disabledFunc, String description, InputBindingGroup keybind
        )
        {
            this(null, image, imageSX, imageSY, f, disabledFunc, () -> true, description, keybind);
        }

        public EditorButton(
                ArrayList<EditorButton> location, String image, double imageSX, double imageSY, Runnable f,
                ToBooleanFunction disabledFunc, ToBooleanFunction shownFunc,
                String description, InputBindingGroup keybind
        )
        {
            super(0, -1000, 70, 70, "", f, description, keybind != null ? keybind.getInputs() : "");

            if (image != null)
                this.image = image;

            if (!this.image.startsWith("text:"))
                this.image = "icons/" + this.image;

            this.imageSizeX = imageSX;
            this.imageSizeY = imageSY;
            this.baseImageSX = imageSX;
            this.baseImageSY = imageSY;
            this.fullInfo = true;
            this.disabledClick = true;

            this.keybind = keybind;
            this.disabledFunc = disabledFunc;
            this.shownFunc = shownFunc;
            this.location = location;

            if (location != null)
                location.add(this);
        }

        @Override
        public void draw()
        {
            if (!subMenuButtons.isEmpty())
            {
                double percentage = Math.sin(Math.min((age - menuOpenAge) / 25, 1) * Math.PI / 2);
                if (!showSubButtons)
                    percentage = 1 - percentage;

                double totalSizeX = (sizeX + 10) * (subMenuButtons.size() + 1);
                double sX = percentage * totalSizeX;

                if (slideAnimation)
                {
                    if (showSubButtons)
                        menuOpenAge = age;
                    showSubButtons = false;
                }

                if (!enabled)
                    Drawing.drawing.setColor(bgColR - 10, bgColG - 10, bgColB - 10, 200);
                else
                    Drawing.drawing.setColor(disabledColR, disabledColG, disabledColB, 200);

                double centerX = posX - sizeX / 2 + Math.max(sizeX / 2, sX / 2);
                Drawing.drawing.fillInterfaceRect(centerX, posY, sX, sizeY, 9999);

                for (int i = subMenuButtons.size() - 1; i >= 0; i--)
                {
                    EditorButton b = subMenuButtons.get(i);
                    double x = posX + (sizeX + 10) * (i + 1) - (totalSizeX - sX);
                    if (x <= posX)
                        continue;

                    b.enableHover = !b.hoverTextRaw.isEmpty() && percentage == 1;
                    b.setPosition(x, posY);
                    b.bgColA = 255;
                    b.draw();
                }
            }

            super.draw();

            if (secondaryImage != null && option > 0 && option <= subMenuButtons.size())
            {
                EditorButton b = subMenuButtons.get(option - 1);
                imageXOffset = imageSizeX * -0.15;
                imageYOffset = imageSizeY * -0.1;
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.drawInterfaceImage(secondaryImage, posX + imageSizeX * 0.25, posY + imageSizeY * 0.25, b.imageSizeX * 0.75, b.imageSizeY * 0.75);
            }
            else
            {
                imageXOffset = 0;
                imageYOffset = 0;
            }
        }

        @Override
        public void update()
        {
            super.update();

            if (firstFrame && age > 10)
            {
                setUpSubButtons();
                firstFrame = false;
            }

            if ((!enabled || showSubButtons) && Game.game.input.editorResetTool.isValid())
            {
                if (secondaryImage == null)
                    return;

                secondaryImage = null;
                Game.game.input.editorResetTool.invalidate();

                if (resetFunc != null)
                    resetFunc.run();
            }

            if (showSubButtons && (keybind != null && keybind.isPressed()))
            {
                if (Game.game.window.validScrollUp)
                {
                    Game.game.window.validScrollUp = false;
                    option--;
                    setOption();
                }

                if (Game.game.window.validScrollDown)
                {
                    Game.game.window.validScrollDown = false;
                    option++;
                    setOption();
                }
            }

            for (EditorButton b : subMenuButtons)
            {
                b.enabled = !b.disabledFunc.apply();

                if (showSubButtons)
                    b.update();
                else
                    b.updateKeybind();
            }
        }

        private void setOption()
        {
            int len = subMenuButtons.size() + 1;
            option = (option + len) % len;
            if (option == 0)
            {
                if (resetFunc != null)
                    resetFunc.run();
                secondaryImage = null;
                return;
            }

            subMenuButtons.get(option - 1).onClick();
            keyHoldTime = 60;
        }

        @Override
        public void onClick()
        {
            super.onClick();
            Game.game.window.pressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
        }

        @Override
        public void updateKeybind()
        {
            super.updateKeybind();
            if (keybind == null)
                return;

            if (keybind.isPressed())
                keyHoldTime = Math.min(60, keyHoldTime + Panel.frameFrequency);
            else
                keyHoldTime = Math.max(0, keyHoldTime - Panel.frameFrequency * 0.1);

            boolean prev = showSubButtons;
            showSubButtons = keyHoldTime >= 50;
            if (showSubButtons != prev)
                menuOpenAge = age;
        }

        public void setUpSubButtons()
        {
            for (EditorButton b : subMenuButtons)
            {
                b.bgColR = bgColR;
                b.bgColG = bgColG;
                b.bgColB = bgColB;

                b.disabledColR += 30;
                b.disabledColG += 30;
                b.disabledColB += 30;

                b.sizeX = sizeX * 0.9;
                b.sizeY = sizeY * 0.9;
                b.imageSizeX *= 0.75;
                b.imageSizeY *= 0.75;

                Runnable r = b.function;
                b.function = () ->
                {
                    secondaryImage = b.image;
                    option = subMenuButtons.indexOf(b) + 1;
                    r.run();
                };
            }
        }

        public EditorButton addSubButtons(EditorButton... buttons)
        {
            Collections.addAll(subMenuButtons, buttons);
            return this;
        }

        public EditorButton onReset(Runnable f)
        {
            this.resetFunc = f;
            return this;
        }

        public EditorButton setDescription(String s, Object... options)
        {
            this.hoverTextRaw = String.format(s, options);
            this.hoverTextRawTranslated = Translation.translate(s, options);
            this.hoverText = this.hoverTextRawTranslated.split("---");
            return this;
        }

        public EditorButton moveToBottom()
        {
            location.remove(this);
            location.add(this);
            return this;
        }
    }
}