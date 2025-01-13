package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;

public class SearchBoxInstant extends SearchBox
{
	public SearchBoxInstant(double x, double y, double sX, double sY, String text, Runnable f, String defaultText)
	{
		super(x, y, sX, sY, text, f, defaultText);
	}

	public void submitEffect()
	{

	}

	public void inputKey(char key)
	{
		super.inputKey(key);
		function.run();
	}

	public void submit()
	{
		Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ENTER);
		Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);

		this.performValueCheck();
		this.previousInputText = this.inputText;
		Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
		Drawing.drawing.playVibration("click");
		selected = false;
		Game.game.window.showKeyboard = false;
		Panel.selectedTextBox = null;

		if (Game.glowEnabled)
		{
			this.submitEffect();
		}
	}

	@Override
	public void revert()
	{
		selected = false;
		Panel.selectedTextBox = null;
		this.inputText = "";
		function.run();
		Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
		Game.game.window.showKeyboard = false;
	}

	@Override
	public void clear()
	{
		super.clear();
		function.run();
	}
}
