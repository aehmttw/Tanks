package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.*;
import tanks.gui.Button;
import tanks.obstacle.Obstacle;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

public class ScreenTitle extends Screen implements ISeparateBackgroundScreen
{
	boolean controlPlayer = false;
	TankPlayer logo;

	public double lCenterX;
	public double lCenterY;

	public double rCenterX;
	public double rCenterY;

	Button exit = new Button(this.rCenterX, this.rCenterY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Exit the game", () ->
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
	);
	
	Button options = new Button(this.rCenterX, this.rCenterY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Options", () ->
	{
		Game.silentCleanUp();
		Game.screen = new ScreenOptions();
	}
	);

	Button debug = new Button(this.rCenterX, this.rCenterY + this.objYSpace * 3, this.objWidth, this.objHeight, "Debug menu", () ->
	{
		Game.silentCleanUp();
		Game.screen = new ScreenDebug();
	}
	);

	Button about = new Button(this.rCenterX, this.rCenterY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "About", () ->
	{
		Game.silentCleanUp();
		Game.screen = new ScreenAbout();
	}
	);
	
	Button play = new Button(this.rCenterX, this.rCenterY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Play!", () ->
	{
		Game.silentCleanUp();
		Game.screen = new ScreenPlay();
	}
	);

	Button takeControl = new Button(0, 0, Game.tile_size, Game.tile_size, "", new Runnable()
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

	Button languages = new Button(-1000, -1000, this.objHeight * 1.5, this.objHeight * 1.5, "", () ->
	{
		Game.silentCleanUp();
		Game.screen = new ScreenLanguage();
	}
	);

	public ScreenTitle()
	{
		Game.movables.clear();
		ScreenGame.finished = false;

		takeControl.silent = true;

		this.music = "menu_1.ogg";
		this.musicID = "menu";

		languages.image = "icons/language.png";

		languages.imageSizeX = this.objHeight;
		languages.imageSizeY = this.objHeight;
	}
	
	@Override
	public void update()
	{
		play.update();
		exit.update();
		options.update();

		languages.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * Drawing.drawing.interfaceScaleZoom;
		languages.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
				+ Drawing.drawing.interfaceSizeY - 50 * Drawing.drawing.interfaceScaleZoom;

		languages.update();

		if (Drawing.drawing.interfaceScaleZoom == 1)
		{
			takeControl.update();
		}

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
		if (this.logo == null)
		{
			this.logo = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 250 * Drawing.drawing.interfaceScaleZoom, 0);
			takeControl.posX = logo.posX;
			takeControl.posY = logo.posY;
			this.logo.size *= 1.5 * Drawing.drawing.interfaceScaleZoom * this.objHeight / 40;
			this.logo.invulnerable = true;
			this.logo.drawAge = 50;
			this.logo.depthTest = false;
			this.logo.networkID = 0;

			if (Drawing.drawing.interfaceScaleZoom > 1)
			{
				this.logo.posY += 180 * Drawing.drawing.interfaceScaleZoom;
				this.logo.posX -= 260 * Drawing.drawing.interfaceScaleZoom;
			}

			Game.movables.add(logo);
		}

		play.draw();
		exit.draw();
		options.draw();
		languages.draw();

		if (Game.debug)
			debug.draw();

		about.draw();


		Drawing.drawing.setColor(Turret.calculateSecondaryColor(Game.player.colorR), Turret.calculateSecondaryColor(Game.player.colorG), Turret.calculateSecondaryColor(Game.player.colorB));
		Drawing.drawing.setInterfaceFontSize(this.titleSize * 2.5);
		Drawing.drawing.displayInterfaceText(this.lCenterX + 4, 4 + this.lCenterY - this.objYSpace, "Tanks");

		Drawing.drawing.setColor(Turret.calculateSecondaryColor(Game.player.turretColorR), Turret.calculateSecondaryColor(Game.player.turretColorG), Turret.calculateSecondaryColor(Game.player.turretColorB));
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.lCenterX + 2, 2 + this.lCenterY - this.objYSpace * 2 / 9, "The Crusades");

		Drawing.drawing.setColor(Game.player.colorR, Game.player.colorG, Game.player.colorB);
		Drawing.drawing.setInterfaceFontSize(this.titleSize * 2.5);
		Drawing.drawing.displayInterfaceText(this.lCenterX, this.lCenterY - this.objYSpace, "Tanks");

		Drawing.drawing.setColor(Game.player.turretColorR, Game.player.turretColorG, Game.player.turretColorB);
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.lCenterX, this.lCenterY - this.objYSpace * 2 / 9, "The Crusades");

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
		if (!this.controlPlayer && (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT)) && Drawing.drawing.interfaceScaleZoom == 1)
		{
			this.logo.draw();
		}
	}

	@Override
	public void setupLayoutParameters()
	{
		this.lCenterX = Drawing.drawing.interfaceSizeX / 2;
		this.lCenterY = Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.5;

		this.rCenterX = Drawing.drawing.interfaceSizeX / 2;
		this.rCenterY = Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5;

		if (Drawing.drawing.interfaceScaleZoom > 1)
		{
			this.rCenterX = Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2;
			this.rCenterY = Drawing.drawing.interfaceSizeY / 2;

			this.lCenterX = Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2;
			this.lCenterY = Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5;
		}
	}
}
