package tanks.tank;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.Button;
import tanks.gui.IFixedMenu;
import tanks.gui.Joystick;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenTitle;
import tanks.hotbar.Hotbar;
import tanks.item.*;
import tanks.network.event.EventLayMine;
import tanks.network.event.EventShootBullet;

/**
 * A tank that is controlled by the player. TankPlayerController is used instead if we are connected to a party as a client.
 */
public class TankPlayer extends Tank implements ILocalPlayerTank, IServerPlayerTank, IDrawableLightSource
{
	public static Bullet default_bullet;
	public static Mine default_mine;

	public static String default_bullet_name;
	public static String default_mine_name;

	public static Joystick controlStick;
	public static Joystick shootStick;
	public static Button mineButton;

	public static boolean controlStickSnap = false;
	public static boolean controlStickMobile = true;

	public Player player = Game.player;
	public static boolean enableDestroyCheat = false;

	public boolean drawTouchCircle = false;
	public double touchCircleSize = 400;
	public long prevTap = 0;

	public static boolean shootStickEnabled = false;
	public static boolean shootStickHidden = false;

	protected double prevDistSq;

	protected long lastTrace = 0;
	protected static boolean lockTrace = false;

	protected double drawRange = -1;

	public double mouseX;
	public double mouseY;

	public TankPlayer(double x, double y, double angle)
	{
		super("player", x, y, Game.tile_size, 0, 150, 255);
		this.angle = angle;
		this.orientation = angle;
		this.player.tank = this;
		this.bulletItem.item.name = default_bullet_name;
		this.mineItem.item.name = default_mine_name;

		this.colorR = Game.player.colorR;
		this.colorG = Game.player.colorG;
		this.colorB = Game.player.colorB;
		this.secondaryColorR = Game.player.colorR2;
		this.secondaryColorG = Game.player.colorG2;
		this.secondaryColorB = Game.player.colorB2;
		this.enableTertiaryColor = Game.player.enableTertiaryColor;
		this.tertiaryColorR = Game.player.colorR3;
		this.tertiaryColorG = Game.player.colorG3;
		this.tertiaryColorB = Game.player.colorB3;

		if (enableDestroyCheat)
		{
			this.showName = true;
			this.nameTag.name = "Destroy cheat enabled!!!";
		}

		if (Game.nameInMultiplayer && ScreenPartyHost.isServer)
		{
			this.nameTag.name = Game.player.username;
			this.showName = true;
		}

		if (Game.invulnerable)
		{
			this.resistExplosions = true;
			this.resistBullets = true;
		}
	}

	public void setDefaultColor()
	{
		this.colorR = 0;
		this.colorG = 150;
		this.colorB = 255;
		this.secondaryColorR = Turret.calculateSecondaryColor(this.colorR);
		this.secondaryColorG = Turret.calculateSecondaryColor(this.colorG);
		this.secondaryColorB = Turret.calculateSecondaryColor(this.colorB);
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
			if (this.recoilSpeed <= this.maxSpeed * this.maxSpeedModifier * 1.0001)
			{
				this.tookRecoil = false;
				this.inControlOfMotion = true;
			}
			else
			{
				this.setMotionInDirection(this.vX + this.posX, this.vY + this.posY, this.recoilSpeed);
				this.recoilSpeed *= Math.pow(1 - this.friction * this.frictionModifier, Panel.frameFrequency);
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
			{
				if (Game.followingCam)
					a += this.angle + Math.PI / 2;

				this.addPolarMotion(a, acceleration * this.maxSpeed * Panel.frameFrequency);
			}

			if (a == -1)
			{
				this.vX *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);
				this.vY *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);

				if (Math.abs(this.vX) < 0.001)
					this.vX = 0;

				if (Math.abs(this.vY) < 0.001)
					this.vY = 0;
			}

			double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

			if (speed > maxVelocity)
				this.setPolarMotion(this.getPolarDirection(), maxVelocity);
		}

		double reload = this.getAttributeValue(AttributeModifier.reload, 1);

		this.bulletItem.updateCooldown(reload);
		this.mineItem.updateCooldown(reload);

		Hotbar h = Game.player.hotbar;
		if (h.enabledItemBar)
		{
			for (Item.ItemStack<?> i: h.itemBar.slots)
			{
				if (i != null && !i.isEmpty)
				{
					i.updateCooldown(reload);
				}
			}
		}

		boolean shoot = !Game.game.window.touchscreen && Game.game.input.shoot.isPressed();

		boolean mine = !Game.game.window.touchscreen && Game.game.input.mine.isPressed();

		boolean showRange = false;
		if (h.enabledItemBar && h.itemBar.selected >= 0)
		{
			Item.ItemStack<?> i = h.itemBar.slots[h.itemBar.selected];

			if (i.item instanceof ItemBullet)
				showRange = ((ItemBullet) i.item).bullet.range > 0;
			else if (i.item instanceof ItemRemote)
				showRange = ((ItemRemote) i.item).range > 0;
		}

		TankPlayer.shootStickHidden = showRange;

		boolean prevTouchCircle = this.drawTouchCircle;
		this.drawTouchCircle = false;
		if (Game.game.window.touchscreen)
		{
			if (shootStickEnabled)
			{
				if (!Game.bulletLocked && !this.disabled && !this.destroy)
					mineButton.update();

				if (!showRange)
					shootStick.update();
			}

			if (!Game.bulletLocked && !this.disabled && !this.destroy)
			{
				double distSq = 0;

				if (shootStickEnabled)
				{
					if (mineButton.justPressed)
						mine = true;

					if (shootStick.inputIntensity >= 0.2 && !showRange)
					{
						this.angle = shootStick.inputAngle;
						trace = true;

						if (shootStick.inputIntensity >= 1.0)
							shoot = true;
					}
				}

				if (!shootStickEnabled || shootStickHidden)
				{
					for (int i : Game.game.window.touchPoints.keySet())
					{
						InputPoint p = Game.game.window.touchPoints.get(i);

						if (!p.tag.equals("") && !p.tag.equals("aim") && !p.tag.equals("shoot"))
							continue;

						if (Game.screen instanceof ScreenGame)
						{
							Game.game.window.transformations.add(((ScreenGame) Game.screen).slantTranslation);
							Game.game.window.transformations.add(((ScreenGame) Game.screen).slantRotation);
						}
						float[] tp = Game.game.window.getTransformedMouse(p.x, p.y);
						if (Game.screen instanceof ScreenGame)
						{
							Game.game.window.transformations.remove(((ScreenGame) Game.screen).slantTranslation);
							Game.game.window.transformations.remove(((ScreenGame) Game.screen).slantRotation);
						}
						double px = Drawing.drawing.getInterfacePointerX(tp[0]);
						double py = Drawing.drawing.getInterfacePointerY(tp[1]);

						if (!Game.followingCam)
						{
							this.mouseX = Drawing.drawing.toGameCoordsX(px);
							this.mouseY = Drawing.drawing.toGameCoordsY(py);
							this.angle = this.getAngleInDirection(this.mouseX, this.mouseY);
						}

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
		else if (!Game.followingCam)
		{
			this.mouseX = Drawing.drawing.getMouseX();
			this.mouseY = Drawing.drawing.getMouseY();
			this.angle = this.getAngleInDirection(this.mouseX, this.mouseY);
		}

		if (shoot && this.getItem(false).cooldown <= 0 && !this.disabled)
			this.shoot();

		if (mine && this.getItem(true).cooldown <= 0 && !this.disabled)
			this.layMine();

		if ((trace || lockTrace) && !Game.bulletLocked && !this.disabled && (Game.screen instanceof ScreenGame || Game.screen instanceof ScreenTitle))
		{
			double range = -1;

			Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);

			if (h.enabledItemBar && h.itemBar.selected >= 0)
			{
				Item.ItemStack<?> i = h.itemBar.slots[h.itemBar.selected];
				if (i instanceof ItemBullet.ItemStackBullet)
				{
					Bullet b = ((ItemBullet.ItemStackBullet) i).item.bullet;
					r.bounces = b.bounces;
					range = b.range;

					if (range > 0)
						range *= this.getAttributeValue(AttributeModifier.bullet_speed, 1);
				}
				else if (i instanceof ItemRemote.ItemStackRemote)
				{
					ItemRemote ir = (ItemRemote) i.item;
					if (ir.bounces >= 0)
						r.bounces = ir.bounces;

					range = ir.range * this.getAttributeValue(AttributeModifier.bullet_speed, 1);
				}
			}

			r.vX /= 2;
			r.vY /= 2;
			r.trace = true;
			r.dotted = true;
			r.moveOut(10 * this.size / Game.tile_size);

			if (range >= 0)
				this.drawRange = range;
			else
				r.getTarget();
		}

		super.update();
	}

	public Item.ItemStack<?> getItem(boolean rightClick)
	{
		Item.ItemStack<?> i;

		if (rightClick)
			i = this.mineItem;
		else
			i = this.bulletItem;

		if (Game.player.hotbar.enabledItemBar)
		{
			Item.ItemStack<?> i2 = Game.player.hotbar.itemBar.getSelectedItem(rightClick);
			if (i2 != null)
				i = i2;
		}

		return i;
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

		this.bulletItem.attemptUse(this);
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

		this.mineItem.attemptUse(this);
	}

	public void fireBullet(Bullet b, double speed, double offset)
	{
		if (speed <= 0)
			speed = Double.MIN_NORMAL;

		if (b.shotSound != null)
		{
			Drawing.drawing.playGlobalSound(b.shotSound, (float) ((Bullet.bullet_size / b.size) * (1 - (Math.random() * 0.5) * b.pitchVariation)));
		}

		b.setPolarMotion(this.angle + offset, speed);
		b.speed = speed;
		b.range *= this.getAttributeValue(AttributeModifier.bullet_speed, 1);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * this.getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);

		this.recoilSpeed = this.getSpeed();
		if (this.recoilSpeed > this.maxSpeed * 1.01)
		{
			this.tookRecoil = true;
			this.inControlOfMotion = false;
		}

		if (b.moveOut)
			b.moveOut(50 * this.size / Game.tile_size);

		b.setTargetLocation(this.mouseX, this.mouseY);

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
	}

	public void layMine(Mine m)
	{
		if (Game.bulletLocked || this.destroy)
			return;

		Drawing.drawing.playGlobalSound("lay_mine.ogg", (float) (Mine.mine_size / m.size));

		Game.eventsOut.add(new EventLayMine(m));
		Game.movables.add(m);
	}


	@Override
	public void onDestroy()
	{
		if (Crusade.crusadeMode)
			this.player.remainingLives--;

		for (IFixedMenu m : ModAPI.menuGroup)
		{
			if (m instanceof Scoreboard && ((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.deaths))
			{
				if (((Scoreboard) m).players.isEmpty())
					((Scoreboard) m).addTeamScore(this.team, 1);
				else
					((Scoreboard) m).addPlayerScore(this.player, 1);
			}
		}
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

	@Override
	public double getDrawRange()
	{
		return this.drawRange;
	}

	@Override
	public void setDrawRange(double range)
	{
		this.drawRange = range;
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

	@Override
	public Player getPlayer()
	{
		return this.player;
	}

	@Override
	public void setBufferCooldown(double value)
	{
		super.setBufferCooldown(value);

		Hotbar h = Game.player.hotbar;
		if (h.enabledItemBar)
		{
			for (Item.ItemStack<?> i: h.itemBar.slots)
			{
				if (i != null && !i.isEmpty)
				{
					i.cooldown = Math.max(i.cooldown, value);
				}
			}
		}
	}

	@Override
	public boolean lit()
	{
		return false;
	}

	double[] lightInfo = new double[]{0, 0, 0, 2, 255, 255, 255};

	@Override
	public double[] getLightInfo()
	{
		this.glowSize = 4;
		return this.lightInfo;
	}
}