package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableLightSource;
import tanks.gui.screen.leveleditor.selector.SelectorColor;
import tanks.gui.screen.leveleditor.selector.SelectorLuminosity;
import tanks.gui.screen.leveleditor.selector.SelectorStackHeight;
import tanks.gui.screen.leveleditor.selector.SelectorTeam;
import tanks.tankson.MetadataProperty;

import java.util.Arrays;

public class ObstacleLight extends Obstacle implements IDrawableLightSource
{
	/** 7 values, first 3 are automatically set to coords, 4th is brightness, 5-7 are color*/
	public double[] lightInfo;

	@MetadataProperty(id = "luminosity", name = "Luminosity", selector = SelectorLuminosity.selector_name, image = "block_luminosity.png", keybind = "editor.height")
	public double luminosity = 1;

	public static int default_color = 255 * 256 * 256 + 250 * 256 + 235;

	@MetadataProperty(id = "color", name = "Color", selector = SelectorColor.selector_name, image = "color.png", keybind = "editor.groupID")
	public int lightColor = default_color;

	public ObstacleLight(String name, double posX, double posY)
	{
		super(name, posX, posY);

		this.lightInfo = new double[]{0, 0, 0, 0, 255, 250, 235};

		this.draggable = false;
		this.destructible = false;
		this.bulletCollision = false;
		this.tankCollision = false;
		this.colorR = 255;
		this.colorG = 250;
		this.colorB = 235;
		this.glow = 1.0;
		this.batchDraw = false;
		this.replaceTiles = false;

		this.drawLevel = 9;

		this.primaryMetadataID = SelectorLuminosity.selector_name;
		this.secondaryMetadataID = SelectorColor.selector_name;

		this.type = ObstacleType.extra;
		this.description = "A light to illuminate dark levels";
	}

	@Override
	public void draw()
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA, this.glow);

		if (Game.enable3d)
			Drawing.drawing.fillBox(this.posX, this.posY, 0, Obstacle.draw_size / 2, Obstacle.draw_size / 2, Obstacle.draw_size / 2);
		else
			Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size / 2, Obstacle.draw_size / 2);

		double frac = Obstacle.draw_size / Game.tile_size;
		Drawing.drawing.setColor(this.colorR * frac, this.colorG * frac, this.colorB * frac, this.colorA, this.glow);

		//double s = this.stackHeight * Game.tile_size * 4;
		//Drawing.drawing.fillForcedGlow(this.posX, this.posY, 0, s * 3, s * 3, false, false, false, true);

	}

	@Override
	public void drawGlow()
	{
		double s = this.luminosity * Game.tile_size * 4;
		double frac = Obstacle.draw_size / Game.tile_size * 0.75;
		Drawing.drawing.setColor(this.colorR * frac, this.colorG * frac, this.colorB * frac, this.colorA, this.glow);

		if (!Game.fancyLights)
			Drawing.drawing.fillForcedGlow(this.posX, this.posY, 0, s, s, false, false, false, false);
	}

	@Override
	public boolean isGlowEnabled()
	{
		return true;
	}

	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
		Drawing.drawing.fillInterfaceRect(x, y, draw_size / 2, draw_size / 2);
	}

	@Override
	public void draw3dOutline(double r, double g, double b, double a)
	{

	}

	@Override
	public String getMetadata()
	{
		if (this.lightColor != default_color)
			return this.luminosity + "@" + this.lightColor;
		else if (this.luminosity != 1)
			return this.luminosity + "";
		else
			return "";
	}

	@Override
	public void setMetadata(String meta)
	{
		if (!meta.isEmpty())
		{
			String[] parts = meta.split("@");
			this.luminosity = Double.parseDouble(parts[0]);
			if (parts.length > 1)
				this.lightColor = Integer.parseInt(parts[1]);
		}
		else
			this.luminosity = 1;

		this.refreshMetadata();
	}

	@Override
	public void refreshMetadata()
	{
		this.lightInfo[4] = this.lightColor / (256 * 256) % 256;
		this.lightInfo[5] = this.lightColor / (256) % 256;
		this.lightInfo[6] = this.lightColor % 256;

		this.colorR = this.lightInfo[4];
		this.colorG = this.lightInfo[5];
		this.colorB = this.lightInfo[6];
	}

	public double getTileHeight()
	{
		return 0;
	}

	@Override
	public boolean lit()
	{
		return Game.fancyLights;
	}

	@Override
	public double[] getLightInfo()
	{
		this.lightInfo[3] = Math.pow(this.luminosity, 2) * Obstacle.draw_size / Game.tile_size;
		return this.lightInfo;
	}
}