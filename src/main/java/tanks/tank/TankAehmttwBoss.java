package tanks.tank;

import basewindow.InputCodes;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventCreateTank;
import tanks.event.EventShootBullet;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;

import java.util.ArrayList;

public class TankAehmttwBoss extends TankAIControlled
{
	public ArrayList<Tank> spawned = new ArrayList<>();
	int nextAction = (int) (Math.random() * 1000 + 500);

	int[] possibleControlSwaps = new int[] {InputCodes.KEY_A, InputCodes.KEY_B ,InputCodes.KEY_C, InputCodes.KEY_D, InputCodes.KEY_E, InputCodes.KEY_F, InputCodes.KEY_G, InputCodes.KEY_H, InputCodes.KEY_I, InputCodes.KEY_J, InputCodes.KEY_K, InputCodes.KEY_L, InputCodes.KEY_M, InputCodes.KEY_N, InputCodes.KEY_O, InputCodes.KEY_P, InputCodes.KEY_Q, InputCodes.KEY_R, InputCodes.KEY_S, InputCodes.KEY_T, InputCodes.KEY_U, InputCodes.KEY_V, InputCodes.KEY_W, InputCodes.KEY_X, InputCodes.KEY_Y, InputCodes.KEY_Z, InputCodes.KEY_SPACE, InputCodes.KEY_LEFT_SHIFT, InputCodes.KEY_LEFT_ALT, InputCodes.KEY_LEFT_CONTROL, InputCodes.KEY_TAB, InputCodes.KEY_1, InputCodes.KEY_2, InputCodes.KEY_3, InputCodes.KEY_4, InputCodes.KEY_5, InputCodes.KEY_6, InputCodes.KEY_7, InputCodes.KEY_8, InputCodes.KEY_9, InputCodes.KEY_0, InputCodes.KEY_ENTER, InputCodes.KEY_MINUS, InputCodes.KEY_EQUAL, InputCodes.KEY_COMMA, InputCodes.KEY_PERIOD, InputCodes.KEY_SLASH, InputCodes.KEY_SEMICOLON, InputCodes.KEY_LEFT_BRACKET, InputCodes.KEY_RIGHT_BRACKET, InputCodes.KEY_GRAVE_ACCENT};
	String[] prevEvent = {"this.health = 20;"};

	public TankAehmttwBoss(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size * 3, 255, 255, 50, angle, ShootAI.alternate);

		this.enableMovement = true;
		this.enableMineLaying = false;
		this.liveBulletMax = 5;
		this.cooldownRandom = 100;
		this.cooldownBase = 100;
		this.bulletBounces = 0;
		this.size = 50;
		this.aimAccuracyOffset = 0;
		this.health = 20;
		this.baseHealth = 20;
		this.coinValue = 0;

		this.description = "An \"aehmttw\" Boss Tank that---is a boss tank that---can cheat.";
	}

	@Override
	public void update()
	{
		if (this.team != null && this.team.friendlyFire)
			this.team.friendlyFire = false;

		super.update();

		for (Movable tank : Game.movables) {
			if (tank instanceof TankPlayer) {
				if (!tank.destroy)
					nextAction -= Panel.frameFrequency;

				if (((TankPlayer) tank).destroyTimer <= 0 && ((TankPlayer) tank).health <= 0)
					Game.game.input.load();
			}
		}

		if (nextAction <= 0) {
			int actionNo = (int) (Math.random() * 4);

			if (actionNo == 0)
				controlFlip();

			else if (actionNo == 1)
				for (Movable movable: Game.movables) {
					if (movable instanceof TankPlayer) {
						Game.movables.add(new AreaEffectFreeze(movable.posX, movable.posY));
						this.prevEvent = new String[] {"I'll freeze you for three", "seconds, enjoy!"};
						break;
					}
				}
			else if (actionNo == 2) {
				for (Movable movable : Game.movables)
					if (movable instanceof TankPlayer) {
						Mine m = new Mine(movable.posX, movable.posY, this);
						m.radius /= 2;
						Game.movables.add(m);

						this.prevEvent = new String[] {"Here's a mine!"};
						break;
					}
			}

			nextAction = (int) (Math.random() * 1000 + 500);
		}
		
		ArrayList<Tank> removeSpawned = new ArrayList<>();

		for (Tank tank : this.spawned)
			if (!Game.movables.contains(tank))
				removeSpawned.add(tank);

		for (Tank tank : removeSpawned)
			this.spawned.remove(tank);

		
		if (Math.random() < 0.003 * Panel.frameFrequency && this.spawned.size() < 6) {
			this.spawnTank();
			prevEvent = new String[] {"this.spawnTank();"};
		}
	}

	@Override
	public void draw() {
		super.draw();

		Drawing.drawing.setInterfaceFontSize(24);

		for (int i = 0; i < this.prevEvent.length; i++)
			Drawing.drawing.drawInterfaceText(this.posX, this.posY - 50 - (this.prevEvent.length - i * 30), this.prevEvent[i]);
	}

	public void controlFlip() {
		int actionNum = (int) (Math.random() * 6);

		if (actionNum == 0) {
			Game.game.input.moveUp = new InputBindingGroup("tank.up", new InputBinding(InputBinding.InputType.keyboard, possibleControlSwaps[(int) (Math.random() * (possibleControlSwaps.length - 1))] ));
			this.prevEvent = new String[] {"\"Move Up\" Key changed to " + Game.game.input.moveUp.input1.getInputName()};
		} else if (actionNum == 1) {
			Game.game.input.moveDown = new InputBindingGroup("tank.down", new InputBinding(InputBinding.InputType.keyboard, possibleControlSwaps[(int) (Math.random() * (possibleControlSwaps.length - 1))] ));
			this.prevEvent = new String[]{"\"Move Down\" Key changed to " + Game.game.input.moveDown.input1.getInputName()};
		} else if (actionNum == 2) {
			Game.game.input.moveLeft = new InputBindingGroup("tank.left", new InputBinding(InputBinding.InputType.keyboard, possibleControlSwaps[(int) (Math.random() * (possibleControlSwaps.length - 1))] ));
			this.prevEvent = new String[]{"\"Move Left\" Key changed to " + Game.game.input.moveLeft.input1.getInputName()};
		} else if (actionNum == 3) {
			Game.game.input.moveRight = new InputBindingGroup("tank.right", new InputBinding(InputBinding.InputType.keyboard, possibleControlSwaps[(int) (Math.random() * (possibleControlSwaps.length - 1))] ));
			this.prevEvent = new String[]{"\"Move Right\" Key changed to " + Game.game.input.moveRight.input1.getInputName()};
		} else if (actionNum == 4) {
			Game.game.input.shoot = new InputBindingGroup("tank.shoot", new InputBinding(InputBinding.InputType.keyboard, possibleControlSwaps[(int) (Math.random() * (possibleControlSwaps.length - 1))] ));
			this.prevEvent = new String[]{"\"Shoot\" Key changed to " + Game.game.input.shoot.input1.getInputName()};
		} else if (actionNum == 5) {
			Game.game.input.aim = new InputBindingGroup("tank.aim", new InputBinding(InputBinding.InputType.keyboard, possibleControlSwaps[(int) (Math.random() * (possibleControlSwaps.length - 1))] ));
			this.prevEvent = new String[]{"\"Aim\" Key changed to " + Game.game.input.aim.input1.getInputName()};
		}
	}

	@Override
	public void launchBullet(double offset)
	{
		Drawing.drawing.playGlobalSound("shoot.ogg", (float) (Bullet.bullet_size / this.bulletSize));

		for (int i = -2; i <= 2; i++){
			Bullet b = new Bullet(this.posX, this.posY, this.bulletBounces, this);
			b.setPolarMotion(angle + offset + (Math.PI / 16 * i), this.bulletSpeed);
			b.moveOut(50 / this.bulletSpeed * this.size / Game.tile_size);
			b.effect = this.bulletEffect;
			b.size = this.bulletSize;
			b.damage = this.bulletDamage;
			b.heavy = this.bulletHeavy;

			Game.movables.add(b);
			Game.eventsOut.add(new EventShootBullet(b));
		}

		this.cooldown = Math.random() * this.cooldownRandom + this.cooldownBase;

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;
	}

	@Override
	public void onDestroy() {
		boolean hasMoreBosses = false;
		for (Movable movable: Game.movables) {
			if (movable instanceof TankAehmttwBoss) {
				hasMoreBosses = true;
				break;
			}
		}

		if (!hasMoreBosses) {
			Game.game.input.load();

			for (int i = 0; i < Game.movables.size(); i++)
				if (Game.movables.get(i) instanceof AreaEffectFreeze) {
					Game.movables.remove(i);
					break;
				}
		}
	}

	public void spawnTank()
	{
		double x;
		double y;

		int attempts = 0;
		while (true)
		{
			attempts++;

			double pos = Math.random() * 200 - 100;
			int side = (int) (Math.random() * 4);

			x = pos;
			y = pos;

			if (side == 0)
				x = -100;
			else if (side == 1)
				x = 100;
			else if (side == 2)
				y = -100;
			else if (side == 3)
				y = 100;

			boolean retry = false;
			if (this.posX + x > Game.tile_size / 2 && this.posX + x < (Game.currentSizeX - 0.5) * Game.tile_size &&
					this.posY + y > Game.tile_size / 2 && this.posY + y < (Game.currentSizeY - 0.5) * Game.tile_size)
			{
				for (Obstacle o: Game.obstacles)
				{
					if (o.tankCollision && Math.abs(o.posX - (this.posX + x)) < Game.tile_size && Math.abs(o.posY - (this.posY + y)) < Game.tile_size)
					{
						retry = true;
						break;
					}
				}
			}
			else
				retry = true;

			if (!retry || attempts >= 10)
				break;
		}
		
		RegistryTank.TankEntry e = Game.registryTank.getEntry(this.name);

		while (e.name.equals(this.name) || e.isBoss)
		{
			e = Game.registryTank.getRandomTank();
		}

		Tank t = e.getTank(this.posX + x, this.posY + y, this.angle);
		t.team = this.team;
		t.coinValue = 0;

		Game.eventsOut.add(new EventCreateTank(t));
		this.spawned.add(t);
		
		Game.movables.add(t);
	}
}
