package tanks.tank;

import basewindow.Color;
import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletAirStrike;
import tanks.bullet.BulletArc;
import tanks.bullet.BulletGas;
import tanks.attribute.AttributeModifier;
import tanks.gui.Button;
import tanks.gui.IFixedMenu;
import tanks.gui.Joystick;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.gui.screen.ScreenTitle;
import tanks.gui.screen.leveleditor.selector.SelectorTeam;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.network.ConnectedPlayer;
import tanks.network.event.EventLayMine;
import tanks.network.event.EventShootBullet;
import tanks.network.event.EventUpdateEliminatedPlayers;
import tanks.tankson.Property;
import tanks.tankson.Serializer;
import tanks.tankson.TanksONable;

import static tanks.tank.TankPropertyCategory.general;

/**
 * A tank that is controlled by the player. TankPlayerController is used instead if we are connected to a party as a client.
 */
@TanksONable("player_tank")
public class TankPlayer extends TankPlayable implements ILocalPlayerTank, IServerPlayerTank
{
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

	protected double drawRangeMin = -1;
	protected double drawRangeMax = -1;
    protected double drawSpread = -1;
	protected double drawLifespan = -1;
	protected boolean drawTrace = true;

	public double bufferCooldown = 0;
	public Item.ItemStack<?> lastItem = null;

	public double mouseX;
	public double mouseY;

	public static Color default_primary_color = new Color(0, 150, 255, 255);
	public static Color default_secondary_color = new Color((int) Turret.calculateSecondaryColor(0), (int) Turret.calculateSecondaryColor(150), (int) Turret.calculateSecondaryColor(255), 255);
	public static Color default_tertiary_color = new Color((default_primary_color.red + default_secondary_color.red) / 2, (default_primary_color.green + default_secondary_color.green) / 2, (default_primary_color.blue + default_secondary_color.blue) / 2, 255);

	public static final int max_abilities = 5;

	public TankPlayer()
	{
		super(0, 0);

		this.overrideMetadataPropertyIDs.put(SelectorTeam.selector_name, SelectorTeam.player_selector_name);
		this.primaryMetadataID = SelectorTeam.player_selector_name;

		this.addDefaultAbilities();
	}

	public TankPlayer(double x, double y, double angle)
	{
		super(x, y);

		this.overrideMetadataPropertyIDs.put(SelectorTeam.selector_name, SelectorTeam.player_selector_name);
		this.primaryMetadataID = SelectorTeam.player_selector_name;

		this.angle = angle;
		this.orientation = angle;

		if (!ScreenPartyLobby.isClient)
			this.player.tank = this;

		this.addDefaultAbilities();

		this.setPlayerColor();

		if (enableDestroyCheat)
		{
			this.hasName = true;
			this.nameTag.name = "Destroy cheat enabled!!!";
		}

		if (Game.nameInMultiplayer && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
		{
			this.nameTag.name = Game.player.username;
			this.hasName = true;
		}
	}

	public static TankPlayer fromString(String s)
	{
		return (TankPlayer) Serializer.fromTanksON(s);
	}

	public TankPlayer setPlayerColor()
	{
		this.color.set(Game.player.color);
		this.secondaryColor.set(Game.player.color2);
		this.enableTertiaryColor = Game.player.enableTertiaryColor;
		this.tertiaryColor.set(Game.player.color3);
		this.emblemColor.set(Game.player.color2);
		this.saveColors();
		return this;
	}

	public TankPlayer setDefaultColor()
	{
		this.setDefaultPlayerColor();
		return this;
	}

	@Override
	public void update()
	{
        if (Game.invulnerable)
		{
			this.resistExplosions = true;
			this.resistBullets = true;
		}

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

		if (destroy && enableDestroyCheat)
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
                this.recoilSpeed *= Math.pow(1 - Math.min(1, this.friction * this.frictionModifier), Panel.frameFrequency);
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
				this.vX *= Math.pow(1 - Math.min(1, this.friction * this.frictionModifier), Panel.frameFrequency);
				this.vY *= Math.pow(1 - Math.min(1, this.friction * this.frictionModifier), Panel.frameFrequency);

				if (Math.abs(this.vX) < 0.001)
					this.vX = 0;

				if (Math.abs(this.vY) < 0.001)
					this.vY = 0;
			}

			double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

            if (speed > maxVelocity)
				this.setPolarMotion(this.getPolarDirection(), maxVelocity);

        }

		double reload = em().getAttributeValue(AttributeModifier.reload, 1);

		for (Item.ItemStack<?> s: this.abilities)
		{
			s.player = this.player;
			s.updateCooldown(reload);
		}

		this.bufferCooldown -= Panel.frameFrequency;

		Hotbar h = Game.player.hotbar;
		if (h.enabledItemBar)
		{
			for (Item.ItemStack<?> i: h.itemBar.slots)
			{
				if (i != null && !i.isEmpty)
					i.updateCooldown(reload);
			}
		}

		boolean shoot = !Game.game.window.touchscreen && Game.game.input.shoot.isPressed();
		boolean mine = !Game.game.window.touchscreen && Game.game.input.mine.isPressed();

		boolean hideShootStick = false;
		int hotbarSlots = (this.player.hotbar.itemBar.showItems ? ItemBar.item_bar_size : 0);

		if (h.enabledItemBar && h.itemBar.selected >= 0 && h.itemBar.selected < hotbarSlots)
		{
			Item.ItemStack<?> i = h.itemBar.slots[h.itemBar.selected];

			if (i.item instanceof ItemBullet)
				hideShootStick = ((ItemBullet) i.item).bullet instanceof BulletArc || ((ItemBullet) i.item).bullet instanceof BulletAirStrike;
		}

		TankPlayer.shootStickHidden = hideShootStick;

		boolean prevTouchCircle = this.drawTouchCircle;
		this.drawTouchCircle = false;
		if (Game.game.window.touchscreen)
		{
			if (shootStickEnabled)
			{
				if (!Game.bulletLocked && !this.disabled && !this.destroy)
					mineButton.update();

				if (!hideShootStick)
					shootStick.update();
			}

			if (!Game.bulletLocked && !this.disabled && !this.destroy)
			{
				double distSq = 0;

				if (shootStickEnabled)
				{
					if (mineButton.justPressed)
						mine = true;

					if (shootStick.inputIntensity >= 0.2 && !hideShootStick)
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

						if (!p.tag.isEmpty() && !p.tag.equals("aim") && !p.tag.equals("shoot"))
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

						distSq = Math.pow(px - Drawing.drawing.gameToInterfaceCoordsX(this.posX), 2)
								+ Math.pow(py - Drawing.drawing.gameToInterfaceCoordsY(this.posY), 2);

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

		if (shoot && !this.disabled)
			this.action(false);

		if (mine && !this.disabled)
			this.action(true);

		if (!this.disabled)
		{
			for (int i = 0; i < this.abilities.size(); i++)
			{
				if (Game.game.input.abilityBindings[i].isPressed())
					this.quickAction(i);
			}
		}

		if ((trace || lockTrace) && !Game.bulletLocked && !this.disabled && (Game.screen instanceof ScreenGame || Game.screen instanceof ScreenTitle))
		{
			boolean showTrace = true;

			ItemBullet.ItemStackBullet i = null;

			if (this.getPrimaryAbility() instanceof ItemBullet.ItemStackBullet)
				i = (ItemBullet.ItemStackBullet) this.getPrimaryAbility();

			if (h.enabledItemBar && h.itemBar.selected >= 0 && h.itemBar.selected < hotbarSlots)
			{
				Item.ItemStack<?> is = h.itemBar.slots[h.itemBar.selected];
				if (is instanceof ItemBullet.ItemStackBullet)
				{
					i = (ItemBullet.ItemStackBullet) is;
				}
			}

			if (i != null)
			{
				Bullet b = i.item.bullet;
				double range = b.lifespan > 0 ?
                    b.lifespan * b.speed + this.turretLength * em().getAttributeValue(AttributeModifier.bullet_speed, 1)
                    : 0;
				double rangeMax = b.getRangeMax();
				double rangeMin = b.getRangeMin();
				showTrace = b.showDefaultTrace;

				for (int j = 0; j < b.shotCount; j++)
				{
					double baseOff = 0;
					int gasSpread = (b instanceof BulletGas || b.accuracySpread > 0) ? 1 : 0;

					for (int k = -gasSpread; k <= gasSpread; k++)
					{
						double spreadOff = 0;
						if (b instanceof BulletGas)
							spreadOff = Math.atan(((BulletGas) b).endSize / range) / 2 * k;

						spreadOff += Math.toRadians(b.accuracySpread / 2) * k;

						if (b.shotCount > 1)
						{
							if (b.multishotSpread >= 360)
								baseOff = Math.PI * 2 * j / b.shotCount;
							else
								baseOff = Math.toRadians(b.multishotSpread) * ((j * 1.0 / (b.shotCount - 1)) - 0.5);
						}

						Ray r = Ray.newRay(this.posX, this.posY, this.angle + baseOff + spreadOff, b.bounces, this)
                            .setSize(k != 0 ? b.size / 2 : b.size);
                        if (range > 0)
                            r.setRange(range - this.turretLength);

						double mx = this.mouseX - this.posX;
						double my = this.mouseY - this.posY;

						double offset = baseOff + spreadOff;
						double tx = Math.cos(offset) * mx + Math.sin(offset) * my;
						double ty = -Math.sin(offset) * mx + Math.cos(offset) * my;
						b.setTargetLocation(this.posX + tx, this.posY + ty);

						if (b instanceof BulletArc)
							((BulletArc) b).drawTrace(this.posX, this.posY, this.posX + tx, this.posY + ty);

						if (b instanceof BulletAirStrike)
							((BulletAirStrike) b).drawTrace(this.posX, this.posY, this.posX + tx, this.posY + ty);

						r.vX /= 2;
						r.vY /= 2;
                        r.setTrace(true, true).moveOut(10 * this.size / Game.tile_size);

						if (rangeMax > 0)
							this.drawRangeMax = rangeMax;

						if (rangeMin > 0)
							this.drawRangeMin = rangeMin;

						if (range > 0)
							this.drawLifespan = range;

                        this.drawSpread = spreadOff;

						if (showTrace)
							r.getTarget();
					}
				}
			}
        }

		Item.ItemStack<?> ib = this.player.hotbar.itemBar.getSelectedAction(false);
		Bullet b = null;
		if (ib instanceof ItemBullet.ItemStackBullet)
			b = ((ItemBullet.ItemStackBullet) ib).item.bullet;

		if (!(b instanceof BulletArc || b instanceof BulletAirStrike))
			this.pitch -= GameObject.angleBetween(this.pitch, 0) / 10 * Panel.frameFrequency;

		if (b instanceof BulletArc)
		{
			double pitch = Math.atan(GameObject.distanceBetween(this.posX, this.posY, this.mouseX, this.mouseY) / b.speed * 0.5 * BulletArc.gravity / b.speed);
			this.pitch -= GameObject.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
		}
		else if (b instanceof BulletAirStrike)
		{
			double pitch = Math.PI / 2;
			this.pitch -= GameObject.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
		}

        super.update();
    }

	public Item.ItemStack<?> getItem(int click)
	{
		Item.ItemStack<?> i;

		i = this.getAbility(click);

		if (Game.player.hotbar.enabledItemBar && click < 2)
		{
			Item.ItemStack<?> i2 = Game.player.hotbar.itemBar.getSelectedItem(click == 1);
			if (i2 != null)
				i = i2;
		}

		return i;
	}

	public void action(boolean right)
	{
		if (Game.bulletLocked || this.destroy)
			return;

		if (Game.player.hotbar.itemBar.getSelectedAction(right) != this.lastItem && this.bufferCooldown > 0)
			return;

		if (Game.player.hotbar.enabledItemBar)
		{
			if (Game.player.hotbar.itemBar.useItem(right))
				return;
		}

		int a = right ? selectedSecondaryAbility : selectedPrimaryAbility;
		Item.ItemStack<?> s = right ? this.getSecondaryAbility() : this.getPrimaryAbility();
		if (s != null)
		{
			s.networkIndex = -a - 1;
			s.attemptUse(this);
		}
	}

	public void quickAction(int click)
	{
		if (Game.bulletLocked || this.destroy)
			return;

		Item.ItemStack<?> s = this.getAbility(click);

		if (s != this.lastItem && this.bufferCooldown > 0)
			return;

		if (s != null)
		{
			s.networkIndex = -click - 1;
			s.attemptUse(this);
		}
	}

	public void fireBullet(Bullet b, double speed, double offset)
	{
		if (speed == 0)
			speed = Double.MIN_VALUE;

		b.setPolarMotion(this.angle + offset, speed);
		b.speed = Math.abs(speed);

		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * em().getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);

		if (b.recoil != 0)
		{
			this.recoilSpeed = this.getSpeed();
			if (this.recoilSpeed > this.maxSpeed * 1.01)
			{
				this.tookRecoil = true;
				this.inControlOfMotion = false;
			}
		}

		if (b.moveOut)
			b.moveOut(Math.signum(speed) * this.turretLength * this.size / Game.tile_size);

		double mx = this.mouseX - this.posX;
		double my = this.mouseY - this.posY;

		double tx = Math.cos(offset) * mx + Math.sin(offset) * my;
		double ty = -Math.sin(offset) * mx + Math.cos(offset) * my;
		b.setTargetLocation(this.posX + tx, this.posY + ty);

		if (b.item.networkIndex >= 0)
		{
			Integer num = 0;
			if (Game.currentLevel != null)
				num = Game.currentLevel.itemNumbers.get(b.item.item.name);
			b.item.networkIndex = num == null ? 0 : num;
		}

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
	}

	public void layMine(Mine m)
	{
		if (Game.bulletLocked || this.destroy)
			return;

		Drawing.drawing.playGlobalSound("lay_mine.ogg", (float) (Mine.mine_size / m.size));

		if (m.item.networkIndex > 0)
		{
			Integer num = 0;
			if (Game.currentLevel != null)
				num = Game.currentLevel.itemNumbers.get(m.item.item.name);
			m.item.networkIndex = num == null ? 0 : num;
		}

		Game.eventsOut.add(new EventLayMine(m));
		Game.avoidObjects.add(m);
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

		if (Game.screen instanceof ScreenGame && !ScreenPartyLobby.isClient)
		{
			((ScreenGame) Game.screen).eliminatedPlayers.add(new ConnectedPlayer(this.player));
			Game.eventsOut.add(new EventUpdateEliminatedPlayers(((ScreenGame) Game.screen).eliminatedPlayers));
			((ScreenGame) Game.screen).onPlayerDeath(this.player);
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
	public double getDrawRangeMin() { return this.drawRangeMin; }

	@Override
	public double getDrawRangeMax() { return this.drawRangeMax; }

	@Override
	public double getDrawLifespan() { return this.drawLifespan; }

    @Override
    public double getDrawSpread() { return this.drawSpread; }

    @Override
	public boolean getShowTrace() { return this.drawTrace; }

	@Override
	public void setDrawRanges(double lifespan, double rangeMin, double rangeMax, boolean trace)
	{
		this.drawLifespan = lifespan;
		this.drawRangeMin = rangeMin;
		this.drawRangeMax = rangeMax;
		this.drawTrace = trace;
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
	public void setBufferCooldown(Item.ItemStack<?> stack, double value)
	{
		this.lastItem = stack;
		this.bufferCooldown = value;
	}

	@TanksONable("shop_build")
	public static class ShopTankBuild extends TankPlayer
	{
		@TankBuildProperty @Property(id = "price", name = "Price", category = general)
		public int price;

		public ShopTankBuild()
		{

		}

		public ShopTankBuild(TankPlayable p)
		{
			p.copyPropertiesTo(this);
		}

		public static ShopTankBuild fromString(String s)
		{
			Object o = Serializer.fromTanksON(s);
			if (o instanceof ShopTankBuild)
				return (ShopTankBuild) o;
			else
				return new ShopTankBuild((TankPlayable) o);
		}
	}

	@TanksONable("crusade_shop_build")
	public static class CrusadeShopTankBuild extends ShopTankBuild
	{
		@TankBuildProperty @Property(id = "unlock_level", name = "Unlocks after level", category = general, miscType = Property.MiscType.defaultBuildForbidden)
		public int levelUnlock;

		public CrusadeShopTankBuild()
		{

		}
		public CrusadeShopTankBuild(TankPlayable p)
		{
			p.copyPropertiesTo(this);
		}

		public static CrusadeShopTankBuild fromString(String s)
		{
			return (CrusadeShopTankBuild) Serializer.fromTanksON(s);
		}
	}
}
