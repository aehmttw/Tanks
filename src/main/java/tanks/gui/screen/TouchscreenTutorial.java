package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.obstacle.ObstacleText;
import tanks.tank.Tank;
import tanks.tank.TankDummy;

public class TouchscreenTutorial
{
	public static void loadTutorial(boolean initial)
	{
		Level l = new Level("{48,18,235,207,166,20,20,20|" +
				"38-6...11,14-4...17-hard,19-0...5-hard,20...31-4-hard,38-0...5-hard,38-12...17-hard,32...37-4-hole|" +
				"46-13-gray-2,4-6-player-0,19-13-brown-3}");
		
		l.loadLevel();
		Tank t = new TankDummy("dummy", 7.5 * Game.tank_size, 12.5 * Game.tank_size, Math.PI);
		t.team = Game.enemyTeam;
		Game.movables.add(t);

		Tank t2 = new TankDummy("dummy", 26.5 * Game.tank_size, 1.5 * Game.tank_size, 0);
		t2.team = Game.enemyTeam;
		Game.movables.add(t2);
		
		Game.obstacles.add(new ObstacleText("text", "Welcome to Tanks!", 4, 2));
		Game.obstacles.add(new ObstacleText("text", "You control this blue tank", 4, 3));
		Game.obstacles.add(new ObstacleText("text", "Move it with the blue joystick", 4, 4));

		Game.obstacles.add(new ObstacleText("text", "This is an enemy tank", 7, 11));
		Game.obstacles.add(new ObstacleText("text", "Tap it to shoot it!", 7, 13));
		Game.obstacles.add(new ObstacleText("text", "You can have up to", 7, 15));
		Game.obstacles.add(new ObstacleText("text", "5 active bullets at once", 7, 16));

		Game.obstacles.add(new ObstacleText("text", "Move over here to continue", 14, 2));
		
		Game.obstacles.add(new ObstacleText("text", "This tank will shoot at you", 19, 11));
		Game.obstacles.add(new ObstacleText("text", "Move to avoid the bullets", 19, 12));
		Game.obstacles.add(new ObstacleText("text", "If any bullet hits you,", 19, 14));
		Game.obstacles.add(new ObstacleText("text", "You must restart the level", 19, 15));

		Game.obstacles.add(new ObstacleText("text", "To destroy this tank, you need", 31, 0));
		Game.obstacles.add(new ObstacleText("text", "to rebound your bullets", 31, 1));
		Game.obstacles.add(new ObstacleText("text", "Tap your tank and drag inside the circle", 31, 2));
		Game.obstacles.add(new ObstacleText("text", "to aim, then drag your finger out to shoot!", 31, 3));

		Game.obstacles.add(new ObstacleText("text", "This brown wall section can be broken!", 32, 7));
		Game.obstacles.add(new ObstacleText("text", "Double tap your tank to lay a mine", 32, 8));
		Game.obstacles.add(new ObstacleText("text", "The mine will explode if", 32, 9));
		Game.obstacles.add(new ObstacleText("text", "you shoot it, or after some time", 32, 10));
		Game.obstacles.add(new ObstacleText("text", "and it will destroy nearby", 32, 11));
		Game.obstacles.add(new ObstacleText("text", "tanks and obstacles, so back off!", 32, 12));

		Game.obstacles.add(new ObstacleText("text", "This gray tank can move,", 43, 7));
		Game.obstacles.add(new ObstacleText("text", "so watch out!", 43, 8));
		Game.obstacles.add(new ObstacleText("text", "Destroy all enemy tanks", 43, 10));
		Game.obstacles.add(new ObstacleText("text", "to clear the level!", 43, 11));

		Drawing.drawing.movingCamera = true;
		ScreenInterlevel.tutorialInitial = initial;
		ScreenInterlevel.tutorial = true;

		if (initial)
			Game.screen = new ScreenTutorialGame();
		else
			Game.screen = new ScreenGame();
	}
}