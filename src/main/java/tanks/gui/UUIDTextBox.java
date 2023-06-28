package tanks.gui;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;

import java.util.ArrayList;

public class UUIDTextBox extends TextBox
{
    public UUIDTextBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText)
    {
        super(x, y, sX, sY, text, f, defaultText);
        this.maxChars = 32;
    }

    public UUIDTextBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText, String hoverText)
    {
        super(x, y, sX, sY, text, f, defaultText, hoverText);
        this.maxChars = 32;
    }

    @Override
    public void checkKeys()
    {
        if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER))
        {
            this.submit();
            return;
        }

        if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_CONTROL) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_CONTROL) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_SUPER) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_SUPER))
        {
            if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_C))
            {
                Game.game.window.textPressedKeys.clear();
                Game.game.window.textValidPressedKeys.clear();
                Game.game.window.getRawTextKeys().clear();

                Game.game.window.setClipboard(this.inputText);
            }

            if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_V))
            {
                Game.game.window.textPressedKeys.clear();
                Game.game.window.textValidPressedKeys.clear();
                Game.game.window.getRawTextKeys().clear();

                String s = Game.game.window.getClipboard();

                for (int i = 0; i < s.length(); i++)
                {
                    this.inputKey(0, s.substring(i, i + 1).toLowerCase());
                }
            }

            if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_BACKSPACE) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_DELETE))
            {
                Game.game.window.textPressedKeys.clear();
                Game.game.window.textValidPressedKeys.clear();
                Game.game.window.getRawTextKeys().clear();

                this.clear();
            }
        }

        ArrayList<Character> texts = Game.game.window.getRawTextKeys();

        for (char key : texts)
        {
            String text = Character.toString(key);
            inputKey(Game.game.window.translateTextKey(key), text);
        }

        texts.clear();

        Game.game.window.pressedKeys.clear();
        Game.game.window.validPressedKeys.clear();
    }

    public void inputKey(int key, String text)
    {
        if (key == InputCodes.KEY_BACKSPACE || key == '\b')
            inputText = inputText.substring(0, Math.max(0, inputText.length() - 1));

        if (text != null && inputText.length() + text.length() <= maxChars && "1234567890abcdef".contains(text))
        {
            this.inputText += text;
        }
    }

    @Override
    public void drawInput()
    {
        Drawing.drawing.setInterfaceFontSize(this.sizeY * 0.6);

        double width = 17;

        for (int i = 0; i < this.maxChars + 4; i++)
        {
            double x = this.posX + (i - (this.maxChars + 3) / 2.0) * width;

            int o;
            int in = i;

            if (in < 8)
                o = 0;
            else if (in == 8)
                o = -1;
            else if (in < 13)
                o = 1;
            else if (in == 13)
                o = -1;
            else if (in < 18)
                o = 2;
            else if (in == 18)
                o = -1;
            else if (in < 23)
                o = 3;
            else if (in == 23)
                o = -1;
            else
                o = 4;

            String c;

            if (o == -1)
                c = "\u00A7127127127255-";
            else if (i - o < inputText.length())
                c = inputText.charAt(i - o) + "";
            else
                c = "\u00A7127127127255_";

            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.drawInterfaceText(x, this.posY, c);
        }
    }
}
