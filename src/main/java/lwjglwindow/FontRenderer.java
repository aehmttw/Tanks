package lwjglwindow;

import basewindow.BaseFontRenderer;
import org.lwjgl.opengl.GL11;

public class FontRenderer extends BaseFontRenderer
{
	public String chars;
	public int[] charSizes;
	public String image;

	//how many characters fit per horizontal line
	public float size = 16;

	//spacing between rows, increase this to 2 for antialiasing to prevent weird artifacts
	public int hSpace = 2;

	public FontRenderer(LWJGLWindow h, String fontFile)
	{
		super(h);
		this.chars = " !\"#$%&'()*+,-./" +
				"0123456789:;<=>?" +
				"@ABCDEFGHIJKLMNO" +
				"PQRSTUVWXYZ[\\]^_" +
				"'abcdefghijklmno" +
				"pqrstuvwxyz{|}~`" +
				"âăîşţàçæèéêëïôœù" +
				"úûüÿáíóñ¡¿äöå";
		this.charSizes = new int[]
				{
						3, 2, 4, 5, 5, 6, 5, 2, 3, 3, 4, 5, 1, 5, 1, 5,
						5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 5, 5, 5, 5,
						7, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5,
						5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 3, 5, 5,
						2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 4, 2, 5, 5, 5,
						5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5, 4, 1, 4, 6, 2,
						5, 5, 5, 5, 3, 5, 5, 7, 5, 5, 5, 5, 3, 5, 7, 5,
						5, 5, 5, 5, 5, 3, 5, 5, 3, 5, 5, 5, 5
				};

		this.image = fontFile;
	}

	public boolean supportsChar(char c)
	{
		return this.chars.indexOf(c) >= 0;
	}

	protected int drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
	{
		int i = this.chars.indexOf(c);

		if (i == -1)
			i = 31;

		int col = (int) (i % size);
		int row = (int) (i / size);
		int width = charSizes[i];
		//this.window.shapeRenderer.drawRect(x, y - sY * 16, sX * width * 4, sY * 64);

		if (this.drawBox)
		{
			this.window.shapeRenderer.drawRect(x, y, sX * width * 2, sY * 32);
			this.window.shapeRenderer.drawRect(x, y + sY * 16, sX * width * 2, sY * 16);
			this.window.shapeRenderer.drawRect(x + sX * width * 2, y, sX * width * 2, sY * 32);
			this.window.shapeRenderer.drawRect(x + sX * width * 2, y + sY * 16, sX * width * 2, sY * 16);
		}

		this.window.shapeRenderer.drawImage(x, y - sY * 16, z, sX * 32 * size, sY * 32 * size,
				col / size, (row * hSpace) / size,
				(col + width / 8f) / size, (row * hSpace + 2) / size,
				image, false, depthtest);
		return width;
	}

	public void drawString(double x, double y, double z, double sX, double sY, String s)
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		double curX = x;
		char[] c = s.toCharArray();

		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '\u00C2')
				continue;
			else if (c[i] == '\u00A7')
			{
				int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
				int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
				int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
				int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
				this.window.setColor(r, g, b, a);

				i += 12;
			}
			else
				curX += (drawChar(curX, y, z, sX, sY, c[i], true) + 1) * sX * 4;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	public void drawString(double x, double y, double sX, double sY, String s)
	{
		double curX = x;
		char[] c = s.toCharArray();

		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '\u00C2')
				continue;
			else if (c[i] == '\u00A7')
			{
				int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
				int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
				int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
				int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
				this.window.setColor(r, g, b, a);

				i += 12;
			}
			else
				curX += (drawChar(curX, y, 0, sX, sY, c[i], false) + 1) * sX * 4;
		}
	}

	public double getStringSizeX(double sX, String s)
	{
		double w = 0;
		char[] c = s.toCharArray();

		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '\u00C2')
				continue;
			else if (c[i] == '\u00A7')
				i += 12;
			else if (this.chars.indexOf(c[i]) == -1)
				c[i] = '?';
			else
				w += (charSizes[this.chars.indexOf(c[i])] + 1) * sX * 4;
		}

		return Math.max(w - sX * 4, 0);
	}

	public double getStringSizeY(double sY, String s)
	{
		return (sY * 32);
	}
}
