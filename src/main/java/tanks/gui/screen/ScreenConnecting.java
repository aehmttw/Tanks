package tanks.gui.screen;

import com.codedisaster.steamworks.SteamID;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.network.Client;

public class ScreenConnecting extends Screen
{
	public String text = "Connecting...";
	public String exception = "";
	public boolean finished = false;
	public Thread thread;
	public SteamID steamID;

	double time = 0;

	public ScreenConnecting(Thread t)
	{
		Panel.forceRefreshMusic = true;
		this.music = "menu_3.ogg";
		this.musicID = "menu";

		this.thread = t;
	}

	Button back = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = Game.lastOfflineScreen;
			Client.connectionID = null;
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
		Drawing.drawing.setInterfaceFontSize(this.textSize);

		double size = Math.min(1, time / 50);
		double size2 = Math.min(1, Math.max(0, time / 50 - 0.25));
		double size3 = Math.min(1, Math.max(0, time / 50 - 0.5));

		if (!this.finished)
		{
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, this.text);

			Drawing.drawing.setColor(0, 0, 0);

			drawSpinny(100, 4, 0.3, 45 * size, 1 * size);
			drawSpinny(99, 3, 0.5, 30 * size2, 0.75 * size2);
			drawSpinny(100, 2, 0.7, 15 * size3, 0.5 * size3);
		}
		else
		{
			Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace, this.text);

			if (Drawing.drawing.interfaceScaleZoom > 1)
				Drawing.drawing.setInterfaceFontSize(this.textSize * 5 / 12);
			else
				Drawing.drawing.setInterfaceFontSize(this.textSize * 7 / 12);
			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, this.exception);
		}

		back.draw();
	}

	public void drawSpinny(int max, int parts, double speed, double size, double dotSize)
	{
		for (int i = 0; i < max; i++)
		{
			double frac = (System.currentTimeMillis() / 1000.0 * speed + i / 100.0) % 1;
			double s = (i % (max * 1.0 / parts)) / 10.0 * parts;
			Drawing.drawing.fillInterfaceOval(this.centerX + size * Math.cos(frac * Math.PI * 2),
					this.centerY - this.objYSpace * 5 / 12 + size * Math.sin(frac * Math.PI * 2),
					s * dotSize, s * dotSize);
		}
	}

}
