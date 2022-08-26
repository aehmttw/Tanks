package tanks.tank;

import tanks.AttributeModifier;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;

public class Turret extends Movable
{
	Tank tank;

	public Turret(Tank t)
	{
		super(t.posX, t.posY);
		this.tank = t;
		this.tank.secondaryColorR = calculateSecondaryColor(this.tank.colorR);
		this.tank.secondaryColorG = calculateSecondaryColor(this.tank.colorG);
		this.tank.secondaryColorB = calculateSecondaryColor(this.tank.colorB);
		this.tank.tertiaryColorR = (this.tank.colorR + this.tank.secondaryColorR) / 2;
		this.tank.tertiaryColorG = (this.tank.colorG + this.tank.secondaryColorG) / 2;
		this.tank.tertiaryColorB = (this.tank.colorB + this.tank.secondaryColorB) / 2;
	}

	public void draw(double rotation, double vAngle, boolean forInterface, boolean in3d, boolean transparent)
	{
		double luminance = this.tank.luminance;

		for (int i = 0; i < this.tank.attributes.size(); i++)
		{
			AttributeModifier a = this.tank.attributes.get(i);

			if (a.type.equals("glow"))
			{
				luminance = a.getValue(luminance);
			}
		}

		if (this.tank.fullBrightness)
			luminance = 1;

		this.posX = tank.posX;
		this.posY = tank.posY;
		this.posZ = tank.posZ;

		double frac = (Game.tile_size - this.tank.destroyTimer - Math.max(Game.tile_size - tank.drawAge, 0)) / Game.tile_size;
		double size = this.tank.size;
		double rawLength = this.tank.turretLength;

		if (forInterface && !in3d)
		{
			frac = 1;
			size = Math.min(this.tank.size, Game.tile_size * 1.5);
		}

		double baseSize = size * frac;
		double length = size / Game.tile_size * frac * rawLength;
		double thickness = this.tank.turretSize * size * frac / 8;

		if (transparent)
			Drawing.drawing.setColor(this.tank.secondaryColorR, this.tank.secondaryColorG, this.tank.secondaryColorB, 127, luminance);
		else
			Drawing.drawing.setColor(this.tank.secondaryColorR, this.tank.secondaryColorG, this.tank.secondaryColorB, 255, luminance);


		if (this.tank.bullet.shotCount > 1 && this.tank.multipleTurrets)
		{
			int q = this.tank.bullet.shotCount;

			int n = 0;

			if (this.tank.bullet.shotSpread < 360)
				n = 1;

			for (int i = 0; i < q; i++)
				this.drawBarrel(forInterface, in3d, baseSize, length, thickness, rotation + Math.toRadians(this.tank.bullet.shotSpread) * ((i * 1.0 / (q - n)) - n / 2.0), vAngle);
		}
		else
			this.drawBarrel(forInterface, in3d, baseSize, length, thickness, rotation, vAngle);

		double turretBaseR = (this.tank.secondaryColorR + this.tank.colorR) / 2;
		double turretBaseG = (this.tank.secondaryColorG + this.tank.colorG) / 2;
		double turretBaseB = (this.tank.secondaryColorB + this.tank.colorB) / 2;

		if (this.tank.enableTertiaryColor)
		{
			turretBaseR = this.tank.tertiaryColorR;
			turretBaseG = this.tank.tertiaryColorG;
			turretBaseB = this.tank.tertiaryColorB;
		}

		if (transparent)
			Drawing.drawing.setColor(turretBaseR, turretBaseG, turretBaseB, 127, luminance);
		else
			Drawing.drawing.setColor(turretBaseR, turretBaseG, turretBaseB, 255, luminance);

		if (forInterface)
		{
			if (!in3d)
				Drawing.drawing.drawInterfaceModel(this.tank.turretBaseModel, this.posX, this.posY, baseSize, baseSize, rotation);
			else
				Drawing.drawing.drawInterfaceModel(this.tank.turretBaseModel, this.posX, this.posY, this.posZ + baseSize / 2, baseSize, baseSize, baseSize, rotation);
		}
		else
		{
			if (!in3d)
				Drawing.drawing.drawModel(this.tank.turretBaseModel, this.posX, this.posY, baseSize, baseSize, rotation);
			else
				Drawing.drawing.drawModel(this.tank.turretBaseModel, this.posX, this.posY, this.posZ + baseSize / 2, baseSize, baseSize, baseSize, rotation);
		}
	}

	public void drawBarrel(boolean forInterface, boolean in3d, double baseSize, double length, double thickness, double rotation, double vAngle)
	{
		if (forInterface)
		{
			if (!in3d)
				Drawing.drawing.drawInterfaceModel(this.tank.turretModel, this.posX, this.posY, length, thickness, rotation);
			else
				Drawing.drawing.drawInterfaceModel(this.tank.turretModel, this.posX, this.posY, this.posZ + (baseSize * 1.3) / 2, length, thickness, thickness, rotation, vAngle, 0);
		}
		else
		{
			if (!in3d)
				Drawing.drawing.drawModel(this.tank.turretModel, this.posX, this.posY, length, thickness, rotation);
			else
				Drawing.drawing.drawModel(this.tank.turretModel, this.posX, this.posY, this.posZ + (baseSize * 1.3) / 2, length, thickness, thickness, rotation, vAngle, 0);
		}
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
