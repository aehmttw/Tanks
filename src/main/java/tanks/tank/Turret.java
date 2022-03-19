package tanks.tank;

import basewindow.Model;
import basewindow.ModelPart;
import tanks.*;

public class Turret extends Movable
{
	public double size = 8;
	public double length = Game.tile_size;
	public double colorR;
	public double colorG;
	public double colorB;

	Tank tank;

	public static Model base_model;
	public static Model turret_model;

	public Turret(Tank t) 
	{
		super(t.posX, t.posY);
		this.tank = t;
		this.colorR = calculateSecondaryColor(this.tank.colorR);
		this.colorG = calculateSecondaryColor(this.tank.colorG);
	    this.colorB = calculateSecondaryColor(this.tank.colorB);
	}

	public void draw(double rotation, double vAngle, boolean forInterface, boolean in3d, boolean transparent)
	{
		double glow = 0.5;

		for (int i = 0; i < this.tank.attributes.size(); i++)
		{
			AttributeModifier a = this.tank.attributes.get(i);

			if (a.type.equals("glow"))
			{
				glow = a.getValue(glow);
			}
		}

		this.posX = tank.posX;
		this.posY = tank.posY;
		this.posZ = tank.posZ;

		double s = (this.tank.size * (Game.tile_size - this.tank.destroyTimer) / Game.tile_size) * Math.min(this.tank.drawAge / Game.tile_size, 1);

		double l = length * (Game.tile_size - this.tank.destroyTimer) / Game.tile_size - Math.max(Game.tile_size - tank.drawAge, 0) / Game.tile_size * length;

		if (forInterface)
			l = Math.min(length, Game.tile_size * 1.5);

		if (transparent)
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 127, glow);
		else
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, glow);

		if (forInterface)
			Drawing.drawing.drawInterfaceModel(this.tank.turretModel, this.posX, this.posY, l, l * size / 8, rotation);
		else if (!in3d)
			Drawing.drawing.drawModel(this.tank.turretModel, this.posX, this.posY, l, l * size / 8, rotation);
		else
			Drawing.drawing.drawModel(this.tank.turretModel, this.posX, this.posY, this.posZ + (s * 1.3) / 2, l, l * size / 8, l * size / 8, rotation, vAngle, 0);

		if (transparent)
			Drawing.drawing.setColor((this.colorR + this.tank.colorR) / 2, (this.colorG + this.tank.colorG) / 2, (this.colorB + this.tank.colorB) / 2, 127, glow);
		else
			Drawing.drawing.setColor((this.colorR + this.tank.colorR) / 2, (this.colorG + this.tank.colorG) / 2, (this.colorB + this.tank.colorB) / 2, 255, glow);

		if (forInterface)
			Drawing.drawing.drawInterfaceModel(this.tank.turretBaseModel, this.posX, this.posY, l, l, rotation);
		else if (!in3d)
			Drawing.drawing.drawModel(this.tank.turretBaseModel, this.posX, this.posY, l, l, rotation);
		else
			Drawing.drawing.drawModel(this.tank.turretBaseModel, this.posX, this.posY, this.posZ + s / 2, l, l, l, rotation);
	}
	
	@Override
	public void update() {}

	@Override
	public void draw() {}

	public static double calculateSecondaryColor(double input)
	{
		return (input + 64) / 2;
	}

}
