package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;

import java.util.ArrayList;

public abstract class ScreenElement
{
    public double duration;
    public double age = 0;

    public static class Notification extends ScreenElement
    {
        public ArrayList<String> text;
        public double sizeY;
        public double removeDuration = 100;
        public double width = 250;

        public Notification(String text)
        {
            this(text, 1000, 250);
        }

        public Notification(String text, double duration)
        {
            this(text, duration, 250);
        }

        public Notification(String text, double duration, double width)
        {
            Drawing.drawing.playSound("toast.ogg");
            this.text = Drawing.drawing.wrapText(text, width, 16);
            this.width = width;
            this.duration = duration;
            this.sizeY = Math.max(2, this.text.size() + 1) * 20;
        }

        public double draw(double prevSY)
        {
            this.age += Panel.frameFrequency;

            double mult = Math.sin(Math.min(1, this.age / 50.0) * Math.PI / 2);
            double addX = (1 - mult) * 400;
            double colA = mult * 255 * Math.min(1, 2.0 - this.age / (this.duration * 0.5));
            double x = Drawing.drawing.interfaceSizeX - 70 - this.width;
            double y = Drawing.drawing.interfaceSizeY - Drawing.drawing.statsHeight - sizeY - 80 - prevSY;

            double bg = Level.isDark() ? 0 : 255;
            double fg = Level.isDark() ? 255 : 0;

            Drawing.drawing.setColor(bg, bg, bg, colA / 2);
            Drawing.drawing.drawConcentricPopup(x + this.width / 2 + 33 + addX, y + sizeY / 2, this.width + 65, sizeY + 10, 5, 27);
            Drawing.drawing.setInterfaceFontSize(16);

            Drawing.drawing.setColor(fg, fg, fg, colA);
            for (int i = 0; i < this.text.size(); i++)
            {
                double r = Game.game.window.colorR;
                double g = Game.game.window.colorG;
                double b = Game.game.window.colorB;
                Drawing.drawing.setColor(fg, fg, fg, colA);
                Drawing.drawing.drawUncenteredInterfaceText(x + 50 + addX, y + i * 20 + 12, String.format("\u00A7%03d%03d%03d255", (int) (r * 255), (int) (g * 255), (int) (b * 255)) + this.text.get(i));
            }

            Drawing.drawing.setColor(0, 150, 255, colA);
            Drawing.drawing.fillInterfaceOval(x + 27 + addX, y + 20, 25, 25);

            Drawing.drawing.setColor(255, 255, 255, colA);
            Drawing.drawing.setInterfaceFontSize(16);
            Drawing.drawing.drawInterfaceText(x + 27 + addX, y + 20, "!");

            double deteriorationProgress = Math.max(this.age - this.duration, 0) / this.removeDuration;
            return (this.sizeY + 15) * (Math.sin(Math.PI * (0.5 - deteriorationProgress)) + 1) / 2;
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
