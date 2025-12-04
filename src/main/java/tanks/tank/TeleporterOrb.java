package tanks.tank;

import tanks.*;
import tanks.bullet.Trail;
import tanks.bullet.Trail3D;
import tanks.attribute.AttributeModifier;
import tanks.gui.screen.ScreenGame;
import tanks.network.event.EventTankTeleport;

import java.util.ArrayList;

/**
 * The orb that transfers the player's tank which is fired from the teleporter.
 * @see TankPlayer
 * @see tanks.obstacle.ObstacleTeleporter
 */
public class TeleporterOrb extends Movable
{
	public Tank tank;
	public double fX;
	public double fY;
	public double iX;
	public double iY;
	public double dX;
	public double dY;
	
	public double age = -100;
	public double maxAge = 200;
	public double endAge = 300;

	public double prevX;
	public double prevY;
	public double prevZ;

	public double lastTrailAngle = -1;
	public double lastTrailPitch = -1;
	public boolean addedTrail = false;

	Trail[] trailSet = new Trail[]{
			new Trail(this, 12.5, 0, 0, 0,0, 0.5, 8, 0, 127, 127, 127, 100, 255, 255, 255, 0, false, 1, true, true),
			new Trail(this, 12.5, 0, 0, 0,0, 1, 10, 0, 127, 127, 127, 100, 0, 0, 0, 0, true, 1, true, true)
	};

	public ArrayList<Trail>[] trails = null;
	public double size;

	public TeleporterOrb(double x, double y, double iX, double iY, double destX, double destY, Tank t)
	{
		super(x, y);
		this.tank = t;
		this.size = t.size;
		this.drawLevel = 9;
		t.invulnerable = true;
		t.currentlyTargetable = false;
        t.teleporting = true;
		this.fX = x;
		this.fY = y;
		this.iX = iX;
		this.iY = iY;
		this.dX = destX;
		this.dY = destY;

		this.tank.tookRecoil = false;
		this.tank.inControlOfMotion = false;
		this.tank.positionLock = true;
		
		if (!t.isRemote)
			Game.eventsOut.add(new EventTankTeleport(this));
	}

    @Override
	public void draw() 
	{
		double frac = 1 - Math.min(this.posZ / 200, 1);
        double endFrac = (ScreenGame.finishTimer / ScreenGame.finishTimerMax);

		if (trails != null)
		{
			for (ArrayList<Trail> tr : trails)
			{
				for (Trail t: tr)
				{
					t.draw();
				}
			}
		}

		//Drawing.drawing.setColor(255, 255, 255);
		Drawing.drawing.setColor(this.tank.color.red * (1 - frac) + 255 * frac, this.tank.color.green * (1 - frac) + 255 * frac, this.tank.color.blue * (1 - frac) + 255 * frac);

		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, (this.size - this.tank.size) / 2 * endFrac, (this.size - this.tank.size) / 2 * endFrac, true, true);

		for (int i = 0; i < (this.size - this.tank.size) * endFrac; i++)
		{
			Drawing.drawing.setColor(this.tank.color.red * (1 - frac) + 255 * frac, this.tank.color.green * (1 - frac) + 255 * frac, this.tank.color.blue * (1 - frac) + 255 * frac, 20);
			//Drawing.drawing.setColor(255, 255, 255, 20);

			if (Game.enable3d)
				Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, i, i, false, true);
			else
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
		}

		//if (this.tank.size == 0)
		//	Game.effects.add(Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.glow));
	}
	
	@Override
	public void update()
	{
		float freq = (float) (Panel.frameFrequency / 10);

		if (Game.game.window.touchscreen)
			freq = 1;

		this.age += Panel.frameFrequency * em().getAttributeValue(AttributeModifier.velocity, 1) * (ScreenGame.finishTimer / ScreenGame.finishTimerMax);

		this.tank.vX = 0;
		this.tank.vY = 0;

		if (this.age > this.endAge)
		{
			Game.removeMovables.add(this);
			this.tank.invulnerable = false;
			this.tank.currentlyTargetable = this.tank.targetable;
            this.tank.teleporting = false;
			this.tank.inControlOfMotion = true;
			this.tank.positionLock = false;

			for (int i = 0; i < 100 * Game.effectMultiplier; i++)
                this.createEffect();

			this.tank.size = this.size;
			return;
		}

		double frac = (Math.sin((this.maxAge - this.age) / this.maxAge * Math.PI - Math.PI / 2) + 1) / 2;
		double frac2 = (Math.sin((-this.age + 50) / 50 * Math.PI - Math.PI / 2) + 1) / 2;

		this.posZ = Math.sin((this.maxAge - Math.max(0, Math.min(this.maxAge, this.age))) / this.maxAge * Math.PI) *
				Math.sqrt(Math.pow(this.dX - this.iX, 2) + Math.pow(this.dY - this.iY, 2)) / 2;

        float fracmod = (float) (ScreenGame.finishTimer / ScreenGame.finishTimerMax);
		if (this.age <= 0)
		{
			if (this.tank == Game.playerTank)
				Drawing.drawing.playSound("teleport1.ogg", 1* fracmod, 0.25f * freq * fracmod);

			frac = 1;
		}
		else if (this.age >= this.maxAge)
		{
			if (this.tank == Game.playerTank)
				Drawing.drawing.playSound("teleport1.ogg", 1* fracmod, 0.25f * freq * fracmod);

			frac = 0;
		}

		if (this.tank == Game.playerTank)
			Drawing.drawing.playSound("teleport2.ogg", (float) (Math.sin((Math.min(Math.max(this.age, 0), this.maxAge) / this.maxAge) * Math.PI) / 4 + 0.5)* fracmod, freq * (1 - (float) (tank.size / size)) / 4f* fracmod);


		if (this.age <= -50)
		{
			this.posX = this.fX * frac2 + this.iX * (1 - frac2);
			this.posY = this.fY * frac2 + this.iY * (1 - frac2);
		}
		else
		{
			this.posX = this.iX * frac + this.dX * (1 - frac);
			this.posY = this.iY * frac + this.dY * (1 - frac);
		}

		this.tank.posX = this.posX;
		this.tank.posY = this.posY;
		this.tank.posZ = this.posZ;
		
		double size = Math.max(-this.age / 100, Math.max((this.age - this.maxAge) / (this.endAge - this.maxAge), 0));
		
		this.tank.size = size * this.size;

		this.tank.disabled = this.tank.size <= 0;

		if (Math.random() < Panel.frameFrequency * Game.effectMultiplier)
			this.createEffect();

		if (this.age > 0)
			this.updateTrails();

		super.update();
	}

	public void updateTrails()
	{
		if (trails == null)
		{
			this.trails = (ArrayList<Trail>[])(new ArrayList[2]);

			for (int i = 0; i < this.trails.length; i++)
				this.trails[i] = new ArrayList<>();
		}

		for (ArrayList<Trail> trails: this.trails)
		{
			double trailLength = 0;
			for (int i = 0; i < trails.size(); i++)
			{
				Trail t = trails.get(i);

				if (this.age > this.maxAge)
					t.spawning = false;

				if (t.expired)
				{
					trails.remove(i);
					i--;
				}
				else
				{
					trailLength += t.update(trailLength, this.age > maxAge || ScreenGame.finishedQuick);
				}
			}
		}

		this.vX = this.posX - prevX;
		this.vY = this.posY - prevY;
		this.vZ = this.posZ - prevZ;

		this.prevX = this.posX;
		this.prevY = this.posY;
		this.prevZ = this.posZ;

		if (!this.addedTrail && !this.destroy &&
				(GameObject.absoluteAngleBetween(this.getPolarDirection(), this.lastTrailAngle) >= 0.001 || (Game.enable3d && GameObject.absoluteAngleBetween(this.getPolarPitch(), this.lastTrailPitch) >= 0.1)))
		{
			this.addTrail();
		}

        this.vX = 0;
        this.vY = 0;
        this.vZ = 0;

		this.addedTrail = false;
	}

	public void addTrail()
	{
		this.addedTrail = true;

		double speed = 6.25;

		double x = this.posX;
		double y = this.posY;
		double z = this.posZ;

		this.lastTrailAngle = this.getPolarDirection();

		if (Game.enable3d)
			this.lastTrailPitch = this.getPolarPitch();

		this.trailSet[0].frontColor.set(tank.color);
		this.trailSet[1].frontColor.set(tank.secondaryColor);

		int i = 0;
		for (Trail t : this.trailSet)
		{
			if (!Game.enable3d)
				this.addTrailObj(new Trail(this, speed, x, y, this.size * speed / 3.125 * t.delay, this.size / 2 * t.backWidth, this.size / 2 * t.frontWidth, this.size * speed / 3.125 * t.maxLength, this.lastTrailAngle,
						t.frontColor.red, t.frontColor.green, t.frontColor.blue, t.frontColor.alpha, t.backColor.red, t.backColor.green, t.backColor.blue, t.backColor.alpha, t.glow, t.luminosity, t.frontCircle, t.backCircle), i);
			else
				this.addTrailObj(new Trail3D(this, speed, x, y, z, this.size * speed / 3.125 * t.delay, this.size / 2 * t.backWidth, this.size / 2 * t.frontWidth, this.size * speed / 3.125 * t.maxLength, this.lastTrailAngle, this.lastTrailPitch,
						t.frontColor.red, t.frontColor.green, t.frontColor.blue, t.frontColor.alpha, t.backColor.red, t.backColor.green, t.backColor.blue, t.backColor.alpha, t.glow, t.luminosity, t.frontCircle, t.backCircle), i);
			i++;
		}
	}

    @Override
    public boolean disableRayCollision()
    {
        return true;
    }

    protected void addTrailObj(Trail t, int slot)
	{
		Trail old = null;

		if (this.trails[slot].size() > 0)
			old = this.trails[slot].get(0);

		this.trails[slot].add(0, t);

		if (old != null && old.spawning)
		{
			old.spawning = false;
			old.frontX = t.backX;
			old.frontY = t.backY;

			double angle = this.getPolarDirection();
			double offset = GameObject.angleBetween(angle, old.angle) / 2;

			if (t instanceof Trail3D && old instanceof Trail3D)
			{
				Trail3D t1 = (Trail3D) t;
				Trail3D old1 = (Trail3D) old;
				double offset2 = GameObject.angleBetween(t1.pitch, old1.pitch) / 2;
				old1.setFrontAngleOffset(offset, offset2);
				t1.setBackAngleOffset(-offset, -offset2);
			}
			else
			{
				old.setFrontAngleOffset(offset);
				t.setBackAngleOffset(-offset);
			}
		}
	}
	
	public void createEffect()
	{
		if (!Game.effectsEnabled || ScreenGame.finishedQuick)
			return;

		Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
		e.setColorsFromTank(this.tank);
		e.drawLevel = 9;

		if (Game.enable3d)
			e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI * 2, Math.random() * 4);
		else
			e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 4);

		Game.effects.add(e);		
	}
}
