package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;

public class SearchBox extends TextBox
{
	public SearchBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText)
	{
		super(x, y, sX, sY, text, f, defaultText);
		this.enableCaps = true;
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;

		drawing.setInterfaceFontSize(this.sizeY * 0.6);

		if (Game.glowEnabled)
			drawTallGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, 0, 0.6, 0, 0, 0, 100, false);

		drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

		drawing.setColor(0, 0, 0, 127);
		drawing.drawInterfaceImage("icons/search.png", this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);

		double m = 0.8;

		if (Game.glowEnabled)
		{
			if (selected)
				Button.drawGlow(this.posX + sizeY / 2, this.posY + 3.5, this.sizeX - this.sizeY * (2 - m), this.sizeY * m, 0.55, 0, 0, 0, 160, false);
			else if (hover && !Game.game.window.touchscreen)
				Button.drawGlow(this.posX + sizeY / 2, this.posY + 5, this.sizeX - this.sizeY * (2 - m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
			else
				Button.drawGlow(this.posX + sizeY / 2, this.posY + 5, this.sizeX - this.sizeY * (2 - m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

			if (this.lastFrame == Panel.panel.ageFrames - 1)
			{
				for (Effect e : this.glowEffects)
				{
					e.drawGlow();
					e.draw();
				}
			}
		}

		if (selected)
		{
			if (this.inputText.length() >= this.maxChars)
				drawing.setColor(this.selectedFullColorR, this.selectedFullColorG, this.selectedFullColorB);
			else
				drawing.setColor(this.selectedColorR, this.selectedColorG, this.selectedColorB);
		}
		else if (hover && !Game.game.window.touchscreen)
			drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
		else
			drawing.setColor(this.colorR, this.colorG, this.colorB);

		drawing.fillInterfaceRect(posX + sizeY / 2, posY, sizeX - sizeY * 2, sizeY * m);
		drawing.fillInterfaceOval(posX - sizeX / 2 + 3 * sizeY / 2, posY, sizeY * m, sizeY * m);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY * m, sizeY * m);

		Drawing.drawing.setColor(0, 0, 0);
		this.drawInput();

		if (inputText.length() > 0)
		{
			if (Game.glowEnabled)
			{
				if (clearSelected && !Game.game.window.touchscreen)
				{
					Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.7, 0, 0, 0, 80, false);
					Drawing.drawing.setColor(127, 0, 0);
					Drawing.drawing.fillInterfaceGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 9 / 4, this.sizeY * 9 / 4);
				}
				else
					Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
			}

			if (!clearSelected || Game.game.window.touchscreen)
				drawing.setColor(160, 160, 160);
			else
				drawing.setColor(255, 0, 0);

			drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);

			drawing.setColor(255, 255, 255);

			drawing.setInterfaceFontSize(this.sizeY * 0.6);
			drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY - 2.5, "x");
		}
	}

	@Override
	public boolean checkMouse(double mx, double my, boolean down, boolean valid, InputPoint p)
	{
		boolean handled = false;

		if (Game.game.window.touchscreen)
		{
			sizeX += 20;
			sizeY += 20;
		}

		hover = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		clearSelected = this.inputText.length() > 0 && mx > posX + sizeX / 2 - sizeY && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (hover && valid && enabled && !clearSelected)
		{
			if (!selected)
			{
				handled = true;
				selected = true;
				this.previousInputText = this.inputText;

				TextBox prev = Panel.selectedTextBox;

				Panel.selectedTextBox = this;

				if (prev != null)
					prev.submit();

				Drawing.drawing.playVibration("click");
				Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
				Game.game.window.getRawTextKeys().clear();
			}
		}

		if (clearSelected && valid && inputText.length() > 0)
		{
			handled = true;
			this.clear();
			function.run();
		}

		if (Game.game.window.touchscreen)
		{
			sizeX -= 20;
			sizeY -= 20;
		}

		return handled;
	}

	public void drawInput()
	{
		if (selected)
			Drawing.drawing.drawInterfaceText(posX, posY, inputText + "\u00a7127127127255_");
		else
		{
			if (this.inputText.length() <= 0)
				Drawing.drawing.drawInterfaceText(posX, posY, "\u00a7127127127255Search");
			else
				Drawing.drawing.drawInterfaceText(posX, posY, inputText);
		}
	}

	public void addEffect()
	{
		this.effectTimer += 0.25 * (this.sizeX + this.sizeY) / 400 * Math.random() * Game.effectMultiplier;

		while (this.effectTimer >= 0.4 / Panel.frameFrequency)
		{
			this.effectTimer -= 0.4 / Panel.frameFrequency;
			Button.addEffect(this.posX + this.sizeY / 2, this.posY, this.sizeX - this.sizeY * (2 - 0.8), this.sizeY * 0.8, this.glowEffects);
		}
	}

	public void submitEffect()
	{

	}

	public void inputKey(char key)
	{
		super.inputKey(key);
		this.performValueCheck();
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
}
