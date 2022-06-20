package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableForInterface;

public class ButtonObject extends Button
{
	public IDrawableForInterface object;
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, Runnable f)
	{
		super(x, y, sX, sY, "", f);
		this.initialize(d);
	}

	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, Runnable f, String hoverText, Object... hoverTextOptions)
	{
		super(x, y, sX, sY, "", f, hoverText, hoverTextOptions);
		this.initialize(d);
	}
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY)
	{
		super(x, y, sX, sY, "");	
		this.initialize(d);
	}
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, String hoverText, Object... hoverTextOptions)
	{
		super(x, y, sX, sY, "", hoverText, hoverTextOptions);
		this.initialize(d);
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

		if (this.enableHover && this.selected)
			drawing.drawTooltip(this.hoverText);
	}
}
