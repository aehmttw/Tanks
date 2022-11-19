package tanks;

public abstract class AreaEffect extends Movable
{	
	public boolean constantlyImbue = true;
	public double age = 0;
	public double maxAge = 1000;
	
	public AreaEffect(double x, double y)
	{
		super(x, y);
		this.drawLevel = 5;
	}

	@Override
	public void update()
	{
		double timeMul = 1;
		for (int i = 0; i < this.attributes.size(); i++)
		{
			AttributeModifier a = this.attributes.get(i);

			if (a.type.equals("speed"))
			{
				timeMul = a.getValue(timeMul);
			}
		}

		this.age += Panel.frameFrequency * timeMul;
		
		if (constantlyImbue)
		{
			this.imbueEffects();
		}
		
		if (this.age > this.maxAge)
			Game.removeMovables.add(this);
	}

	public abstract void imbueEffects();
}
