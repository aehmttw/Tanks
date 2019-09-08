package tanks.gui;

import tanks.Drawing;
import tanks.IDrawableForInterface;

public class ButtonObject extends Button
{
	public IDrawableForInterface object;
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, Runnable f)
	{
		super(x, y, sX, sY, "", f);
		this.initialize(d);
	}

	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, Runnable f, String hoverText)
	{
		super(x, y, sX, sY, "", f, hoverText);	
		this.initialize(d);
	}
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY)
	{
		super(x, y, sX, sY, "");	
		this.initialize(d);
	}
	
	public ButtonObject(IDrawableForInterface d, double x, double y, double sX, double sY, String hoverText)
	{
		super(x, y, sX, sY, "", hoverText);
		this.initialize(d);
	}
	
	public void initialize(IDrawableForInterface d)
	{
		this.object = d;
		this.disabledColR = 0;
		this.disabledColG = 0;
		this.disabledColB = 0;
	}
	
	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		
		this.object.drawForInterface(this.posX, this.posY);
		
		if (!enabled)
			drawing.setColor(this.disabledColR, this.disabledColG, this.disabledColB, 127);	
		else if (selected)
			drawing.setColor(this.selectedColR, this.selectedColG, this.selectedColB, 127);
		else
			drawing.setColor(0, 0, 0, 0);

		drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);

		if (enableHover)
		{
			if (selected)
			{
				drawing.setColor(0, 0, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2, this.posY + this.sizeY / 2, this.sizeY * 1 / 4, this.sizeY * 1 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 2 + this.sizeX / 2, this.posY + this.sizeY / 2, "i");
				drawing.drawTooltip(this.hoverText);
			}
			else
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2, this.posY + this.sizeY / 2, this.sizeY * 1 / 4, this.sizeY * 1 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 2 + this.sizeX / 2, this.posY + this.sizeY / 2, "i");
			}
		}
	}
}
