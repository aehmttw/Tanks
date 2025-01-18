package tanks.gui;

import basewindow.BaseShapeBatchRenderer;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.rendering.ShaderFireworkExplosion;
import tanks.rendering.ShaderFireworkExplosionTrail;

import java.util.ArrayList;

public class Firework extends Movable
{
	public static final double trail_length = 50;

	public enum FireworkType {rocket, particle, particle_group, trail, flash}
	public FireworkType type;

	public double age = 0;
	public double maxAge = (int) (Math.random() * 80 + 80);

	public boolean exploded = false;
	public double rotationMultiplier = Math.random() * 4 - 2;

	public double size = 8;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorFrac;

	public double aY = 0.0625 / 2;

	public FireworkPosition position;
	public FireworkPosition lastPosition;

	public FireworkExplosion explosion;

	public static ShaderFireworkExplosion shader;
	public static ShaderFireworkExplosionTrail trailShader;

	public Firework(FireworkType t, double x, double y, ArrayList<Firework> list)
	{
		super(x, y);

		this.type = t;
		this.posX = x;
		this.posY = y;
		list.add(this);
	}

	public void setRandomColor()
	{
		this.colorFrac = Math.random();
		double col = (this.colorFrac * 255 * 6);
		double r = 0;
		double g = 0;
		double b = 0;

		if (col <= 255)
		{
			r = 255;
			g = col;
			b = 0;
		}
		else if (col <= 255 * 2)
		{
			r = 255 * 2 - col;
			g = 255;
			b = 0;
		}
		else if (col <= 255 * 3)
		{
			g = 255;
			b = col - 255 * 2;
		}
		else if (col <= 255 * 4)
		{
			g = 255 * 4 - col;
			b = 255;
		}
		else if (col <= 255 * 5)
		{
			r = col - 255 * 4;
			g = 0;
			b = 255;
		}
		else if (col <= 255 * 6)
		{
			r = 255;
			g = 0;
			b = 255 * 6 - col;
		}

		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
	}

	public void setVelocity()
	{
		vY = -Math.random() * 1.5 - 6;
		vX = Math.random() * 5 - 2.5;
		vZ = Math.random() * 5 - 2.5;
	}

	public void drawUpdate(ArrayList<Firework> current, ArrayList<Firework> next)
	{
		if (!this.exploded)
		{
			this.posX += this.vX * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
			this.posY += this.vY * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
			this.posZ += this.vZ * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
		}

		if (type == FireworkType.rocket)
		{
			if (this.position == null)
			{
				this.position = new FireworkPosition(this);
				this.lastPosition = this.position;
				this.position.isFirst = true;
			}

			if (!this.exploded)
			{
				FireworkPosition pos = new FireworkPosition(this);
				this.lastPosition.next = pos;
				this.lastPosition = pos;
			}

			Game.game.window.shapeRenderer.setBatchMode(true, false, false, false, false);
			this.position.draw();
			Game.game.window.shapeRenderer.setBatchMode(false, false, false, false, false);

			while (this.position.next != null && this.position.expired)
			{
				this.position = this.position.next;
			}

			if (!this.exploded)
			{
				this.vY += aY * Panel.frameFrequency;

				Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

				if (!Game.enable3d)
					Drawing.drawing.fillInterfaceOval(posX, posY, this.size, this.size);
				else
					Drawing.drawing.fillInterfaceOval(posX, posY, posZ, this.size, this.size);
			}

			/*Firework f = new Firework(FireworkType.trail, this.posX, this.posY, next);
			f.posZ = this.posZ;
			f.maxAge = 30;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;

			f.size = this.size;*/

			if (this.age >= this.maxAge && !this.exploded)
			{
				this.exploded = true;

				Drawing.drawing.playSound("destroy.ogg", 0.75f, 0.75f);

				int count;

				boolean circle = Math.random() < 0.3;
				boolean star = circle && Math.random() < 0.5;

				if (circle && !star)
					count = (int) (Math.random() * 5 + 1);
				else
					count = (int) (Math.max(0, Math.random() * 5 - 2) + 1);

				for (int i = 0; i < count; i++)
				{
					FireworkExplosion ex = new FireworkExplosion(this, 1.0 + (i - (count / 2.0 - 0.5)) / count, circle, star);
					Firework e1 = new Firework(FireworkType.particle_group, this.posX, this.posY, next);
					e1.explosion = ex;
					e1.maxAge = 240;
				}

//				double powerBase = Math.random() * 2 + 1;
//				double powerRandom = Math.random() * 2;
//
//				double limit = Math.random() * 50 + 50;
//
//				for (int i = 0; i < limit; i++)
//				{
//					Firework e = new Firework(FireworkType.particle, this.posX, this.posY, next);
//					e.size = 4 + Math.random() * 2;
//					double var = 50;
//					e.colorR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
//					e.colorG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
//					e.colorB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));
//					e.posZ = this.posZ;
//					e.vX = this.vX;
//					e.vY = this.vY;
//					e.vZ = this.vZ;
//
//					double power = Math.random() * powerRandom + powerBase;
//
//					if (!Game.enable3d)
//						e.addPolarMotion(Math.random() * 2 * Math.PI, Math.random() * power);
//					else
//						e.add3dPolarMotion(Math.random() * Math.PI * 2, Math.asin(Math.random() * 2 - 1), power);
//
//					e.maxAge = e.size * 40;
//				}

				Firework e = new Firework(FireworkType.flash, this.posX, this.posY, next);
				e.colorR = this.colorR;
				e.colorG = this.colorG;
				e.colorB = this.colorB;
				e.posZ = this.posZ;
				e.vX = this.vX;
				e.vY = this.vY;
				e.vZ = this.vZ;
				e.maxAge = 25;
			}

			if (this.age < this.maxAge + trail_length)
				next.add(this);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge))));

			double s = Math.max(0, this.size - (this.age * this.size / this.maxAge));

			if (!Game.enable3d)
				Drawing.drawing.fillInterfaceOval(posX, posY, s, s);
			else
				Drawing.drawing.fillInterfaceOval(posX, posY, posZ, s, s);

			if (this.age < this.maxAge)
			{
				next.add(this);
			}
		}
		else if (type == FireworkType.particle)
		{
			double opacity = Math.min(255, Math.max(0, (255 - this.age * 255.0 / this.maxAge)));
			this.vY += 0.0625 * Panel.frameFrequency / 2;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity);
			double s = this.size - (this.age * this.size / this.maxAge);

			if (!Game.enable3d)
				Drawing.drawing.fillInterfaceOval(posX, posY, s, s);
			else
				Drawing.drawing.fillInterfaceOval(posX, posY, posZ, s, s);

			/*Firework f = new Firework(FireworkType.trail, this.posX, this.posY, this.list, this.removeList);
			f.maxAge = opacity / 50;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;
			f.size = this.size;
			this.list.add(f);*/

			if (this.age < this.maxAge)
			{
				next.add(this);
			}
		}
		else if (type == FireworkType.flash)
		{
			if (this.age < this.maxAge)
			{
				next.add(this);
			}
		}
		else if (type == FireworkType.particle_group)
		{
			if (this.age < this.maxAge)
			{
				this.explosion.draw();
				next.add(this);
			}
			else
				this.explosion.renderer.free();
		}

		this.age += Panel.frameFrequency;
	}

	public void drawGlow()
	{
		if (type == FireworkType.rocket)
		{
			Game.game.window.shapeRenderer.setBatchMode(true, false, false, true, false);
			this.position.drawGlow();
			Game.game.window.shapeRenderer.setBatchMode(false, false, false, true, false);

			if (this.exploded)
				return;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

			if (Game.enable3d)
			{
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 4, this.size * 4);
				Drawing.drawing.setColor(this.colorR / 2, this.colorG / 2, this.colorB / 2);
				Drawing.drawing.fillInterfaceGlowSparkle(posX, posY, posZ, this.size * 4, this.age / 100.0 * rotationMultiplier);
			}
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 4, this.size * 4);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 2, this.size * 2);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 2, this.size * 2);
		}
		else if (type == FireworkType.particle)
		{
			double opacity = Math.min(255, Math.max(0, (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);

			if (Game.enable3d)
			{
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 8, this.size * 8);
				Drawing.drawing.setColor(this.colorR / 2, this.colorG / 2, this.colorB / 2);
				double s = Math.max(0, this.size - (this.age * this.size / this.maxAge));
				Drawing.drawing.fillInterfaceGlowSparkle(posX, posY, posZ, s * 4, this.age / 100.0 * rotationMultiplier);
			}
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 8, this.size * 8);
		}
		else if (type == FireworkType.flash)
		{
			double opacity = Math.min(255, Math.max(0, (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 40, this.size * 40);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 40, this.size * 40);
		}
		else if (type == FireworkType.particle_group)
		{
			if (this.age < this.maxAge)
				this.explosion.drawGlow();
		}

	}

	public static class FireworkExplosion
	{
		public BaseShapeBatchRenderer renderer;
		public BaseShapeBatchRenderer trailRenderer;

		public double posX;
		public double posY;
		public double posZ;
		public double color;
		public double colorShift = Math.random() - 0.5;

		public double age = 0;
		public double maxAge = 240;
		public float[] gravity = new float[]{0, 0.03125f, 0};
		public float[] colors = new float[4];
		public boolean circle;

		public boolean hasTrail = Math.random() < 0.25;

		public FireworkExplosion(Firework f, double powerMul, boolean circle, boolean star)
		{
			this.posX = f.posX;
			this.posY = f.posY;
			this.posZ = f.posZ;

			if (!Game.enable3d)
				this.posZ = 0;

			this.color = f.colorFrac;
			this.circle = circle;

			if (Math.random() < 0.6)
				this.colorShift = 0;
			else if (Math.random() < 0.2)
				this.colorShift *= 4;

			int limit = (int) (powerMul * (Math.random() * 250 + 100));
			if (circle && star)
				limit *= 0;
			else if (circle)
				limit /= 2;

			int points = 20;
			int trailLength = 10;
			double trailTimeLength = 20;

			BaseShapeBatchRenderer r = Game.game.window.createStaticBatchRenderer(shader, true, null, false, limit * points * 3);
			r.addAttribute(shader.offset);
			r.addAttribute(shader.maxAge);
			this.renderer = r;


			BaseShapeBatchRenderer r1 = null;

			if (hasTrail)
			{
				r1 = Game.game.window.createStaticBatchRenderer(trailShader, true, null, false, limit * trailLength * 6);
				r1.addAttribute(trailShader.timeOffset);
				r1.addAttribute(trailShader.posOffset);
				r1.addAttribute(trailShader.maxAge);
				this.trailRenderer = r1;
			}

			double powerBase = Math.random() * 3 + 1;
			double powerRandom = Math.random() * 2;
			if (circle)
			{
				powerRandom /= 2;
				powerBase += 0.5;
			}

			double a1 = Math.random() * Math.PI * 2;
			double a2 = Math.asin(Math.random() * 2 - 1);
			double aX = Math.cos(a1) * Math.cos(a2);
			double aY = Math.sin(a1) * Math.cos(a2);
			double aZ = Math.sin(a2);
			double a = Math.random() * 2 * Math.PI;

			double starInner = Math.random() * 0.6 + 0.2;
			int starPoints;

			double[] starX, starY;

			if (star)
			{
				double starRandom = Math.random();
				if (starRandom < 0.1)
					starPoints = 4;
				else if (starRandom < 0.7)
					starPoints = 5;
				else if (starRandom < 0.8)
					starPoints = 6;
				else if (starRandom < 0.85)
					starPoints = 7;
				else if (starRandom < 0.9)
					starPoints = 8;
				else if (starRandom < 0.95)
					starPoints = 9;
				else
					starPoints = 10;

				starX = new double[starPoints * 2];
				starY = new double[starPoints * 2];
				for (int i = 0; i < starPoints; i++)
				{
					starX[i * 2] = Math.cos(i * 1.0 / starPoints * Math.PI * 2);
					starY[i * 2] = Math.sin(i * 1.0 / starPoints * Math.PI * 2);
					starX[i * 2 + 1] = Math.cos((i + 0.5) / starPoints * Math.PI * 2) * starInner;
					starY[i * 2 + 1] = Math.sin((i + 0.5) / starPoints * Math.PI * 2) * starInner;
				}
			}

			for (int i = 0; i < limit; i++)
			{
				double maxAge = Math.random() * 80 + 160;

				double power = powerMul * (Math.random() * powerRandom + powerBase);
				double angle1 = Math.random() * Math.PI * 2;
				double angle2 = Math.asin(Math.random() * 2 - 1);
				double[] vel = new double[3];

				double velX;
				double velY;
				double velZ;

				if (circle)
				{
					double px = Math.sin(angle1);
					double py = Math.cos(angle1);

                    rotateAxis(vel, aX, aY, aZ, a, px, py, 0);
					velX = vel[0] * power + f.vX;
					velY = vel[1] * power + f.vY;
					velZ = vel[2] * power + f.vZ;
				}
				else
				{
					velX = power * Math.cos(angle1) * Math.cos(angle2) + f.vX;
					velY = power * Math.sin(angle1) * Math.cos(angle2) + f.vY;
					velZ = power * Math.sin(angle2) + f.vZ;
				}

				if (!Game.enable3d)
					velZ = 0;

				double var = 50.0;
				float cr = (float) ((Math.random() - 0.5) * var);
				float cg = (float) ((Math.random() - 0.5) * var);
				float cb = (float) ((Math.random() - 0.5) * var);

				r.setAttribute(shader.maxAge, (float) maxAge);

				for (int j = 0; j < points; j++)
				{
					r.setColor(cr, cg, cb, 255);
					r.setAttribute(shader.offset, (float) Math.cos(Math.PI * 2 * j / points), (float) Math.sin(Math.PI * 2 * j / points));
					r.addPoint((float) velX, (float) velY, (float) velZ);
					r.setAttribute(shader.offset, (float) Math.cos(Math.PI * 2 * (j + 1) / points), (float) Math.sin(Math.PI * 2 * (j + 1) / points));
					r.addPoint((float) velX, (float) velY, (float) velZ);
					r.setColor(cr, cg, cb, 127);
					r.setAttribute(shader.offset, 0, 0);
					r.addPoint((float) velX, (float) velY, (float) velZ);
				}

				if (hasTrail)
				{
					r1.setAttribute(trailShader.maxAge, (float) maxAge);

					for (int j = 0; j < trailLength; j++)
					{
						r1.setColor(cr, cg, cb, 255);
						r1.setAttribute(trailShader.posOffset, 0.5f);
						r1.setAttribute(trailShader.timeOffset, (float) trailTimeLength * j / trailLength);
						r1.addPoint((float) velX, (float) velY, (float) velZ);
						r1.setAttribute(trailShader.timeOffset, (float) trailTimeLength * (j + 1) / trailLength);
						r1.addPoint((float) velX, (float) velY, (float) velZ);
						r1.setAttribute(trailShader.posOffset, -0.5f);
						r1.setAttribute(trailShader.timeOffset, (float) trailTimeLength * j / trailLength);
						r1.addPoint((float) velX, (float) velY, (float) velZ);

						r1.setAttribute(trailShader.posOffset, -0.5f);
						r1.setAttribute(trailShader.timeOffset, (float) trailTimeLength * j / trailLength);
						r1.addPoint((float) velX, (float) velY, (float) velZ);
						r1.setAttribute(trailShader.timeOffset, (float) trailTimeLength * (j + 1) / trailLength);
						r1.addPoint((float) velX, (float) velY, (float) velZ);
						r1.setAttribute(trailShader.posOffset, 0.5f);
						r1.addPoint((float) velX, (float) velY, (float) velZ);
					}
				}
			}

			r.stage();

			if (hasTrail)
				r1.stage();
		}

		public static double[] rotate(double[] out, double rx, double ry, double rz, double rw, double px, double py, double pz)
		{
			out[0] = (1 - 2 * ry * ry - 2 * rz * rz) * px + (2 * rx * ry + 2 * rz * rw) * py + (2 * rx * rz - 2 * ry * rw) * pz;
			out[1] = (2 * rx * ry - 2 * rz * rw) * px + (1 - 2 * rx * rx - 2 * rz * rz) * py + (2 * ry * rz + 2 * rx * rw) * pz;
			out[2] = (2 * rx * rz + 2 * ry * rw) * px + (2 * ry * rz - 2 * rx * rw) * py + (1 - 2 * rx * rx - 2 * ry * ry) * pz;
			return out;
		}

		static double[] rotateAxis(double[] out, double x, double y, double z, double angle, double px, double py, double pz)
		{
			return rotate(out, x * Math.sin(angle / 2), y * Math.sin(angle / 2), z * Math.sin(angle / 2), Math.cos(angle / 2), px, py, pz);
		}

		public void draw()
		{
			this.age += Panel.frameFrequency;

			double[] col = Game.getRainbowColor(color + this.colorShift * (1.0 - Math.pow(1.0 - this.age / this.maxAge, 2)));
			renderer.settings(false, false, false);
			renderer.setPosition(Drawing.drawing.interfaceToAbsoluteX(this.posX), Drawing.drawing.interfaceToAbsoluteY(this.posY), this.posZ * Drawing.drawing.interfaceScale);
			renderer.setScale(Drawing.drawing.interfaceScale, Drawing.drawing.interfaceScale, Drawing.drawing.interfaceScale);

			shader.gravity.set(gravity);
			shader.time.set((float) this.age);
			shader.fireworkGlow.set(0f);
			colors[0] = (float) col[0] / 255;
			colors[1] = (float) col[1] / 255;
			colors[2] = (float) col[2] / 255;
			colors[3] = 1;
			shader.color.set(colors);
			renderer.draw();
		}

		public void drawTrail()
		{
			if (!hasTrail)
				return;

			double[] col = Game.getRainbowColor(color + this.colorShift * (1.0 - Math.pow(1.0 - this.age / this.maxAge, 2)));
			trailRenderer.settings(false, true, false);
			trailRenderer.setPosition(Drawing.drawing.interfaceToAbsoluteX(this.posX), Drawing.drawing.interfaceToAbsoluteY(this.posY), this.posZ * Drawing.drawing.interfaceScale);
			trailRenderer.setScale(Drawing.drawing.interfaceScale, Drawing.drawing.interfaceScale, Drawing.drawing.interfaceScale);

			trailShader.gravity.set(gravity);
			trailShader.time.set((float) this.age);
			colors[0] = (float) col[0] / 255;
			colors[1] = (float) col[1] / 255;
			colors[2] = (float) col[2] / 255;
			colors[3] = 0.25f;
			trailShader.color.set(colors);
			trailRenderer.draw();
		}

		public void drawGlow()
		{
			double[] col = Game.getRainbowColor(color + this.colorShift * (1.0 - Math.pow(1.0 - this.age / this.maxAge, 2)));
			renderer.settings(false, true, false);
			renderer.setPosition(Drawing.drawing.interfaceToAbsoluteX(this.posX), Drawing.drawing.interfaceToAbsoluteY(this.posY), this.posZ * Drawing.drawing.interfaceScale);
			renderer.setScale(Drawing.drawing.interfaceScale, Drawing.drawing.interfaceScale, Drawing.drawing.interfaceScale);

			shader.gravity.set(gravity);
			shader.time.set((float) this.age);
			shader.fireworkGlow.set(1f);
			colors[0] = (float) col[0] / 255;
			colors[1] = (float) col[1] / 255;
			colors[2] = (float) col[2] / 255;
			colors[3] = 1;
			shader.color.set(colors);
			renderer.draw();
		}
	}

	@Override
	public void draw() { }

	public static class FireworkPosition
	{
		public double posX;
		public double posY;
		public double posZ;
		public double age;
		public boolean isFirst = false;

		public double oX;
		public double oY;

		public double maxAge = trail_length;
		public boolean expired = false;

		public Firework firework;

		public FireworkPosition next;

		public FireworkPosition(Firework f)
		{
			this.posX = f.posX;
			this.posY = f.posY;
			this.posZ = f.posZ;
			this.age = 0;

			this.firework = f;

			double speed = Math.sqrt(f.vX * f.vX + f.vY * f.vY);
			this.oX = -f.vY / speed / 2;
			this.oY = f.vX / speed / 2;

			if (speed == 0)
			{
				this.oX = 0.5;
				this.oY = 0;
			}
		}

		public void draw()
		{
			double s = Math.max(0, this.firework.size - (this.age * this.firework.size / this.maxAge)) * 0.75;
			double a = 0.5;

			if (s < 0)
				s = 0;

			if (next != null)
			{
				double s2 = Math.max(0, next.firework.size - (next.age * this.firework.size / next.maxAge)) * 0.75;

				if (s2 < 0)
				{
					this.expired = true;
					return;
				}

				Drawing.drawing.setColor(firework.colorR, firework.colorG, firework.colorB, a * Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));

				Drawing.drawing.addInterfaceVertex(this.posX + this.oX * s, this.posY + this.oY * s, this.posZ);
				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				Drawing.drawing.setColor(next.firework.colorR, next.firework.colorG, next.firework.colorB, a * Math.max(0, Math.min(255, 255 - (next.age * 255.0 / next.maxAge))));

				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);

				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);
				Drawing.drawing.addInterfaceVertex(next.posX - next.oX * s2, next.posY - next.oY * s2, next.posZ);

				Drawing.drawing.setColor(firework.colorR, firework.colorG, firework.colorB, a * Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));

				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				this.next.draw();
			}

			if (next == null || isFirst)
			{
				int sign = 1;

				if (next == null)
					sign = -1;

				double angle = Movable.getPolarDirection(this.oX, this.oY);

				int sides = 12;
				for (int i = 0; i < sides; i++)
				{
					double a1 = angle + Math.PI * i / sides * sign;
					double a2 = angle + Math.PI * (i + 1) / sides * sign;

					Drawing.drawing.setColor(firework.colorR, firework.colorG, firework.colorB, a * Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a1) * s / 2, this.posY + Math.sin(a1) * s / 2, this.posZ);
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a2) * s / 2, this.posY + Math.sin(a2) * s / 2, this.posZ);
					Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);
				}
			}

			this.age += Panel.frameFrequency;
		}

		public void drawGlow()
		{
			double s = Math.max(0, this.firework.size - (this.age * this.firework.size / this.maxAge)) * 3;
			double a = Math.min(255, 255 - (this.age * 255.0 / this.maxAge)) / 255.0 * 0.75;

			if (next != null)
			{
				double s2 = Math.max(0, next.firework.size - (next.age * this.firework.size / next.maxAge)) * 3;

				double a2 = Math.min(255, 255 - (next.age * 255.0 / next.maxAge)) / 255.0 * 0.75;

				if (s < 0)
					s = 0;

				if (s2 < 0)
				{
					this.expired = true;
					return;
				}

				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(this.posX + this.oX * s, this.posY + this.oY * s, this.posZ);

				Drawing.drawing.setColor(firework.colorR * a, firework.colorG * a, firework.colorB * a);
				Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);

				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);
				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);

				Drawing.drawing.setColor(firework.colorR * a2, firework.colorG * a2, firework.colorB * a2);
				Drawing.drawing.addInterfaceVertex(next.posX, next.posY, next.posZ);

				Drawing.drawing.setColor(firework.colorR * a, firework.colorG * a, firework.colorB * a);
				Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);

				Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				Drawing.drawing.setColor(firework.colorR * a2, firework.colorG * a2, firework.colorB * a2);
				Drawing.drawing.addInterfaceVertex(next.posX, next.posY, next.posZ);

				Drawing.drawing.addInterfaceVertex(next.posX, next.posY, next.posZ);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(next.posX - next.oX * s2, next.posY - next.oY * s2, next.posZ);

				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				this.next.drawGlow();
			}

			if (next == null || isFirst)
			{
				int sign = 1;

				if (next == null)
					sign = -1;

				double angle = Movable.getPolarDirection(this.oX, this.oY);

				int sides = 12;
				for (int i = 0; i < sides; i++)
				{
					double a1 = angle + Math.PI * i / sides * sign;
					double a2 = angle + Math.PI * (i + 1) / sides * sign;

					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a1) * s / 2, this.posY + Math.sin(a1) * s / 2, this.posZ);
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a2) * s / 2, this.posY + Math.sin(a2) * s / 2, this.posZ);
					Drawing.drawing.setColor(firework.colorR * a, firework.colorG * a, firework.colorB * a);
					Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);
				}
			}
		}
	}
}
