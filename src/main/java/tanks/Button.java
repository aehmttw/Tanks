package tanks;

import org.lwjgl.glfw.GLFW;

public class Button 
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;
	public String text;

	public boolean enableHover = false;
	public String[] hoverText;

	public boolean selected = false;
	public boolean infoSelected = false;

	public boolean clicked = false;
	
	public boolean enabled = true;

	double disabledColR = 200;
	double disabledColG = 200;
	double disabledColB = 200;
	
	double unselectedColR = 255;
	double unselectedColG = 255;
	double unselectedColB = 255;
	
	double selectedColR = 240;
	double selectedColG = 240;
	double selectedColB = 255;

	public Button(double x, double y, double sX, double sY, String text, Runnable f)
	{
		this.function = f;

		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;
		this.text = text;
	}

	public Button(double x, double y, double sX, double sY, String text, Runnable f, String hoverText)
	{
		this(x, y, sX, sY, text, f);
		this.enableHover = true;
		this.hoverText = hoverText.split("---");
	}
	
	public Button(double x, double y, double sX, double sY, String text)
	{
		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;
		this.text = text;
		
		this.enabled = false;
	}
	
	public Button(double x, double y, double sX, double sY, String text, String hoverText)
	{
		this(x, y, sX, sY, text);
		
		this.enableHover = true;
		this.hoverText = hoverText.split("---");		
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setInterfaceFontSize(24);
		
		if (!enabled)
			drawing.setColor(this.disabledColR, this.disabledColG, this.disabledColB);	
		
		else if (selected)
			drawing.setColor(this.selectedColR, this.selectedColG, this.selectedColB);
		else
			drawing.setColor(this.unselectedColR, this.unselectedColG, this.unselectedColB);

		//drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);

		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);
		 
		drawing.setColor(0, 0, 0);
		drawing.drawInterfaceText(posX, posY, text);

		if (enableHover)
		{
			if (infoSelected)
			{
				drawing.setColor(0, 0, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 2 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
				drawing.drawTooltip(this.hoverText);
			}
			else
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 2 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
			}
		}
	}

	public void update()
	{
		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		if (mx > posX - sizeX/2 && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2)
			selected = true;
		else
			selected = false;
		
		if (mx > posX + sizeX/2 - sizeY && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2)
			infoSelected = true;
		else
			infoSelected = false;

		if (selected && Game.game.window.validPressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1) && !clicked && enabled)
		{
			function.run();
			clicked = true;
			Game.game.window.validPressedButtons.remove((Integer)GLFW.GLFW_MOUSE_BUTTON_1);
		}

		if (!(selected && Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1)))
			clicked = false;
	}
}
