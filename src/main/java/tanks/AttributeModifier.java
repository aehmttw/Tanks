package tanks;

import java.util.UUID;

public class AttributeModifier
{
	enum Operation {add, multiply}
	
	/**An unique name for the modifier, to prevent double effects*/
	public String name = UUID.randomUUID().toString();
	
	/**Duration of the Attribute Modifier, leave at 0 for indefinite duration*/
	public double duration = 0;
	
	/**Age at which the Attribute starts to wear off*/
	public double deteriorationAge = 0;
	
	/**Age at which the Attribute is at full strength*/
	public double warmupAge = 0;
	
	public double value;
	
	public Operation effect;
	
	protected double age;
	
	public boolean expired = false;
	
	public String type;
	
	public AttributeModifier(String type, Operation op, double amount)
	{
		this.type = type;
		this.effect = op;
		this.value = amount;
	}
	
	public AttributeModifier(String name, String type, Operation op, double amount)
	{
		this(type, op, amount);
		this.name = name;
	}
	
	public void update()
	{
		this.age += Panel.frameFrequency;
	
		if (this.duration > 0 && this.age > this.duration)
			this.expired = true;
	}
	
	public double getValue(double in)
	{
		double val = 0;
		
		if (this.expired)
			return in;
		else if (this.age < this.warmupAge)
			val = this.value * this.age / this.warmupAge;
		else if (this.age < this.deteriorationAge || this.deteriorationAge <= 0)
			val = this.value;
		else
			val = this.value * (this.duration - this.age) / (this.duration - this.deteriorationAge);
		
		if (this.effect == Operation.add)
			return in + val;
		else if (this.effect == Operation.multiply)
			return in * (val + 1);
		else
			return in;
			
	}
	
}
