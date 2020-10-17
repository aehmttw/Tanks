package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.gui.Button;
import tanks.obstacle.Obstacle;
import tanks.tank.TankPlayer;

import java.net.URL;

public class ScreenTitle extends Screen
{
	boolean controlPlayer = false;
	TankPlayer logo = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 250 * Drawing.drawing.interfaceScaleZoom, 0);

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Exit the game", new Runnable()
	{
		@Override
		public void run() 
		{
			if (Game.framework == Game.Framework.libgdx)
				Game.screen = new ScreenExit();
			else
			{
				if (Game.game.window.soundsEnabled)
					Game.game.window.soundPlayer.exit();

				Game.game.window.windowHandler.onWindowClose();

				System.exit(0);
			}
		}
	}
			);
	
	Button options = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "Options", new Runnable()
	{
		@Override
		public void run()
		{
			Game.silentCleanUp();
			Game.screen = new ScreenOptions();
		}
	}
			);

	Button debug = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 270, this.objWidth, this.objHeight, "Debug menu", new Runnable()
	{
		@Override
		public void run()
		{
			Game.silentCleanUp();
			Game.screen = new ScreenDebug();
		}
	}
	);

	Button about = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "About", new Runnable()
	{
		@Override
		public void run()
		{
			Game.silentCleanUp();
			Game.screen = new ScreenAbout();
		}
	}
	);
	
	Button play = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Play!", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.silentCleanUp();
			Game.screen = new ScreenPlay();
		}
	}
			);

	Button takeControl = new Button(logo.posX, logo.posY, Game.tile_size, Game.tile_size, "", new Runnable()
	{
		@Override
		public void run()
		{
			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT))
			{
				Game.bulletLocked = false;
				ScreenGame.finishTimer = ScreenGame.finishTimerMax;
				logo.depthTest = true;
				controlPlayer = true;
			}
		}
	}
		);

	public ScreenTitle()
	{
		Game.movables.clear();
		this.logo.size *= 1.5 * Drawing.drawing.interfaceScaleZoom;
		this.logo.turret.length *= 1.5 * Drawing.drawing.interfaceScaleZoom;
		this.logo.invulnerable = true;
		this.logo.drawAge = 50;
		this.logo.depthTest = false;
		Game.movables.add(logo);
		ScreenGame.finished = false;

		takeControl.silent = true;

		this.music = "tomato_feast_1.ogg";
		this.musicID = "menu";
	}
	
	@Override
	public void update()
	{
		play.update();
		exit.update();
		options.update();
		takeControl.update();

		if (Game.debug)
			debug.update();

		about.update();

		if (this.controlPlayer)
		{
			Obstacle.draw_size = Game.tile_size;
			for (int i = 0; i < Game.tracks.size(); i++)
			{
				Game.tracks.get(i).update();
			}

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				m.preUpdate();
				m.update();
			}

			for (int i = 0; i < Game.effects.size(); i++)
			{
				Game.effects.get(i).update();
			}

			Game.tracks.removeAll(Game.removeTracks);
			Game.removeTracks.clear();

			Game.movables.removeAll(Game.removeMovables);
			Game.removeMovables.clear();

			Game.effects.removeAll(Game.removeEffects);
			Game.removeEffects.clear();
		}
	}

	public void drawWithoutBackground()
	{
		play.draw();
		exit.draw();
		options.draw();

		if (Game.debug)
			debug.draw();

		about.draw();

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(60);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Tanks");
		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 110, "The Crusades");

		for (int i = 0; i < Game.tracks.size(); i++)
		{
			Game.tracks.get(i).draw();
		}

		for (int i = Game.movables.size() - 1; i >= 0; i--)
		{
			Game.movables.get(i).draw();
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).draw();
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).drawGlow();
		}
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		this.drawWithoutBackground();
	}

	@Override
	public void drawPostMouse()
	{
		if (!this.controlPlayer && (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT)))
		{
			this.logo.draw();
		}
	}
}
