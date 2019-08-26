package tanks;

import tanks.tank.Tank;

public class Turret extends Movable
{
	public double size = 8;
	public double length = Game.tank_size;
	public double colorR;
	public double colorG;
	public double colorB;

	Tank tank;
	
	public Turret(Tank t) 
	{
		super(t.posX, t.posY);
		this.tank = t;
		this.colorR = (tank.colorR + 64) / 2;
		this.colorG = (tank.colorG + 64) / 2;
	    this.colorB = (tank.colorB + 64) / 2;

	}
	
	@Override
	public void checkCollision() {}

	public void draw(double angle, boolean forInterface, boolean in3d) 
	{
		this.posX = tank.posX;
		this.posY = tank.posY;
		
		//double amount = 1;
		//if (Game.fancyGraphics)
		//	amount = 0.25;
		
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		double s = (size * (Game.tank_size - this.tank.destroyTimer - Math.max(Game.tank_size - tank.drawAge, 0)) / Game.tank_size);
		double l = length * (Game.tank_size - this.tank.destroyTimer) / Game.tank_size - Math.max(Game.tank_size - tank.drawAge, 0) / Game.tank_size * length;
		
		if (forInterface)
		{
			s = Math.min(size, 12);
			l = Math.min(length, Game.tank_size * 1.5);
		}
		
		double y1 = -s / 2;
		double x1 = -s / 2;
		
		double y2 = -x1;
		double x2 = x1;

		double y3 = y1;
		double x3 = l + s / 2;
		
		double y4 = y2;
		double x4 = x3;
		
		double dx1 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
		double dx2 = x2 * Math.cos(angle) - y2 * Math.sin(angle);
		double dx3 = x3 * Math.cos(angle) - y3 * Math.sin(angle);
		double dx4 = x4 * Math.cos(angle) - y4 * Math.sin(angle);

		double dy1 = x1 * Math.sin(angle) + y1 * Math.cos(angle);
		double dy2 = x2 * Math.sin(angle) + y2 * Math.cos(angle);
		double dy3 = x3 * Math.sin(angle) + y3 * Math.cos(angle);
		double dy4 = x4 * Math.sin(angle) + y4 * Math.cos(angle);
		
		if (forInterface)
			Drawing.drawing.fillInterfaceQuad(this.posX + dx1, this.posY + dy1, this.posX + dx2, this.posY + dy2, this.posX + dx4, this.posY + dy4, this.posX + dx3, this.posY + dy3);
		else
		{
			if (Game.enable3d && in3d)
				Drawing.drawing.fillQuadBox(this.posX + dx1, this.posY + dy1, this.posX + dx2, this.posY + dy2, this.posX + dx4, this.posY + dy4, this.posX + dx3, this.posY + dy3, this.tank.size * 0.5 * (s / this.size), s);
			else
				Drawing.drawing.fillQuad(this.posX + dx1, this.posY + dy1, this.posX + dx2, this.posY + dy2, this.posX + dx4, this.posY + dy4, this.posX + dx3, this.posY + dy3);
		}
		//this.setPolarMotion(angle, 1);
		
		/*for (double i = 0; i < length * (Game.tank_size - this.tank.destroyTimer) / Game.tank_size - Math.max(Game.tank_size - tank.drawAge, 0); i += amount)
		{
			//p.setColor(new Color(75, 40, 0));
			
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
			double s = (size * (Game.tank_size - this.tank.destroyTimer - Math.max(Game.tank_size - tank.drawAge, 0)) / Game.tank_size);
			Drawing.drawing.fillOval(this.posX, this.posY, s, s);
			this.posX += this.vX * amount;
			this.posY += this.vY * amount;
		}*/
	}
	
	@Override
	public void update() {}

	@Override
	public void draw() {}

}
