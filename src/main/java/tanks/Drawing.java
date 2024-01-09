package tanks;

import basewindow.*;
import tanks.obstacle.Obstacle;
import tanks.rendering.TerrainRenderer;
import tanks.rendering.TrackRenderer;
import tanks.network.event.EventPlaySound;
import tanks.gui.Button;
import tanks.gui.Joystick;
import tanks.gui.screen.ScreenGame;
import tanks.tank.TankPlayer;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;

public class Drawing
{
	protected static boolean initialized = false;

	public double sizeX = 1400;
	public double sizeY = 900;

	public double playerX = sizeX / 2;
	public double playerY = sizeY / 2;

	public double interfaceScaleZoomDefault = 1;
	public double interfaceScaleZoom = 1;
	public double baseInterfaceSizeX = 1400;
	public double baseInterfaceSizeY = 900;
	public double interfaceSizeX = baseInterfaceSizeX / interfaceScaleZoom;
	public double interfaceSizeY = baseInterfaceSizeY / interfaceScaleZoom;

	public double scale = 1;
	public double unzoomedScale = 1;
	public double interfaceScale = 1;

	public boolean enableMovingCamera = false;
	public boolean enableMovingCameraX = false;
	public boolean enableMovingCameraY = false;

	public int statsHeight = 0;
	public boolean enableStats = false;

	public boolean movingCamera = true;

	public static Drawing drawing;
	public double textSize = 24;
	public double titleSize = 30;

	public double objWidth = 350;
	public double objHeight = 40;
	public double objXSpace = 380;
	public double objYSpace = 60;

	public double fontSize = 1;

	public double currentColorR;
	public double currentColorG;
	public double currentColorB;
	public double currentColorA;
	public double currentGlow = 1;

	public boolean disableFaceRemoval = true;

	public TerrainRenderer terrainRenderer;
	public TrackRenderer trackRenderer;

	public static ModelPart rotatedRect;

	public HashMap<String, Model> modelsByDir = new HashMap<>();

	private Drawing()
	{
	}

	public static void initialize()
	{
		if (!initialized)
			drawing = new Drawing();

		initialized = true;
	}

	public void showStats(boolean stats)
	{
		this.enableStats = stats;

		if (this.enableStats)
			this.statsHeight = 40;
		else
			this.statsHeight = 0;
	}

	public void setInterfaceScaleZoom(double value)
	{
		this.interfaceScaleZoomDefault = value;
		this.interfaceScaleZoom = value;
		this.interfaceSizeX = baseInterfaceSizeX / interfaceScaleZoom;
		this.interfaceSizeY = baseInterfaceSizeY / interfaceScaleZoom;

		TankPlayer.controlStick = new Joystick(150, Drawing.drawing.interfaceSizeY - 150, 200);
		TankPlayer.shootStick = new Joystick(Drawing.drawing.interfaceSizeX - 150, Drawing.drawing.interfaceSizeY - 150, 200);
		TankPlayer.mineButton = new Button(Drawing.drawing.interfaceSizeX - 300, Drawing.drawing.interfaceSizeY - 75, 60, 60, "", () -> Drawing.drawing.playVibration("heavyClick"));

		TankPlayer.shootStick.clickIntensities[0] = 1.0;
		TankPlayer.shootStick.mobile = false;
		TankPlayer.shootStick.snap = true;
		TankPlayer.shootStick.colorR = 255;
		TankPlayer.shootStick.colorB = 0;
		TankPlayer.shootStick.name = "aim";
		TankPlayer.mineButton.silent = true;

		if (value > 1)
			ScreenGame.shopOffset = -100;
	}

	public void setColor(double r, double g, double b)
	{
		Game.game.window.setColor(r, g, b);

		this.currentColorR = r;
		this.currentColorG = g;
		this.currentColorB = b;
		this.currentColorA = 255;
		this.currentGlow = 0;
	}

	public void setColor(double r, double g, double b, double a)
	{
		Game.game.window.setColor(r, g, b, a);

		this.currentColorR = r;
		this.currentColorG = g;
		this.currentColorB = b;
		this.currentColorA = a;
		this.currentGlow = 0;
	}

	public void setColor(double r, double g, double b, double a, double glow)
	{
		Game.game.window.setColor(r, g, b, a, glow);

		this.currentColorR = r;
		this.currentColorG = g;
		this.currentColorB = b;
		this.currentColorA = a;
		this.currentGlow = glow;
	}

	public void fillOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillGlow(double x, double y, double sizeX, double sizeY)
	{
		this.fillGlow(x, y, sizeX, sizeY, false);
	}

	public void fillGlow(double x, double y, double sizeX, double sizeY, boolean shade)
	{
		this.fillGlow(x, y, sizeX, sizeY, shade, false);
	}

	public void fillGlow(double x, double y, double sizeX, double sizeY, boolean shade, boolean light)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade, light);
	}

	public void fillOval(double x, double y, double z, double sizeX, double sizeY)
	{
		this.fillOval(x, y, z, sizeX, sizeY, true, true);
	}

	public void fillOval(double x, double y, double z, double sizeX, double sizeY, double oZ)
	{
		this.fillOval(x, y, z, sizeX, sizeY, oZ, true, true);
	}

	public void fillOval(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double dZ = z * scale;

		if (Game.game.window.angled && facing)
			Game.game.window.shapeRenderer.fillFacingOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
		else
			Game.game.window.shapeRenderer.fillOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
	}

	public void fillOval(double x, double y, double z, double sizeX, double sizeY, double oZ, boolean depthTest, boolean facing)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double dZ = z * scale;

		if (Game.game.window.angled && facing)
			Game.game.window.shapeRenderer.fillFacingOval(drawX, drawY, dZ, drawSizeX, drawSizeY, oZ * scale, depthTest);
		else
			Game.game.window.shapeRenderer.fillOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
	}

	public void fillForcedOval(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double dZ = z * scale;

		if (Game.game.window.angled && facing)
			Game.game.window.shapeRenderer.fillFacingOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
		else
			Game.game.window.shapeRenderer.fillOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
	}

	public void fillGlow(double x, double y, double z, double sizeX, double sizeY)
	{
		this.fillGlow(x, y, z, sizeX, sizeY, true, true, false);
	}

	public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean shade)
	{
		this.fillGlow(x, y, z, sizeX, sizeY, true, true, shade);
	}

	public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing)
	{
		this.fillGlow(x, y, z, sizeX, sizeY, depthTest, facing, false);
	}

	public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade)
	{
		this.fillGlow(x, y, z, sizeX, sizeY, depthTest, facing, shade, false);
	}

	public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade, boolean light)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double dZ = z * scale;

		if (Game.game.window.angled && facing)
			Game.game.window.shapeRenderer.fillFacingGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
		else
			Game.game.window.shapeRenderer.fillGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
	}

	public void fillForcedGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade, boolean light)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double dZ = z * scale;

		if (Game.game.window.angled && facing)
			Game.game.window.shapeRenderer.fillFacingGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
		else
			Game.game.window.shapeRenderer.fillGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
	}

	public void fillForcedOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillBackgroundRect(IBatchRenderableObject o, double x, double y, double sizeX, double sizeY)
	{
		this.fillBackgroundRect(x, y, sizeX, sizeY);
	}

	public void fillRect(IBatchRenderableObject o, double x, double y, double sizeX, double sizeY)
	{
		this.fillRect(x, y, sizeX, sizeY);
	}

	public void drawImage(String img, double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, false);
	}

	public void drawImage(double rotation, String img, double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, 0);
		double drawY = gameToAbsoluteY(y, 0);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
	}

	public void drawImage(String img, double x, double y, double z, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double drawZ = z * scale;

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, "/images/" + img, false);
	}

	public void drawImage(double rotation, String img, double x, double y, double z, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, 0);
		double drawY = gameToAbsoluteY(y, 0);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		double drawZ = z * scale;

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
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

		Game.game.window.shapeRenderer.fillQuad(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4);
	}

	public void fillQuadBox(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double z, double sZ)
	{
		fillQuadBox(x1, y1, x2, y2, x3, y3, x4, y4, z, sZ, (byte) 0);
	}

	public void fillQuadBox(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double z, double sZ, byte options)
	{
		double dX1 = getPointX(x1);
		double dX2 = getPointX(x2);
		double dX3 = getPointX(x3);
		double dX4 = getPointX(x4);

		double dY1 = getPointY(y1);
		double dY2 = getPointY(y2);
		double dY3 = getPointY(y3);
		double dY4 = getPointY(y4);

		double dZ = z * scale;
		double dsZ = sZ * scale;

		Game.game.window.shapeRenderer.fillQuadBox(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4, dZ, dsZ, options);
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

		Game.game.window.shapeRenderer.fillQuad(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4);
	}

	public double getPointX(double x)
	{
		if (Game.screen.enableMargins)
			return (scale * (x + Game.screen.getOffsetX()) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		else
			return scale * (x + Game.screen.getOffsetX());
	}

	public double getPointY(double y)
	{
		if (Game.screen.enableMargins)
			return (scale * (y + Game.screen.getOffsetY()) + Math.max(0, Panel.windowHeight - statsHeight - this.sizeY * scale) / 2);
		else
			return scale * (y + Game.screen.getOffsetY());
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
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);
		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.setBatchMode(true, true, true, false, false);
		Game.game.window.shapeRenderer.fillRect(drawX, drawY, drawSizeX, drawSizeY);
		Game.game.window.shapeRenderer.setBatchMode(false, true, true, false, false);
	}

	public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
	{
		fillBox(x, y, z, sizeX, sizeY, sizeZ, (byte) 0, null);
	}

	public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, String texture)
	{
		fillBox(x, y, z, sizeX, sizeY, sizeZ, (byte) 0, texture);
	}

	public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options)
	{
		fillBox(x, y, z, sizeX, sizeY, sizeZ, options, null);
	}

	/**
	 * Options byte:
	 * <p>
	 * 0: default
	 * <p>
	 * +1 hide behind face
	 * +2 hide front face
	 * +4 hide bottom face
	 * +8 hide top face
	 * +16 hide left face
	 * +32 hide right face
	 * <p>
	 * +64 draw on top
	 */
	public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options, String texture)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);
		double drawZ = z * scale;

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = sizeX * scale;
		double drawSizeY = sizeY * scale;
		double drawSizeZ = sizeZ * scale;

		if (!Game.followingCam && !Game.angledView && !disableFaceRemoval)
		{
			if (drawX < Game.game.window.absoluteWidth / 2)
				options = (byte) (options | 16);

			if (drawX > Game.game.window.absoluteWidth / 2)
				options = (byte) (options | 32);

			if (drawY < Game.game.window.absoluteHeight / 2)
				options = (byte) (options | 8);

			if (drawY > Game.game.window.absoluteHeight / 2)
				options = (byte) (options | 4);
		}

		Game.game.window.shapeRenderer.fillBox(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, options, texture);
	}

	public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sizeX, double sizeY, double sizeZ)
	{
		fillBox(o, x, y, z, sizeX, sizeY, sizeZ, (byte) 0);
	}

	/**
	 * Options byte:
	 * <p>
	 * 0: default
	 * <p>
	 * +1 hide behind face
	 * +2 hide front face
	 * +4 hide bottom face
	 * +8 hide top face
	 * +16 hide left face
	 * +32 hide right face
	 * <p>
	 * +64 draw on top
	 */
	public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options)
	{
		this.terrainRenderer.addBox(o, x - sizeX / 2, y - sizeY / 2, z, sizeX, sizeY, sizeZ, options, false);
	}

	public void fillForcedBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);
		double drawZ = z * scale;

		double drawSizeX = sizeX * scale;
		double drawSizeY = sizeY * scale;
		double drawSizeZ = sizeZ * scale;

		Game.game.window.shapeRenderer.fillBox(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, options, null);
	}

	public void drawModel(IModel m, double x, double y, double width, double height, double angle)
	{
		double drawX = gameToAbsoluteX(x, 0);
		double drawY = gameToAbsoluteY(y, 0);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = width * scale;
		double drawSizeY = height * scale;

		m.draw(drawX, drawY, drawSizeX, drawSizeY, angle);
	}

	public void drawModel(IModel m, double x, double y, double z, double width, double height, double depth, double angle)
	{
		double drawX = gameToAbsoluteX(x, 0);
		double drawY = gameToAbsoluteY(y, 0);
		double drawZ = z * scale;

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = width * scale;
		double drawSizeY = height * scale;
		double drawSizeZ = depth * scale;

		m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, angle, 0, 0, true);
	}

	public void drawInterfaceModel(IModel m, double x, double y, double width, double height, double angle)
	{
		double drawX = (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (width * interfaceScale);
		double drawSizeY = (height * interfaceScale);

		m.draw(drawX, drawY, drawSizeX, drawSizeY, angle);
	}

	public void drawInterfaceModel(IModel m, double x, double y, double z, double width, double height, double depth, double yaw)
	{
		this.drawInterfaceModel(m, x, y, z, width, height, depth, yaw, 0, 0);
	}

	public void drawInterfaceModel(IModel m, double x, double y, double z, double width, double height, double depth, double yaw, double pitch, double roll)
	{
		double drawX = (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawZ = z * interfaceScale;

		double drawSizeX = (width * interfaceScale);
		double drawSizeY = (height * interfaceScale);
		double drawSizeZ = depth * interfaceScale;

		m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, yaw, pitch, roll, true);
	}

	public void drawInterfaceModel2D(IModel m, double x, double y, double z, double width, double height, double depth)
	{
		double drawX = (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawZ = z * interfaceScale;

		double drawSizeX = (width * interfaceScale);
		double drawSizeY = (height * interfaceScale);
		double drawSizeZ = depth * interfaceScale;

		m.draw2D(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ);
	}

	public void drawModel(IModel m, double x, double y, double z, double width, double height, double depth, double yaw, double pitch, double roll)
	{
		double drawX = gameToAbsoluteX(x, 0);
		double drawY = gameToAbsoluteY(y, 0);
		double drawZ = z * scale;

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = width * scale;
		double drawSizeY = height * scale;
		double drawSizeZ = depth * scale;

		m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, yaw, pitch, roll, true);
	}

	public void drawRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		if (isOutOfBounds(drawX, drawY))
			return;

		double drawSizeX = (sizeX * scale);
		double drawSizeY = (sizeY * scale);

		Game.game.window.shapeRenderer.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceOval(double x, double y, double z, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);
		double drawZ = z * interfaceScale;

		Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawZ, drawSizeX, drawSizeY, false);
	}


	public void fillInterfaceGlow(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceGlow(double x, double y, double z, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);
		double drawZ = interfaceScale * z;

		Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawZ, drawSizeX, drawSizeY, false);
	}

	public void addInterfaceVertexRotated(double x, double y, double z, double ox, double oy, double rot)
	{
		Drawing.drawing.addInterfaceVertex(x + Math.cos(rot) * ox + Math.sin(rot) * oy, y - Math.sin(rot) * ox + Math.cos(rot) * oy, z);
	}

	public void fillInterfaceGlowSparkle(double x, double y, double z, double size, double angle)
	{
		double size2 = size * 0.05;
		Game.game.window.shapeRenderer.setBatchMode(true, true, false, true);

		double r = this.currentColorR;
		double g = this.currentColorG;
		double b = this.currentColorB;
		double a = this.currentColorA;

		Drawing.drawing.setColor(0, 0, 0, 0);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, size, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, size, angle);
		Drawing.drawing.setColor(r, g, b, a);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

		Drawing.drawing.setColor(0, 0, 0, 0);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, -size, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, -size, angle);
		Drawing.drawing.setColor(r, g, b, a);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

		angle += Math.PI / 2;
		Drawing.drawing.setColor(0, 0, 0, 0);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, size, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, size, angle);
		Drawing.drawing.setColor(r, g, b, a);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

		Drawing.drawing.setColor(0, 0, 0, 0);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, -size, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, -size, angle);
		Drawing.drawing.setColor(r, g, b, a);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
		Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

		Game.game.window.shapeRenderer.setBatchMode(false, true, false, true);

	}


	public void fillInterfaceGlow(double x, double y, double sizeX, double sizeY, boolean shade)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade);
	}

	public void fillInterfaceGlow(double x, double y, double sizeX, double sizeY, boolean shade, boolean light)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade, light);
	}

	public void drawInterfaceOval(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillShadedInterfaceRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.setBatchMode(true, true, true, false, false);
		Game.game.window.shapeRenderer.fillBox(drawX, drawY, 0, drawSizeX, drawSizeY, 0, (byte) 61, null);
		Game.game.window.shapeRenderer.setBatchMode(false, true, true, false, false);
	}

	public void fillInterfaceProgressRect(double x, double y, double sizeX, double sizeY, double progress)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale * progress);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawInterfaceImage(String img, double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, false);
	}

	public void drawInterfaceImage(double rotation, String img, double x, double y, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
	}

	public void drawInterfaceImage(double rotation, String img, double x, double y, double z, double sizeX, double sizeY)
	{
		double drawX = (interfaceScale * (x) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * (y) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawZ = z * interfaceScale;
		double drawSizeX = (sizeX * interfaceScale);
		double drawSizeY = (sizeY * interfaceScale);

		Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
	}

	public void drawInterfaceRect(double x, double y, double sizeX, double sizeY)
	{
		double drawX = Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawSizeX = Math.round(sizeX * interfaceScale);
		double drawSizeY = Math.round(sizeY * interfaceScale);

		Game.game.window.shapeRenderer.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawText(double x, double y, String text)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text) / scale;
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text) / scale;

		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);

		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}

	public void drawText(double x, double y, double z, String text)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text) / scale;
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text) / scale;

		double drawX = gameToAbsoluteX(x, sizeX);
		double drawY = gameToAbsoluteY(y, sizeY);
		double drawZ = z * scale;

		Game.game.window.fontRenderer.drawString(drawX, drawY, drawZ, this.fontSize, this.fontSize, text);
	}

	public void drawInterfaceText(double x, double y, String text)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

		double drawX = (interfaceScale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y - sizeY / 2 + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);

		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);

		if (Game.game.window.fontRenderer.drawBox)
		{
			Drawing.drawing.setColor(0, 255, 0);
			Game.game.window.shapeRenderer.drawRect(drawX, drawY, sizeX, sizeY);
		}
	}

	public void drawInterfaceText(double x, double y, String text, boolean rightAligned)
	{
		double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
		double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

		double offX = sizeX;

		if (!rightAligned)
			offX = 0;

		double drawX = (interfaceScale * x - offX + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y - sizeY / 2 + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}

	public void drawUncenteredInterfaceText(double x, double y, String text)
	{
		double drawX = (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
	}

	public void displayInterfaceText(double x, double y, String text, Object... objects)
	{
		drawInterfaceText(x, y, Translation.translate(text, objects));
	}

	public void displayInterfaceText(double x, double y, boolean rightAligned, String text, Object... objects)
	{
		drawInterfaceText(x, y, Translation.translate(text, objects), rightAligned);
	}

	public void displayUncenteredInterfaceText(double x, double y, String text, Object... objects)
	{
		drawUncenteredInterfaceText(x, y, Translation.translate(text, objects));
	}

	public double getStringWidth(String s)
	{
		return Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale;
	}

	public double getStringHeight(String s)
	{
		return Game.game.window.fontRenderer.getStringSizeY(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale;
	}

	public void setFontSize(double size)
	{
		this.fontSize = size / 36.0 * scale;
	}

	public void setInterfaceFontSize(double size)
	{
		this.fontSize = size / 36.0 * interfaceScale;
	}

	public void setBoundedInterfaceFontSize(double maxFontSize, double width, String text)
	{
		this.setInterfaceFontSize(maxFontSize);

		if (width > 0)
		{
			double sizeRaw = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text) / Drawing.drawing.interfaceScale;
			this.setInterfaceFontSize(maxFontSize * Math.min(1, width / sizeRaw));
		}
	}

	public void addVertex(double x, double y, double z)
	{
		double drawX = gameToAbsoluteX(x, 0);
		double drawY = gameToAbsoluteY(y, 0);
		double drawZ = z * scale;

		if (Game.enable3d)
			Game.game.window.addVertex(drawX, drawY, drawZ);
		else
			Game.game.window.addVertex(drawX, drawY);
	}

	public void addInterfaceVertex(double x, double y, double z)
	{
		double drawX = (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
		double drawY = (interfaceScale * y + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
		double drawZ = z * interfaceScale;

		if (Game.enable3d)
			Game.game.window.addVertex(drawX, drawY, drawZ);
		else
			Game.game.window.addVertex(drawX, drawY);
	}

	public void addFacingVertex(double x, double y, double z, double sX, double sY, double sZ)
	{
		if (Game.enable3d && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).slant != 0)
		{
			double angle = ((ScreenGame) Game.screen).slantRotation.pitch;

			double drawX = gameToAbsoluteX(x, 0);
			double drawY = gameToAbsoluteY(y, 0);
			double drawZ = z * scale;

			double drawSX = sX * scale;
			double drawSY = sY * scale * Math.cos(angle) - sZ * scale * Math.sin(angle);
			double drawSZ = sZ * scale * Math.cos(angle) + sY * scale * Math.sin(angle);

			Game.game.window.addVertex(drawX + drawSX, drawY + drawSY, drawZ + drawSZ);
		}
		else
			addVertex(x + sX, y + sY, z + sZ);
	}

	public void setLighting(double light, double shadow)
	{
		Game.game.window.setLighting(light, Math.max(1, light), shadow, Math.max(1, shadow));
	}

	public void drawTooltip(String[] text)
	{
		double x = getInterfaceMouseX();
		double y = getInterfaceMouseY();

		int xPadding = 16;
		int yPadding = 8;

		setInterfaceFontSize(14);

		int sizeX = 0;
		for (String s : text)
		{
			sizeX = Math.max(sizeX, (int) Math.round(Game.game.window.fontRenderer.getStringSizeX(fontSize, s) / this.interfaceScale) + xPadding);
		}

		double sizeY = 14;

		double endX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Drawing.drawing.interfaceSizeX - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
		if (x + sizeX + xPadding * 2 - 14 > endX)
			x -= x + sizeX + xPadding * 2 - 14 - endX;

		if (y + sizeY + yPadding * 2 * text.length > Drawing.drawing.interfaceSizeY)
			y -= y + sizeY + yPadding * 2 * text.length - Drawing.drawing.interfaceSizeY;

		double drawX = x + sizeX / 2.0 + xPadding;
		double drawY = y + sizeY / 2.0 + yPadding * text.length;

		setColor(0, 0, 0, 127);
		fillInterfaceRect(drawX - 7, drawY, sizeX + xPadding * 2 - 14, sizeY + yPadding * 2 * text.length);
		fillInterfaceRect(drawX - 7, drawY, sizeX + xPadding * 2 - 14 - 10, sizeY + yPadding * 2 * text.length - 10);

		setColor(255, 255, 255);
		for (int i = 0; i < text.length; i++)
		{
			drawUncenteredInterfaceText(x + xPadding, y + yPadding * (2 * i + 1), text[i]);
		}

		//return (y - (drawY / Window.scale + sizeY + yPadding / Window.scale * 2));
	}

	public void playMusic(String sound, float volume, boolean looped, String id, long fadeTime)
	{
		if (Game.game.window.soundsEnabled && Game.musicEnabled)
			Game.game.window.soundPlayer.playMusic("/music/" + sound, volume, looped, id, fadeTime);
	}

	public void playMusic(String sound, float volume, boolean looped, String id, long fadeTime, boolean stoppable)
	{
		if (Game.game.window.soundsEnabled && Game.musicEnabled)
			Game.game.window.soundPlayer.playMusic("/music/" + sound, volume, looped, id, fadeTime, stoppable);
	}

	public void stopMusic()
	{
		if (Game.game.window.soundsEnabled)
			Game.game.window.soundPlayer.stopMusic();
	}

	public void addSyncedMusic(String sound, float volume, boolean looped, long fadeTime)
	{
		if (Game.game.window.soundsEnabled && Game.musicEnabled)
			Game.game.window.soundPlayer.addSyncedMusic("/music/" + sound, volume, looped, fadeTime);
	}

	public void removeSyncedMusic(String sound, long fadeTime)
	{
		if (Game.game.window.soundsEnabled && Game.musicEnabled)
			Game.game.window.soundPlayer.removeSyncedMusic("/music/" + sound, fadeTime);
	}

	public void playSound(String sound)
	{
		if (Game.game.window.soundsEnabled && Game.soundsEnabled)
			Game.game.window.soundPlayer.playSound("/sounds/" + sound, 1, Game.soundVolume);
	}

	public void playGlobalSound(String sound)
	{
		this.playSound(sound);
		Game.eventsOut.add(new EventPlaySound(sound, 1, 1));
	}

	public void playSound(String sound, float pitch)
	{
		if (Game.game.window.soundsEnabled && Game.soundsEnabled)
			Game.game.window.soundPlayer.playSound("/sounds/" + sound, pitch, Game.soundVolume);
	}

	public void playSound(String sound, float volume, boolean asMusic)
	{
		if (Game.game.window.soundsEnabled)
		{
			if (asMusic)
			{
				if (Game.musicEnabled)
					Game.game.window.soundPlayer.playSound("/music/" + sound, 1.0f, volume * Game.musicVolume);
			}
			else if (Game.soundsEnabled)
				Game.game.window.soundPlayer.playSound("/sound/" + sound, 1.0f, volume * Game.soundVolume);
		}
	}

	public void playGlobalSound(String sound, float pitch)
	{
		this.playSound(sound, pitch);
		Game.eventsOut.add(new EventPlaySound(sound, pitch, 1));
	}

	public void playSound(String sound, float pitch, float volume)
	{
		if (Game.game.window.soundsEnabled && Game.soundsEnabled)
			Game.game.window.soundPlayer.playSound("/sounds/" + sound, pitch, volume * Game.soundVolume);
	}

	public void playGlobalSound(String sound, float pitch, float volume)
	{
		this.playSound(sound, pitch, volume);
		Game.eventsOut.add(new EventPlaySound(sound, pitch, volume));
	}

	public void playVibration(String vibration)
	{
		if (!Game.game.window.vibrationsEnabled || !Game.enableVibrations)
			return;

		switch (vibration)
		{
			case "click":
				Game.game.window.vibrationPlayer.click();
				break;
			case "heavyClick":
				Game.game.window.vibrationPlayer.heavyClick();
				break;
			case "selectionChanged":
				Game.game.window.vibrationPlayer.selectionChanged();
				break;
		}
	}

	public double toGameCoordsX(double x)
	{
		return absoluteToGameX(interfaceToAbsoluteX(x));
	}

	public double toGameCoordsY(double y)
	{
		return absoluteToGameY(interfaceToAbsoluteY(y));
	}

	public double toInterfaceCoordsX(double x)
	{
		return absoluteToInterfaceX(gameToAbsoluteX(x, 0));

		/*double rawX = (x + getPlayerMouseOffsetX()) * scale;
		rawX += (Drawing.drawing.interfaceSizeX - sizeX * scale / interfaceScale) / 2 * interfaceScale;
		double x1 = rawX / interfaceScale;

		if (enableMovingCamera && movingCamera && enableMovingCameraX)
			x1 -= (Game.game.window.absoluteWidth - interfaceScale * interfaceSizeX) / 2 / interfaceScale;

		return x1;*/
	}

	public double toInterfaceCoordsY(double y)
	{
		return absoluteToInterfaceY(gameToAbsoluteY(y, 0));

		/*double rawY = (y + getPlayerMouseOffsetY()) * scale;
		rawY += (Drawing.drawing.interfaceSizeY - sizeY * scale / interfaceScale) / 2 * interfaceScale;
		double y1 = rawY / interfaceScale;

		if (enableMovingCamera && movingCamera && enableMovingCameraY)
			y1 -= (Game.game.window.absoluteHeight - interfaceScale * interfaceSizeY - statsHeight) / 2 / interfaceScale;

		return y1;*/
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
		return (Game.game.window.absoluteMouseX - Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2) / interfaceScale;
	}

	public double getInterfaceMouseY()
	{
		return (Game.game.window.absoluteMouseY - Math.max(0, Panel.windowHeight - this.statsHeight - interfaceSizeY * interfaceScale) / 2) / interfaceScale;
	}

	public double getInterfacePointerX(double x)
	{
		return (x - Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2) / interfaceScale;
	}

	public double getInterfacePointerY(double y)
	{
		return (y - Math.max(0, Panel.windowHeight - this.statsHeight - interfaceSizeY * interfaceScale) / 2) / interfaceScale;
	}

	/*public void setScreenSize(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		this.setSize((int) (x * scale), (int) ((y) * scale));
	}*/

	public void setScreenBounds(double x, double y)
	{
		sizeX = x;
		sizeY = y;
		Game.currentSizeX = (int) (x / Game.tile_size);
		Game.currentSizeY = (int) (y / Game.tile_size);
	}

	public double getPlayerOffsetX()
	{
		if (!enableMovingCameraX && !Game.followingCam)
			return 0;

		while (Panel.panel.pastPlayerTime.size() > 1 && Panel.panel.pastPlayerTime.get(1) < Panel.panel.age - getTrackOffset())
		{
			Panel.panel.pastPlayerX.remove(0);
			Panel.panel.pastPlayerY.remove(0);
			Panel.panel.pastPlayerTime.remove(0);
		}

		double x = playerX;

		if (Panel.panel.pastPlayerTime.size() == 1)
			x = Panel.panel.pastPlayerX.get(0);
		else if (Panel.panel.pastPlayerTime.size() > 1)
		{
			double frac = (Panel.panel.age - getTrackOffset() - Panel.panel.pastPlayerTime.get(0)) / (Panel.panel.pastPlayerTime.get(1) - Panel.panel.pastPlayerTime.get(0));
			x = Panel.panel.pastPlayerX.get(0) * (1 - frac) + Panel.panel.pastPlayerX.get(1) * frac;
		}

		double result = (x - (Panel.windowWidth) / scale / 2);

		double margin = Obstacle.draw_size / Game.tile_size * Math.max(0, Math.min(Game.tile_size * 2, Game.currentSizeX * Game.tile_size * Drawing.drawing.scale - Panel.windowWidth)) / 2;

		boolean less = result < -margin;
		boolean greater = result + (Panel.windowWidth) / scale > sizeX + margin;

		if (scale * Game.currentSizeX * Game.tile_size + margin > Panel.windowWidth)
		{
			if (Game.followingCam)
				return -result * Panel.panel.zoomTimer;
			else if (less && !greater)
				return margin;
			else if (greater && !less)
				return -margin - (sizeX - (Panel.windowWidth) / scale);
			else
				return 0 - result;
		}
		else
		{
			if (Game.followingCam)
				return -result * Panel.panel.zoomTimer;
			else if (less && !greater)
				return margin;
			else if (greater && !less)
				return -margin;
			else
				return 0;
		}

		//return 0 - result;
	}

	public double getPlayerOffsetY()
	{
		if (!enableMovingCameraY && !Game.followingCam)
			return 0;

		double y = playerY;

		if (Panel.panel.pastPlayerTime.size() == 1)
			y = Panel.panel.pastPlayerY.get(0);
		else if (Panel.panel.pastPlayerTime.size() > 1)
		{
			double frac = (Panel.panel.age - getTrackOffset() - Panel.panel.pastPlayerTime.get(0)) / (Panel.panel.pastPlayerTime.get(1) - Panel.panel.pastPlayerTime.get(0));
			y = Panel.panel.pastPlayerY.get(0) * (1 - frac) + Panel.panel.pastPlayerY.get(1) * frac;
		}

		double result = (y - (Panel.windowHeight - statsHeight) / scale / 2);

		double margin = Math.max(0, Math.min(Game.tile_size * 2, Game.currentSizeY * Game.tile_size * Drawing.drawing.scale - (Panel.windowHeight - Drawing.drawing.statsHeight))) / 2;

		boolean less = result < -margin;
		boolean greater = result + (Panel.windowHeight - statsHeight) / scale > sizeY + margin;

		if (scale * Game.currentSizeY * Game.tile_size + margin > Panel.windowHeight - statsHeight)
		{
			if (Game.followingCam)
				return -result * Panel.panel.zoomTimer;
			else if (less && !greater)
				return margin;
			else if (greater && !less)
				return -margin - (sizeY - (Panel.windowHeight - statsHeight) / scale);
			else
				return 0 - result;
		}
		else
		{
			if (Game.followingCam)
				return -result * Panel.panel.zoomTimer;
			else if (less && !greater)
				return margin;
			else if (greater && !less)
				return -margin;
			else
				return 0;
		}

		//return 0 - result;
	}

	public double getPlayerMouseOffsetX()
	{
		if (!enableMovingCamera || !enableMovingCameraX)
			return 0;

		return Game.screen.getOffsetX() + (Game.currentSizeX * Drawing.drawing.interfaceScaleZoom / 28.0 - 1) * interfaceSizeX / 2 * scale;
	}

	public double getPlayerMouseOffsetY()
	{
		if (!enableMovingCamera || !enableMovingCameraY)
			return 0;

		return Game.screen.getOffsetY() + (Game.currentSizeY * Drawing.drawing.interfaceScaleZoom / 18.0 - 1) * interfaceSizeY / 2 * scale;
	}

	public double getHorizontalMargin()
	{
		return (Game.game.window.absoluteWidth - sizeX / scale) / 2;
	}

	public double getVerticalMargin()
	{
		return (Game.game.window.absoluteHeight - statsHeight - sizeY / scale) / 2;
	}

	/**
	 *	Gets interface coordinate position of left/right edge of screen
	 */
	public double getInterfaceEdgeX(boolean right)
	{
		if (right)
			return (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
		else
			return (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
				+ Drawing.drawing.interfaceSizeX - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
	}

	/**
	 *	Gets interface coordinate position of top/bottom edge of screen
	 */
	public double getInterfaceEdgeY(boolean bottom)
	{
		if (bottom)
			return ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
					+ Drawing.drawing.interfaceSizeY;
		else
			return -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
	}


	public double gameToAbsoluteX(double x, double sizeX)
	{
		if (Game.screen.enableMargins)
			return (scale * (x + Game.screen.getOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
		else
			return scale * (x + Game.screen.getOffsetX() - sizeX / 2);
	}

	public double gameToAbsoluteY(double y, double sizeY)
	{
		if (Game.screen.enableMargins)
			return (scale * (y + Game.screen.getOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - this.sizeY * scale) / 2);
		else
			return scale * (y + Game.screen.getOffsetY() - sizeY / 2);
	}

	public double absoluteToGameX(double x)
	{
		if (Game.screen.enableMargins)
			return (x - Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2) / scale - Game.screen.getOffsetX();
		else
			return x / scale - Game.screen.getOffsetX();
	}

	public double absoluteToGameY(double y)
	{
		if (Game.screen.enableMargins)
			return (y - Math.max(0, Panel.windowHeight - statsHeight - this.sizeY * scale) / 2) / scale - Game.screen.getOffsetY();
		else
			return y / scale - Game.screen.getOffsetY();
	}

	public double interfaceToAbsoluteX(double x)
	{
		return (interfaceScale * x + Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2);
	}

	public double interfaceToAbsoluteY(double y)
	{
		return (interfaceScale * y + Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2);
	}

	public double absoluteToInterfaceX(double x)
	{
		return (x - Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2) / interfaceScale;
	}

	public double absoluteToInterfaceY(double y)
	{
		return (y - Math.max(0, Panel.windowHeight - statsHeight - interfaceSizeY * interfaceScale) / 2 ) / interfaceScale;
	}

	public boolean isOutOfBounds(double drawX, double drawY)
	{
		if (Game.screen.forceInBounds)
			return false;

		int dist = 200;

		if (Game.angledView)
			dist = 300;

		if (!Game.followingCam || !(Game.screen instanceof ScreenGame))
			return drawX - dist * scale > Panel.windowWidth || drawX + dist * scale < 0 || drawY - dist * scale > Panel.windowHeight || drawY + dist * scale < 0;
		else
		{
//			return (drawX - gameToAbsoluteX(Game.playerTank.posX, 0)) * Math.cos(Game.playerTank.angle)
//					+ (drawY - gameToAbsoluteY(Game.playerTank.posY, 0)) * Math.sin(Game.playerTank.angle) < -dist;
			return false;
		}
	}

	public boolean isIncluded(double x1, double y1, double x2, double y2)
	{
		x1 = gameToAbsoluteX(x1, 0);
		y1 = gameToAbsoluteY(y1, 0);
		x2 = gameToAbsoluteX(x2, 0);
		y2 = gameToAbsoluteY(y2, 0);

		int dist = 200;

		if (Game.angledView)
			dist = 300;

		int xp1 = (x1 - dist * scale > Panel.windowWidth ? 1 : 0) + (x1 + dist * scale < 0 ? -1 : 0);
		int yp1 = (y1 - dist * scale > Panel.windowHeight ? 1 : 0) + (y1 + dist * scale < 0 ? -1 : 0);
		int xp2 = (x2 - dist * scale > Panel.windowWidth ? 1 : 0) + (x2 + dist * scale < 0 ? -1 : 0);
		int yp2 = (y2 - dist * scale > Panel.windowHeight ? 1 : 0) + (y2 + dist * scale < 0 ? -1 : 0);
		int xp = xp1 + xp2;
		int yp = yp1 + yp2;
		return xp >= -1 && xp <= 1 && yp >= -1 && yp <= 1;
	}

	public double getTrackOffset()
	{
		if (Game.followingCam)
			return 0;
		else
			return 20;
	}

	public ArrayList<String> wrapText(String msg, double max, double fontSize)
	{
		Drawing.drawing.setInterfaceFontSize(fontSize);

		ArrayList<String> lines = new ArrayList<>();
		StringBuilder l = new StringBuilder();

		boolean first = true;
		for (String s : msg.split(" "))
		{
			if (s.equals("\n"))
			{
				lines.add(l.toString());
				l = new StringBuilder();
				first = true;
				continue;
			}

			if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, l + " " + s) / Drawing.drawing.interfaceScale <= max)
			{
				if (!first)
					l.append(" ");

				l.append(s);
			}
			else if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale > max)
			{
				if (!first)
					l.append(" ");

				for (char c : s.toCharArray())
				{
					if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, l.toString() + c) / Drawing.drawing.interfaceScale > max)
					{
						lines.add(l.toString());
						l = new StringBuilder();
					}

					l.append(c);
				}
			}
			else
			{
				lines.add(l.toString());
				l = new StringBuilder();
				l.append(s);
			}

			first = false;
		}

		if (l.length() > 0)
			lines.add(l.toString());

		return lines;
	}

	public ModelPart createModel()
	{
		return Game.game.window.createModelPart();
	}

	public Model createModel(String dir)
	{
		if (modelsByDir.get(dir) != null)
			return modelsByDir.get(dir);

		Model m = new Model(Game.game.window, Game.game.fileManager, dir);
		modelsByDir.put(dir, m);

		return m;
	}
}
