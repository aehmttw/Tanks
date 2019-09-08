package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.BulletFlame;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenLevelBuilder;

public class ObstacleShrubbery extends Obstacle
{
	public double opacity = 255;
	public double heightMultiplier = Math.random() * 0.2 + 1;
	
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
	}
	
	@Override
	public void draw()
	{			
		double om = 1;
		
		if (Game.enable3d)
			om = 0.5;
		
		this.opacity = Math.min(this.opacity + Panel.frameFrequency * om, 255);
		
		if (Game.screen instanceof ScreenLevelBuilder || Game.screen instanceof ScreenGame && (!((ScreenGame) Game.screen).playing))
		{
			this.opacity = 127;
		}
		
		if (Game.player.destroy)
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
	
	@Override
	public void onObjectEntry(Movable m)
	{
		this.opacity = Math.max(this.opacity - Panel.frameFrequency * Math.pow(Math.abs(m.vX) + Math.abs(m.vY), 2), 127);
		m.hiddenTimer = Math.min(100, m.hiddenTimer + (this.opacity - 127) / 255);
		m.canHide = true;
		
		if (m instanceof BulletFlame)
			Game.removeObstacles.add(this);
	}

}
