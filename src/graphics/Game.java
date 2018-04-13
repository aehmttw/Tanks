package graphics;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Game 
{
	public static final int tank_size = 50;
	public static ArrayList<Movable> movables = new ArrayList<Movable>();
	public static ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

	public static PlayerTank player;
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater
		(
			new Runnable()
			{
				@Override
				public void run() 
				{
					Screen gamescreen = new Screen();
					
					start();

					//movables.add(new EnemyTankStationary(120, 600, tank_size));
					//movables.add(new EnemyTankStationary(900, 700, tank_size));

					//add things to movables

					Panel screen = new Panel();

					gamescreen.add(screen);
					screen.startTimer();
				}
			}
		);
	}
	
	public static void reset()
	{
		obstacles.clear();
		movables.clear();
		start();
	}
	
	public static void start()
	{
		player = new PlayerTank(3 * tank_size, 15 * tank_size, tank_size, new Color(0, 150, 255));
		movables.add(player);
			
		obstacles.add(new Obstacle(4, 6, Color.black));
		obstacles.add(new Obstacle(5, 6, Color.black));
		obstacles.add(new Obstacle(6, 6, Color.black));
		obstacles.add(new Obstacle(7, 6, Color.black));
		obstacles.add(new Obstacle(8, 6, Color.black));
		obstacles.add(new Obstacle(9, 6, Color.black));
		obstacles.add(new Obstacle(10, 6, Color.black));
		obstacles.add(new Obstacle(11, 6, Color.black));
		obstacles.add(new Obstacle(11, 5, Color.black));
		obstacles.add(new Obstacle(11, 4, Color.black));
		obstacles.add(new Obstacle(11, 3, Color.black));
		obstacles.add(new Obstacle(11, 2, Color.black));
		obstacles.add(new Obstacle(11, 1, Color.black));
		obstacles.add(new Obstacle(11, 0, Color.black));

		obstacles.add(new Obstacle(17, 6, Color.black));
		obstacles.add(new Obstacle(18, 6, Color.black));
		obstacles.add(new Obstacle(19, 6, Color.black));
		obstacles.add(new Obstacle(20, 6, Color.black));
		obstacles.add(new Obstacle(21, 6, Color.black));
		obstacles.add(new Obstacle(22, 6, Color.black));
		obstacles.add(new Obstacle(23, 6, Color.black));
		obstacles.add(new Obstacle(24, 6, Color.black));
		obstacles.add(new Obstacle(25, 6, Color.black));
		obstacles.add(new Obstacle(26, 6, Color.black));
		obstacles.add(new Obstacle(27, 6, Color.black));
		obstacles.add(new Obstacle(16, 6, Color.black));
		obstacles.add(new Obstacle(16, 5, Color.black));
		obstacles.add(new Obstacle(16, 4, Color.black));
		obstacles.add(new Obstacle(16, 3, Color.black));

		obstacles.add(new Obstacle(0, 11, Color.black));
		obstacles.add(new Obstacle(1, 11, Color.black));
		obstacles.add(new Obstacle(2, 11, Color.black));
		obstacles.add(new Obstacle(3, 11, Color.black));
		obstacles.add(new Obstacle(4, 11, Color.black));
		obstacles.add(new Obstacle(5, 11, Color.black));
		obstacles.add(new Obstacle(6, 11, Color.black));
		obstacles.add(new Obstacle(7, 11, Color.black));
		obstacles.add(new Obstacle(8, 11, Color.black));
		obstacles.add(new Obstacle(9, 11, Color.black));
		obstacles.add(new Obstacle(10, 11, Color.black));
		obstacles.add(new Obstacle(11, 11, Color.black));
		obstacles.add(new Obstacle(11, 12, Color.black));
		obstacles.add(new Obstacle(11, 13, Color.black));
		obstacles.add(new Obstacle(11, 14, Color.black));

		obstacles.add(new Obstacle(16, 11, Color.black));
		obstacles.add(new Obstacle(17, 11, Color.black));
		obstacles.add(new Obstacle(18, 11, Color.black));
		obstacles.add(new Obstacle(19, 11, Color.black));
		obstacles.add(new Obstacle(20, 11, Color.black));
		obstacles.add(new Obstacle(21, 11, Color.black));
		obstacles.add(new Obstacle(22, 11, Color.black));
		obstacles.add(new Obstacle(23, 11, Color.black));
		obstacles.add(new Obstacle(16, 12, Color.black));
		obstacles.add(new Obstacle(16, 13, Color.black));
		obstacles.add(new Obstacle(16, 14, Color.black));
		obstacles.add(new Obstacle(16, 15, Color.black));
		obstacles.add(new Obstacle(16, 16, Color.black));
		obstacles.add(new Obstacle(16, 17, Color.black));

		movables.add(new EnemyTankStationaryFire(7.5 * tank_size, 3.5 * tank_size, tank_size));
		movables.add(new EnemyTankStationaryFire(20.5 * tank_size, 14.5 * tank_size, tank_size));
		movables.add(new EnemyTankStationaryFire(22.5 * tank_size, 3.5 * tank_size, tank_size));
		movables.add(new EnemyTankStationaryFire(8.5 * tank_size, 9 * tank_size, tank_size));
		movables.add(new EnemyTankStationaryFire(19.5 * tank_size, 9 * tank_size, tank_size));
		movables.add(new EnemyTankStationaryFire(14 * tank_size, 5.5 * tank_size, tank_size));
	}
}
