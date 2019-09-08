package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.obstacle.ObstacleText;
import tanks.tank.Tank;
import tanks.tank.TankDummy;

public class Tutorial 
{
	public static void loadTutorial(boolean initial)
	{
		Level l = new Level("{48,18,235,207,166,20,20,20"
				+ "|38-6...11,19-4...17-hard,24-0...5-hard,38-0...5-hard,38-12...17-hard"
				+ "|4-6-player-0-ally,25-13-brown-0,46-13-gray-2}");
		
		l.loadLevel();
		Tank t = new TankDummy("dummy", 13.5 * Game.tank_size, 10.5 * Game.tank_size, Math.PI);
		t.team = Game.enemyTeam;
		Game.movables.add(t);
		
		Game.obstacles.add(new ObstacleText("text", "Welcome to Tanks!", 4, 2));
		Game.obstacles.add(new ObstacleText("text", "You control this blue tank", 4, 3));
		Game.obstacles.add(new ObstacleText("text", "Move it with WASD or the arrow keys", 4, 4));
		
		Game.obstacles.add(new ObstacleText("text", "This is an enemy tank", 13, 9));
		Game.obstacles.add(new ObstacleText("text", "Put your cursor on it to aim", 13, 11));
		Game.obstacles.add(new ObstacleText("text", "and left click or press space to shoot.", 13, 12));
		Game.obstacles.add(new ObstacleText("text", "You can have up to 5 active bullets at once", 13, 13));

		Game.obstacles.add(new ObstacleText("text", "Move over here to continue", 18, 2));
		
		Game.obstacles.add(new ObstacleText("text", "This brown tank will shoot at you", 25, 11));
		Game.obstacles.add(new ObstacleText("text", "Move to avoid the bullets", 25, 12));
		Game.obstacles.add(new ObstacleText("text", "If any bullet hits you,", 25, 14));
		Game.obstacles.add(new ObstacleText("text", "You will have to restart the level", 25, 15));
		
		Game.obstacles.add(new ObstacleText("text", "This brown wall section can be broken!", 33, 6));
		Game.obstacles.add(new ObstacleText("text", "Right click or press enter to lay a mine", 33, 7));
		Game.obstacles.add(new ObstacleText("text", "The mine will explode if", 33, 8));
		Game.obstacles.add(new ObstacleText("text", "you shoot it, or after some time", 33, 9));
		Game.obstacles.add(new ObstacleText("text", "and it will destroy nearby", 33, 10));
		Game.obstacles.add(new ObstacleText("text", "tanks and obstacles, so back off!", 33, 11));

		Game.obstacles.add(new ObstacleText("text", "This gray tank can move, so watch out!", 43, 8));
		Game.obstacles.add(new ObstacleText("text", "Destroy all enemy tanks to clear the level!", 43, 10));
		
		Drawing.drawing.movingCamera = true;
		ScreenInterlevel.tutorialInitial = initial;
		ScreenInterlevel.tutorial = true;

		if (initial)
			Game.screen = new ScreenTutorialGame();
		else
			Game.screen = new ScreenGame();
	}
}