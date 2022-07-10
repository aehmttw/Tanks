package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableForInterface;
import tanks.tank.TankProperty;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ButtonObject extends Button
{
	public IDrawableForInterface object;
	public boolean tempDisableHover = false;
	public Runnable drawBeforeTooltip = null;
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, Runnable f)
	{
		super(x, y, sX, sY, "", f);
		this.initialize(d);
	}

	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, Runnable f, String hoverText)
	{
		super(x, y, sX, sY, "", f);
		this.enableHover = true;
		this.hoverTextRaw = hoverText;
		this.hoverTextRawTranslated = Translation.translate(this.hoverTextRaw);
		this.hoverText = formatDescription(this.hoverTextRawTranslated);
		this.initialize(d);
	}
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY)
	{
		super(x, y, sX, sY, "");	
		this.initialize(d);
	}

	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, String hoverText)
	{
		super(x, y, sX, sY, "");
		this.enableHover = true;
		this.hoverTextRaw = hoverText;
		this.hoverTextRawTranslated = Translation.translate(this.hoverTextRaw);
		this.hoverText = formatDescription(this.hoverTextRawTranslated);
		this.initialize(d);
	}

	public String[] formatDescription(String desc)
	{
		ArrayList<String> text = Drawing.drawing.wrapText(desc, 300, 12);
		String[] s = new String[text.size()];
		return text.toArray(s);
	}
	
	public void initialize(IDrawableForInterface d)
	{
		this.object = d;
		this.disabledColR = 0;
		this.disabledColG = 0;
		this.disabledColB = 0;

		if (Game.game.window.touchscreen)
			this.enableHover = false;
	}
	
	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		
		this.object.drawForInterface(this.posX, this.posY);
		
		if (!enabled)
			drawing.setColor(this.disabledColR, this.disabledColG, this.disabledColB, 127);	
		else if (selected && !Game.game.window.touchscreen)
			drawing.setColor(this.selectedColR, this.selectedColG, this.selectedColB, 127);
		else
			drawing.setColor(0, 0, 0, 0);

		drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);

		if (this.drawBeforeTooltip != null)
			this.drawBeforeTooltip.run();

		this.drawBeforeTooltip = null;

		if (this.enableHover && this.selected && !tempDisableHover)
			drawing.drawTooltip(this.hoverText);

		this.tempDisableHover = false;
	}
}
