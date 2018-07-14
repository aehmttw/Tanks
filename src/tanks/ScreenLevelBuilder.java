package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ScreenLevelBuilder extends Screen
{
	enum Placeable {enemyTank, playerTank, obstacle}

	Placeable currentPlaceable = Placeable.enemyTank;
	int tankNum = 0;

	Tank mouseTank = Game.registry.getRegistry(tankNum).getTank(0, 0, 0);
	Obstacle mouseObstacle = new Obstacle(0, 0, Obstacle.getRandomColor());

	public ScreenLevelBuilder()
	{
		Obstacle.draw_size = Obstacle.obstacle_size;

		ScrollInputListener.validScrollDown = false;
		ScrollInputListener.validScrollUp = false;

		Game.player = new PlayerTank(25, 25, 0);
		Game.movables.add(Game.player);
	}

	@Override
	public void update()
	{		
		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).update();
		}

		for (int i = 0; i < Game.removeEffects.size(); i++)
		{
			Game.effects.remove(Game.removeEffects.get(i));
		}

		Game.removeEffects.clear();

		boolean up = false;
		boolean down = false;

		if (KeyInputListener.validKeys.contains(KeyEvent.VK_DOWN))
		{
			KeyInputListener.validKeys.remove((Integer)KeyEvent.VK_DOWN);
			down = true;
		}
		else if (ScrollInputListener.validScrollDown)
		{
			ScrollInputListener.validScrollDown = false;
			down = true;
		}

		if (KeyInputListener.validKeys.contains(KeyEvent.VK_UP))
		{
			KeyInputListener.validKeys.remove((Integer)KeyEvent.VK_UP);
			up = true;
		}
		else if (ScrollInputListener.validScrollUp)
		{
			ScrollInputListener.validScrollUp = false;
			up = true;
		}

		if (down && currentPlaceable == Placeable.enemyTank)
		{
			tankNum = (tankNum + 1) % Game.registry.tankRegistries.size();
			Tank t = Game.registry.getRegistry(tankNum).getTank(0, 0, 0);
			t.angle = mouseTank.angle;
			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
		}

		if (up && currentPlaceable == Placeable.enemyTank)
		{
			tankNum = ((tankNum - 1) + Game.registry.tankRegistries.size()) % Game.registry.tankRegistries.size();
			Tank t = Game.registry.getRegistry(tankNum).getTank(0, 0, 0);
			t.angle = mouseTank.angle;
			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
		}

		boolean right = false;
		boolean left = false;

		if (KeyInputListener.validKeys.contains(KeyEvent.VK_RIGHT))
		{
			KeyInputListener.validKeys.remove((Integer)KeyEvent.VK_RIGHT);
			right = true;
		}
		else if (MouseInputListener.b5ClickValid)
		{
			MouseInputListener.b5ClickValid = false;
			right = true;
		}

		if (KeyInputListener.validKeys.contains(KeyEvent.VK_LEFT))
		{
			KeyInputListener.validKeys.remove((Integer)KeyEvent.VK_LEFT);
			left = true;
		}
		else if (MouseInputListener.b4ClickValid)
		{
			MouseInputListener.b4ClickValid = false;
			left = true;
		}

		if (right)
		{
			if (currentPlaceable == Placeable.enemyTank)
			{
				currentPlaceable = Placeable.obstacle;
			}
			else if (currentPlaceable == Placeable.obstacle)
			{
				currentPlaceable = Placeable.playerTank;
				mouseTank = new PlayerTank(0, 0, 0);
			}
			else if (currentPlaceable == Placeable.playerTank)
			{
				currentPlaceable = Placeable.enemyTank;
				mouseTank = Game.registry.getRegistry(tankNum).getTank(0, 0, 0);
			}
		}

		if (left)
		{
			if (currentPlaceable == Placeable.playerTank)
			{
				currentPlaceable = Placeable.obstacle;
			}
			else if (currentPlaceable == Placeable.obstacle)
			{
				currentPlaceable = Placeable.enemyTank;
				mouseTank = new PlayerTank(0, 0, 0);
			}
			else if (currentPlaceable == Placeable.enemyTank)
			{
				currentPlaceable = Placeable.playerTank;
				mouseTank = Game.registry.getRegistry(tankNum).getTank(0, 0, 0);
			}
		}

		mouseTank.posX = Math.round(Game.window.getMouseX() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2;
		mouseTank.posY = Math.round(Game.window.getMouseY() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2;
		mouseObstacle.posX = Math.round(Game.window.getMouseX() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2;
		mouseObstacle.posY = Math.round(Game.window.getMouseY() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2;

		if (MouseInputListener.rClick)
		{
			boolean skip = false;

			if (MouseInputListener.rClickValid)
			{
				for (int i = 0; i < Game.movables.size(); i++)
				{
					Movable m = Game.movables.get(i);
					if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank && m != Game.player)
					{
						skip = true;
						Game.movables.remove(i);

						for (int z = 0; z < 100; z++)
						{
							Effect e = new Effect(m.posX, m.posY, Effect.EffectType.piece);
							int var = 50;
							e.col = new Color((int) Math.min(255, Math.max(0, ((Tank)m).color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, ((Tank)m).color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, ((Tank)m).color.getBlue() + Math.random() * var - var / 2)));
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2);
							e.maxAge /= 2;
							Game.effects.add(e);
						}

						break;
					}
				}
			}

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle m = Game.obstacles.get(i);
				if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
				{
					skip = true;
					Game.obstacles.remove(i);
					break;
				}
			}

			if (!skip && (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank) && MouseInputListener.rClickValid)
			{
				mouseTank.angle += Math.PI / 2;
			}

			MouseInputListener.rClickValid = false;
		}

		if (MouseInputListener.lClickValid || (MouseInputListener.lClick && currentPlaceable == Placeable.obstacle))
		{
			boolean skip = false;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
				{
					if (m == Game.player || !MouseInputListener.lClickValid)
						skip = true;
					else
						Game.movables.remove(i);

					break;
				}
			}

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle m = Game.obstacles.get(i);
				if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
				{
					if (!MouseInputListener.lClickValid)
						skip = true;
					else
						Game.obstacles.remove(i);

					break;
				}
			}

			if (!skip)
			{
				if (currentPlaceable == Placeable.enemyTank)
					Game.movables.add(Game.registry.getRegistry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle));
				else if (currentPlaceable == Placeable.playerTank)
				{
					Game.player.posX = mouseTank.posX;
					Game.player.posY = mouseTank.posY;
					Game.player.angle = mouseTank.angle;
				}
				else if (currentPlaceable == Placeable.obstacle)
				{
					Obstacle o = new Obstacle(0, 0, mouseObstacle.color);
					mouseObstacle.color = Obstacle.getRandomColor();
					o.posX = mouseObstacle.posX;
					o.posY = mouseObstacle.posY;
					Game.obstacles.add(o);
				}
			}

			MouseInputListener.lClickValid = false;
		}

		if (KeyInputListener.validKeys.contains(KeyEvent.VK_ENTER))
		{
			String level = "{28,18|";
			
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);
				int x = (int) (o.posX / Game.tank_size);
				int y = (int) (o.posY / Game.tank_size);

				level += x + "-" + y + ",";
			}
			
			if (Game.obstacles.size() == 0) {
				level += "|";
			}

			level = level.substring(0, level.length() - 1);
			level += "|";
			
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof Tank)
				{
					Tank t = (Tank)Game.movables.get(i);
					int x = (int) (t.posX / Game.tank_size);
					int y = (int) (t.posY / Game.tank_size);
					int angle = (int) (t.angle * 2 / Math.PI);

					level += x + "-" + y + "-" + t.name + "-" + angle + ",";
				}
			}

			level = level.substring(0, level.length() - 1);
			
			level += "}";
			
			Game.currentLevel = level;
					
			Game.screen = new ScreenGame();
		}
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);

		if (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)
			mouseTank.drawOutline(g);
		else if (currentPlaceable == Placeable.obstacle)
			mouseObstacle.drawOutline(g);

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Game.movables.get(i).draw(g);
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Game.obstacles.get(i).draw(g);
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).draw(g);
		}
	}

}
