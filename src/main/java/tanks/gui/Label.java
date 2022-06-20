package tanks.gui;

import tanks.*;
import tanks.translation.Translation;

public class Label implements IDrawable
{
	public double posX;
	public double posY;
	public double fontSize;

	public String originalText;
	public String text;

	public boolean centered = true;
	public boolean rightAligned = false;

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

	public Label(double x, double y, double size, String text)
	{
		this.posX = x;
		this.posY = y;
		this.fontSize = size;
		this.originalText = text;
		this.text = Translation.translate(text);
	}

	public Label(double x, double y, double size, String text, Object... objects)
	{
		this(x, y, size, text);

		this.originalText = text;
		this.text = Translation.translate(text, objects);
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setInterfaceFontSize(this.fontSize);

		if (centered)
			drawing.drawInterfaceText(this.posX, this.posY, this.text);
		else
			drawing.drawInterfaceText(this.posX, this.posY, this.text, this.rightAligned);
	}

	public void setText(String text)
	{
		this.originalText = text;
		this.text = Translation.translate(text);
	}

	public void setText(String text, Object... objects)
	{
		this.originalText = text;
		this.text = Translation.translate(text, objects);
	}

	public void draw(Object... objects)
	{
		Drawing drawing = Drawing.drawing;
		drawing.setInterfaceFontSize(this.fontSize);

		this.text = Translation.translate(text, objects);

		if (centered)
			drawing.drawInterfaceText(this.posX, this.posY, this.text);
		else
			drawing.drawInterfaceText(this.posX, this.posY, this.text, this.rightAligned);
	}
}
