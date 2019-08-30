package tanks.gui.screen;

import tanks.Game;
import tanks.gui.Button;

public class ScreenTutorialGame extends ScreenGame {
	private final Button skip = new Button(100, 40, 175, 40, "Skip Tutorial", new Runnable() {
		@Override
		public void run() {
			Game.exitToTitle();
		}
	});
	
	@Override
	public void update() {
		super.update();
		
		if (!paused)
			skip.update();
	}
	
	@Override
	public void draw() {
		super.draw();
		
		if (!paused)
			skip.draw();
	}
}
