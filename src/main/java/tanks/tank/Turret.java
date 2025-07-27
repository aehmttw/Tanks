package tanks.tank;

import basewindow.Color;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.bullet.Bullet;
import tanks.effect.AttributeModifier;

public class Turret extends Movable
{
	Tank tank;

	public Turret(Tank t)
	{
		super(t.posX, t.posY);
		this.tank = t;
		setSecondary(this.tank.color, this.tank.secondaryColor);
		setTertiary(this.tank.color, this.tank.secondaryColor, this.tank.tertiaryColor);
	}

	public void draw(double rotation, double vAngle, boolean forInterface, boolean in3d, boolean transparent)
	{
		double luminance = this.tank.em().getAttributeValue(AttributeModifier.glow, this.tank.luminance);

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

		Drawing.drawing.setColor(this.tank.secondaryColor.red, this.tank.secondaryColor.green, this.tank.secondaryColor.blue, transparent ? 127 : 255, luminance);

		Bullet b = null;
		if (this.tank instanceof TankAIControlled)
			b = ((TankAIControlled) this.tank).getBullet();
		else if (this.tank instanceof TankRemote && !(((TankRemote) this.tank).tank instanceof TankPlayer))
			b = ((TankAIControlled) ((TankRemote) this.tank).tank).getBullet();

		if (b != null && b.shotCount > 1 && this.tank.multipleTurrets)
		{
			int q = b.shotCount;

			int n = 0;

			if (b.multishotSpread < 360)
				n = 1;

			for (int i = 0; i < q; i++)
				this.drawBarrel(forInterface, in3d, baseSize, length, thickness, rotation + Math.toRadians(b.multishotSpread) * ((i * 1.0 / (q - n)) - n / 2.0), vAngle);
		}
		else
			this.drawBarrel(forInterface, in3d, baseSize, length, thickness, rotation, vAngle);

		double turretBaseR = (this.tank.secondaryColor.red + this.tank.color.red) / 2;
		double turretBaseG = (this.tank.secondaryColor.green + this.tank.color.green) / 2;
		double turretBaseB = (this.tank.secondaryColor.blue + this.tank.color.blue) / 2;

		if (this.tank.enableTertiaryColor)
		{
			turretBaseR = this.tank.tertiaryColor.red;
			turretBaseG = this.tank.tertiaryColor.green;
			turretBaseB = this.tank.tertiaryColor.blue;
		}

		Drawing.drawing.setColor(turretBaseR, turretBaseG, turretBaseB, transparent ? 127 : 255, luminance);

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

	public static void setSecondary(Color src, Color target)
	{
		target.set(calculateSecondaryColor(src.red), calculateSecondaryColor(src.green), calculateSecondaryColor(src.blue));
	}

	public static void setTertiary(Color c1, Color c2, Color target)
	{
		target.set((c1.red + c2.red) / 2, (c1.green + c2.green) / 2, (c1.blue + c2.blue) / 2);
	}
}
