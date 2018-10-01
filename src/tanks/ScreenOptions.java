package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ScreenOptions extends Screen
{
	Button graphics = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 90, 350, 40, "Graphics: fancy", new Runnable()
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
	},
		"Fast graphics disable most graphical effects and use solid colors for the background---Fancy graphics may significantly reduce framerate"	);

	Button mouseTarget = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 30, 350, 40, "Mouse target: on", new Runnable()
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
	},
		"When enabled, 2 small black rings will appear around your mouse pointer"	);

	Button scale = new Button(0, 0, 350, 40, "Scale: 100%", new Runnable()
	{
		@Override
		public void run() 
		{
			if (InputKeyboard.keys.contains(KeyEvent.VK_SHIFT))
				Drawing.scale -= 0.1;
			else
				Drawing.scale += 0.1;

			if (Drawing.scale < 0.45)
				Drawing.scale = 2;

			if (Drawing.scale > 2.05)
				Drawing.scale = 0.5;

			Drawing.scale = Math.round(Drawing.scale * 10) / 10.0;

			scale.text = "Scale: " + (int)Math.round(Drawing.scale * 100) + "%";
			Game.window.setSize((int)(Drawing.sizeX * Drawing.scale), (int) ((Drawing.sizeY) * Drawing.scale ));
		}
	}
	, "Click to increase scale by 10%---Hold shift while clicking to decrease scale by 10%");


	Button back = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 90, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.screen = new ScreenTitle();
		}
	}
			);

	Button insanity = new Button(0, 0, 350, 40, "Insanity mode: disabled", new Runnable()
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
	
	Button autostart = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Autostart: on", new Runnable()
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
	},
		"When enabled, levels will start playing automatically---4 seconds after they are loaded if the play button isn't clicked earlier"	);

	
	@Override
	public void update()
	{
		autostart.update();
		mouseTarget.update();
		graphics.update();
		back.update();
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);
		back.draw(g);
		autostart.draw(g);
		mouseTarget.draw(g);
		graphics.draw(g);
		Drawing.setInterfaceFontSize(g, 24);
		g.setColor(Color.black);
		Drawing.drawInterfaceText(g, Drawing.sizeX / 2, Drawing.sizeY / 2 - 150, "Options");
	}

}
