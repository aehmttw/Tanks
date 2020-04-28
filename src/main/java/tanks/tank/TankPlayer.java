package tanks.tank;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventShootBullet;
import tanks.gui.Button;
import tanks.gui.Joystick;

public class TankPlayer extends Tank implements IPlayerTank
{
	public static Joystick controlStick = new Joystick(150, Drawing.drawing.interfaceSizeY - 150, 200);
	public static Joystick shootStick = new Joystick(Drawing.drawing.interfaceSizeX - 150, Drawing.drawing.interfaceSizeY - 150, 200);
	public static Button mineButton = new Button(Drawing.drawing.interfaceSizeX - 300, Drawing.drawing.interfaceSizeY - 75, 60, 60, "", new Runnable()
	{
		@Override
		public void run()
		{
			Drawing.drawing.playVibration("heavyClick");
		}
	});

	public Player player = Game.player;
	public boolean enableDestroyCheat = false;

	public boolean drawTouchCircle = false;
	public double touchCircleSize = 400;
	public long prevTap = 0;

	public static boolean shootStickEnabled = false;

	protected double prevDistSq;

	public TankPlayer(double x, double y, double angle)
	{
		super("player", x, y, Game.tank_size, 0, 150, 255);
		this.liveBulletMax = 5;
		this.liveMinesMax = 2;
		this.angle = angle;
		this.player.tank = this;
	}

	@Override
	public void update()
	{
		boolean up = Game.game.window.pressedKeys.contains(InputCodes.KEY_UP) || Game.game.window.pressedKeys.contains(InputCodes.KEY_W);
		boolean down = Game.game.window.pressedKeys.contains(InputCodes.KEY_DOWN) || Game.game.window.pressedKeys.contains(InputCodes.KEY_S);
		boolean left = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_A);
		boolean right = Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_D);
		boolean trace = Game.game.window.pressedKeys.contains(InputCodes.KEY_PERIOD) || Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_4);

		boolean destroy = Game.game.window.pressedKeys.contains(InputCodes.KEY_BACKSPACE);

		if (destroy && this.enableDestroyCheat)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (!Team.isAllied(this, Game.movables.get(i)))
					Game.movables.get(i).destroy = true;
			}
		}

		double acceleration = accel;
		double maxVelocity = maxV;

		double x = 0;
		double y = 0;

		double a = -1;

		if (left)
			x -= 1;

		if (right)
			x += 1;

		if (up)
			y -= 1;

		if (down)
			y += 1;

		if (x == 1 && y == 0)
			a = 0;
		else if (x == 1 && y == 1)
			a = Math.PI / 4;
		else if (x == 0 && y == 1)
			a = Math.PI / 2;
		else if (x == -1 && y == 1)
			a = 3 * Math.PI / 4;
		else if (x == -1 && y == 0)
			a = Math.PI;
		else if (x == -1 && y == -1)
			a = 5 * Math.PI / 4;
		else if (x == 0 && y == -1)
			a = 3 * Math.PI / 2;
		else if (x == 1 && y == -1)
			a = 7 * Math.PI / 4;

		double intensity = 1;

		if (a < 0 && Game.game.window.touchscreen)
		{
			intensity = controlStick.inputIntensity;

			if (intensity >= 0.2)
				a = controlStick.inputAngle;
		}

		if (a >= 0 && intensity >= 0.2)
			this.addPolarMotion(a, acceleration * Panel.frameFrequency);
		else
		{
			this.vX *= Math.pow(0.95, Panel.frameFrequency);
			this.vY *= Math.pow(0.95, Panel.frameFrequency);

			if (Math.abs(this.vX) < 0.001)
				this.vX = 0;

			if (Math.abs(this.vY) < 0.001)
				this.vY = 0;
		}

		double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

		if (speed > maxVelocity)
			this.setPolarMotion(this.getPolarDirection(), maxVelocity);

		if (this.cooldown > 0)
			this.cooldown -= Panel.frameFrequency;

		boolean shoot = false;
		if (!Game.game.window.touchscreen && (Game.game.window.pressedKeys.contains(InputCodes.KEY_SPACE) || Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1)))
			shoot = true;

		boolean mine = false;
		if (!Game.game.window.touchscreen && (Game.game.window.pressedKeys.contains(InputCodes.KEY_ENTER) || Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_2)))
			mine = true;

		boolean prevTouchCircle = this.drawTouchCircle;
		this.drawTouchCircle = false;
		if (Game.game.window.touchscreen)
		{
			if (!Game.bulletLocked && !this.disabled && !this.destroy)
			{
				double distSq = 0;

				if (shootStickEnabled)
				{
					mineButton.update();

					if (mineButton.justPressed)
						mine = true;

					shootStick.update();

					if (shootStick.inputIntensity >= 0.2)
					{
						this.angle = shootStick.inputAngle;
						trace = true;

						if (shootStick.inputIntensity >= 1.0)
							shoot = true;
					}
				}
				else
				{
					for (int i : Game.game.window.touchPoints.keySet())
					{
						InputPoint p = Game.game.window.touchPoints.get(i);

						if (!p.tag.equals("") && !p.tag.equals("aim"))
							continue;

						double px = Drawing.drawing.getInterfacePointerX(p.x);
						double py = Drawing.drawing.getInterfacePointerY(p.y);

						this.angle = this.getAngleInDirection(Drawing.drawing.toGameCoordsX(px),
								Drawing.drawing.toGameCoordsY(py));

						distSq = Math.pow(px - Drawing.drawing.toInterfaceCoordsX(this.posX), 2)
								+ Math.pow(py - Drawing.drawing.toInterfaceCoordsY(this.posY), 2);

						if (distSq <= Math.pow(this.touchCircleSize / 4, 2) || p.tag.equals("aim"))
						{
							p.tag = "aim";
							this.drawTouchCircle = true;

							if (!prevTouchCircle)
							{
								if (System.currentTimeMillis() - prevTap <= 500)
								{
									Drawing.drawing.playVibration("heavyClick");
									mine = true;
									this.prevTap = 0;
								}
								else
									prevTap = System.currentTimeMillis();
							}

							trace = true;
						}
						else
							shoot = true;

						double proximity = Math.pow(this.touchCircleSize / 2, 2);

						if (p.tag.equals("aim") && ((distSq <= proximity && prevDistSq > proximity) || (distSq > proximity && prevDistSq <= proximity)))
							Drawing.drawing.playVibration("selectionChanged");

						if (distSq > proximity)
							shoot = true;
					}
				}

				this.prevDistSq = distSq;
			}
		}
		else
			this.angle = this.getAngleInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY());

		if (shoot && this.cooldown <= 0 && !this.disabled)
			this.shoot();

		if (mine && this.cooldown <= 0 && !this.disabled)
			this.layMine();


		if (trace && !Game.bulletLocked && !this.disabled)
		{
			Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);
			r.vX /= 2;
			r.vY /= 2;
			r.trace = true;
			r.dotted = true;
			r.highAccuracy = true;
			r.moveOut(10 * this.size / Game.tank_size);
			r.getTarget();
		}

		super.update();
	}

	public void shoot()
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Panel.panel.hotbar.enabledItemBar)
		{
			if (Panel.panel.hotbar.currentItemBar.useItem(false))
				return;
		}

		if (this.liveBullets >= this.liveBulletMax)
			return;

		this.cooldown = 20;

		fireBullet(25 / 4.0, 1, Bullet.BulletEffect.trail);
	}

	public void fireBullet(double speed, int bounces, Bullet.BulletEffect effect)
	{
		Drawing.drawing.playGlobalSound("shoot.ogg");

		Bullet b = new Bullet(posX, posY, bounces, this);
		b.setPolarMotion(this.angle, speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

		b.moveOut((int) (25.0 / speed * 2 * this.size / Game.tank_size));
		b.effect = effect;

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
	}

	public void fireBullet(Bullet b, double speed)
	{
		if (b.itemSound != null)
			Drawing.drawing.playGlobalSound(b.itemSound, (float) (Bullet.bullet_size / b.size));

		b.setPolarMotion(this.angle, speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0 * b.recoil);

		b.moveOut((int) (25.0 / speed * 2 * this.size / Game.tank_size));

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
	}

	public void layMine()
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Panel.panel.hotbar.enabledItemBar)
		{
			if (Panel.panel.hotbar.currentItemBar.useItem(true))
				return;
		}

		if (this.liveMines >= this.liveMinesMax )
			return;

		Drawing.drawing.playGlobalSound("lay_mine.ogg");

		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);

		Game.movables.add(m);
	}

	@Override
	public void onDestroy()
	{
		if (Crusade.crusadeMode)
			this.player.remainingLives--;
	}

	@Override
	public double getTouchCircleSize()
	{
		return this.touchCircleSize;
	}

	@Override
	public boolean showTouchCircle()
	{
		return this.drawTouchCircle;
	}

	public static void setShootStick(boolean enabled)
	{
		shootStickEnabled = enabled;

		if (enabled)
		{
			controlStick.domain = 1;
			shootStick.domain = 2;
		}
		else
		{
			controlStick.domain = 0;
			shootStick.domain = 0;
		}
	}
}
