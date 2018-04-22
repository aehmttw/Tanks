package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Mine extends Movable
{
	public static int mine_size = 30;
	int timer = 1000;
	int size = mine_size;
	
	Tank tank;
	
	public Mine(double x, double y, Tank t) 
	{
		super(x, y);
		tank = t;
		t.liveMines++;
	}

	@Override
	public void checkCollision() {	}

	@Override
	public void draw(Graphics p) 
	{
		int s = (int) (this.size - Game.player.destroyTimer);
		
		//p.setColor(Color.yellow);
		//if (timer < 150 && timer % 2 == 1)
		//	p.setColor(Color.red);
		p.setColor(new Color(255, (int) ((this.timer) / 1000.0 * 255), 0));
		
		if (timer < 150 && (timer % 8) / 4 == 1)
				p.setColor(Color.yellow);
		
		Screen.fillRect(p, this.posX, this.posY, s, s);
	}
	
	@Override
	public void update()
	{
		this.timer--;
		
		if (destroy)
			this.explode();
		
		if (this.timer <= 0)
			this.explode();
		super.update();
	}
	
	public void explode()
	{
		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Math.abs(Game.movables.get(i).posX - this.posX) < Game.tank_size * 2.5
					&& Math.abs(Game.movables.get(i).posY - this.posY) < Game.tank_size * 2.5)
				Game.movables.get(i).destroy = true;
		}
		
		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			if (Math.abs(Game.obstacles.get(i).posX - this.posX) < Game.tank_size * 2.5 &&
					Math.abs(Game.obstacles.get(i).posY - this.posY) < Game.tank_size * 2.5)
				Game.removeObstacles.add(Game.obstacles.get(i));
		}
		
		tank.liveMines--;
		Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.mineExplosion));
		
		Game.removeMovables.add(this);
	}

}
