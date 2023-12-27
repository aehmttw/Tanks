package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.*;
import tanks.gui.Button;
import tanks.minigames.Minigame;
import tanks.network.event.EventArcadeHit;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.Arrays;
import java.util.Collections;

public class ScreenTitle extends Screen implements ISeparateBackgroundScreen
{
	boolean controlPlayer = false;
	TankPlayer logo;

	public int chain;

	public double lCenterX;
	public double lCenterY;

	public double rCenterX;
	public double rCenterY;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	public int wave = 0;

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
				Drawing.drawing.playSound("rampage.ogg", (float) Math.pow(2, (0) / 12.0));

				chain = 0;
				wave = 0;
				Game.bulletLocked = false;
				ScreenGame.finishTimer = ScreenGame.finishTimerMax;
				logo.depthTest = true;
				controlPlayer = true;

				Game.currentLevel = new ScreenTitleMinigame((ScreenTitle) Game.screen);
				Game.currentLevel.shadow = 0.75;
				Game.currentSizeX = 28;
				Game.currentSizeY = 18;
				Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
				Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

				ScreenGame.finishedQuick = false;
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

		this.horizontalFaces = new Face[2];
		this.horizontalFaces[0] = new Face(null, 0, 0, Game.currentSizeX * Game.tile_size, 0, true, false, true, true);
		this.horizontalFaces[1] = new Face(null, 0, Game.currentSizeY * Game.tile_size, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size, true, true,true, true);

		this.verticalFaces = new Face[2];
		this.verticalFaces[0] = new Face(null, 0, 0,0, Game.currentSizeY * Game.tile_size, false, false,true, true);
		this.verticalFaces[1] = new Face(null, Game.currentSizeX * Game.tile_size, 0, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size, false, true, true, true);
	}
	
	@Override
	public void update()
	{
		languages.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * Drawing.drawing.interfaceScaleZoom;
		languages.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
				+ Drawing.drawing.interfaceSizeY - 50 * Drawing.drawing.interfaceScaleZoom;

		if (!this.controlPlayer)
		{
			play.update();
			exit.update();
			options.update();

			languages.update();

			if (Drawing.drawing.interfaceScaleZoom == 1)
			{
				takeControl.update();
			}

			if (Game.debug)
				debug.update();

			about.update();

			this.music = "menu_1.ogg";
		}

		if (this.controlPlayer)
		{
			this.logo.hidden = false;
			this.logo.invulnerable = false;
		}

		for (int i = 0; i < Game.game.groundHeightGrid.length; i++)
		{
			System.arraycopy(Game.tilesDepth[i], 0, Game.game.groundHeightGrid[i], 0, Game.game.groundHeightGrid[i].length);
		}

		Game.horizontalFaces.clear();
		Game.verticalFaces.clear();

		this.horizontalFaces[0].update(0, 0, Game.currentSizeX * Game.tile_size, 0);
		this.horizontalFaces[1].update(0, Game.currentSizeY * Game.tile_size, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size);
		Game.horizontalFaces.add(this.horizontalFaces[0]);
		Game.horizontalFaces.add(this.horizontalFaces[1]);

		this.verticalFaces[0].update(0, 0, 0, Game.currentSizeY * Game.tile_size);
		this.verticalFaces[1].update(Game.currentSizeX * Game.tile_size, 0, Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size);
		Game.verticalFaces.add(this.verticalFaces[0]);
		Game.verticalFaces.add(this.verticalFaces[1]);

		for (Movable m : Game.movables)
		{
			if (Double.isNaN(m.posX) || Double.isNaN(m.posY))
			{
				throw new RuntimeException("Movable with NaN position: " + m.toString() + " " + m.lastPosX + " " + m.lastPosY);
			}

			if (m instanceof ISolidObject && !(m instanceof Tank && !(((Tank) m).targetable || ((Tank) m).invulnerabilityTimer > 0)))
			{
				Game.horizontalFaces.addAll(Arrays.asList(((ISolidObject) m).getHorizontalFaces()));

				Game.verticalFaces.addAll(Arrays.asList(((ISolidObject) m).getVerticalFaces()));
			}
		}

		for (Obstacle o : Game.obstacles)
		{
			Face[] faces = o.getHorizontalFaces();
			boolean[] valid = o.getValidHorizontalFaces(true);
			for (int i = 0; i < faces.length; i++)
			{
				if (valid[i])
					Game.horizontalFaces.add(faces[i]);
			}

			faces = o.getVerticalFaces();
			valid = o.getValidVerticalFaces(true);
			for (int i = 0; i < faces.length; i++)
			{
				if (valid[i])
					Game.verticalFaces.add(faces[i]);
			}
		}

		try
		{
			Collections.sort(Game.horizontalFaces);
		}
		catch (Exception e)
		{
			System.out.println(Game.horizontalFaces);
			Game.exitToCrash(e);
		}

		try
		{
			Collections.sort(Game.verticalFaces);
		}
		catch (Exception e)
		{
			System.out.println(Game.verticalFaces);
			Game.exitToCrash(e);
		}

		Obstacle.draw_size = Game.tile_size;
		for (int i = 0; i < Game.tracks.size(); i++)
		{
			Game.tracks.get(i).update();
		}

		int enemies = 0;
		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m != this.logo || this.controlPlayer)
			{
				m.preUpdate();
				m.update();
			}

			if (m instanceof Tank && m != this.logo)
			{
				if (this.controlPlayer)
					m.team = Game.enemyTeam;
				else
					m.team = null;
			}

			if ((m instanceof Tank && m.team != logo.team) || (m instanceof Crate && ((Crate) m).tank.team != logo.team))
				enemies++;
		}

		if (enemies <= 1 && !this.controlPlayer)
		{
			for (Movable m: Game.movables)
			{
				if (m instanceof TankAIControlled)
				{
					TankAIControlled t = (TankAIControlled) m;

					if (!t.suicidal)
						t.timeUntilDeath = 500;

					t.enableSuicide = true;
					t.suicidal = true;
				}
			}
		}

		if (enemies <= 0 && controlPlayer)
		{
			wave++;

			if (wave > 1)
				Drawing.drawing.playSound("rampage.ogg", (float) Math.pow(2, (wave - 1) / 12.0));

			music = "menu_" + Math.min(5, wave) + ".ogg";
			Panel.forceRefreshMusic = true;

			for (int i = 0; i < (this.wave - 1) * 3 * (Math.random() * 0.5 + 0.5) + 3; i++)
			{
				Drawing.drawing.playGlobalSound("flame.ogg", 0.75f);
				int x = (int) (Math.random() * Game.tilesDepth.length);
				int y = (int) (Math.random() * Game.tilesDepth[0].length);
				Tank t = Game.registryTank.getRandomTank().getTank((x + 0.5) * Game.tile_size, (y + 0.5) * Game.tile_size, (int) (Math.random() * 4));
				t.team = Game.enemyTeam;
				Game.movables.add(new Crate(t));
			}
		}

		if (wave < 1)
			wave = 1;

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

		if (!Game.movables.contains(this.logo) && Game.screen == this)
		{
			this.logo = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 250 * Drawing.drawing.interfaceScaleZoom, 0);
			this.logo.networkID = 0;
			this.logo.size *= 1.5 * Drawing.drawing.interfaceScaleZoom * this.objHeight / 40;
			this.logo.invulnerable = true;
			this.logo.hidden = true;
			this.logo.team = Game.playerTeam;
			this.logo.maxSpeed *= 1.5;
			this.logo.bullet.speed *= 1.5;
			Game.playerTank = logo;
			Game.movables.add(this.logo);
			this.controlPlayer = false;
		}

		if (!controlPlayer)
		{
			this.logo.posX = Drawing.drawing.sizeX / 2;
			this.logo.posY = Drawing.drawing.sizeY / 2 - 250 * Drawing.drawing.interfaceScaleZoom;
		}
	}

	public void drawWithoutBackground()
	{
		languages.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * Drawing.drawing.interfaceScaleZoom;
		languages.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
				+ Drawing.drawing.interfaceSizeY - 50 * Drawing.drawing.interfaceScaleZoom;

		if (this.logo == null)
		{
			this.logo = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 250 * Drawing.drawing.interfaceScaleZoom, 0);
			takeControl.posX = logo.posX;
			takeControl.posY = logo.posY;
			this.logo.size *= 1.5 * Drawing.drawing.interfaceScaleZoom * this.objHeight / 40;
			this.logo.drawAge = 50;
			this.logo.depthTest = false;
			this.logo.networkID = 0;
			this.logo.invulnerable = true;
			this.logo.hidden = true;
			this.logo.maxSpeed *= 1.5;
			this.logo.bullet.speed *= 1.5;
			Game.playerTank = logo;
			this.logo.team = Game.playerTeam;

			if (Drawing.drawing.interfaceScaleZoom > 1)
			{
				this.logo.posY += 180 * Drawing.drawing.interfaceScaleZoom;
				this.logo.posX -= 260 * Drawing.drawing.interfaceScaleZoom;
			}

			Game.movables.add(logo);
		}

		this.logo.luminance = Math.max(0.5, 1 - this.screenAge / 50.0);

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

//		for (int i = 0; i < Game.tracks.size(); i++)
//		{
//			Game.tracks.get(i).draw();
//		}

		for (int i = Game.movables.size() - 1; i >= 0; i--)
		{
			Game.movables.get(i).draw();

			if (Game.movables.get(i) instanceof IDrawableWithGlow)
				((IDrawableWithGlow) Game.movables.get(i)).drawGlow();
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

	public static class ScreenTitleMinigame extends Minigame
	{
		public ScreenTitle screen;

		public ScreenTitleMinigame(ScreenTitle screen)
		{
			super("{28,18||0-0-player}");
			this.screen = screen;
		}

		@Override
		public void onKill(Tank attacker, Tank target)
		{
			if (target == screen.logo)
			{
				screen.music = "menu_1.ogg";
				Panel.forceRefreshMusic = true;
			}
			else if (attacker == screen.logo && screen.controlPlayer)
			{
				Drawing.drawing.playSound("hit_chain.ogg", (float) Math.pow(2, Math.min(24 - 1, screen.chain) / 12.0), 0.5f);
				screen.chain++;

				Effect e = Effect.createNewEffect(target.posX, target.posY, target.size / 2, Effect.EffectType.chain);
				e.radius = screen.chain;
				Game.effects.add(Game.effects.size(), e);
			}
		}
	}
}
