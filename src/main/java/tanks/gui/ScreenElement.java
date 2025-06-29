package tanks.gui;

import tanks.Drawing;
import tanks.Level;
import tanks.Panel;

import java.util.ArrayList;

public abstract class ScreenElement
{
    public int duration;
    public double age = 0;

    public static class Notification extends ScreenElement
    {
        public ArrayList<String> text;
        public double sY;
        public boolean fadeStart = false;

        public int colorR = 255, colorG = 255, colorB = 128;

        public Notification(String text)
        {
            this(text, 500);
        }

        public Notification(String text, int duration)
        {
            this.text = Drawing.drawing.wrapText(text, 250, 16);
            this.duration = duration;
            this.sY = Math.max(4, this.text.size() + 2) * 20;
        }

        public void draw(double prevSY)
        {
            this.age += Panel.frameFrequency;

            double fadeDuration = Math.min(75, duration * 0.2);
            double mult = Math.sin(Math.min(Math.min(1, age / fadeDuration), 1 - Math.max(0, age - duration + fadeDuration) / fadeDuration) * Math.PI / 2);
            double addX = (1 - mult) * 400, addY = (1 - mult) * 50, colA = mult * 255;
            double x = Drawing.drawing.interfaceSizeX - 320;
            double y = Drawing.drawing.interfaceSizeY - Drawing.drawing.statsHeight - sY - 80 - prevSY;

            if (!fadeStart && duration - age < fadeDuration)
            {
                fadeStart = true;
                Panel.lastNotifTime = System.currentTimeMillis();
            }

            Drawing.drawing.setColor(0, 0, 0, colA / 2);
            Drawing.drawing.drawPopup(x + 158 + addX, y + addY + sY / 2, 315, sY + 10, 5, 5);
            Drawing.drawing.setInterfaceFontSize(16);

            Drawing.drawing.setColor(colorR, colorG, colorB);
            Drawing.drawing.fillInterfaceProgressRect(x + 157.5 + addX, y + addY + sY - 2.5, 303, 5, 1 - Math.min(duration, age + fadeDuration - 10) / duration);

            Drawing.drawing.setColor(255, 255, 255, colA);
            for (int i = 0; i < this.text.size(); i++)
                Drawing.drawing.drawUncenteredInterfaceText(x + 50 + addX, y + addY + i * 20 + 12, this.text.get(i));

            Drawing.drawing.setColor(0, 150, 255, colA);
            Drawing.drawing.fillInterfaceOval(x + 27 + addX, y + addY + 20, 25, 25);

            Drawing.drawing.setColor(255, 255, 255, colA);
            Drawing.drawing.setInterfaceFontSize(16);
            Drawing.drawing.drawInterfaceText(x + 27 + addX, y + addY + 20, "!");
        }

        public Notification setColor(int r, int g, int b)
        {
            this.colorR = r;
            this.colorG = g;
            this.colorB = b;
            return this;
        }
    }

    public static class CenterMessage extends ScreenElement
    {
        public boolean previous;
        public TextWithStyling styling;
        public double baseColorA = -1;

        public CenterMessage(String message, int duration, Object... objects)
        {
            message = String.format(message, objects);
            int brightness = Level.isDark() ? 255 : 0;
            this.styling = new TextWithStyling(message, brightness, brightness, brightness, 80 - Math.max(8, message.length() * 2));
            this.styling.colorA = 128;
            this.duration = duration;
            this.previous = Panel.currentMessage != null;
        }

        public void draw()
        {
            this.age += Panel.frameFrequency;
            this.styling.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 200);

            if (this.age < 50 && !previous)
            {
                if (this.baseColorA < 0)
                    this.baseColorA = this.styling.colorA;

                this.styling.colorA = this.baseColorA * Math.min(1, this.age / 50);
            }
            else if (this.age > this.duration - 50)
                this.styling.colorA = this.baseColorA * Math.max(0, (this.duration - this.age) / 50);
            else
                this.baseColorA = this.styling.colorA;

            if (this.age > this.duration)
                Panel.currentMessage = null;
        }
    }
}
