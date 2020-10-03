package tanks.tank;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventLayMine;
import tanks.event.EventShootBullet;
import tanks.gui.Button;
import tanks.gui.Joystick;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemBullet;

public class TankPlayer extends Tank implements IPlayerTank
{
	public static Joystick controlStick;
	public static Joystick shootStick;
	public static Button mineButton;

	public static boolean controlStickSnap;
	public static boolean controlStickMobile;

	public Player player = Game.player;
	public boolean enableDestroyCheat = false;

	public boolean drawTouchCircle = false;
	public double touchCircleSize = 400;
	public long prevTap = 0;

	public static boolean shootStickEnabled = false;

	protected double prevDistSq;

	protected long lastTrace = 0;
	protected static boolean lockTrace = false;

	public static final double base_deceleration = 0.05;

	public TankPlayer(double x, double y, double angle)
	{
		super("player", x, y, Game.tile_size, 0, 150, 255);
		this.liveBulletMax = 5;
		this.liveMinesMax = 2;
		this.angle = angle;
		this.orientation = angle;
		this.player.tank = this;
	}

	@Override
	public void update()
	{
		boolean up = Game.game.input.moveUp.isPressed();
		boolean down = Game.game.input.moveDown.isPressed();
		boolean left = Game.game.input.moveLeft.isPressed();
		boolean right = Game.game.input.moveRight.isPressed();
		boolean trace = Game.game.input.aim.isPressed();

		boolean destroy = Game.game.window.pressedKeys.contains(InputCodes.KEY_BACKSPACE);

		if (Game.game.input.aim.isValid())
		{
			Game.game.input.aim.invalidate();

			long time = System.currentTimeMillis();

			lockTrace = false;
			if (time - lastTrace <= 500)
			{
				lastTrace = 0;
				lockTrace = true;
			}
			else
				lastTrace = time;
		}

		if (destroy && this.enableDestroyCheat)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (!Team.isAllied(this, Game.movables.get(i)))
					Game.movables.get(i).destroy = true;
			}
		}

		if (this.tookRecoil)
		{
			if (this.recoilSpeed <= this.maxSpeed * 1.0001)
			{
				this.tookRecoil = false;
				this.inControlOfMotion = true;
			}
			else
			{
				this.setMotionInDirection(this.vX + this.posX, this.vY + this.posY, this.recoilSpeed);
				this.recoilSpeed *= Math.pow(1 - TankPlayer.base_deceleration * this.frictionModifier, Panel.frameFrequency);
			}
		}
		else if (this.inControlOfMotion)
		{
			double acceleration = this.acceleration * this.accelerationModifier;
			double maxVelocity = this.maxSpeed * this.maxSpeedModifier;

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

			if (a == -1)
			{
				this.vX *= Math.pow(1 - (0.05 * this.frictionModifier), Panel.frameFrequency);
				this.vY *= Math.pow(1 - (0.05 * this.frictionModifier), Panel.frameFrequency);

				if (Math.abs(this.vX) < 0.001)
					this.vX = 0;

				if (Math.abs(this.vY) < 0.001)
					this.vY = 0;
			}

			double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

			if (speed > maxVelocity)
				this.setPolarMotion(this.getPolarDirection(), maxVelocity);
		}

		if (this.cooldown > 0)
			this.cooldown -= Panel.frameFrequency;

		boolean shoot = false;
		if (!Game.game.window.touchscreen && Game.game.input.shoot.isPressed())
			shoot = true;

		boolean mine = false;
		if (!Game.game.window.touchscreen && Game.game.input.mine.isPressed())
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

						if (!p.tag.equals("") && !p.tag.equals("aim") && !p.tag.equals("shoot"))
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
						{
							shoot = true;
							p.tag = "shoot";
						}

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


		if ((trace || lockTrace) && !Game.bulletLocked && !this.disabled && Game.screen instanceof ScreenGame)
		{
			Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);

			if (this.player.hotbar.enabledItemBar && this.player.hotbar.itemBar.slots[this.player.hotbar.itemBar.selected] instanceof ItemBullet)
			{
				r.bounces = ((ItemBullet)this.player.hotbar.itemBar.slots[this.player.hotbar.itemBar.selected]).bounces;
			}

			r.vX /= 2;
			r.vY /= 2;
			r.trace = true;
			r.dotted = true;
			r.moveOut(10 * this.size / Game.tile_size);
			r.getTarget();
		}

		super.update();
	}

	public void shoot()
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Game.player.hotbar.enabledItemBar)
		{
			if (Game.player.hotbar.itemBar.useItem(false))
				return;
		}

		if (this.liveBullets >= this.liveBulletMax)
			return;

		this.cooldown = 20;

		fireBullet(25 / 8.0, 1, Bullet.BulletEffect.trail);
	}

	public void fireBullet(double speed, int bounces, Bullet.BulletEffect effect)
	{
		Drawing.drawing.playGlobalSound("shoot.ogg");

		Bullet b = new Bullet(posX, posY, bounces, this);
		b.setPolarMotion(this.angle, speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0);

		b.moveOut(50 / speed * this.size / Game.tile_size);
		b.effect = effect;

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);

		this.processRecoil();
	}

	public void fireBullet(Bullet b, double speed)
	{
		if (b.itemSound != null)
			Drawing.drawing.playGlobalSound(b.itemSound, (float) (Bullet.bullet_size / b.size));

		b.setPolarMotion(this.angle, speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil);

		b.moveOut(50 / speed * this.size / Game.tile_size);

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);

		this.processRecoil();
	}

	public void layMine()
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Game.player.hotbar.enabledItemBar)
		{
			if (Game.player.hotbar.itemBar.useItem(true))
				return;
		}

		if (this.liveMines >= this.liveMinesMax )
			return;

		Drawing.drawing.playGlobalSound("lay_mine.ogg");

		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);

		Game.eventsOut.add(new EventLayMine(m));

		Game.movables.add(m);
	}

	public void layMine(Mine m)
	{
		if (Game.bulletLocked || this.destroy)
			return;

		Drawing.drawing.playGlobalSound("lay_mine.ogg", (float) (Mine.mine_size / m.size));

		this.cooldown = m.cooldown;

		Game.eventsOut.add(new EventLayMine(m));
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

		if (controlStick != null && shootStick != null)
		{
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
}
