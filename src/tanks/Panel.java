package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Panel extends JPanel
{
	Timer timer;
	int height = Screen.sizeY;
	int width = Screen.sizeX;
	boolean resize = true;

	static int preGameTimer = 0;

	public Panel()
	{
		timer = new Timer(10, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (preGameTimer > 0)
				{
					preGameTimer--;
					if (Game.movables.contains(Game.player))
					{
						Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size+1);
					}
				}
				else
				{
					for (int i = 0; i < Game.movables.size(); i++)
						Game.movables.get(i).update();

					if (!Game.movables.contains(Game.player))
					{
						for (int i = 0; i < Game.movables.size(); i++)
							Game.movables.get(i).destroy = true;

						Obstacle.draw_size--;
						if (Obstacle.draw_size <= 0)
							Game.reset();
					}
				}
				
				for (int i = 0; i < Game.removeMovables.size(); i++)
					Game.movables.remove(Game.removeMovables.get(i));
				
				for (int i = 0; i < Game.removeObstacles.size(); i++)
					Game.obstacles.remove(Game.removeObstacles.get(i));
				
				for (int i = 0; i < Game.removeEffects.size(); i++)
					Game.effects.remove(Game.removeEffects.get(i));
				
				Game.removeMovables.clear();
				Game.removeObstacles.clear();
				Game.removeEffects.clear();
				
				repaint();
			}

		});

	}
	public void startTimer()
	{
		timer.start();
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		int verticalCenter = this.getHeight()/2;
		int horizontalCenter = this.getWidth()/2;

		if(!resize)
		{
			int topLeftSquareCornerY = verticalCenter - (height/2);
			int topLeftSquareCornerX = horizontalCenter - (width/2);

			g.setColor(Color.BLUE);
			g.drawRect(topLeftSquareCornerX, topLeftSquareCornerY, width, height);
		}
		else
		{
			g.setColor(Color.MAGENTA);
			g.drawRect(15,15,(this.getWidth()-30), this.getHeight()-30);
		}

	}

	@Override
	public void paint(Graphics g)
	{	
		for (int n = 0; n < Game.movables.size(); n++)
			Game.movables.get(n).draw(g);

		for (int i = 0; i < Game.obstacles.size(); i++)
			Game.obstacles.get(i).draw(g);

		for (int i = 0; i < Game.effects.size(); i++)
			Game.effects.get(i).draw(g);

		g.setColor(Color.red);
		double mx = Screen.screen.getMouseX();
		double my = Screen.screen.getMouseY();
		g.drawOval((int)(mx-4), (int)(my-4), 8, 8);
		g.drawOval((int)(mx-2), (int)(my-2), 4, 4);

	}
}