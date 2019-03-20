package tanks;
import java.util.ArrayList;

public class Drawing
{
	protected static boolean initialized = false;
	
	public int sizeX = 1400;//1920;
	public int sizeY = 900;//1100;

	public double playerX = sizeX / 2;
	public double playerY = sizeY / 2;

	public double interfaceSizeX = 1400;
	public double interfaceSizeY = 900;

	public double scale = 1;
	public double unzoomedScale = 1;

	public double interfaceScale = 1;

	public boolean enableMovingCamera = false;
	public boolean enableMovingCameraX = false;
	public boolean enableMovingCameraY = false;

	public int statsHeight = 40;
	
	public boolean movingCamera = false;

	public static Drawing drawing;

	public int mouseXoffset = 0;
	public int mouseYoffset = 0;

	public ArrayList<String> pendingSounds = new ArrayList<String>();
	
	public double fontSize = 1;

	private Drawing() {}
	
	public static void initialize()
	{
		if (!initialized)
			drawing = new Drawing();
		
		initialized = true;
	}
	
	public void setScale(double scale)
	{
		this.scale = scale;
	}
	
	public double getScale() 
	{
		return this.scale;
	}

	/*@Override
	public void setSize(int x, int y)
	{
		Panel.panel.setPreferredSize(new Dimension(x, (int) (y + statsHeight)));
		this.getContentPane().remove(Panel.panel);
		this.getContentPane().add(Panel.panel);
		this.pack();
	}*/

	public void setColor(double r, double g, double b)
	{
		Game.game.window.setColor(r, g, b);
	}
	
	public void setColor(double r, double g, double b, double a)
	{
		Game.game.window.setColor(r, g, b, a);
	}
	
	public void fillOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - this.sizeX * this.scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * this.scale) / 2);

		if (drawX - 200 * this.scale > Panel.windowWidth || drawX + 200 * this.scale < 0 || drawY - 200 * this.scale > Panel.windowHeight || drawY + 200 * this.scale < 0)
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void fillForcedOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - this.sizeX * this.scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * this.scale) / 2);

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - this.sizeX * scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;
		
		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void drawImage(String img, double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;
		
		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.drawImage(drawX, drawY, drawSizeX, drawSizeY, img, false);
	}
	
	public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double dX1 = getPointX(x1);
		double dX2 = getPointX(x2);
		double dX3 = getPointX(x3);
		double dX4 = getPointX(x4);

		double dY1 = getPointY(y1);
		double dY2 = getPointY(y2);
		double dY3 = getPointY(y3);
		double dY4 = getPointY(y4);
		
		Game.game.window.fillQuad(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4);
	}
	
	public void fillInterfaceQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double dX1 = getInterfacePointX(x1);
		double dX2 = getInterfacePointX(x2);
		double dX3 = getInterfacePointX(x3);
		double dX4 = getInterfacePointX(x4);

		double dY1 = getInterfacePointY(y1);
		double dY2 = getInterfacePointY(y2);
		double dY3 = getInterfacePointY(y3);
		double dY4 = getInterfacePointY(y4);
		
		Game.game.window.fillQuad(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4);
	}
	
	public double getPointX(double x)
	{
		return (scale * (x + getPlayerOffsetX()) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
	}
	
	public double getPointY(double y)
	{
		return (scale * (y + getPlayerOffsetY()) + Math.max(0, Panel.windowHeight - statsHeight - this.sizeY * scale) / 2);
	}
	
	public double getInterfacePointX(double x)
	{
		return (interfaceScale * (x) + Math.max(0, Panel.windowWidth - this.interfaceSizeX * interfaceScale) / 2);
	}
	
	public double getInterfacePointY(double y)
	{
		return (interfaceScale * (y) + Math.max(0, Panel.windowHeight - statsHeight - this.interfaceSizeY * interfaceScale) / 2);
	}

	public void fillBackgroundRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * scale) / 2);
		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		double drawY = (scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;

		double drawSizeX = Math.round(sizeX * scale);
		double drawSizeY = Math.round(sizeY * scale);

		Game.game.window.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth  - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawInterfaceOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth  - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);
		
		Game.game.window.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void fillInterfaceProgressRect(double x, double y, double sizeX, double sizeY, double progress)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale * progress);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void drawInterfaceImage(String img, double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.drawImage(drawX, drawY, drawSizeX, drawSizeY, img, false);
	}

	public void drawInterfaceRect(double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		Game.game.window.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawText(double x, double y, String text)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);
		
		int drawX = (int) (scale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		int drawY = (int) (scale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - this.sizeY * scale) / 2);
		
		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}

	public void drawInterfaceText(double x, double y, String text)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);
				
		int drawX = (int) (interfaceScale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		int drawY = (int) (interfaceScale * y - sizeY / 2 + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		
		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}

	public void drawInterfaceText(double x, double y, String text, boolean rightAligned)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);
		//System.out.println(sizeX);
		//System.out.println(sizeY);

		double offX = sizeX;
		
		if (!rightAligned)
			offX = 0;
		
		int drawX = (int) (interfaceScale * x - offX + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		int drawY = (int) (interfaceScale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}
	
	public void drawUncenteredInterfaceText(double x, double y, String text)
	{
		int drawX = (int) (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		int drawY = (int) (interfaceScale * y + Math.max(0, Panel.windowHeight  - statsHeight - interfaceSizeY * interfaceScale) / 2);
		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}

	public void setFontSize(double size)
	{
		this.fontSize = size / 36.0 * scale;
	}

	public void setInterfaceFontSize(double size)
	{
		this.fontSize = size / 36.0 * interfaceScale;
	}

	public void drawTooltip(String[] text)
	{
		double x = getInterfaceMouseX();
		double y = getInterfaceMouseY();

		int xPadding = (int) (16);
		int yPadding = (int) (8);

		setInterfaceFontSize(14);

		int sizeX = 0;
		for (int i = 0; i < text.length; i++)
		{
			sizeX = Math.max(sizeX, (int) Math.round(Game.game.window.fontRenderer.getStringSizeX(fontSize, text[i]) / this.interfaceScale) + xPadding);
		}

		int sizeY = 14;

		double drawX = x + sizeX / 2 + xPadding;
		double drawY = y + sizeY / 2 + yPadding * text.length;

		setColor(0, 0, 0, 127);
		fillInterfaceRect(drawX, drawY, sizeX + xPadding * 2, sizeY + yPadding * 2 * text.length);

		setColor(255, 255, 255);
		for (int i = 0; i < text.length; i++)
		{
			drawUncenteredInterfaceText(x + xPadding, y + 2 + yPadding * (2 * i + 1), text[i]);
		}

		//return (y - (drawY / Window.scale + sizeY + yPadding / Window.scale * 2));
	}

	public void playSound(String sound)
	{
		pendingSounds.add(sound);
	}
	
	public double toGameCoordsX(double x)
	{
		double x1 = x;
		
		if (enableMovingCamera && movingCamera && enableMovingCameraX)
			x1 += (Game.game.window.absoluteWidth - interfaceScale * interfaceSizeX) / 2 / interfaceScale;
		
		double rawX = interfaceScale * (x1);

		rawX -= (1400 - sizeX * scale / interfaceScale) / 2 * interfaceScale;

		double gameX = (rawX) / scale - getPlayerMouseOffsetX();	
		
		return gameX;
	}
	
	public double toGameCoordsY(double y)
	{
		double y1 = y;

		if (enableMovingCamera && movingCamera && enableMovingCameraX)
			y1 += (Game.game.window.absoluteHeight - interfaceScale * interfaceSizeY - statsHeight) / 2 / interfaceScale;
		
		double rawY = interfaceScale * (y1);
		
		rawY -= (900 - sizeY * scale / interfaceScale) / 2 * interfaceScale;

		double gameY = (rawY) / scale - getPlayerMouseOffsetY();
		
		return gameY;
	}

	public double getMouseX()
	{
		return toGameCoordsX(getInterfaceMouseX());
	}

	public double getMouseY()
	{
		return toGameCoordsY(getInterfaceMouseY());
	}

	public double getInterfaceMouseX()
	{
		return (Game.game.window.absoluteMouseX - Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2) / interfaceScale + mouseXoffset / interfaceScale;
	}

	public double getInterfaceMouseY()
	{
		return (Game.game.window.absoluteMouseY - Math.max(0, Panel.windowHeight - this.statsHeight - interfaceSizeY * interfaceScale) / 2) / interfaceScale + mouseYoffset / interfaceScale;
	}

	/*public void setScreenSize(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		this.setSize((int) (x * scale), (int) ((y) * scale));
	}*/

	public void setScreenBounds(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		Game.currentSizeX = x / Game.tank_size;
		Game.currentSizeY = y / Game.tank_size;
	}

	public double getPlayerOffsetX()
	{
		if (!enableMovingCameraX)
			return 0;

		double result = (playerX - (Panel.windowWidth) / scale / 2);

		if (result < 0)
			return 0;
		else if (result + (Panel.windowWidth) / scale > sizeX)
			return 0 - (sizeX - (Panel.windowWidth) / scale);
		else
			return 0 - result;
	}

	public double getPlayerOffsetY()
	{
		if (!enableMovingCameraY)
			return 0;

		double result = (playerY - Panel.windowHeight / scale / 2);

		if (result < 0)
			return 0;
		else if (result + (Panel.windowHeight - statsHeight) / scale > sizeY)
			return 0 - (sizeY - (Panel.windowHeight - statsHeight) / scale);
		else
			return 0 - result;
	}
	
	public double getPlayerMouseOffsetX()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraX)
			return 0;

		return getPlayerOffsetX() + (Game.currentSizeX / 28.0 - 1) * interfaceSizeX / 2;
	}

	public double getPlayerMouseOffsetY()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraY)
			return 0;

		return getPlayerOffsetY() + (Game.currentSizeY / 18.0 - 1) * interfaceSizeY / 2;
	}
}
