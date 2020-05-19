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

	double time = 0;

	public ScreenConnecting(Thread t)
	{
		this.music = "tomato_feast_3.ogg";
		this.musicID = "menu";

		this.thread = t;
	}

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = Game.lastOfflineScreen;
			try
			{
				thread.stop();
			}
			catch (Exception ignored)
			{

			}
		}
	}
	);

	@Override
	public void update() 
	{
		back.update();
		time += Panel.frameFrequency;
	}

	@Override
	public void draw() 
	{
		this.drawDefaultBackground();
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(24);

		double size = Math.min(1, time / 50);
		double size2 = Math.min(1, Math.max(0, time / 50 - 0.25));
		double size3 = Math.min(1, Math.max(0, time / 50 - 0.5));

		if (!this.finished)
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, this.text);

			Drawing.drawing.setColor(0, 0, 0);

			drawSpinny(100, 4, 0.3, 45 * size, 1 * size);
			drawSpinny(99, 3, 0.5, 30 * size2, 0.75 * size2);
			drawSpinny(100, 2, 0.7, 15 * size3, 0.5 * size3);
		}
		else
		{
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.text);

			Drawing.drawing.setInterfaceFontSize(14);
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
			Drawing.drawing.fillInterfaceOval(Drawing.drawing.interfaceSizeX / 2 + size * Math.cos(frac * Math.PI * 2),
					Drawing.drawing.interfaceSizeY / 2 - 25 + size * Math.sin(frac * Math.PI * 2),
					s * dotSize, s * dotSize);
		}
	}

}
