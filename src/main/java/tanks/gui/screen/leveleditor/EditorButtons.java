package tanks.gui.screen.leveleditor;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.ToBooleanFunction;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
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
    public double animationTime = 50;
    public double animationTimer = animationTime;

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
                b.shown = b.shownFunc.apply();
                if (b.shown)
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
            animationTimer = animationTime - animationTimer;
            prevShowControls = editor.showControls;
        }

        if (slideAnimation)
        {
            animationTimer += Panel.frameFrequency * 2;
            animationMultiplier = Math.sin(animationTimer / animationTime * Math.PI / 2);
            refreshButtons();

            if (animationTimer > animationTime)
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

            int posDir = (pos % 2 == 1 ? -1 : 1);
            if (v == 0)
                posDir = pos / 2 == 1 ? -1 : 1;
            axis[pos] += (animationMultiplier * (direction ? 1 : -1) * 100 * editor.controlsSizeMultiplier) + (editor.showControls ? -1 : 0) * posDir * 100 * editor.controlsSizeMultiplier;
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

            b.shown = b.shownFunc.apply();
            if (b.shown)
                prev = b;
        }
    }

    public static class EditorButton extends Button
    {
        public EditorButton parent;

        public ArrayList<EditorButton> location;
        public boolean firstFrame = true;

        public ToBooleanFunction disabledFunc;
        public ToBooleanFunction shownFunc;
        public Runnable resetFunc;

        public ArrayList<EditorButton> subMenuButtons = new ArrayList<>();
        public double hoverTime = 0;
        public double menuOpenAge = -9999;
        public int option = 0;
        public boolean showSubButtons = false;
        public boolean subButtonsAsOptions = true;

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
                String description, InputBindingGroup keybind)
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
            boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >=
                    Game.game.window.absoluteHeight - Drawing.drawing.statsHeight - 0.001;

            if (!subMenuButtons.isEmpty())
            {
                double percentage = Math.sin(Math.min((age - menuOpenAge) / 25, 1) * Math.PI / 2);
                if (!showSubButtons)
                    percentage = 1 - percentage;

                double totalSize = (sizeX + 10) * (subMenuButtons.size() + 1) - 10;
                double s = percentage * totalSize;

                if (slideAnimation)
                {
                    if (showSubButtons)
                        menuOpenAge = age;
                    showSubButtons = false;
                }

                Drawing.drawing.setColor(disabledColR * 0.75, disabledColG * 0.75, disabledColB * 0.75, 200);

                if (!vertical)
                {
                    double centerY = posY - sizeY / 2 + Math.max(sizeY / 2, s / 2);
                    Drawing.drawing.fillInterfaceRect(posX, centerY, sizeX, s, 9999);
                }
                else
                {
                    double centerX = posX - sizeX / 2 + Math.max(sizeX / 2, s / 2);
                    Drawing.drawing.fillInterfaceRect(centerX, posY, s, sizeY, 9999);
                }

                for (int i = subMenuButtons.size() - 1; i >= 0; i--)
                {
                    EditorButton b = subMenuButtons.get(i);

                    double x = posX;
                    double y = posY;

                    if (!vertical)
                    {
                        y = posY + (sizeY + 10) * (i + 1) - (totalSize - s);
                        if (y <= posY)
                            continue;
                    }
                    else
                    {
                        x = posX + (sizeX + 10) * (i + 1) - (totalSize - s);
                        if (x <= posX)
                            continue;
                    }

                    b.enableHover = !b.hoverTextRaw.isEmpty() && percentage == 1;
                    b.sizeX = sizeX * 0.9;
                    b.sizeY = sizeY * 0.9;
                    b.setPosition(x, y);
                    b.bgColA = 255;
                    b.draw();
                }
            }

            super.draw();

            if (secondaryImage != null && option > 0 && option <= subMenuButtons.size() && !this.showSubButtons)
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

            if (keybind != null && keybind.isPressed() && subButtonsAsOptions)
            {
                if (Game.game.window.validScrollUp)
                {
                    Game.game.window.validScrollUp = false;
                    option--;
                    initOption();
                }

                if (Game.game.window.validScrollDown)
                {
                    Game.game.window.validScrollDown = false;
                    option++;
                    initOption();
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

        protected void initOption()
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
        }

        @Override
        public void onClick()
        {
            if (parent != null)
                parent.onClick();

            if (subButtonsAsOptions)
            {
                option = 0;
                initOption();
            }

            if (this.enabled || disabledClick)
                super.onClick();

            Game.game.window.pressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
        }

        @Override
        public void updateKeybind()
        {
            if (!(this.enabled || disabledClick) && keybind != null && keybind.isValid() && subButtonsAsOptions)
            {
                if (option != 0)
                {
                    option = 0;
                    initOption();
                }

                keybind.invalidate();
            }

            super.updateKeybind();
            if (keybind == null)
                return;

            if (this.selected && ((this.subButtonsAsOptions && !this.enabled) || (!this.subButtonsAsOptions && this.enabled)))
                hoverTime = 25;
            else if (this.hoverTime > 0)
            {
                double totalSize = (sizeX + 10) * (subMenuButtons.size() + 1) - 10;

                boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >=
                        Game.game.window.absoluteHeight - Drawing.drawing.statsHeight - 0.001;

                double startX;
                double startY;
                double endX;
                double endY;

                if (vertical)
                {
                    startX = this.posX - this.sizeX / 2;
                    endX = startX + totalSize;
                    startY = this.posY - this.sizeY / 2;
                    endY = this.posY + this.sizeY / 2;
                }
                else
                {
                    startX = this.posX - this.sizeX / 2;
                    endX = this.posX + this.sizeX / 2;
                    startY = this.posY - this.sizeY / 2;
                    endY = startY + totalSize;
                }

                double mx = Drawing.drawing.getInterfaceMouseX();
                double my = Drawing.drawing.getInterfaceMouseY();

                if (!(this.subButtonsAsOptions || this.enabled))
                    hoverTime = 0;
                else if (mx >= startX && mx <= endX && my >= startY && my <= endY)
                    hoverTime = 25;
                else
                    hoverTime = Math.max(0, this.hoverTime - Panel.frameFrequency);
            }

            boolean prev = showSubButtons;
            showSubButtons = hoverTime > 0;
            if (showSubButtons != prev)
                menuOpenAge = age;
        }

        public EditorButton setParent(EditorButton parent)
        {
            this.parent = parent;
            return this;
        }

        public void setUpSubButtons()
        {
            for (EditorButton b : subMenuButtons)
            {
                b.bgColR = bgColR;
                b.bgColG = bgColG;
                b.bgColB = bgColB;

                b.sizeX = sizeX * 0.9;
                b.sizeY = sizeY * 0.9;
                b.imageSizeX *= 0.75;
                b.imageSizeY *= 0.75;

                Runnable r = b.function;
                b.function = () ->
                {
                    if (subButtonsAsOptions)
                    {
                        secondaryImage = b.image;
                        option = subMenuButtons.indexOf(b) + 1;
                    }
                    r.run();
                };
            }
        }

        public EditorButton addSubButtons(EditorButton... buttons)
        {
            Collections.addAll(subMenuButtons, buttons);
            for (EditorButton b : buttons)
                b.setParent(this);
            return this;
        }

        public EditorButton setSubButtonsAsOptions(boolean value)
        {
            this.subButtonsAsOptions = value;
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