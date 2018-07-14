package tanks;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ScreenGame extends Screen
{
	private Button play = new Button(350, 40, "Play", () -> {
		playing = true;
	});
	
	private boolean playing;
	
	@Override
	public void update()
	{
		if (!playing)
		{
			play.update(Window.sizeX-200, Window.sizeY-50);
			
			if (Game.movables.contains(Game.player))
			{
				Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size + Panel.frameFrequency);
			}
		}
		else
		{
			Obstacle.draw_size = Math.min(Obstacle.obstacle_size, Obstacle.draw_size);
			int tanks = 0;
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				m.update();
				if (m instanceof Tank)
					tanks++;
			}

			for (int i = 0; i < Game.effects.size(); i++)
			{
				Game.effects.get(i).update();
			}
			
			for (int i = 0; i < Game.belowEffects.size(); i++)
			{
				Game.belowEffects.get(i).update();
			}
			
			if (!Game.movables.contains(Game.player))
			{
				for (int m = 0; m < Game.movables.size(); m++)
				{
					Movable mo = Game.movables.get(m);
					if (mo instanceof Bullet || mo instanceof Mine)
						mo.destroy = true;
				}

				if (Game.effects.size() == 0)
				{
					Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);
					for (int i = 0; i < Game.movables.size(); i++)
						Game.movables.get(i).destroy = true;

					if (Obstacle.draw_size <= 0)
					{
						Panel.winlose = "You were destroyed!";
						Panel.win = false;
						Game.exit();
					}
				}

			}

			/*for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Game.obstacles.get(i).posX += (Game.obstacles.get(i).posX - Game.player.posX) / 1000;
			Game.obstacles.get(i).posY += (Game.obstacles.get(i).posY - Game.player.posY) / 1000;
		}*/

			if (tanks <= 1 && !Game.player.destroy)
			{
				Game.bulletLocked = true;
				for (int m = 0; m < Game.movables.size(); m++)
				{
					Movable mo = Game.movables.get(m);
					if (mo instanceof Bullet || mo instanceof Mine)
						mo.destroy = true;
				}

				if (Game.effects.size() == 0)
				{
					Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);

					if (Obstacle.draw_size <= 0)
					{
						Panel.winlose = "Level Cleared!";
						Panel.win = true;
						Game.exit();
					}
				}
			}
			else
				Game.bulletLocked = false;
		}

		for (int i = 0; i < Game.removeMovables.size(); i++)
			Game.movables.remove(Game.removeMovables.get(i));

		for (int i = 0; i < Game.removeObstacles.size(); i++)
			Game.obstacles.remove(Game.removeObstacles.get(i));

		for (int i = 0; i < Game.removeEffects.size(); i++)
			Game.effects.remove(Game.removeEffects.get(i));

		for (int i = 0; i < Game.removeBelowEffects.size(); i++)
			Game.belowEffects.remove(Game.removeBelowEffects.get(i));

		Game.removeMovables.clear();
		Game.removeObstacles.clear();
		Game.removeEffects.clear();
		Game.removeBelowEffects.clear();
		
		if (KeyInputListener.keys.contains(KeyEvent.VK_ESCAPE))
		{
			if (!Panel.pausePressed)
				Game.screen = new ScreenPaused();
			
			Panel.pausePressed = true;
		}
		else
			Panel.pausePressed = false;

	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);
		
		for (int i = 0; i < Game.belowEffects.size(); i++)
			Game.belowEffects.get(i).draw(g);

		for (int n = 0; n < Game.movables.size(); n++)
			Game.movables.get(n).draw(g);

		for (int i = 0; i < Game.obstacles.size(); i++)
			Game.obstacles.get(i).draw(g);

		for (int i = 0; i < Game.effects.size(); i++)
			((Effect)Game.effects.get(i)).draw(g);
		
		if (!playing) {
			play.draw(g, Window.sizeX-200, Window.sizeY-50);
		}
	}

}
