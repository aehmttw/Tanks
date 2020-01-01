package tanks.obstacle;

import tanks.*;
import tanks.bullet.BulletFlame;
import tanks.event.EventObstacleShrubberyBurn;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenLevelBuilder;
import tanks.tank.Tank;

public class ObstacleShrubbery extends Obstacle
{
	public double opacity = 255;
	public double heightMultiplier = Math.random() * 0.2 + 0.6;
	
	public ObstacleShrubbery(String name, double posX, double posY) 
	{
		super(name, posX, posY);
		
		if (Game.enable3d)
			this.drawLevel = 1;
		else
			this.drawLevel = 8;
		
		this.destructible = true;
		this.tankCollision = false;
		this.bulletCollision = false;
		this.checkForObjects = true;
		this.colorR = (Math.random() * 20);
		this.colorG = (Math.random() * 50) + 150;
		this.colorB = (Math.random() * 20);
		
		if (!Game.fancyGraphics)
		{
			this.colorR = 10;
			this.colorG = 175;
			this.colorB = 10;
			this.heightMultiplier = 1;
		}

		this.description = "A destructible bush in which you---can hide by standing still";
	}
	
	@Override
	public void draw()
	{
		this.opacity = Math.min(this.opacity + Panel.frameFrequency, 255);
		
		if (Game.screen instanceof ScreenLevelBuilder || Game.screen instanceof ScreenGame && (!((ScreenGame) Game.screen).playing))
		{
			this.opacity = 127;
		}
		
		if (Game.playerTank == null || Game.playerTank.destroy)
			this.opacity = Math.max(127, this.opacity - Panel.frameFrequency * 2);
		
		if (Game.enable3d)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
			Drawing.drawing.fillBox(this.posX, this.posY, 0, draw_size, draw_size, draw_size * (0.25 + 0.75 * this.heightMultiplier * (1 - (255 - this.opacity) / 128)));
		}
		else
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.opacity);
			Drawing.drawing.fillRect(this.posX, this.posY, draw_size, draw_size);
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
		return (x >= this.posX - Obstacle.obstacle_size / 2 &&
				x <= this.posX + Obstacle.obstacle_size / 2 &&
				y >= this.posY - Obstacle.obstacle_size / 2 &&
				y <= this.posY + Obstacle.obstacle_size / 2);
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
							(this.opacity >= 255 && this.isInside(m.posX + ((Tank) m).size * 0.5 * x, m.posY + ((Tank) m).size * 0.5 * x));
				}
			}
		}

		//m.hiddenTimer = Math.min(100, m.hiddenTimer + (this.opacity - 127) / 255);
		//m.canHide = true;

		if (m instanceof BulletFlame)
		{
			Game.removeObstacles.add(this);

			Effect e;
			if (Game.enable3d)
				e = (Effect.createNewEffect(this.posX, this.posY, draw_size * (0.25 + 0.75 * this.heightMultiplier * (1 - (255 - this.opacity) / 128)), Effect.EffectType.bushBurn));
			else
				e = (Effect.createNewEffect(this.posX, this.posY, this.opacity, Effect.EffectType.bushBurn));

			e.colR = this.colorR;
			e.colG = this.colorG;
			e.colB = this.colorB;

			Game.effects.add(e);

			Game.eventsOut.add(new EventObstacleShrubberyBurn(this.posX, this.posY));
		}

		this.onObjectEntryLocal(m);
	}

	@Override
	public void onObjectEntryLocal(Movable m)
	{
		this.opacity = Math.max(this.opacity - Panel.frameFrequency * Math.pow(Math.abs(m.vX) + Math.abs(m.vY), 2), 127);
	}
}
