package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.gui.Button;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

import java.time.LocalDate;

public class ScreenTitle extends Screen implements ISeparateBackgroundScreen
{
	boolean controlPlayer = false;
	TankPlayer logo;

	public double lCenterX;
	public double lCenterY;

	public double rCenterX;
	public double rCenterY;

	public static LocalDate now = LocalDate.now();
	public boolean birthday = now.getMonthValue() == 3 && Game.lessThan(20, now.getDayOfMonth(), 24);

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

	Button languages = new Button(-1000, -1000, this.objHeight * 1.5, this.objHeight * 1.5, "", () ->
	{
		Game.silentCleanUp();
		Game.screen = new ScreenLanguage();
	}
	);

	public ScreenTitle()
	{
		Game.movables.clear();
		this.logo.size *= 1.5 * Drawing.drawing.interfaceScaleZoom * this.objHeight / 40;
		this.logo.turret.length *= 1.5 * Drawing.drawing.interfaceScaleZoom * this.objHeight / 40;
		this.logo.invulnerable = true;
		this.logo.drawAge = 50;
		this.logo.depthTest = false;

		Game.movables.add(logo);
		ScreenGame.finished = false;

		takeControl.silent = true;

		this.music = "menu_1.ogg";
		this.musicID = "menu";

		languages.image = "language.png";

		languages.imageSizeX = this.objHeight;
		languages.imageSizeY = this.objHeight;

		if (birthday)
		{
			this.logo.posX -= 150 * Drawing.drawing.interfaceScaleZoom;
			this.logo.posY -= 50 * Drawing.drawing.interfaceScaleZoom;

			Game.movables.add(new Cake());
		}
	}
	
	@Override
	public void update()
	{
		this.takeControl.posX = this.logo.posX;
		this.takeControl.posY = this.logo.posY;

		play.update();
		exit.update();
		options.update();

		languages.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * Drawing.drawing.interfaceScaleZoom;
		languages.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
				+ Drawing.drawing.interfaceSizeY - 50 * Drawing.drawing.interfaceScaleZoom;

		languages.update();

		if (Drawing.drawing.interfaceScaleZoom == 1)
			takeControl.update();

		if (Game.debug)
			debug.update();

		about.update();

		if (this.controlPlayer)
		{
			Obstacle.draw_size = Game.tile_size;
			for (int i = 0; i < Game.tracks.size(); i++)
				Game.tracks.get(i).update();

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				m.preUpdate();
				m.update();
			}

			for (int i = 0; i < Game.effects.size(); i++)
				Game.effects.get(i).update();

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
		languages.draw();

		if (Game.debug)
			debug.draw();

		about.draw();

		Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255));
		Drawing.drawing.setInterfaceFontSize(this.titleSize * 2.5);
		Drawing.drawing.displayInterfaceText(this.lCenterX + 4, 4 + this.lCenterY - this.objYSpace, "Tanks");

		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.lCenterX + 2, 2 + this.lCenterY - this.objYSpace * 2 / 9, "The Crusades");

		Drawing.drawing.setColor(0, 150, 255);
		Drawing.drawing.setInterfaceFontSize(this.titleSize * 2.5);
		Drawing.drawing.displayInterfaceText(this.lCenterX, this.lCenterY - this.objYSpace, "Tanks");

		Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255));
		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.displayInterfaceText(this.lCenterX, this.lCenterY - this.objYSpace * 2 / 9, "The Crusades");

		for (int i = 0; i < Game.tracks.size(); i++)
			Game.tracks.get(i).draw();

		for (int i = Game.movables.size() - 1; i >= 0; i--)
			Game.movables.get(i).draw();

		for (int i = 0; i < Game.effects.size(); i++)
			Game.effects.get(i).draw();

		for (int i = 0; i < Game.effects.size(); i++)
			Game.effects.get(i).drawGlow();
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

		this.logo = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 250 * Drawing.drawing.interfaceScaleZoom, 0);

		if (Drawing.drawing.interfaceScaleZoom > 1)
		{
			this.rCenterX = Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2;
			this.rCenterY = Drawing.drawing.interfaceSizeY / 2;

			this.lCenterX = Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 2;
			this.lCenterY = Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 1.5;

			this.logo.posY += 180 * Drawing.drawing.interfaceScaleZoom;
			this.logo.posX -= 260 * Drawing.drawing.interfaceScaleZoom;
		}
	}

	public static class Cake extends Tank
	{
		public int age = now.getYear() - 2018;

		public Cake() {
			super(
					"cake",
					Drawing.drawing.sizeX / 2 + Drawing.drawing.interfaceScaleZoom * 100,
					Drawing.drawing.sizeY / 2 - Drawing.drawing.interfaceScaleZoom * 300,
					200, 255, 255, 255, false
			);

			this.invulnerable = true;
		}

		@Override
		public void draw()
		{
			Drawing.drawing.setColor(0, 0, 0, 100);
			Drawing.drawing.fillOval(this.posX + 5, this.posY + 5, 200, 200);

			Drawing.drawing.setColor(255, 255, 255);
			Drawing.drawing.drawImage("lance's cake.png", this.posX, this.posY, 200, 200);

			Drawing.drawing.setColor(255, 128, 0);
			Drawing.drawing.setFontSize(72);
			Drawing.drawing.drawText(this.posX, this.posY, age + "");

			if (Game.lessThan(this.posX - 100, Drawing.drawing.getInterfaceMouseX(), this.posX + 100) && Game.lessThan(this.posY - 100, Drawing.drawing.getInterfaceMouseY(), this.posY + 100))
				Drawing.drawing.drawTooltip(new String[] {"It's Tanks' birthday!"});
		}

		@Override
		public void update()
		{
			super.update();

			if (this.vX != 0)
				this.vX *= 1 - 1.0 / 60;

			if (this.vY != 0)
				this.vY *= 1 - 1.0 / 60;
		}
	}
}
