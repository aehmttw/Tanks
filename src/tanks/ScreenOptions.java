package tanks;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ScreenOptions extends Screen
{
	Button graphics = new Button(350, 40, "Graphics: fancy", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.graphicalEffects = !Game.graphicalEffects;

			if (Game.graphicalEffects)
				graphics.text = "Graphics: fancy";
			else
				graphics.text = "Graphics: fast";
		}
	}
			);

	Button mouseTarget = new Button(350, 40, "Mouse target: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Panel.showMouseTarget = !Panel.showMouseTarget;

			if (Panel.showMouseTarget)
				mouseTarget.text = "Mouse target: on";
			else
				mouseTarget.text = "Mouse target: off";
		}
	}
			);

	Button scale = new Button(350, 40, "Scale: 100%", new Runnable()
	{
		@Override
		public void run() 
		{
			if (KeyInputListener.keys.contains(KeyEvent.VK_SHIFT))
				Window.scale -= 0.1;
			else
				Window.scale += 0.1;

			if (Window.scale < 0.45)
				Window.scale = 2;

			if (Window.scale > 2.05)
				Window.scale = 0.5;

			Window.scale = Math.round(Window.scale * 10) / 10.0;

			scale.text = "Scale: " + (int)Math.round(Window.scale * 100) + "%";
			Game.window.setSize((int)(Window.sizeX * Window.scale), (int) ((Window.sizeY) * Window.scale ));
		}
	}
	, "Click to increase scale by 10%---Hold shift while clicking to decrease scale by 10%");


	Button back = new Button(350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenTitle();
		}
	}
			);

	Button insanity = new Button(350, 40, "Insanity mode: disabled", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.insanity = !Game.insanity;

			if (Game.insanity)
				insanity.text = "Insanity mode: enabled";
			else
				insanity.text = "Insanity mode: disabled";
		}
	}
			);
	
	Button autostart = new Button(350, 40, "Autostart: on", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.autostart = !Game.autostart;

			if (Game.autostart)
				autostart.text = "Autostart: on";
			else
				autostart.text = "Autostart: off";
		}
	}
			);

	@Override
	public void update()
	{
		autostart.update(Window.sizeX / 2, Window.sizeY / 2 + 30);
		mouseTarget.update(Window.sizeX / 2, Window.sizeY / 2 - 30);
		graphics.update(Window.sizeX / 2, Window.sizeY / 2 - 90);
		back.update(Window.sizeX / 2, Window.sizeY / 2 + 90);
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);
		autostart.draw(g, Window.sizeX / 2, Window.sizeY / 2 + 30);
		mouseTarget.draw(g, Window.sizeX / 2, Window.sizeY / 2 - 30);
		graphics.draw(g, Window.sizeX / 2, Window.sizeY / 2 - 90);
		back.draw(g, Window.sizeX / 2, Window.sizeY / 2 + 90);
		Window.drawText(g, Window.sizeX / 2, Window.sizeY / 2 - 150, "Options");
	}

}
