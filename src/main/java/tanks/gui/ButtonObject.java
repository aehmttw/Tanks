package tanks.gui;

import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableForInterface;
import tanks.obstacle.Obstacle;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ButtonObject extends Button
{
	public IDrawableForInterface object;
	public boolean tempDisableHover = false;
	public Runnable drawBeforeTooltip = null;
    public BiConsumer<Boolean, Boolean> drawSelected = this::drawSelected;
	public boolean showText = false;

	public double disabledColA = 127;
	public double selectedColA = 64;

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

	public static String[] formatDescription(String desc)
	{
		ArrayList<String> text = Drawing.drawing.wrapText(desc, 300, 12);
		String[] s = new String[text.size()];
		return text.toArray(s);
	}
	
	public void initialize(IDrawableForInterface d)
	{
		this.object = d;
		this.disabledColR = 255;
		this.disabledColG = 255;
		this.disabledColB = 255;

		if (Game.game.window.touchscreen)
			this.enableHover = false;
	}
	
	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;

		if (object instanceof Obstacle)
		{
			drawing.setColor(127, 127, 127);
			Drawing.drawing.fillInterfaceGlow(posX, posY, Game.tile_size * 3, Game.tile_size * 3);
		}
		
		this.object.drawForInterface(this.posX, this.posY);
        drawSelected.accept(enabled, selected && !Game.game.window.touchscreen);

        if (!this.text.equals("") && this.showText)
		{
			drawing.setColor(255, 255, 255);
			drawing.setInterfaceFontSize(12);
			drawing.drawInterfaceText(posX, posY + this.sizeX / 2, this.text);
		}

		if (this.drawBeforeTooltip != null)
			this.drawBeforeTooltip.run();

		this.drawBeforeTooltip = null;

		if (this.enableHover && this.selected && !tempDisableHover)
			drawing.drawTooltip(this.hoverText);

		this.tempDisableHover = false;
	}

    private void drawSelected(boolean hovered, boolean selected)
    {
        double thickness = sizeX * 0.2;
        Drawing drawing = Drawing.drawing;

        if (!enabled)
            drawing.setColor(this.disabledColR, this.disabledColG, this.disabledColB, this.disabledColA);
        else if (selected)
            drawing.setColor(this.selectedColR, this.selectedColG, this.selectedColB, this.selectedColA);
        else
            drawing.setColor(0, 0, 0, 0);

        drawing.fillInterfaceRect(posX - sizeX / 2 + thickness / 2, posY, thickness, sizeY);
        drawing.fillInterfaceRect(posX + sizeX / 2 - thickness / 2, posY, thickness, sizeY);
        drawing.fillInterfaceRect(posX, posY - sizeY / 2 + thickness / 2, sizeX - thickness * 2, thickness);
        drawing.fillInterfaceRect(posX, posY + sizeY / 2 - thickness / 2, sizeX - thickness * 2, thickness);
    }

    @Override
	public void setHoverTextUntranslated(String hoverText)
	{
		this.enableHover = true;
		this.hoverTextRaw = hoverText;
		this.hoverText = formatDescription(hoverTextRaw.replace("---", " \n "));
	}
}
