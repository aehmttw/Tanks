package tanks;

public abstract class AreaEffect extends Movable
{	
	public double duration = 500;
	public boolean constantlyImbue = true;
	public double age = 0;
	public double maxAge = 1000;
	
	public AreaEffect(double x, double y)
	{
		super(x, y);
		this.drawLevel = 5;
	}	
	
	@Override
	public void checkCollision()
	{
		
	}
	
	@Override
	public void update()
	{
		this.age += Panel.frameFrequency;
		
		if (constantlyImbue)
		{
			this.imbueEffects();
		}
		
		if (this.age > this.maxAge)
			Game.removeMovables.add(this);
	}

	public abstract void imbueEffects();
}
