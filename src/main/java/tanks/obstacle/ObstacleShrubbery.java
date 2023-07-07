package tanks.obstacle;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.legacy.BulletAir;
import tanks.bullet.BulletInstant;
import tanks.network.event.EventObstacleShrubberyBurn;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.IOverlayScreen;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public class ObstacleShrubbery extends Obstacle
{
	public double height = 255;
	public double heightMultiplier = Math.random() * 0.2 + 0.6;

	protected double finalHeight;
	protected double previousFinalHeight;

	public ObstacleShrubbery(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		if (Game.enable3d)
			this.drawLevel = 1;
		else
			this.drawLevel = 9;
		
		this.destructible = true;
		this.tankCollision = false;
		this.bulletCollision = false;
		this.checkForObjects = true;
		this.colorR = (Math.random() * 20);
		this.colorG = (Math.random() * 50) + 150;
		this.colorB = (Math.random() * 20);
		this.enableStacking = false;

		if (!Game.fancyTerrain)
		{
			this.colorR = 10;
			this.colorG = 175;
			this.colorB = 10;
			this.heightMultiplier = 0.8;
		}

		this.update = true;

		this.description = "A destructible bush in which you can hide by standing still";
	}

	@Override
	public void update()
	{
		this.previousFinalHeight = this.finalHeight;

		this.height = Math.min(this.height + Panel.frameFrequency, 255);

		if (ScreenGame.finishedQuick && !Game.game.window.shapeRenderer.supportsBatching)
		{
			this.height = Math.max(127, this.height - Panel.frameFrequency * 2);
		}

		this.finalHeight = this.baseGroundHeight + draw_size * (0.2 + this.heightMultiplier * (1 - (255 - this.height) / 128));
	}

	@Override
	public void draw()
	{
		this.finalHeight = this.baseGroundHeight + draw_size * (0.2 + this.heightMultiplier * (1 - (255 - this.height) / 128));

		if (!Game.game.window.shapeRenderer.supportsBatching)
		{
			if (Game.screen instanceof ILevelPreviewScreen || Game.screen instanceof IOverlayScreen || Game.screen instanceof ScreenGame && (!((ScreenGame) Game.screen).playing))
			{
				this.height = 127;
			}
		}

		if (Game.enable3d)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
			Drawing.drawing.setShrubberyMode();
			Drawing.drawing.fillBox(this, this.posX, this.posY, 0, draw_size, draw_size, this.finalHeight, (byte) (this.getOptionsByte(this.getTileHeight()) + 1));
		}
		else
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.height);
			Drawing.drawing.fillRect(this, this.posX, this.posY, draw_size, draw_size);
		}
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 127);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size, draw_size);
	}

	public boolean isInside(double x, double y)
	{
		return (x >= this.posX - Game.tile_size / 2 &&
				x <= this.posX + Game.tile_size / 2 &&
				y >= this.posY - Game.tile_size / 2 &&
				y <= this.posY + Game.tile_size / 2);
	}
	
	@Override
	public void onObjectEntry(Movable m)
	{
		if (m instanceof Tank)
		{
			for (int x = -1; x <= 1; x++)
			{
				for (int y = -1; y <= 1; y++)
				{
					((Tank) m).canHidePoints[x + 1][y + 1] = ((Tank) m).canHidePoints[x + 1][y + 1] ||
							this.isInside(m.posX + ((Tank) m).size * 0.5 * x, m.posY + ((Tank) m).size * 0.5 * x);

					((Tank) m).hiddenPoints[x + 1][y + 1] = ((Tank) m).hiddenPoints[x + 1][y + 1] ||
							(this.height >= 255 && this.isInside(m.posX + ((Tank) m).size * 0.5 * x, m.posY + ((Tank) m).size * 0.5 * x));
				}
			}
		}

		//m.hiddenTimer = Math.min(100, m.hiddenTimer + (this.opacity - 127) / 255);
		//m.canHide = true;

		if (m instanceof Bullet && ((Bullet) m).burnsBushes)
		{
			Game.removeObstacles.add(this);

			Effect e;
			if (Game.enable3d)
				e = Effect.createNewEffect(this.posX, this.posY,this.baseGroundHeight + draw_size * (0.2 + this.heightMultiplier * (1 - (255 - this.height) / 128)), Effect.EffectType.bushBurn);
			else
				e = Effect.createNewEffect(this.posX, this.posY, this.height, Effect.EffectType.bushBurn);

			e.colR = this.colorR;
			e.colG = this.colorG;
			e.colB = this.colorB;

			Game.effects.add(e);

			Game.eventsOut.add(new EventObstacleShrubberyBurn(this.posX, this.posY));
		}

		this.onObjectEntryLocal(m);

		this.finalHeight = this.baseGroundHeight + draw_size * (0.2 + this.heightMultiplier * (1 - (255 - this.height) / 128));
	}

	@Override
	public void onObjectEntryLocal(Movable m)
	{
		if (m instanceof Bullet && !((Bullet) m).lowersBushes)
		{
			if (Math.random() < Panel.frameFrequency / Math.pow(((Bullet) m).size, 2) * 20 * Game.effectMultiplier)
			{
				Effect e = Effect.createNewEffect(this.posX + (Math.random() - 0.5) * Obstacle.draw_size, this.posY + (Math.random() - 0.5) * Obstacle.draw_size, this.getTileHeight() * (Math.random() * 0.8 + 0.2), Effect.EffectType.piece);
				e.vX = m.vX * (Math.random() * 0.5 + 0.5);
				e.vY = m.vY * (Math.random() * 0.5 + 0.5);
				e.vZ = Math.random() * m.getSpeed() / 8;
				e.colR = this.colorR;
				e.colG = this.colorG;
				e.colB = this.colorB;
				e.enableGlow = false;
				Game.effects.add(e);
			}
		}
		else
		{
			double speed = Math.sqrt((Math.pow(m.vX, 2) + Math.pow(m.vY, 2)));
			this.height = Math.max(this.height - Panel.frameFrequency * speed * speed * 2, 127);

			if (Game.playerTank == null || Game.playerTank.destroy)
				return;

			double distsq = Math.pow(m.posX - Game.playerTank.posX, 2) + Math.pow(m.posY - Game.playerTank.posY, 2);

			double radius = 62500;
			if (distsq <= radius && Math.random() < Panel.frameFrequency * 0.1 && speed > 0 && Game.playerTank != null && !Game.playerTank.destroy && !(m instanceof BulletInstant))
			{
				int sound = (int) (Math.random() * 4 + 1);
				Drawing.drawing.playSound("leaves" + sound + ".ogg", (float) (speed / 3.0f) + 0.5f, (float) (speed * 0.05 * (radius - distsq) / radius));
			}
		}
	}

	public double getTileHeight()
	{
		if (Obstacle.draw_size < Game.tile_size)
			return 0;

		double shrubScale = 0.25;
		if (Game.screen instanceof ScreenGame)
			shrubScale = ((ScreenGame) Game.screen).shrubberyScale;

		return this.finalHeight * shrubScale;
	}

	public byte getOptionsByte(double h)
	{
		return 0;
	}

	public boolean positionChanged()
	{
		return this.previousFinalHeight != this.finalHeight;
	}
}
