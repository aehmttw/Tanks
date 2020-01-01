package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenConnecting extends Screen
{
	public String text = "Connecting...";
	public String exception = "";
	public boolean finished = false;
	public Thread thread;

	public ScreenConnecting(Thread t)
	{
		this.thread = t;
	}

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenJoinParty();
			thread.stop();
		}
	}
	);

	@Override
	public void update() 
	{
		back.update();
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setFontSize(24);

		if (!this.finished)
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, this.text);

			Drawing.drawing.setColor(0, 0, 0);

			drawSpinny(100, 4, 0.3, 45, 1);
			drawSpinny(99, 3, 0.5, 30, 0.75);
			drawSpinny(100, 2, 0.7, 15, 0.5);
		}
		else
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.text);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.exception);
		}

		back.draw();
	}

	public void drawSpinny(int max, int parts, double speed, double size, double dotSize)
	{
		for (int i = 0; i < max; i++)
		{
			double frac = (System.currentTimeMillis() / 1000.0 * speed + i / 100.0) % 1;
			double s = (i % (max * 1.0 / parts)) / 10.0 * parts;
			Drawing.drawing.fillOval(Drawing.drawing.interfaceSizeX / 2 + size * Math.cos(frac * Math.PI * 2),
					Drawing.drawing.interfaceSizeY / 2 - 25 + size * Math.sin(frac * Math.PI * 2),
					s * dotSize, s * dotSize);
		}
	}

}
