package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenBindInput;
import tanks.gui.screen.ScreenInfo;

import java.util.ArrayList;

public class InputSelector implements IDrawable, ITrigger
{
	public InputBindingGroup input;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;
	public String text;

	public boolean enableHover = false;
	public String[] hoverText;
	public String hoverTextRaw = "";

	public boolean infoSelected = false;
	public boolean selected;

	public boolean justPressed = false;

	public double colorR = 255;
	public double colorG = 255;
	public double colorB = 255;
	public double bgColorR = 200;
	public double bgColorG = 200;
	public double bgColorB = 200;
	public double hoverColorR = 240;
	public double hoverColorG = 240;
	public double hoverColorB = 255;

	public double effectTimer = 0;
	public long lastFrame = 0;

	public ArrayList<Effect> glowEffects = new ArrayList<>();

	boolean right = false;

	public boolean silent = false;

	public InputSelector(double x, double y, double sX, double sY, String text, InputBindingGroup i)
	{
		this.input = i;

		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;
		this.text = text;

		//if (text.toLowerCase().contains("back") || text.toLowerCase().contains("quit"))
		//	this.sound = "cancel.ogg";
	}

	public InputSelector(double x, double y, double sX, double sY, String text, InputBindingGroup i, String hoverText)
	{
		this(x, y, sX, sY, text, i);
		this.enableHover = true;
		this.hoverText = hoverText.split("---");
		this.hoverTextRaw = hoverText;
	}

	public void draw()
	{
		double q1 = this.posX - this.sizeX / 4;
		double q3 = this.posX + this.sizeX / 4;

		Drawing drawing = Drawing.drawing;

		drawing.setInterfaceFontSize(this.sizeY * 0.6);

		if (Game.glowEnabled)
			TextBox.drawTallGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);

		drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

		drawing.fillInterfaceRect(posX, posY - sizeY * 3 / 4, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);

		drawing.fillInterfaceRect(posX, posY - 15, sizeX, sizeY * 3 / 4);

		double m = 0.8;

		if (Game.glowEnabled)
		{
			if (selected && !right && !Game.game.window.touchscreen)
				Button.drawGlow(q1, this.posY + 5, this.sizeX / 2 - this.sizeY * (1 - m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
			else
				Button.drawGlow(q1, this.posY + 5, this.sizeX / 2 - this.sizeY * (1 - m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

			if (selected && right && !Game.game.window.touchscreen)
				Button.drawGlow(q3, this.posY + 5, this.sizeX / 2 - this.sizeY * (1 - m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
			else
				Button.drawGlow(q3, this.posY + 5, this.sizeX / 2 - this.sizeY * (1 - m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

			if (this.lastFrame == Panel.panel.ageFrames - 1 && !Game.game.window.drawingShadow)
			{
				for (Effect e : this.glowEffects)
				{
					e.drawGlow();
					e.draw();
				}
			}
		}

		if (selected && !Game.game.window.touchscreen && !right)
			drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
		else
			drawing.setColor(this.colorR, this.colorG, this.colorB);

		drawing.fillInterfaceRect(q1, posY, sizeX / 2 - sizeY, sizeY * m);
		drawing.fillInterfaceOval(q1 - sizeX / 4 + sizeY / 2, posY, sizeY * m, sizeY * m);
		drawing.fillInterfaceOval(q1 + sizeX / 4 - sizeY / 2, posY, sizeY * m, sizeY * m);

		if (selected && !Game.game.window.touchscreen && right)
			drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
		else
			drawing.setColor(this.colorR, this.colorG, this.colorB);

		drawing.fillInterfaceRect(q3, posY, sizeX / 2 - sizeY, sizeY * m);
		drawing.fillInterfaceOval(q3 - sizeX / 4 + sizeY / 2, posY, sizeY * m, sizeY * m);
		drawing.fillInterfaceOval(q3 + sizeX / 4 - sizeY / 2, posY, sizeY * m, sizeY * m);

		drawing.setColor(0, 0, 0);

		drawing.drawInterfaceText(posX, posY - sizeY * 13 / 16, text);

		if (input.input1.inputType == null)
			drawing.setColor(127, 127, 127);
		else
			drawing.setColor(0, 0, 0);

		Drawing.drawing.drawInterfaceText(q1, posY, input.input1.getInputName());

		if (input.input2.inputType == null)
			drawing.setColor(127, 127, 127);
		else
			drawing.setColor(0, 0, 0);

		Drawing.drawing.drawInterfaceText(q3, posY, input.input2.getInputName());

		if (enableHover)
		{
			if (Game.glowEnabled)
			{
				if (infoSelected && !Game.game.window.touchscreen)
				{
					Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.7, 0, 0, 0, 80, false);
					Drawing.drawing.setColor(0, 0, 255);
					Drawing.drawing.fillInterfaceGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 9 / 4, this.sizeY * 9 / 4);
				}
				else
					Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
			}

			if (infoSelected && !Game.game.window.touchscreen)
			{
				drawing.setColor(0, 0, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 1 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
				drawing.drawTooltip(this.hoverText);
			}
			else
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 1 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
			}
		}
	}

	@Override
	public void setPosition(double x, double y)
	{
		this.posX = x;
		this.posY = y;
	}

	public void update()
	{
		this.justPressed = false;

		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getInterfaceMouseX();
			double my = Drawing.drawing.getInterfaceMouseY();

			boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

			if (handled)
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
		}
		else
		{
			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.equals(""))
				{
					double mx = Drawing.drawing.getInterfacePointerX(p.x);
					double my = Drawing.drawing.getInterfacePointerY(p.y);

					boolean handled = checkMouse(mx, my, p.valid);

					if (handled)
						p.tag = "button";
				}
			}
		}

		if (Game.glowEnabled && !Game.game.window.drawingShadow)
		{
			if (this.lastFrame < Panel.panel.ageFrames - 1)
				this.glowEffects.clear();

			this.lastFrame = Panel.panel.ageFrames;

			for (int i = 0; i < this.glowEffects.size(); i++)
			{
				Effect e = this.glowEffects.get(i);
				e.update();

				if (e.age > e.maxAge)
				{
					this.glowEffects.remove(i);
					i--;
				}
			}

			if (this.selected && !Game.game.window.touchscreen)
			{
				this.effectTimer += 0.25 * (this.sizeX + this.sizeY) / 400 * Math.random() * Game.effectMultiplier;

				while (this.effectTimer >= 0.4 / Panel.frameFrequency)
				{
					this.effectTimer -= 0.4 / Panel.frameFrequency;

					double off = -this.sizeX / 4;

					if (this.right)
						off = -off;

					Button.addEffect(this.posX + off, this.posY, this.sizeX / 2 - this.sizeY * (1 - 0.8), this.sizeY * 0.8, this.glowEffects);
				}
			}
		}
	}

	public boolean checkMouse(double mx, double my, boolean valid)
	{
		boolean handled = false;

		if (Game.game.window.touchscreen)
		{
			sizeX += 20;
			sizeY += 20;
		}

		selected = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - sizeY * 3 / 4 && my < posY + sizeY / 2;
		infoSelected = (mx > posX + sizeX/2 - sizeY && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);
		right = mx >= posX;

		if (selected && valid)
		{
			if (infoSelected && this.enableHover && Game.game.window.touchscreen)
			{
				handled = true;
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
				//Drawing.drawing.playSound(this.sound, 1f, 1f);
				Drawing.drawing.playVibration("click");
				Game.screen = new ScreenInfo(Game.screen, this.text, this.hoverText);
			}
			else
			{
				if (mx < this.posX)
					Game.screen = new ScreenBindInput(Game.screen, input.input1, this.text);
				else
					Game.screen = new ScreenBindInput(Game.screen, input.input2, this.text);

				handled = true;
				this.justPressed = true;

				if (!this.silent)
				{
					Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
					//Drawing.drawing.playSound(this.sound, 1f, 1f);
					Drawing.drawing.playVibration("click");
				}
			}
		}

		if (Game.game.window.touchscreen)
		{
			sizeX -= 20;
			sizeY -= 20;
		}

		return handled;
	}
}
