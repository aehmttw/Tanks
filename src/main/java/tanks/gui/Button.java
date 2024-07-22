package tanks.gui;

import basewindow.IModel;
import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenInfo;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.translation.Translation;

import java.util.ArrayList;

public class Button implements IDrawable, ITrigger
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;

	public boolean translated = true;

	public String rawText;
	public String text;
	public String translatedText;

	public String rawSubtext;
	public String subtext;
	public String translatedSubtext;

	public boolean enableHover = false;
	public String[] hoverText;
	public String hoverTextRaw = "";
	public String hoverTextRawTranslated = "";

	public boolean selected = false;
	public boolean infoSelected = false;

	public boolean justPressed = false;

	public boolean enabled = true;

	public double disabledColR = 200;
	public double disabledColG = 200;
	public double disabledColB = 200;

	public double unselectedColR = 255;
	public double unselectedColG = 255;
	public double unselectedColB = 255;

	public double selectedColR = 240;
	public double selectedColG = 240;
	public double selectedColB = 255;

	public double textColR = 0;
	public double textColG = 0;
	public double textColB = 0;

	public double textOffsetX = 0;
	public double textOffsetY = 0;

	public double imageR = 255;
	public double imageG = 255;
	public double imageB = 255;
	public boolean drawImageShadow = false;

	public boolean silent = false;

	public boolean fullInfo = false;

	public String image = null;
	public IModel model = null;
	public double imageSizeX = 0;
	public double imageSizeY = 0;

	public double imageXOffset = 0;
	public double imageYOffset = 0;

	public double effectTimer = 0;
	public long lastFrame = 0;

	public double fontSize = -1;

	public ArrayList<Effect> glowEffects = new ArrayList<>();

	//public String sound = "click.ogg";

	/** If set to true and is part of an online service, pressing the button sends the player to a loading screen*/
	public boolean wait = false;

	/** For online service use with changing interface scales
	 * -1 = left
	 * 0 = middle
	 * 1 = right*/
	public int xAlignment = 0;

	/** For online service use with changing interface scales
	 * -1 = top
	 * 0 = middle
	 * 1 = bottom*/
	public int yAlignment = 0;

	public Button(double x, double y, double sX, double sY, String text, Runnable f)
	{
		this.function = f;

		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;

		this.setText(text);
	}

	public Button(double x, double y, double sX, double sY, String text, Runnable f, String hoverText, Object... hoverTextOptions)
	{
		this(x, y, sX, sY, text, f);

		if (hoverText != null)
		{
			this.enableHover = true;
			this.setHoverText(hoverText, hoverTextOptions);
		}
	}

	public Button(double x, double y, double sX, double sY, String text)
	{
		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;
		this.setText(text);

		this.enabled = false;
	}

	public Button(double x, double y, double sX, double sY, String text, String hoverText, Object... hoverTextOptions)
	{
		this(x, y, sX, sY, text);

		if (hoverText != null)
		{
			this.enableHover = true;
			this.setHoverText(hoverText, hoverTextOptions);
		}
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;

		if (this.fontSize < 0)
		{
			if (this.enableHover)
				drawing.setBoundedInterfaceFontSize(this.sizeY * 0.6, this.sizeX - 80, this.text);
			else
				drawing.setBoundedInterfaceFontSize(this.sizeY * 0.6, this.sizeX - 40, this.text);
		}
		else
			drawing.setInterfaceFontSize(this.fontSize);

		//drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);

		if (Game.glowEnabled)
		{
			if (!enabled)
				drawGlow(this.posX, this.posY + 3.5, this.sizeX, this.sizeY, 0.55, 0, 0, 0, 160, false);
			else if (selected && !Game.game.window.touchscreen)
				drawGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, 0.65, 0, 0, 0, 80, false);
			else
				drawGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, 0.6, 0, 0, 0, 100, false);

			if (this.lastFrame == Panel.panel.ageFrames - 1 && !Game.game.window.drawingShadow)
			{
				for (Effect e : this.glowEffects)
				{
					e.drawGlow();
					e.draw();
				}
			}
		}

		if (!enabled)
			drawing.setColor(this.disabledColR, this.disabledColG, this.disabledColB);

		else if (selected && !Game.game.window.touchscreen)
			drawing.setColor(this.selectedColR, this.selectedColG, this.selectedColB);
		else
			drawing.setColor(this.unselectedColR, this.unselectedColG, this.unselectedColB);

		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

		drawing.setColor(this.textColR, this.textColG, this.textColB);

		String t = this.text;
		if (this.translated)
			t = this.translatedText;

		drawing.drawInterfaceText(posX + this.textOffsetX, posY + this.textOffsetY, t);

		if (this.subtext != null)
		{
			drawing.setInterfaceFontSize(12);
			drawing.drawInterfaceText(this.posX + sizeX / 2 - sizeY / 2, this.posY + this.sizeY * 0.325, this.subtext, true);
		}

		if (this.image != null)
		{
			if (this.drawImageShadow)
			{
				drawing.setColor(127, 127, 127);
				drawing.drawInterfaceImage(image, this.posX + this.imageXOffset + 1.5, this.posY + this.imageYOffset + 1.5, this.imageSizeX, this.imageSizeY);
			}

			drawing.setColor(this.imageR, this.imageG, this.imageB);
			drawing.drawInterfaceImage(image, this.posX + this.imageXOffset, this.posY + this.imageYOffset, this.imageSizeX, this.imageSizeY);
		}

		if (this.model != null)
		{
			Drawing.drawing.setColor(127, 180, 255);
			drawing.drawInterfaceModel2D(model, this.posX + this.imageXOffset, this.posY + this.imageYOffset, 0,this.imageSizeX * 0.75, this.imageSizeY * 0.75, this.imageSizeY * 0.75);
		}

		if (this.fontSize < 0)
			Drawing.drawing.setInterfaceFontSize(this.sizeY * 0.6);
		else
			Drawing.drawing.setInterfaceFontSize(this.fontSize);

		if (enableHover)
		{
			if (Game.glowEnabled && !fullInfo)
			{
				if (infoSelected && !Game.game.window.touchscreen)
				{
					drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.7, 0, 0, 0, 80, false);
					Drawing.drawing.setColor(0, 0, 255);
					Drawing.drawing.fillInterfaceGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 9 / 4, this.sizeY * 9 / 4);
				}
				else
					drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
			}

			if ((infoSelected || (selected && fullInfo)) && !Game.game.window.touchscreen)
			{
				if (!fullInfo)
				{
					drawing.setColor(0, 0, 255);
					drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
					drawing.setColor(255, 255, 255);
					drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
				}

				drawing.drawTooltip(this.hoverText);
			}
			else if (!fullInfo)
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
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

			if (this.selected && this.enabled && !Game.game.window.touchscreen)
			{
				this.effectTimer += 0.25 * (this.sizeX + this.sizeY) / 400 * Math.random() * Game.effectMultiplier;

				while (this.effectTimer >= 0.4 / Panel.frameFrequency)
				{
					this.effectTimer -= 0.4 / Panel.frameFrequency;
					addEffect(this.posX, this.posY, this.sizeX, this.sizeY, this.glowEffects);
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

		selected = (mx > posX - sizeX/2 && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);
		infoSelected = (mx > posX + sizeX/2 - sizeY && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);

		if (selected && valid)
		{
			if (infoSelected && this.enableHover && Game.game.window.touchscreen && !fullInfo)
			{
				handled = true;
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
				//Drawing.drawing.playSound(this.sound, 1f, 1f);
				Drawing.drawing.playVibration("click");

				if (Game.screen instanceof ScreenGame && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
					((ScreenGame) Game.screen).overlay = new ScreenInfo(null, this.translatedText, this.hoverText);
				else
					Game.screen = new ScreenInfo(Game.screen, this.translatedText, this.hoverText);
			}
			else if (enabled)
			{
				handled = true;

				function.run();

				if (!this.silent)
				{
					Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
					Drawing.drawing.playVibration("click");
				}

				this.justPressed = true;
			}
		}

		if (Game.game.window.touchscreen)
		{
			sizeX -= 20;
			sizeY -= 20;
		}

		return handled;
	}

	public static void drawGlow(double posX, double posY, double sizeX, double sizeY, double size, double r, double g, double b, double a, boolean glow)
	{
		Game.game.window.shapeRenderer.setBatchMode(true, true, false, glow);

		Drawing drawing = Drawing.drawing;
		drawing.setColor(0, 0, 0, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY - sizeY * size, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY - sizeY * size, 0);
		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);

		drawing.setColor(0, 0, 0, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY + sizeY * size, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY + sizeY * size, 0);
		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);

		Game.game.window.shapeRenderer.setBatchMode(false, true, false, glow);
		Game.game.window.shapeRenderer.setBatchMode(true, false, false, glow);

		for (int i = 0; i < 30; i++)
		{
			drawing.setColor(r, g, b, a);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);
			drawing.setColor(0, 0, 0, 0);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 + sizeY * Math.cos((i + 15) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 15) / 30.0 * Math.PI) * size, 0);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 + sizeY * Math.cos((i + 16) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 16) / 30.0 * Math.PI) * size, 0);

			drawing.setColor(r, g, b, a);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);
			drawing.setColor(0, 0, 0, 0);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + sizeY * Math.cos((i + 45) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 45) / 30.0 * Math.PI) * size, 0);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + sizeY * Math.cos((i + 46) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 46) / 30.0 * Math.PI) * size, 0);
		}


		Game.game.window.shapeRenderer.setBatchMode(false, false, false, glow);

	}

	public static void addEffect(double posX, double posY, double sizeX, double sizeY, ArrayList<Effect> glowEffects)
	{
		addEffect(posX, posY, sizeX, sizeY, glowEffects, Math.random() * 0.2 + 0.8, -1, 1);
	}

	public static void addEffect(double posX, double posY, double sizeX, double sizeY, ArrayList<Effect> glowEffects, double velocity, double mul, double max)
	{
		if (!Game.effectsEnabled)
			return;

		Effect e = Effect.createNewEffect(posX, posY, Effect.EffectType.interfacePiece);

		if (mul == -1)
			mul = 2 * Math.max(0, (sizeY / 2 - 20) / sizeY);

		double total = (sizeX - sizeY) * 2 + sizeY * Math.PI * 2;
		double rand = Math.random() * total;

		if (rand < sizeX - sizeY)
		{
			e.posX = posX + rand - (sizeX - sizeY) / 2;
			e.posY = posY + sizeY / 2 * mul;
			e.vY = velocity;
		}
		else if (rand < (sizeX - sizeY) * 2)
		{
			e.posX = posX + rand - (sizeX - sizeY) * 3 / 2;
			e.posY = posY - sizeY / 2 * mul;
			e.vY = -velocity;
		}
		else if (rand < (sizeX - sizeY) * 2 + sizeY * Math.PI)
		{
			double a = (rand - (sizeX - sizeY) * 2) / sizeY - Math.PI / 2;
			e.posX = posX + (sizeX - sizeY) / 2;
			e.posX += sizeY / 2 * Math.cos(a) * mul;
			e.posY += sizeY / 2 * Math.sin(a) * mul;
			e.setPolarMotion(a, velocity);
		}
		else
		{
			double a = (rand - (sizeX - sizeY) * 2 + sizeY * Math.PI) / sizeY + Math.PI / 2;
			e.posX = posX - (sizeX - sizeY) / 2;
			e.posX += sizeY / 2 * Math.cos(a) * mul;
			e.posY += sizeY / 2 * Math.sin(a) * mul;
			e.setPolarMotion(a, velocity);
		}

		//e.size = 0.5;
		e.colR = 255;
		e.colG = 255;
		e.colB = 255;
		e.glowR = 255 * 0.25;
		e.glowG = e.glowR;
		e.glowB = e.glowR;
		e.vX /= 2;
		e.vY /= 2;
		e.maxAge *= max;
		glowEffects.add(e);
	}

	public void setText(String text)
	{
		this.rawText = text;
		this.text = text;
		this.translatedText = Translation.translate(text);
	}

	public void setText(String text, String text2)
	{
		this.rawText = text + text2;
		this.text = text + text2;
		this.translatedText = Translation.translate(text) + Translation.translate(text2);
	}

	public void setText(String text, Object... objects)
	{
		this.rawText = text;
		this.text = String.format(text, objects);
		this.translatedText = Translation.translate(text, objects);
	}

	public void setTextArgs(Object... objects)
	{
		this.text = String.format(this.rawText, objects);
		this.translatedText = Translation.translate(this.rawText, objects);
	}

	public void setSubtext(String text)
	{
		this.rawSubtext = text;
		this.subtext = text;
		this.translatedSubtext = Translation.translate(text);
	}

	public void setSubtext(String text, Object... objects)
	{
		this.rawSubtext = text;
		this.subtext = String.format(text, objects);
		this.translatedSubtext = Translation.translate(text, objects);
	}

	public void setSubtextArgs(Object... objects)
	{
		this.subtext = String.format(this.rawSubtext, objects);
		this.translatedSubtext = Translation.translate(this.rawSubtext, objects);
	}

	public void setHoverText(String hoverText, Object... objects)
	{
		this.hoverTextRaw = hoverText;
		this.hoverTextRawTranslated = Translation.translate(hoverText, objects);
		this.hoverText = this.hoverTextRawTranslated.split("---");
	}
}
